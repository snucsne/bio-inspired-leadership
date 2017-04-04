#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
$dataDir =~ /indcount-(\d+)/;
my $indCount = $1;

# Get the outputfile prefix
my $outputFilePrefix = shift( @ARGV );

# Get the eps file
my $epsFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
my @dataFiles;

# Grab the map directories
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
@dataFiles = sort( grep { /\.dat$/ } readdir( DIR ) );
closedir( DIR );

print "Found ",(scalar @dataFiles)," files\n";

# -------------------------------------------------------------------
# Process each data file
my %data;
foreach my $dataFile (@dataFiles)
{
    # Create a variable in which data is stored
    my %data;

    # Open the file
    open( INPUT, "$dataDir/$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

    # Get the map id
    $dataFile =~ /map-(\d+)/;
    my $mapID = $1;

    # Read each line
    while( <INPUT> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Get the data key and individual id
        my ($key,$value) = split( /\s+=\s+/, $_ );
        my ($trash, $id, $dataKey ) = split( /\./, $key, 3 );

        # Is it data we need to process?
        if( $dataKey =~ /rel-positions-nearest-neighbors/ )
        {
            $value =~ s/\}//g;
            $value =~ s/\{//g;
            my @positions = split( /\s+/, $value );
            $data{$id}{$dataKey} = \@positions;
        }
        else
        {
            # Just store the value
            $data{$id}{$dataKey} = $value;
        }
    }

    # Close the file
    close( INPUT );

    # Process the data
    processData( $outputFilePrefix."position-analysis-map-$mapID.dat",
            $epsFilePrefix,
            $mapID,
            \%data );
}


# ===================================================================
sub processData
{
    my ($outputFile, $epsFilePrefix, $mapID, $dataRef) = @_;

    my %data = %{$dataRef};

    # Build arrays of each individual position and other data
    my @xPos;
    my @yPos;
    my @xMeanResultantVector;
    my @yMeanResultantVector;
    my @meanResultantDistance;
    my @meanRelativeDistance;
    foreach my $id (sort (keys %data))
    {
        # Get the location and split it into two pieces
        my ($x, $y) = split( /\s+/, $data{$id}{'location'} );
        push( @xPos, $x );
        push( @yPos, $y );

        # Get the mean resultant vector
        ($x, $y) = split( /\s+/, $data{$id}{'mean-resultant-vector-to-neighbors.cartesian'} );
        push( @xMeanResultantVector, $x );
        push( @yMeanResultantVector, $y );

        # Get the mean relative distance to neighbors
        push( @meanResultantDistance, $data{$id}{'mean-resultant-vector-to-neighbors.length'} );

        # Get the mean relative distance to neighbors
        push( @meanRelativeDistance, $data{$id}{'mean-relative-distance-to-neighbors'} );
    }

    # -------------------------------------------------------------------
    # Create an Rinput file
    my $rInputFile = "/tmp/position-analysis.r";
    my $rOutputFile = $rInputFile.".out";

    open( RINPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print RINPUT "library(extrafont)\n";
    print RINPUT "loadfonts(device = \"postscript\")\n";
    print RINPUT "library(plotrix)\n";
    print RINPUT "library('CircStats')\n";
    print RINPUT "library('grDevices')\n";

    # Store the names of all the eps files we create
    my @epsFiles;

    # Build R variables
    print RINPUT "xPos <- scan()\n";
    print RINPUT join( " ", @xPos ),"\n\n";
    print RINPUT "yPos <- scan()\n";
    print RINPUT join( " ", @yPos ),"\n\n";
    print RINPUT "xMeanResultantVector <- scan()\n";
    print RINPUT join( " ", @xMeanResultantVector ),"\n\n";
    print RINPUT "yMeanResultantVector <- scan()\n";
    print RINPUT join( " ", @yMeanResultantVector ),"\n\n";
    print RINPUT "meanResultantDistance <- scan()\n";
    print RINPUT join( " ", @meanResultantDistance ),"\n\n";
    print RINPUT "meanRelativeDistance <- scan()\n";
    print RINPUT join( " ", @meanRelativeDistance ),"\n\n";

    # -------------------------------------------------------------------
    # Build variables containing all the relative positions of neighbors
    my $xMRVStr = "";
    my $yMRVStr = "";
    my $meanDistanceStr = "";
    foreach my $id (sort (keys %data))
    {
        my @xNN;
        my @yNN;
        my @atanOffset;
        my @relPositions = @{$data{$id}{'rel-positions-nearest-neighbors'}};
        foreach my $position (@relPositions)
        {
            my ($x, $y) = split( /,/, $position );
            push( @xNN, $x );
            push( @yNN, $y );

            if( $x > 0 )
            {
                push( @atanOffset, 0 );
            }
            elsif( ($x < 0) && ($y >= 0) )
            {
                push( @atanOffset, "pi" );
            }
            elsif( ($x < 0) && ($y < 0) )
            {
                push( @atanOffset, "-pi" );
            }
            elsif( ($x == 0) && ($y > 0) )
            {
                push( @atanOffset, "(pi / 2)" );
            }
            elsif( ($x == 0) && ($y < 0) )
            {
                push( @atanOffset, "(-pi / 2)" );
            }
        }

        print RINPUT "xNN$id <- scan()\n";
        print RINPUT join( " ", @xNN ),"\n\n";
        print RINPUT "yNN$id <- scan()\n";
        print RINPUT join( " ", @yNN ),"\n\n";
        print RINPUT "atanOffsetNN$id <- c(",join( ", ", @atanOffset ),")\n\n";
        print RINPUT "angleNN$id <- (atan( yNN$id / xNN$id ) + atanOffsetNN$id)\n";
        print RINPUT "mrv$id <- circ.summary( angleNN$id )\n";
        print RINPUT "xMRV$id <- mrv$id\$rho * cos( mrv$id\$mean.dir )\n";
        print RINPUT "yMRV$id <- mrv$id\$rho * sin( mrv$id\$mean.dir )\n";
        print RINPUT "allDistancesNN$id <- sqrt( xNN$id**2 + yNN$id**2 )\n";
#        print RINPUT "allDistancesNN$id\n";
        print RINPUT "meanDistanceNN$id <- mean( allDistancesNN$id )\n";
        print RINPUT "chull$id <- chull( xNN$id, yNN$id )\n";
        my ($x, $y) = split( /\s+/, $data{$id}{'location'} );
        print RINPUT "xCHull$id <- xNN$id\[c(chull$id, chull$id\[1\])]\n";
        print RINPUT "xCHull$id <- xCHull$id + $x\n";
        print RINPUT "yCHull$id <- yNN$id\[c(chull$id, chull$id\[1\])]\n";
        print RINPUT "yCHull$id <- yCHull$id + $y\n";
        $xMRVStr .= "xMRV$id, ";
        $yMRVStr .= "yMRV$id, ";
        $meanDistanceStr .= "meanDistanceNN$id, ";
    }
    chop( $xMRVStr );
    chop( $xMRVStr );
    chop( $yMRVStr );
    chop( $yMRVStr );
    chop( $meanDistanceStr );
    chop( $meanDistanceStr );

    # Build a variable of all the MRVs and mean distance str
    print RINPUT "xMRV <- c($xMRVStr)\n";
    print RINPUT "yMRV <- c($yMRVStr)\n";
    print RINPUT "meanDistances <- c($meanDistanceStr)\n";
    print RINPUT "\n\n";

    # -------------------------------------------------------------------
    # Create labels for polar plots
    print RINPUT "polarlabels<-c(\"0\",\"\",expression(pi/2),\"\",expression(pi),\"\",expression(3*pi/2),\"\")\n\n";

    # Create a MRV plot for each individual
    foreach my $id (sort (keys %data))
    {
        my $epsFile = $epsFilePrefix."map-$mapID-mrv-circular-$id.eps";
        push( @epsFiles, $epsFile );

        print RINPUT "postscript( file=\"$epsFile\", height=6, width=6, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
        print RINPUT "par(mar=c(2,2,2,2)+0.1 )\n";

        # Temp calculations
        print RINPUT "tempradius <- c(0,rep(1, length(angleNN$id)))\n";

        print RINPUT "mrv$id\$mean.dir\n";
        print RINPUT "mrv$id\$rho\n";

        print RINPUT "xValuePos <- c(mrv$id\$rho * cos(mrv$id\$mean.dir) / 2)\n";
        print RINPUT "yValuePos <- c(mrv$id\$rho * sin(mrv$id\$mean.dir) / 2)\n";
        print RINPUT "if( yValuePos < 0.2 && yValuePos >= 0 && abs(xValuePos) > 0.2 ) {\n";
        print RINPUT "  yValuePos <- 0.2\n";
        print RINPUT "} else if( yValuePos > -0.2 && yValuePos < 0 && abs(xValuePos) > 0.2) {\n";
        print RINPUT "  yValuePos <- -0.2\n";
        print RINPUT "}\n";

        print RINPUT "radial.plot( tempradius, c(0,angleNN$id), rp.type=\"s\", ".
                "point.symbols=16, show.grid.labels=FALSE, cex=ifelse(tempradius==0,0,1.5), ".
                "labels=polarlabels, point.col=\"orange\" )\n\n";
#        print RINPUT "radial.plot( c(0, mrv$id\$rho), c(0, mrv$id\$mean.dir), add=TRUE, ".
#                "rp.type=\"r\", line.col=\"orange\", lwd=3)\n\n";
#        print RINPUT "radial.plot( c(0, mrv$id\$rho*0.85, mrv$id\$rho*0.85, mrv$id\$rho, mrv$id\$rho*0.85, mrv$id\$rho*0.85), "
#                ."c(0, mrv$id\$mean.dir, mrv$id\$mean.dir+arrowheadangle, mrv$id\$mean.dir, mrv$id\$mean.dir-arrowheadangle, mrv$id\$mean.dir), "
#                ."add=TRUE, rp.type=\"p\", line.col=\"orange\", lwd=1.5, poly.col=\"orange\" )\n";
        print RINPUT "arrows( c(0), c(0), "
                ."c(mrv$id\$rho * cos(mrv$id\$mean.dir)), c(mrv$id\$rho * sin(mrv$id\$mean.dir)), "
                ."code=2, col=\"blue\", lwd=4, length=pmax(mrv$id\$rho*0.2,0.1) )\n";
        print RINPUT "text( xValuePos, yValuePos, labels=sprintf( \"%5.3f\", mrv$id\$rho),"
                ."pos=ifelse( cos(mrv$id\$mean.dir) > 0, 2, 4 ), offset=1, cex=1.5, col=\"blue\" )\n";

        print RINPUT "dev.off()\n\n";   
    }

    # -------------------------------------------------------------------
    # Create a plot showing mean resultant vector
    my $epsFile = $epsFilePrefix."map-$mapID-mrv-combined.eps";
    push( @epsFiles, $epsFile );

    print RINPUT "postscript( file=\"$epsFile\", height=6, width=6, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
    print RINPUT "par(mar=c(2,2,2,2)+0.1 )\n";

    print RINPUT "plot( xPos, yPos, xlim=c( (min(c(xPos, xMRV)) - 2), (max(c(xPos, xMRV)) + 2)),"
            ."ylim=c( (min(c(yPos, yMRV)) - 2), (max(c(yPos, yMRV)) + 2)), pch=21, col=\"black\", bg=\"orange\", "
            ."cex=1.5, bty=\"n\", axes=FALSE )\n";
    print RINPUT "arrows( xPos, yPos, (xPos + xMRV), (yPos + yMRV), code=2, length=0, col=\"blue\", lwd=3 )\n";

    print RINPUT "xMRV\n";
    print RINPUT "yMRV\n";

    print RINPUT "dev.off()\n\n";

    # -------------------------------------------------------------------
    # Create a plot showing mean distance to neighbors
    my $epsFile = $epsFilePrefix."map-$mapID-mean-distance.eps";
    push( @epsFiles, $epsFile );

#    print RINPUT "meanDistanceNNInd00000\n";
#    print RINPUT "meanDistances\n";
    print RINPUT "minMeanDist <- min(meanDistances)\n";
#    print RINPUT "minMeanDist\n";
    print RINPUT "normalizedMeanDist <- meanDistances - minMeanDist\n";
#    print RINPUT "normalizedMeanDist\n";
    print RINPUT "maxNormalizedMeanDist <-  max(normalizedMeanDist)\n";
#    print RINPUT "maxNormalizedMeanDist\n";
    print RINPUT "normalizedMeanDist <- normalizedMeanDist / maxNormalizedMeanDist\n";
#    print RINPUT "normalizedMeanDist\n";
    print RINPUT "distBasedColor <- rgb( (1-normalizedMeanDist), (1-normalizedMeanDist), 1)\n";
#    print RINPUT "distBasedColor\n";

    print RINPUT "postscript( file=\"$epsFile\", height=6, width=6, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
    print RINPUT "par(mar=c(2,2,2,2)+0.1 )\n";

    print RINPUT "plot( xPos, yPos, xlim=c( (min(c(xPos, xMRV)) - 2), (max(c(xPos, xMRV)) + 2)),"
            ."ylim=c( (min(c(yPos, yMRV)) - 2), (max(c(yPos, yMRV)) + 2)), pch=21, col=\"black\", bg=distBasedColor, "
            ."cex=1.5, bty=\"n\", axes=FALSE, lwd=1.5 )\n";

    print RINPUT "dev.off()\n\n";

    # -------------------------------------------------------------------
    # Create a convex hull plot for each individual
#    foreach my $id (sort (keys %data))
#    {
#        my $epsFile = $epsFilePrefix."map-$mapID-chull-$id.eps";
#        push( @epsFiles, $epsFile );
#
#        print RINPUT "chull$id\n";
#        print RINPUT "xCHull$id\n";
#        print RINPUT "yCHull$id\n";
#
#        print RINPUT "postscript( file=\"$epsFile\", height=6, width=6, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
#        print RINPUT "par(mar=c(2,2,2,2)+0.1 )\n";
#
#        print RINPUT "plot( xPos, yPos, xlim=c( (min(c(xPos, xMRV)) - 2), (max(c(xPos, xMRV)) + 2)), "
#                ."ylim=c( (min(c(yPos, yMRV)) - 2), (max(c(yPos, yMRV)) + 2)), pch=21, col=\"black\", "
#                ."cex=1.5, bty=\"n\", axes=FALSE )\n";
#        print RINPUT "lines( xCHull$id, yCHull$id, lwd=2, col=\"blue\" )\n";
#        print RINPUT "points( xCHull$id, yCHull$id, pch=16, col=\"blue\", cex=1.5 )\n";
#        my ($x, $y) = split( /\s+/, $data{$id}{'location'} );
#        print RINPUT "points( c($x), c($y), pch=16, col=\"orange\", cex=1.5 );\n";
#
#        print RINPUT "dev.off()\n\n";
#    }

    # -------------------------------------------------------------------
    # Record all the interesting data
    print RINPUT "sink(\"$outputFile\")\n";

    my $nowString = localtime;
    print RINPUT buildRCatText( "# Time     [$nowString]" );
    print RINPUT buildRCatText( "# ===========================================" );
    print RINPUT buildRCatText( "" );

    foreach my $id (sort (keys %data))
    {
        print RINPUT "mrvDir$id <- mrv$id\$mean.dir\n";
        print RINPUT "if( mrvDir$id < 0 ) {\n";
        print RINPUT "  mrvDir$id <- mrvDir$id + 2 * pi\n";
        print RINPUT "}\n";
        print RINPUT buildRCatText( "" );
        print RINPUT buildRCatText( "# ----------------------------------------------------------" );
        print RINPUT buildRCatText( "# Individual: $id" );
        print RINPUT buildRCatText( "$id.rel-vector-to-neighbors         = \",sprintf( \"%+6.4f,%+6.4f\", mean( xNN$id ), mean( yNN$id ) ),\"" );
        print RINPUT buildRCatText( "$id.mean-resultant-vector.direction =  \",format( mrvDir$id ),\"" );
        print RINPUT buildRCatText( "$id.mean-resultant-vector.length    =  \",format( mrv$id\$rho ),\"" );
        print RINPUT buildRCatText( "$id.distance-to-neighbors.all       =  \",format( sort( allDistancesNN$id ) ),\"" );
        print RINPUT buildRCatText( "$id.distance-to-neighbors.mean      =  \",format( mean( allDistancesNN$id ) ),\"" );
        print RINPUT buildRCatText( "$id.distance-to-neighbors.sd        =  \",format( sd( allDistancesNN$id ) ),\"" );
        print RINPUT buildRCatText( "$id.distance-to-neighbors.min       =  \",format( min( allDistancesNN$id ) ),\"" );
        print RINPUT buildRCatText( "$id.distance-to-neighbors.max       =  \",format( max( allDistancesNN$id ) ),\"" );
        print RINPUT buildRCatText( "" );
    }

    print RINPUT "sink()\n";

    # -------------------------------------------------------------------
    # Close the input file
    close( RINPUT );

    # -------------------------------------------------------------------
    # Run it
    `R --no-save < $rInputFile > $rOutputFile 2>&1`;

    # -------------------------------------------------------------------
    # Clean up the EPS files
    my $tmpEPSFile = "/tmp/tmp.eps";
    my $fixedTmpEPSFile = "/tmp/fixed-tmp.eps";
    foreach my $epsFile (@epsFiles)
    {
        if( -e $epsFile )
        {
           `cp $epsFile $tmpEPSFile`;
            `/usr/bin/epstool --copy --bbox $tmpEPSFile $epsFile`;
            `epstopdf $epsFile`;

            my $pdfFile = $epsFile;
            $pdfFile =~ s/eps$/pdf/;
            my $title = $epsFile;
            $title =~ s/.*\/(.*)\.eps$/\1/;
            `exiftool -Title="$title" -Author="Brent E. Eskridge" $pdfFile`;
            # See the link below for more exiftool help with PDFs
            # http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/PDF.html
            # See the link below for more exiftool help with PostScript
            # http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/PostScript.html
        }
    }
    
}

# ===================================================================
sub buildRCatText
{
    my ($text) = @_;
    return "cat(\"".$text."\\n\")\n";
}


