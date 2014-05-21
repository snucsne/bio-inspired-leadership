#!/usr/bin/perl
use strict;
use POSIX;

# -------------------------------------------------------------------
# Get the root data directory
my $rootDataDir = shift( @ARGV );

# Get the data type
my $dataType = shift( @ARGV );

# Get the data type title
my $dataTypeTitle = shift( @ARGV );

# Get the data output file prefix
my $outputFilePrefix = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );


# -------------------------------------------------------------------
# What kind of data are we dealing with?

# Get all the data directories in the data directory
opendir( DIR, $rootDataDir ) or die "Unable to open root data directory [$rootDataDir]: $!\n";
my @dataDirs = sort( grep { /$dataType/ } readdir( DIR ) );
closedir( DIR );

# Is there a baseline?
if( -d "$rootDataDir/baseline/" )
{
    unshift( @dataDirs, "baseline" );
}

# -------------------------------------------------------------------
my %data;
my @dataSetOrder;
foreach my $dataDir (@dataDirs)
{
    # Get the data set identifier
    my $dataSet = "None";
    if( $dataDir =~ m/$dataType-(.*)/ )
    {
         $dataSet = $1;
    }

    #print "Processing $dataType=[$dataSet]\n";

    # Read all the neighbor stats from the analysis directory
    my $analysisDir = $rootDataDir.$dataDir."/analysis/";
    readNeighborStats( $analysisDir, \%data, $dataSet );
    push( @dataSetOrder, $dataSet );
} 

# -------------------------------------------------------------------
# Create the R input file
my $rInputFile = "/tmp/aggregate-neighbor-stats.r";
my $rOutputFile = $rInputFile.".out";
my @epsFiles = createRInput( $rInputFile,
        $outputFilePrefix."aggregate-neighbor-stats.dat",
        $epsFilePrefix,
        $dataType,
        $dataTypeTitle,
        \@dataSetOrder,
        \%data );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# -------------------------------------------------------------------
# Clean up the EPS files
my $tmpEPSFile = "/tmp/tmp.eps";
my $fixedTmpEPSFile = "/tmp/fixed-tmp.eps";
foreach my $epsFile (@epsFiles)
{
    `cp $epsFile $tmpEPSFile`;
    `../replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
    `/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
    `epstopdf $epsFile`;
}


# ===================================================================
sub readNeighborStats
{
    my ($dir, $dataRef, $dataSet) = @_;

    # Open the directory and find all the neighbor stats files
    opendir( DIR, $dir ) or die "Unable to open neighbor stats directory [$dir]: $!\n";
    my @neighborFiles = sort( grep { /-neighbor-stats.dat$/ } readdir( DIR ) );
    closedir( DIR );

    #print "\tProcessing [",($#neighborFiles + 1),"] neighbor files from [$dir]\n";

    # Process each file
    foreach my $neighborFile (@neighborFiles)
    {
        # Get the number of individuals
        $neighborFile =~ /indcount-(\d+)/;
        my $indCount = $1;

        # Open the file
        open( NEIGHBOR, "$dir/$neighborFile" ) or die "Unable to open neighbor stats file [$neighborFile]: $!\n";

        # Read each line
        while( <NEIGHBOR> )
        {
            # Remove any other comments or whitespace
            s/#.*//;
            s/^\s+//;
            s/\s+$//;
            next unless length;

            # Split it into key/value pairs
            my ($key,$value) = split( /\s+=\s+/, $_ );

            # Is it the neighbor probability?
            if( $key =~ /follower-(\d+).neighbor-probability/ )
            {
                # Yup, grab the follower index
                my $followerIdx = $1;

                # Store the value
                $dataRef->{$dataSet}{$indCount}{$followerIdx}{"probability"} .= $value." ";
            }
            # Nope, is it the neighbor percentage?
            elsif( $key =~ /follower-(\d+).following-neighbor-percentage/ )
            {
                # Yup, grab the follower index
                my $followerIdx = $1;

                # Store the value
                $dataRef->{$dataSet}{$indCount}{$followerIdx}{"percentage"} .= $value." ";
            }
        }

        # Close the file
        close( NEIGHBOR );
    }
}


# ===================================================================
sub createRInput
{
    my ($rInputFile, $resultsFile, $epsFilePrefix, $dataType, $dataTypeTitle, $dataSetOrderRef, $dataRef) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# ===============================================\\n\")\n";

    # Sanitize the datatype
    my $cleanDataType = $dataType;
    $cleanDataType =~ s/\.//g;
    $cleanDataType =~ s/-//g;

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";
    print INPUT "library('igraph')\n";
    print INPUT "library('Hmisc')\n";

    # Build a standard error function
    print INPUT "stderr <- function(x) sd(x)/sqrt(length(x))\n\n";

    # Build a function to handle the t-test error in case the data is constant
    print INPUT "tryttestpvalue <- function(...) {\n";
    print INPUT "    obj<-try(t.test(...), silent=TRUE)\n";
    print INPUT "    if (is(obj, \"try-error\")) return(NA) else return(obj\$p.value)\n";
    print INPUT "}\n";

    # ---------------------------------------------------------------
    # Add the data
    my %dataIDs;
    my @allDataIDs;
    my %dataSetHash;
    my %indCountHash;
    foreach my $dataSet (@{$dataSetOrderRef})
    {
        # Sanitize the data set
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;

        foreach my $indCount (sort (keys %{$dataRef->{$dataSet}} ) )
        {
            foreach my $followerIdx (sort keys %{$dataRef->{$dataSet}{$indCount}})
            {
                foreach my $statType (sort keys %{$dataRef->{$dataSet}{$indCount}{$followerIdx}})
                {
                    # Create an ID and use it to store the data
                    my $id = $cleanDataType.$cleanDataSet."indcount".$indCount.$followerIdx.$statType;
                    print INPUT "$id <- scan()\n";
                    print INPUT $dataRef->{$dataSet}{$indCount}{$followerIdx}{$statType},"\n\n";
                    $dataIDs{$dataSet}{$indCount}{$followerIdx}{$statType} = $id;
                    push( @allDataIDs, $id );
                }
            }
            $indCountHash{$indCount} = 1;
        }
        $dataSetHash{$dataSet} = 1;
    }
    my @dataSets = @{$dataSetOrderRef};
    my @indCounts = sort( keys( %indCountHash ) );

    # ---------------------------------------------------------------
    # Start sending output to the results file
#    print INPUT "sink(\"$resultsFile\")\n";

    # ---------------------------------------------------------------
    # Add the statistics
    my %statTypeMap;
    foreach my $dataSet (sort (keys %dataIDs) )
    {
        # Sanitize the data set
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;

        foreach my $indCount (sort (keys %{$dataIDs{$dataSet}} ) )
        {
            foreach my $followerIdx (sort keys %{$dataRef->{$dataSet}{$indCount}})
            {
                foreach my $statType (sort keys %{$dataRef->{$dataSet}{$indCount}{$followerIdx}})
                {
                    my $id = $dataIDs{$dataSet}{$indCount}{$followerIdx}{$statType};
                    my $prefix = "summary.$dataType-$dataSet.indcount-$indCount.follower-$followerIdx.type-$statType";
                    print INPUT "cat(\"# ===============================================\\n\")\n";
                    print INPUT "cat(\"# Summary: $dataType=[$dataSet]  indCount=[$indCount]  followerIdx=[$followerIdx]  type=[$statType]\\n\")\n";
                    print INPUT "cat(\"$prefix.mean =   \", format( mean($id) ), \"\\n\")\n";
                    print INPUT "cat(\"$prefix.max =    \", format( max($id) ), \"\\n\")\n";
                    print INPUT "cat(\"$prefix.min =    \", format( min($id) ), \"\\n\")\n";
                    print INPUT "cat(\"$prefix.sd =     \", format( sd($id) ), \"\\n\")\n";
                    print INPUT "cat(\"$prefix.se =     \", format( stderr($id) ), \"\\n\")\n";
                    print INPUT "cat(\"$prefix.length = \", format( length($id) ), \"\\n\")\n";
                    print INPUT "cat(\"\\n\")\n";
                    $statTypeMap{$statType} = 1;
                }
            }
        }
    }
    my @statTypes = sort( keys( %statTypeMap ) );

    # ---------------------------------------------------------------
    # Add the KS tests for data with the same individual count
    foreach my $indCount (@indCounts)
    {
        foreach my $followerIdx (sort keys %{$dataRef->{$dataSets[0]}{$indCount}})
        {
            foreach my $statType (sort keys %{$dataRef->{$dataSets[0]}{$indCount}{$followerIdx}})
            {
                for( my $i = 0; $i <= ($#dataSets - 1); $i++ )
                {
                    for( my $j = $i + 1; $j <= ($#dataSets); $j++ )
                    {
                        my $firstDataSet = $dataSets[$i];
                        my $secondDataSet = $dataSets[$j];
                        print INPUT $rSpacer;
                        print INPUT "cat(\"# KS IndCount [",
                                $indCount,
                                "] follower=[",
                                $followerIdx,
                                "] stat=[",
                                $statType,
                                "]: ",
                                ucfirst($dataType),
                                "=[",
                                $dataSets[$i],
                                "] vs ",
                                ucfirst($dataType),
                                "=[",
                                $dataSets[$j],
                                "]\\n\")\n";
                        printKSTestText( *INPUT,
                                $dataIDs{$firstDataSet}{$indCount}{$followerIdx}{$statType},
                                "$dataType-$firstDataSet.indcount-$indCount.follower-$followerIdx.type-$statType",
                                $dataIDs{$secondDataSet}{$indCount}{$followerIdx}{$statType},
                                "$dataType-$secondDataSet.indcount-$indCount.follower-$followerIdx.type-$statType" );
                    }
                }
            }

            print INPUT "cat(\"\\n\")\n";

        }
    }

    # ---------------------------------------------------------------
    # Stop sending output to the results file
#    print INPUT "sink()\n";

    # ---------------------------------------------------------------
    # Set up the format for each figure
    my %dataFormat = (
        "percentage" => { "y-min" => 0,
                          "y-max" => 1,
                          "y-title" => "Percentage of nearest\\n neighbors following",
                          "x-title" => "Total followers",
                          "legend-loc" => "bottomright" },
        "probability" => { "y-min" => 0,
                          "y-max" => 1,
                          "y-title" => "Probability of follower\\n begin a nearest neighbor",
                          "x-title" => "Total followers",
                          "legend-loc" => "topleft" } );

    # ---------------------------------------------------------------
    # Build a plot for each stat type
    foreach my $statType (@statTypes)
    {
        # Build a plot for each group size
        foreach my $indCount (@indCounts)
        {
            # Build the data to plot
            my @meanIDStrings;
            my @sdIDStrings;
            my @seIDStrings;
            my @labelIDStrings;

            # Iterate through each data set
            my %followerIdxMap;
            foreach my $dataSet (@dataSets)
            {
                # Build some handy variables
                my $meanStr = "";
                my $sdStr = "";
                my $seStr = "";
                my $labelStr = "";
                my $dataCount = 0;

                # Iterate through every follower index to build the data
                foreach my $followerIdx (sort keys %{$dataRef->{$dataSet}{$indCount}})
                {
                    # Build the plot data
                    my $id = $dataIDs{$dataSet}{$indCount}{$followerIdx}{$statType};
                    $meanStr .= "mean($id), ";
                    $sdStr .= "sd($id), ";
                    $seStr .= "stderr($id), ";

                    # Build a nice label
                    my $followerIdxLabel = $followerIdx;
                    $followerIdxLabel =~ s/^0+(\d+)/\1/;
                    $labelStr .= "\"followerIdxLabel\", ";

                    $dataCount++;
                    $followerIdxMap{$followerIdxLabel} = 1;
                }

                # Clean up the strings
                $meanStr =~ s/, $//;
                $sdStr =~ s/, $//g;
                $seStr =~ s/, $//g;
                $labelStr =~ s/, $//;

                # Sanitize the data set
                my $cleanDataSet = $dataSet;
                $cleanDataSet =~ s/\.//g;
                $cleanDataSet =~ s/-//g;

                # Put them into R variables
                my $meanIDStr = "all".$dataType.$cleanDataSet.$statType."means";
                print INPUT "$meanIDStr = c($meanStr)\n";
                push( @meanIDStrings, $meanIDStr );

                my $sdIDStr = "all".$dataType.$cleanDataSet.$statType."sd";
                print INPUT "$sdIDStr = c($sdStr)\n";
                push( @sdIDStrings, $sdIDStr );

                my $seIDStr = "all".$dataType.$cleanDataSet.$statType."se";
                print INPUT "$seIDStr = c($seStr)\n";
                push( @seIDStrings, $seIDStr );

                my $labelIDStr = "all".$dataType.$cleanDataSet.$statType."labels";
                print INPUT "$labelIDStr = c($labelStr)\n";
                push( @labelIDStrings, $labelIDStr );
            }

            # Create a variable for the follower indices
            my @followerIndices = sort( {$a <=> $b} keys( %followerIdxMap ) );
            print INPUT "followerindices = (c(".join( ', ', @followerIndices).")+1)\n";

            # Find the max y value
            print INPUT "ymaxvalue = max( c(".join( ', ', @meanIDStrings)."))\n";

            # Find the x range
            print INPUT "xrange <- range(pretty(c(1,$indCount)))\n";

            # Build the filename
            my $plotEPSFile = $epsFilePrefix."aggregate-neighbor-stats-$dataType-$statType-indcount-$indCount-comparison.eps";
            push( @epsFiles, $plotEPSFile );

            # Send the plot to the specified file and organize the layout
            print INPUT "postscript( file=\"$plotEPSFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
            print INPUT "par(mar=c(8,8,3,5)+0.1)\n";
            print INPUT "par(mgp=c(3.4,1,0))\n";

            # Set up some handy variables
            my @colors = ( "#882255", "#332288", "#117733", "#999933" );
            my @plotSymbols = ( 1, 2, 5, 6 );
            my $dataSetNames = "";
            my $colorsStr = "";
            my $plotSymbolsStr = "";

            # Get the plot format
            my $yMin = 0;
            my $yMax = "ymaxvalue";
            my $yTitle = ucfirst($statType);
            my $xTitle = "Count";
            my $legendLoc = "bottom";
            if( exists( $dataFormat{$statType} ) )
            {
                $yMin = $dataFormat{$statType}{"y-min"};
                $yMax = $dataFormat{$statType}{"y-max"};
                $yTitle = $dataFormat{$statType}{"y-title"};
                $xTitle = $dataFormat{$statType}{"x-title"};
                $legendLoc = $dataFormat{$statType}{"legend-loc"};
            }

            # Plot each data set
            my $index = 0;
            foreach my $dataSet (@dataSets)
            {
                my $misc = "ylab=\"$yTitle\",  xlab=\"$xTitle\", \n".
                        "   ylim=c($yMin,$yMax), xlim=xrange, \n";
                my $cmd = "plot";

                # Add a line behind the plot if it is the right type
                if( $statType =~ /probability/ )
                {
                    $misc .= " panel.first = c(abline( h=0.5, lty=2, col=\"#666666\", lwd=0.75)), \n";
                }
                elsif ( $statType =~ /percentage/ )
                {
                    $misc .= " panel.first = c( abline( a=0, b=(1/9), lty=2, col=\"#666666\", lwd=0.75), abline( a=(-($indCount-10)/9), b=(1/9), lty=2, col=\"#666666\", lwd=0.75)),\n";
                }

                if( $index > 0 )
                {
                    $misc = "";
                    $cmd = "lines";
                }
                print INPUT "$cmd( followerindices, ",$meanIDStrings[$index],", type=\"l\", \n",
                        "    lwd=2,\n", # pch=",$plotSymbols[$index],", cex=1,\n",
                        "    ",$misc," col=\"",$colors[$index],"\", yaxt='n' )\n";

                print INPUT "par(fg=\"",$colors[$index],"\")\n";
                print INPUT "errbar( followerindices, ",$meanIDStrings[$index],", \n",
                        "    (",$meanIDStrings[$index],"+",$seIDStrings[$index],"), \n",
                        "    (",$meanIDStrings[$index],"-",$seIDStrings[$index],"), \n",
                        "    add=TRUE, lwd=0.75, pch=",$plotSymbols[$index],", cex=0.75,\n",
                        "    col=\"",$colors[$index],"\", yaxt='n' )\n";
                print INPUT "par(fg=\"black\")\n";

                # Add the current name, color and symbol
                my $name = "bold";
                if( $dataSet =~ /0.2/ )
                {
                    $name = "shy";
                }
                elsif( $dataSet =~ /0.5/ )
                {
                    $name = "moderate";
                }
                $dataSetNames .= "\"Initial $dataType $name\", ";
                $colorsStr .= "\"".$colors[$index]."\", ";
                $plotSymbolsStr .= $plotSymbols[$index].", ";
    
                $index++;
            }

            # Clean up the names, colors, and symbols
            $dataSetNames =~ s/, $//;
            $colorsStr =~ s/, $//;
            $plotSymbolsStr =~ s/, $//;

            # Plot the legend
            print INPUT "legend( \"$legendLoc\", c($dataSetNames), col=c($colorsStr), \n",
                    "    pch=c($plotSymbolsStr), lty=1, lwd=2, bty=\"n\", ncol=",(ceil($#dataSets / 2)),", \n",
                    "    box.lwd=0, box.col=\"white\", bg=\"white\" )\n";

            # Customize the y axes
#            print INPUT "ticklocations <- axTicks(2)\n";
            print INPUT "ticklocations <- c(seq(from=$yMin,to=$yMax,length.out=5))\n";
            print INPUT "ticklocations\n";

            if( $statType =~ /percentage/ )
            {
                print INPUT "templabels <- ticklocations * 100\n";
                print INPUT "percentagelabels <- paste(templabels, \"%\", sep=\"\" )\n";
                print INPUT "axis( 2, las=2, at=ticklocations, labels=percentagelabels )\n";
#                print INPUT "axis( 4, las=2, at=ticklocations, labels=percentagelabels )\n";
            }
            else
            {
                print INPUT "axis( 2, las=2, at=ticklocations )\n";
#                print INPUT "axis( 4, las=2, at=ticklocations )\n";
            }

            # Turn off the file plot
            print INPUT "dev.off()\n\n\n";
        } 
    }


    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );

    # ---------------------------------------------------------------
    # Return the EPS files built
    return @epsFiles;
}


# ===================================================================
sub printKSTestText
{
    local *INPUT = shift;
    my ($firstID, $firstKey, $secondID, $secondKey) = @_;
return;
            print INPUT "ks <- ks.boot( ",
                    $firstID,
                    ",",
                    $secondID,
                    " )\n";
            print INPUT "cat(\"ks.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value = \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
            print INPUT "cat(\"ks.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".naive-pvalue = \", format(ks\$ks\$p.value), \"\\n\")\n";
            print INPUT "cat(\"ks.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".statistic = \", format(ks\$ks\$statistic), \"\\n\")\n";
            print INPUT "ttest.p.value <- tryttestpvalue( ",
                    $firstID,
                    ",",
                    $secondID,
                    " )\n";
            print INPUT "cat(\"t-test.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value =   \", format(ttest.p.value), \"\\n\")\n";
#            print INPUT "cat(\"t-test.",
#                    $firstKey,
#                    ".vs.",
#                    $secondKey,
#                    ".statistic = \", format(ttest\$statistic), \"\\n\")\n";
            print INPUT "cat(\"\\n\")\n";

}


