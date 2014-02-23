#!/usr/bin/perl
use strict;
use Data::Dumper;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

# Get the output simulation modulus
my $outputSimModulus = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the eps file
my $epsFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @dataFiles = grep { /\.dat$/ } readdir( DIR );
@dataFiles = sort( @dataFiles );
closedir( DIR );

print "Found ",($#dataFiles + 1)," files\n";

# -------------------------------------------------------------------
# Process each data file
my %totalData;
#my $bailCount = 0;
foreach my $dataFile (@dataFiles)
{
    my %fileData;
    loadData( "$dataDir/$dataFile", \%fileData );
    buildSuccessData( \%fileData, \%totalData );

#$bailCount++;
#if( $bailCount > 5 )
#{
#last;
#}

}

#print Dumper(%totalData);

# -------------------------------------------------------------------
# Analyze the data
analyzeData( \%totalData, $outputSimModulus, $outputFile, $epsFilePrefix );



# ===================================================================
sub loadData
{
    my ($dataFile, $dataRef) = @_;

    # Open the file
    open( DATA, "$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

    # Get the run ID
    $dataFile =~ /var-(\d\d)-seed/;
    my $runID = $1;
    $dataRef->{"run-id"} = $runID;

    # Read each line
    my %rawData;
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

        if( $key =~ /^total-simulations$/ )
        {
            $dataRef->{"total-simulations"} = $value;
        }
        elsif( $key =~ /initiation-history/ )
        {
            # Process the history
            processHistoryData( $value, $dataRef, $runID );
        }
    }

    # Close the file
    close( DATA );
}

# ===================================================================
sub processHistoryData
{
    my ($historyData, $dataRef) = @_;

    # Clean up and split the history
    $historyData =~ s/^\s+\[//;
    $historyData =~ s/\]\s+$//;
    my @initiations = split( /\]\s+\[/, $historyData );

    # Process each initiation
    foreach my $initiation (@initiations)
    {
        # Split the initiation
        my ($idx, $beforePersonality, $result, $afterPersonality, $followers) =
                split( /\s+/, $initiation );

        # If it was successful, log the simulation index
        if( $result =~ /true/ )
        {
            $dataRef->{"initiations"}{$idx} = 1;
        }
    }
}


# ===================================================================
sub buildSuccessData
{
    my ($fileDataRef, $totalDataRef) = @_;

    # Create some handy variables
    my $successTotalStr = "";
    my $successPercentageStr = "";
    my @successTotals;
    my $successCount = 0;
    my $totalSims = $fileDataRef->{"total-simulations"};
    $totalDataRef->{"total-simulations"} = $totalSims;

    # Iterate through every simulation
    for( my $i = 0; $i < $totalSims; $i++ )
    {
        # Was it successful?
        if( $fileDataRef->{"initiations"}{$i} )
        {
            $successCount++;
        }
        push( @{$totalDataRef->{"successes"}{$i}}, $successCount );
        push( @successTotals, $successCount );
        $successTotalStr .= $successCount."  ";
    }

    # Save the total number of successes
    push( @{$totalDataRef->{"total-successes"}}, $successCount );

    # Build success percentages
    for( my $i = 0; $i < $totalSims; $i++ )
    {
        push( @{$totalDataRef->{"success-percentages"}{$i}},
                (${$totalDataRef->{"successes"}{$i}}[-1] / $successCount) );
    }
}


# ===================================================================
sub analyzeData
{
    my ($totalDataRef, $outputSimModulus, $dataOutput, $epsOutputPrefix) = @_;

    my $plotRunID = $$;
    my $totalSims = $totalDataRef->{"total-simulations"};

    # ---------------------------------------------------------------
    # Build an constant data set
    my %constant;
    for( my $i = 0; $i < $totalSims; $i++ )
    {
        $constant{$i} = ($i / $totalSims);
    }


    # ---------------------------------------------------------------
    my $rSpacer = "# =====================================\n";
    my $rCatSpacer = "cat(\"# =====================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create an input file for R
    my $rInputFile = "/tmp/success-history-TEST.r";
#    my $rInputFile = "/tmp/success-history-$plotRunID.r";
    my $rOutputFile = $rInputFile.".out";

    # ---------------------------------------------------------------
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
#    print INPUT "library(ggplot2)\n";

    # ---------------------------------------------------------------
    # Add the data
    my @dataIDs;
    my @meanDataValues;
    my @sdDataValues;
    my @constantValues;
    foreach my $sim ( sort {$a <=> $b} ( keys %{$totalDataRef->{"success-percentages"}} ) )
    {
        # Is it one we actually use?
        if( (($sim % $outputSimModulus) == 0) || ($sim == $totalSims - 1) )
        {
            # Yup
            my $id = "successpercent".sprintf("%07d", $sim);
            print INPUT "$id <- scan()\n";
            print INPUT join( " ", @{$totalDataRef->{"success-percentages"}{$sim}}),"\n\n";

            # Save some things
            push( @dataIDs, $id );
            push( @meanDataValues, "mean($id)" );
            push( @sdDataValues, "sd($id)" );
            push( @constantValues, $constant{$sim} );
        }
    }

    # ---------------------------------------------------------------
    # Create data for the mean and sd
    my $meanSuccessPercentageID = "meansuccesspercentages";
    print INPUT "$meanSuccessPercentageID <- c(",
            join( ",", @meanDataValues),
            ")\n\n";
    my $sdSuccessPercentageID = "sdsuccesspercentages";
    print INPUT "$sdSuccessPercentageID <- c(",
            join( ",", @sdDataValues),
            ")\n\n";

    # ---------------------------------------------------------------
    # Create data for the constant cumulative fraction
    my $constantID = "constantcumfrac";
    print INPUT "$constantID <- c(",
            join( ",", @constantValues ),
            ")\n\n";

    # ---------------------------------------------------------------
    # Create a dataframe
    print INPUT "spdf <- data.frame( group=rep('a',",
            ($totalSims / $outputSimModulus) + 1,
            "), sims=c(seq(0,",
            ($totalSims - 1),
            ",by=$outputSimModulus),$totalSims), percentages=$meanSuccessPercentageID, sd=$sdSuccessPercentageID )\n\n";

    # ---------------------------------------------------------------
    # Create the difference data
    print INPUT "constantdiff <- ($meanSuccessPercentageID - $constantID)\n";
    print INPUT "constantdiffcolors <- ifelse( constantdiff >= 0, \"blue\", \"orange\" )\n";

    # ---------------------------------------------------------------
    # Create a plot of the difference between the mean and constant
    my @epsFiles;
    my $epsFile = $epsOutputPrefix."-difference-from-constant.eps";
    push( @epsFiles, $epsFile );

    print INPUT "postscript( file=\"$epsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
    print INPUT "par(mgp=c(3,1,0))\n";

    print INPUT "plot(  spdf\$sims, constantdiff, \n",
            "    ylim=c(-0.02,0.02), lwd=2, col=constantdiffcolors, \n",
            "    xlab=\"Simulations\", ylab=\"Difference from constant\", type=\"h\")\n";

#    print INPUT "plot\n";
    print INPUT "dev.off()\n";


    # ---------------------------------------------------------------
    # Create a pretty plot of the success percentages
    $epsFile = $epsOutputPrefix."-success-percentages.eps";
    push( @epsFiles, $epsFile );

    print INPUT "library(ggplot2)\n";
    print INPUT "cairo_ps( file=\"$epsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12 )\n";
    print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
    print INPUT "par(mgp=c(3,1,0))\n";

    print INPUT "plot <- ggplot( spdf, aes(x=sims, y=percentages, color=group,fill=group) ) + \n";
    print INPUT "geom_line() + \n";
    print INPUT "geom_ribbon( aes(ymin=percentages-sd,ymax=percentages+sd), alpha=0.3, colour=NA) +\n";
    print INPUT "theme(legend.position=\"none\") +\n";
    print INPUT "xlab(\"Simulations\") +\n";
    print INPUT "ylab(\"Cumulative fraction of success\") +\n";
    print INPUT "scale_color_manual(values = c('a'=\"blue\"))\n";
    print INPUT "plot\n";
    print INPUT "dev.off()\n";
    



    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );

    # ---------------------------------------------------------------
    # Run it
    `R --no-save < $rInputFile > $rOutputFile 2>&1`;

    # -------------------------------------------------------------------
    # Clean up the EPS files
    my $tmpEPSFile = "/tmp/tmp.eps";
    my $fixedTmpEPSFile = "/tmp/fixed-tmp.eps";
    foreach my $epsFile (@epsFiles)
    {
        `cp $epsFile $tmpEPSFile`;
        `./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
        `/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
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
