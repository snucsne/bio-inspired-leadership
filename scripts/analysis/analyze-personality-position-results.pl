#!/usr/bin/perl
use strict;
use POSIX;
use List::Util qw[min max];

# -------------------------------------------------------------------
use constant BOLDTHRESHOLD => 0.875;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

# Get the data output file prefix
my $outputFilePrefix = shift( @ARGV );

# Get the eps file prefix
#my $epsFilePrefix = shift( @ARGV );

# Get the index of the personality "switch"
my $switchSimIndex = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @dataFiles = grep { /\.dat$/ } readdir( DIR );
@dataFiles = sort( @dataFiles );
closedir( DIR );

#print "Found ",($#dataFiles + 1)," files\n";


# -------------------------------------------------------------------
# Process each data files
foreach my $dataFile (@dataFiles)
{
    processDataFile( "$dataDir/$dataFile", $outputFilePrefix, $switchSimIndex );
}

# ===================================================================
sub processDataFile
{
    my ($dataFile, $dataOutputPrefix,, $switchSimIndex) = @_;

    # Load the data
    my %data;
    $data{"switch-count"}{"returned-to-bold"} = 0;
    $data{"switch-list"}{"returned-to-bold"} = [];
    $data{"switch-count"}{"remained-bold"} = 0;
    $data{"switch-list"}{"remained-bold"} = [];

    $data{"datafile"} = $dataFile;
    loadData( $dataFile, \%data, $switchSimIndex );

    # Analyze it
    analyzeData( \%data, $dataOutputPrefix );
}

# ===================================================================
sub loadData
{
    my ($dataFile, $dataRef, $switchSimIndex) = @_;

    # Open the file
    open( DATA, "$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

#print "    Loading data from [$dataFile]\n";

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

       # Get the individual's ID
       $key =~ /Ind(\d+)/;
       my $indID = $1;

        if( $key =~ /^total-simulations$/ )
        {
            $dataRef->{"total-simulations"} = $value;
        }
        elsif( $key =~ /mimicing-neighbor-count/ )
        {
            $dataRef->{"mimicing-neighbor-count"}{$indID} = $value;
        }
        elsif( $key =~ /individual.*personality/ )
        {
            $dataRef->{"personality"}{$indID} = $value;
        }
        elsif( $key =~ /initiation-history/ )
        {
            push( @{$dataRef->{"initiation-history"}}, $value );

            # Process the history data
            my @simIDXs;
            my @personalityValues;
            processHistoryData( $value,
                    $indID,
                    \@simIDXs,
                    \@personalityValues,
                    $dataRef,
                    $switchSimIndex );
            
            # Get the individual's ID
            $key =~ /Ind(\d+)/;
            my $indID = $1;

            # Store the data
            $dataRef->{"history"}{$indID}{"sim-indices"} = \@simIDXs;
            $dataRef->{"history"}{$indID}{"personality-values"} = \@personalityValues;
        }
    }

    # Close the file
    close( DATA );
}

# ===================================================================
sub processHistoryData
{
    my ($historyData, $indID, $simIDXsRef, $personalityValuesRef, $dataRef, $switchSimIndex) = @_;

    # Clean up and split the history
    $historyData =~ s/^\s+\[//;
    $historyData =~ s/\]\s+$//;
    my @initiations = split( /\]\s+\[/, $historyData );

    # If it wasn't the first initiator, go ahead and add the initial personality
    my ($firstIdx, $initialPersonality, $trash) = split( /\s+/, $historyData );
    push( @{$simIDXsRef}, 0 );
    push( @{$personalityValuesRef}, $initialPersonality );

    # Go through all the initiations and check for our interesting situations
    my $initialBold = 0;
    my $passedSwitch = 0;
    my $reversedToBold = 0;
    my $lastPersonality = 0;
    foreach my $initiation (@initiations)
    {
        my ($idx, $beforePersonality, $result, $afterPersonality, $followers) =
                split( /\s+/, $initiation );
        
        # If multipli initiations are allowed, it is possible for multiple
        # updates to occur in one simulation.  Only use the last update.
        if( @{$simIDXsRef}[$#{$simIDXsRef}] == ($idx + 1) )
        {
            pop( @{$simIDXsRef} );
            pop( @{$personalityValuesRef} );
        }

        push( @{$simIDXsRef}, ($idx + 1) );
        push( @{$personalityValuesRef}, $afterPersonality );
        $lastPersonality = $afterPersonality;

        # Was the individual bold before the switch?
        if( ($idx < $switchSimIndex) && (BOLDTHRESHOLD < $afterPersonality) )
        {
            $initialBold = 1;
        }
        # Is this the first initiation after the switch?
        elsif( !($passedSwitch) && ($idx >= $switchSimIndex) )
        {
            # Yup.  Are they bold?
            if( 0.5 < $beforePersonality )
            {
                # Yup
                $reversedToBold = 1;
            }

            #print "Passed switch: id=[$indID] idx=[$idx] beforePersonality=[$beforePersonality] reversed=[$reversedToBold]\n";

            # Note that we passed the switch index
            $passedSwitch = 1;
        }
    }

    # Was the individual bold at the very end?
    my $finalBold = 0;
    if( BOLDTHRESHOLD < $lastPersonality )
    {
        $finalBold = 1;
    }

    # Store the interesting results
    my $mimickingCount = $dataRef->{"mimicing-neighbor-count"}{$indID};
    $dataRef->{"switch"}{$indID}{"initial-bold"} = 0;
    $dataRef->{"switch"}{$indID}{"returned-to-bold"} = 0;
    $dataRef->{"switch"}{$indID}{"final-bold"} = 0;
    $dataRef->{"switch"}{$indID}{"reversed-to-bold"} = 0;
    $dataRef->{"switch"}{$indID}{"remained-bold"} = 0;

    # Were they initially bold?
    if( $initialBold )
    {
        $dataRef->{"switch-count"}{"initial-bold"}++;
        push( @{$dataRef->{"switch-list"}{"initial-bold"}}, $mimickingCount );
        $dataRef->{"switch"}{$indID}{"initial-bold"} = 1;

        # Where they bold at the end?
        if( $finalBold )
        {
            $dataRef->{"switch-count"}{"returned-to-bold"}++;
            push( @{$dataRef->{"switch-list"}{"returned-to-bold"}}, $mimickingCount );
            $dataRef->{"switch"}{$indID}{"returned-to-bold"} = 1;
        }
    }

    # Were the bold at the end?
    if( $finalBold )
    {
        $dataRef->{"switch-count"}{"final-bold"}++;
        push( @{$dataRef->{"switch-list"}{"final-bold"}}, $mimickingCount );
        $dataRef->{"switch"}{$indID}{"final-bold"} = 1;
    }

    # Was the personality switched?
    if( $reversedToBold )
    {
        $dataRef->{"switch-count"}{"reversed-to-bold"}++;
        push( @{$dataRef->{"switch-list"}{"reversed-to-bold"}}, $mimickingCount );
        $dataRef->{"switch"}{$indID}{"reversed-to-bold"} = 1;

        # Did they remain bold?
        if( $finalBold )
        {
            $dataRef->{"switch-count"}{"remained-bold"}++;
            push( @{$dataRef->{"switch-list"}{"remained-bold"}}, $mimickingCount );
            $dataRef->{"switch"}{$indID}{"remained-bold"} = 1;
        }
    }
}

# ===================================================================
sub analyzeData
{
    my ($dataRef, $dataOutputPrefix) = @_;

    # Build the results file
    my $resultsFile = $dataOutputPrefix.
            "-personality-position-results-".
            $dataRef->{"run-id"}.
            ".dat";

    # ---------------------------------------------------------------
    open( OUTPUT, "> $resultsFile" ) or die "Unable to open results file [$resultsFile]: $!\n";

    # ---------------------------------------------------------------
    # Add the aggregate data
    print OUTPUT "summary.initial-bold.count = ",$dataRef->{"switch-count"}{"initial-bold"},"\n";
    print OUTPUT "summary.initial-bold.mimicking-neighbors = ",
            join( " ", @{$dataRef->{"switch-list"}{"initial-bold"}}),
            "\n\n";
    print OUTPUT "summary.returned-to-bold.count = ",$dataRef->{"switch-count"}{"returned-to-bold"},"\n";
    print OUTPUT "summary.returned-to-bold.mimicking-neighbors = ",
            join( " ", @{$dataRef->{"switch-list"}{"returned-to-bold"}}),
            "\n\n";
    print OUTPUT "summary.final-bold.count = ",$dataRef->{"switch-count"}{"final-bold"},"\n";
    print OUTPUT "summary.final-bold.mimicking-neighbors = ",
            join( " ", @{$dataRef->{"switch-list"}{"final-bold"}}),
            "\n\n";
    print OUTPUT "summary.reversed-to-bold.count = ",$dataRef->{"switch-count"}{"reversed-to-bold"},"\n";
    print OUTPUT "summary.reversed-to-bold.mimicking-neighbors = ",
            join( " ", @{$dataRef->{"switch-list"}{"reversed-to-bold"}}),
            "\n\n";
    print OUTPUT "summary.remained-bold.count = ",$dataRef->{"switch-count"}{"remained-bold"},"\n";
    print OUTPUT "summary.remained-bold.mimicking-neighbors = ",
            join( " ", @{$dataRef->{"switch-list"}{"remained-bold"}}),
            "\n\n";

    # ---------------------------------------------------------------
    # Add the indidividual data
    foreach my $indID ( sort( keys %{$dataRef->{"switch"}} ) )
    {
        my $prefix = "individual.".$indID.".";
        print OUTPUT $prefix,"initial-bold        = ",$dataRef->{"switch"}{$indID}{"initial-bold"},"\n";
        print OUTPUT $prefix,"returned-to-bold    = ",$dataRef->{"switch"}{$indID}{"returned-to-bold"},"\n";
        print OUTPUT $prefix,"final-bold          = ",$dataRef->{"switch"}{$indID}{"final-bold"},"\n";
        print OUTPUT $prefix,"reversed-to-bold    = ",$dataRef->{"switch"}{$indID}{"reversed-to-bold"},"\n";
        print OUTPUT $prefix,"remained-bold       = ",$dataRef->{"switch"}{$indID}{"remained-bold"},"\n";
        print OUTPUT $prefix,"mimicking-neighbors = ",$dataRef->{"mimicing-neighbor-count"}{$indID},"\n\n";
    }

    # ---------------------------------------------------------------
    close( OUTPUT );
}

