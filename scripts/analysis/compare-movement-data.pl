#!/usr/bin/perl
use strict;
use Data::Dumper;

# -------------------------------------------------------------------
# Get the output file
my $outputFile = shift( @ARGV );

# Get all the treatment ID's, their file, and description
my @treatmentIDs;
my %treatments;
while( @ARGV )
{
    my $treatmentID = shift( @ARGV );
    push( @treatmentIDs, $treatmentID );
    $treatments{$treatmentID}{"file"} = shift( @ARGV );
    $treatments{$treatmentID}{"desc"} = shift( @ARGV );
}


# -------------------------------------------------------------------
# Load each of the data files
my %data;
foreach my $treatmentID (@treatmentIDs)
{
    readDataFile( $treatments{$treatmentID}{"file"},
            \%data,
            $treatmentID );
}

#print Dumper(%data);


# -------------------------------------------------------------------
# Create the R file
my $rInputFile = "/tmp/compare-movement-data.r";
my $rOutputFile = $rInputFile.".out";
createRInput( $rInputFile,
        $outputFile,
        "/tmp/compare-eps",
        \@treatmentIDs,
        \%treatments,
        \%data );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;


# ===================================================================
sub readDataFile
{
    my ($dataFile, $dataRef, $dataID) = @_;

    # Open the file
    open( INPUT, $dataFile ) or die "Unable to open data file [$dataFile]: $!\n";

    # Read the file
    while( <INPUT> )
    {
        # Remove any comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Split the line into a key and value
        my ($key, $value) = split( /\s+=\s+/, $_ );

        # Is it the time to destination?
        my $timeToDestKey = "time-to-destination";
        if( $key =~ /^$timeToDestKey\.(.+)/ )
        {
            # Yup, get the specific type
            my $type = $1;

            # Is it a rank?
            if( $type =~ /rank-(\d+)/ )
            {
                # Yup, save it
                $dataRef->{$timeToDestKey}{"rank"}{$1}{$dataID} = $value;
            }
            else
            {
                # Nope, but still save it
                $dataRef->{"time-to-destination"}{$type}{$dataID} = $value;
            }
        }

        # Is it the number of final happy individuals?
        elsif( $key =~ /final-happy-inds/ )
        {
            # Yup
            $dataRef->{"final-happy-inds"}{$dataID} = $value;
        }

        # Is it the number of happy individuals at a specific time step?
        elsif( $key =~ /happy-inds\.(\d+)/ )
        {
            # Yup
            $dataRef->{"happy-inds"}{$1}{$dataID} = $value;
        }
    }

    close( INPUT );
}


# ===================================================================
sub createRInput
{
    my ($rInputFile, $resultsFile, $epsFilePrefix, $treatmentIDsRef, $treatmentsRef, $dataRef) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# =========================================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";

    # Build a standard error function
    print INPUT "stderr <- function(x) sd(x)/sqrt(length(x))\n\n";

    # ---------------------------------------------------------------
    # Add the data
    my %dataIDs;
    my @allDataIDs;
    my %treatmentIDHash;

    # Start with the time to destination
    foreach my $rankID (sort (keys %{$dataRef->{"time-to-destination"}{"rank"}} ) )
    {
        # Go through each treatment
        foreach my $treatmentID ( sort (keys %{$dataRef->{"time-to-destination"}{"rank"}{$rankID}} ) )
        {
            # Build the data ID
            my $dataID = "timetodest.rank".$rankID.$treatmentID;
            $dataIDs{"time-to-destination"}{"rank"}{$rankID}{$treatmentID} = $dataID;

            # Add the data to the R file
            print INPUT "$dataID <- scan()\n";
            print INPUT $dataRef->{"time-to-destination"}{"rank"}{$rankID}{$treatmentID},"\n\n";
        }
    }

    # Move on to the time to destination for all destinations
    foreach my $treatmentID ( sort (keys %{$dataRef->{"time-to-destination"}{"all"}} ) )
    {
        # Build the data ID
        my $dataID = "timetodest.all".$treatmentID;
        $dataIDs{"time-to-destination"}{"all"}{$treatmentID} = $dataID;

        # Add the data to the R file
        print INPUT "$dataID <- scan()\n";
        print INPUT $dataRef->{"time-to-destination"}{"all"}{$treatmentID},"\n\n";
    }

    # Move on to the final number of happy individuals
    foreach my $treatmentID ( sort (keys %{$dataRef->{"final-happy-inds"}} ) )
    {
        # Build the data ID
        my $dataID = "finalhappy".$treatmentID;
        $dataIDs{"final-happy-inds"}{$treatmentID} = $dataID;

        # Add the data to the R file
        print INPUT "$dataID <- scan()\n";
        print INPUT $dataRef->{"final-happy-inds"}{$treatmentID},"\n\n";
    }

    # Finish with the number of happy individuals at each time step
    foreach my $time (sort (keys %{$dataRef->{"happy-inds"}} ) )
    {
        # Go through each treatment
        foreach my $treatmentID ( sort (keys %{$dataRef->{"happy-inds"}{$time}} ) )
        {
            # Build the data ID
            my $dataID = "happyinds".$time.$treatmentID;
            $dataIDs{"happy-inds"}{$time}{$treatmentID} = $dataID;

            # Add the data to the R file
            print INPUT "$dataID <- scan()\n";
            print INPUT $dataRef->{"happy-inds"}{$time}{$treatmentID},"\n\n";
        }
    }



    # ---------------------------------------------------------------
    # Start sending output to the results file
    print INPUT "sink(\"$resultsFile\")\n";

    # ---------------------------------------------------------------
    # Add the statistics
    # Start with the time to destination by destination rank
    foreach my $rankID (sort (keys %{$dataRef->{"time-to-destination"}{"rank"}} ) )
    {
        # Go through each treatment
        foreach my $treatmentID ( sort (keys %{$dataRef->{"time-to-destination"}{"rank"}{$rankID}} ) )
        {
            my $id = $dataIDs{"time-to-destination"}{"rank"}{$rankID}{$treatmentID};
            print INPUT "cat(\"# =====================================\\n\")\n";
            print INPUT "cat(\"# Time-to-destination by rank: rank=[$rankID] treatment=[",
                    $treatmentsRef->{$treatmentID}{"desc"},"]\\n\")\n";
            print INPUT buildDescriptionString( "summary.time-to-destination.rank-$rankID.$treatmentID",
                    $id );
            print INPUT "cat(\"\\n\")\n";
        }

        # Perform the significance tests
        my @timeToDestRankIDs = keys %{$dataRef->{"time-to-destination"}{"rank"}{$rankID}};
        for( my $i = 0; $i <= ($#timeToDestRankIDs - 1); $i++ )
        {

            for( my $j = $i + 1; $j <= ($#timeToDestRankIDs); $j++ )
            {
                print INPUT "cat(\"# =====================================\\n\")\n";
                print INPUT "cat(\"# KS Test rank=[$rankID]: [",
                        $timeToDestRankIDs[$i],
                        "] vs [",
                        $timeToDestRankIDs[$j],
                        "]\\n\")\n";
                printKSTestText( *INPUT,
                        "rank-$rankID.",
                        $dataIDs{"time-to-destination"}{"rank"}{$rankID}{$timeToDestRankIDs[$i]},
                        $timeToDestRankIDs[$i],
                        $dataIDs{"time-to-destination"}{"rank"}{$rankID}{$timeToDestRankIDs[$j]},
                        $timeToDestRankIDs[$j] );
            }
        }

        print INPUT "cat(\"\\n\")\n";
    }

    # ---------------------------------------------------------------
    # Move on to the time to destination for all destinations
    print INPUT "cat(\"\\n\")\n";
    foreach my $treatmentID ( sort (keys %{$dataRef->{"time-to-destination"}{"all"}} ) )
    {
        my $id = $dataIDs{"time-to-destination"}{"all"}{$treatmentID};
        print INPUT "cat(\"# =====================================\\n\")\n";
        print INPUT "cat(\"# Time-to-destination (total): treatment=[",
                $treatmentsRef->{$treatmentID}{"desc"},"]\\n\")\n";
        print INPUT buildDescriptionString( "summary.time-to-destination.all.$treatmentID",
                $id );
            print INPUT "cat(\"\\n\")\n";
    }

    # Perform the significance tests
    my @timeToDestAllIDs = keys %{$dataRef->{"time-to-destination"}{"all"}};
    for( my $i = 0; $i <= ($#timeToDestAllIDs - 1); $i++ )
    {
        for( my $j = $i + 1; $j <= ($#timeToDestAllIDs); $j++ )
        {
            print INPUT "cat(\"# =====================================\\n\")\n";
            print INPUT "cat(\"# KS Test: [",
                    $timeToDestAllIDs[$i],
                    "] vs [",
                    $timeToDestAllIDs[$j],
                    "]\\n\")\n";
            printKSTestText( *INPUT,
                    "all.",
                    $dataIDs{"time-to-destination"}{"all"}{$timeToDestAllIDs[$i]},
                    $timeToDestAllIDs[$i],
                    $dataIDs{"time-to-destination"}{"all"}{$timeToDestAllIDs[$j]},
                    $timeToDestAllIDs[$j] );
        }
    }

    # ---------------------------------------------------------------
    # Move on to the final number of "happy" individuals
    print INPUT "cat(\"\\n\")\n";
    foreach my $treatmentID ( sort (keys %{$dataRef->{"final-happy-inds"}} ) )
    {
        my $id = $dataIDs{"final-happy-inds"}{$treatmentID};
        print INPUT "cat(\"# =====================================\\n\")\n";
        print INPUT "cat(\"# Final number of happy individuals: treatment=[",
                $treatmentsRef->{$treatmentID}{"desc"},"]\\n\")\n";
        print INPUT buildDescriptionString( "summary.final-happy-inds.$treatmentID",
                $id );
            print INPUT "cat(\"\\n\")\n";
    }

    # Perform the significance tests
    my @finalHappyIndsIDs = keys %{$dataRef->{"final-happy-inds"}};
    for( my $i = 0; $i <= ($#finalHappyIndsIDs - 1); $i++ )
    {
        for( my $j = $i + 1; $j <= ($#finalHappyIndsIDs); $j++ )
        {
            print INPUT "cat(\"# =====================================\\n\")\n";
            print INPUT "cat(\"# KS Test: [",
                    $finalHappyIndsIDs[$i],
                    "] vs [",
                    $finalHappyIndsIDs[$j],
                    "]\\n\")\n";
            printKSTestText( *INPUT,
                    "",
                    $dataIDs{"final-happy-inds"}{$finalHappyIndsIDs[$i]},
                    $finalHappyIndsIDs[$i],
                    $dataIDs{"final-happy-inds"}{$finalHappyIndsIDs[$j]},
                    $finalHappyIndsIDs[$j] );
        }
    }


    # ---------------------------------------------------------------
    # Finish with the number of happy individuals at each time step
    foreach my $time (sort (keys %{$dataRef->{"happy-inds"}} ) )
    {
        # Go through each treatment
        foreach my $treatmentID ( sort (keys %{$dataRef->{"happy-inds"}{$time}} ) )
        {
            my $id = $dataIDs{"happy-inds"}{$time}{$treatmentID};
            print INPUT "cat(\"# =====================================\\n\")\n";
            print INPUT "cat(\"# Happy individuals by time: time=[$time] treatment=[",
                    $treatmentsRef->{$treatmentID}{"desc"},"]\\n\")\n";
            print INPUT buildDescriptionString( "summary.happy-inds.time-$time.$treatmentID",
                    $id );
            print INPUT "cat(\"\\n\")\n";
        }

        # Perform the significance tests
        my @happyIndsByTimeIDs = keys %{$dataRef->{"happy-inds"}{$time}};
        for( my $i = 0; $i <= ($#happyIndsByTimeIDs - 1); $i++ )
        {
            for( my $j = $i + 1; $j <= ($#happyIndsByTimeIDs); $j++ )
            {
                print INPUT "cat(\"# =====================================\\n\")\n";
                print INPUT "cat(\"# KS Test time=[$time]: [",
                        $happyIndsByTimeIDs[$i],
                        "] vs [",
                        $happyIndsByTimeIDs[$j],
                        "]\\n\")\n";
#                printKSTestText( *INPUT,
#                        "time-$time.",
#                        $dataIDs{"happy-inds"}{$time}{$happyIndsByTimeIDs[$i]},
#                        $happyIndsByTimeIDs[$i],
#                        $dataIDs{"happy-inds"}{$time}{$happyIndsByTimeIDs[$j]},
#                        $happyIndsByTimeIDs[$j] );
            }
        }

        print INPUT "cat(\"\\n\")\n";
    }


    # ---------------------------------------------------------------
    # Stop sending output to the results file
    print INPUT "sink()\n";

    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );


}

# ===================================================================
sub buildDescriptionString
{
    my ($dataPrefix, $id) = @_;

    my $descStr = "cat(\"$dataPrefix.mean =     \", format( mean($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.max =      \", format( max($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.min =      \", format( min($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.sd =       \", format( sd($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.se =       \", format( stderr($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.length =   \", format( length($id) ), \"\\n\")\n";

    return $descStr;
}


# ===================================================================
sub printKSTestText
{
    local *INPUT = shift;
    my ($keyStr, $firstID, $firstKey, $secondID, $secondKey) = @_;
#return;
            print INPUT "ks <- ks.boot( ",
                    $firstID,
                    ",",
                    $secondID,
                    " )\n";
            print INPUT "cat(\"ks.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value =       \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
            print INPUT "cat(\"ks.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".naive-pvalue =  \", format(ks\$ks\$p.value), \"\\n\")\n";
            print INPUT "cat(\"ks.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".statistic =     \", format(ks\$ks\$statistic), \"\\n\")\n";
            print INPUT "ttest <- t.test( ",
                    $firstID,
                    ",",
                    $secondID,
                    " )\n";
            print INPUT "cat(\"t-test.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value =   \", format(ttest\$p.value), \"\\n\")\n";
            print INPUT "cat(\"t-test.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".statistic = \", format(ttest\$statistic), \"\\n\")\n";
            print INPUT "cat(\"\\n\")\n";

}

