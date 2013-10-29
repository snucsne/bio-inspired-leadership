#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

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
# Process each data files
foreach my $dataFile (@dataFiles)
{
    my %data;
    loadData( "$dataDir/$dataFile", \%data );
    buildSuccessData( \%data );
    analyzeData( \%data, $outputFile, $epsFilePrefix );
#exit;
}



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

#print "idx=[$idx] result=[$result]\n";

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
    my ($dataRef) = @_;

    # Create some handy variables
    my $successTotalStr = "";
    my $successPercentageStr = "";
    my @successTotals;
    my $successCount = 0;
    my $totalSims = $dataRef->{"total-simulations"};

    # Iterate through every simulation
    for( my $i = 0; $i < $totalSims; $i++ )
    {
        # Was it successful?
        if( $dataRef->{"initiations"}{$i} )
        {
            $successCount++;
        }
        push( @successTotals, $successCount );
        $successTotalStr .= $successCount."  ";

#print "i=[$i]\tresult=[".$dataRef->{"initiations"}{$i}."] successCount=[$successCount]\n";
    }

#print "$successTotalStr\n";

    # Now, go back and calculate the percentages
    for( my $i = 0; $i < $totalSims; $i++ )
    {
        $successPercentageStr .= sprintf( "%08.6f", ($successTotals[$i]/$successCount) )."  ";
    }

    # Store the success data
    $dataRef->{"success-totals"} = $successTotalStr;
    $dataRef->{"success-percentages"} = $successPercentageStr;
}


# ===================================================================
sub analyzeData
{
    my ($dataRef, $dataOutput, $epsOutputPrefix) = @_;

    my $plotRunID = $$;
    my $totalSims = $dataRef->{"total-simulations"};

    # ---------------------------------------------------------------
    my $rSpacer = "# =====================================\n";
    my $rCatSpacer = "cat(\"# =====================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create an input file for R
    my $rInputFile = "/tmp/success-history-TEST.r";
#    my $rInputFile = "/tmp/success-history-$plotRunID.r";
    my $rOutputFile = $rInputFile.".out";

    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

    # ---------------------------------------------------------------
    # Add the data
    print INPUT $rSpacer;
    print INPUT "successtotals <- scan()\n";
    print INPUT $dataRef->{"success-totals"}."\n\n";
    print INPUT "possiblesuccesstotals <- (successtotals/$totalSims)\n\n";
    print INPUT "successpercentages <- scan()\n";
    print INPUT $dataRef->{"success-percentages"}."\n\n";

    # ---------------------------------------------------------------
    # Plot it
    my $epsFile = $epsOutputPrefix.
            "success-history-run-".
            $dataRef->{"run-id"}.
            ".eps";

    print INPUT "postscript( file=\"$epsFile\", height=4.5, width=5.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\", colormodel=\"rgb\" )\n";
    print INPUT "par(mar=c(4,4,4,4))\n";
    print INPUT "plot( possiblesuccesstotals, type=\"s\", ylim=c(0,1), col=\"#882255\",",
            " ylab=\"Percentage of Total Possible Successes\", xlab=\"Simulations\" )\n";
#    print INPUT "abline( 0, 1/".$dataRef->{"total-simulations"}.", col=\"#117733\" )\n";
    print INPUT "segments( 0, 0, $totalSims, 1, col=\"#117733\", lty=2 )\n";
    print INPUT "dev.off()\n\n";

    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );

    # ---------------------------------------------------------------
    # Run it
    `R --no-save < $rInputFile > $rOutputFile 2>&1`;

    # ---------------------------------------------------------------
    # Clean up the eps file
    my $tmpEPSFile = "/tmp/success-history.eps";
    my $fixedTmpEPSFile = "/tmp/fixed-success-history.eps";
    `cp $epsFile $tmpEPSFile`;
    `./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
    `/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
    `epstopdf $epsFile`;

}
