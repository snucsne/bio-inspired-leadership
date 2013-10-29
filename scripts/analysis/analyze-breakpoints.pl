#!/usr/bin/perl
use strict;
use Data::Dumper;
use constant BOLDTHRESHOLD => 0.75;
use constant SHYTHRESHOLD => 0.4;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
unless( $dataDir =~ /\/$/ )
{
    $dataDir = "$dataDir/";
}

# Get the individual count
my $indCount = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );


# -------------------------------------------------------------------
# Get the breakpoint files
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @breakpointFiles = grep { /^indcount-0*$indCount-breakpoint-analysis-\d+.dat$/ } readdir( DIR );
@breakpointFiles = sort( @breakpointFiles );
closedir( DIR );


# -------------------------------------------------------------------
# Initialize the data
my %data;
$data{"ind-count"} = $indCount;
$data{"bold-count"} = 0;
$data{"shy-count"} = 0;
$data{"total-bold-then-shy-count"} = 0;
$data{"total-shy-then-bold-count"} = 0;

# -------------------------------------------------------------------
# Process each breakpoint file
foreach my $breakpointFile (@breakpointFiles)
{
#print "Processing [$breakpointFile]\n";
    # Build some analysis-specific data
    my %analysisData;
    $analysisData{"breakpoint-file"} = $breakpointFile;
    $breakpointFile =~ /analysis-(\d+)\.dat/;
    $analysisData{"run-id"} = $1;

    # Provide a default for the simulation count
    $analysisData{"total-simulations"} = $indCount * 2000;

    # Load the original analysis
#    my %analysisData;
    loadBreakPointAnalysis( $dataDir.$breakpointFile, \%analysisData );

    # Provide a default for the simulation count
    unless( $analysisData{"total-simulations"} )
    {
        $analysisData{"total-simulations"} = $indCount * 2000;
#        print "Default total sims [".$analysisData{"total-simulations"}."]\n";
    }

    # Process it
    processBreakPointAnalysis( \%analysisData, \%data );
}

# -------------------------------------------------------------------
# Dump out all the data
open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

print OUTPUT "# =======================================\n";
print OUTPUT "# date=[",scalar(localtime()),"]\n";
print OUTPUT "# dataDir=[$dataDir]\n";
print OUTPUT "# indCount=[$indCount]\n";
print OUTPUT "# =======================================\n\n";

foreach my $key ( sort( keys( %data ) ) )
{
    my $value = $data{$key};
    $value =~ s/\s*$//;
    print OUTPUT "$key = $value\n";
}

close( OUTPUT );



# ===================================================================
sub loadBreakPointAnalysis
{
    my ($dataFile, $dataRef) = @_;

    # Open the file
    open( DATA, "$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

    # Read each line
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

        # Is it the total number of simulations?
        if( $key =~ /total-simulations/ )
        {
            $dataRef->{"total-simulations"} = $value;
        }

        # Is it data about an individual?
        elsif( $key =~ /^(ind\d+)\.(\w+)/ )
        {
            # Yup
            my $id = $1;
            my $dataKey = $2;
            $dataRef->{"results"}{$id}{$dataKey} = $value;
        }
    }

    close( DATA );
}


# ===================================================================
sub processBreakPointAnalysis
{
    my ($analysisDataRef, $dataRef) = @_;

    my $totalUpdates = 0;
    my $totalIndividuals = 0;
    my $totalBoldIndividuals = 0;
    my $boldThenShyCount = 0;
    my $shyThenBoldCount = 0;

    my $runDifferentiation = "[";
    my $runDifferentiationPercentage = "[";

    foreach my $ind (sort keys( %{$analysisDataRef->{"results"}} ) )
    {
#print "ind=[$ind]\n";
        # Get the data
        my $updates = $analysisDataRef->{"results"}{$ind}{"updates"};
        my @breakpoints = split( /\s+/, $analysisDataRef->{"results"}{$ind}{"breakpoints"} );
        my @breakSims = split( /\s+/, $analysisDataRef->{"results"}{$ind}{"breaksims"} );
        my @coefficients = split( /\s+/, $analysisDataRef->{"results"}{$ind}{"coefficients"} );

        # Do some processing
        # Is it bold?
        if( $coefficients[-1] > BOLDTHRESHOLD )
        {
            # Yup
            # Calculate the number of updates & sims for which the individual was bold
            my $lastBoldIdx = findOldestBoldCoefficientIdx( \@coefficients );
            my $boldUpdate = $breakpoints[$lastBoldIdx - 1];
            my $boldSim = $breakSims[$lastBoldIdx - 1];
            my $totalBoldUpdates = $updates - $boldUpdate;
            my $totalBoldSims = $analysisDataRef->{"total-simulations"} - $boldSim;

            # Convert them to percentages
            my $boldUpdatePercentage = $totalBoldUpdates / $updates;
            my $boldSimsPercentage = $totalBoldSims / $analysisDataRef->{"total-simulations"};
            my $boldDifferentiationPercentage = $boldSim / $analysisDataRef->{"total-simulations"};

            # Save them
            $dataRef->{"bold-updates"} .= $totalBoldUpdates."  ";
            $dataRef->{"bold-update-percentage"} .= $boldUpdatePercentage."  ";
#            $dataRef->{"bold-sims"} .= $totalBoldSims."  ";
            $dataRef->{"bold-sim-percentage"} .= $boldSimsPercentage."  ";
            $dataRef->{"bold-differentiation"} .= $boldSim."  ";
            $dataRef->{"bold-differentiation-percentage"} .= $boldDifferentiationPercentage."  ";
            $runDifferentiation .= $boldSim."  ";
            $runDifferentiationPercentage .= $boldDifferentiationPercentage."  ";

            # Save some other interesting information
#            $dataRef->{"bold-breakpoint-count"} .= scalar( @breakpoints )."  ";;
            $dataRef->{"bold-update-count"} .= $updates."  ";

            # Did it used to be shy?
            if( wasShyThenBold( \@coefficients ) )
            {
                # Yup
                $dataRef->{"shy-then-bold"} .= "[".$analysisDataRef->{"run-id"}." ".$ind."]  ";
                $shyThenBoldCount++;
                $dataRef->{"total-shy-then-bold-count"}++;
            }


            $totalBoldIndividuals++;
        }
        else
        {
            # Calculate the number of updates & sims for which the individual was shy
            my $lastShyIdx = findOldestShyCoefficientIdx( \@coefficients );
            my $shyUpdate = $breakpoints[$lastShyIdx - 1];
            my $shySim = $breakSims[$lastShyIdx - 1];
            my $totalShyUpdates = $updates - $shyUpdate;
            my $totalShySims = $analysisDataRef->{"total-simulations"} - $shySim;

            # Convert them to percentages
            my $shyUpdatePercentage = $totalShyUpdates / $updates;
            my $shySimsPercentage = $totalShySims / $analysisDataRef->{"total-simulations"};

            # Save them
            $dataRef->{"shy-updates"} .= $totalShyUpdates."  ";
            $dataRef->{"shy-update-percentage"} .= $shyUpdatePercentage."  ";
#            $dataRef->{"shy-sims"} .= $totalShySims."  ";
            $dataRef->{"shy-sim-percentage"} .= $shySimsPercentage."  ";

            # Save some intersting information
#            $dataRef->{"shy-breakpoint-count"} .= scalar( @breakpoints )."  ";;
            $dataRef->{"shy-update-count"} .= $updates."  ";

            # Did it used to be bold?
            if( wasBoldThenShy( \@coefficients ) )
            {
                # Yup
                $dataRef->{"bold-then-shy"} .= "[".$analysisDataRef->{"run-id"}." ".$ind."]  ";
                $boldThenShyCount++;
                $dataRef->{"total-bold-then-shy-count"}++;
            }
        }

        # Add the update count to the total
        $totalUpdates += $updates;

        $totalIndividuals++;
    }

    $runDifferentiation =~ s/\s+$//;
    $dataRef->{"sim-bold-differentiation"} .= $runDifferentiation."]  ";
    $runDifferentiationPercentage =~ s/\s+$//;
    $dataRef->{"sim-bold-differentiation-percentage"} .= $runDifferentiationPercentage."]  ";

    $dataRef->{"shy-then-bold-count"} .= $shyThenBoldCount."  ";
    $dataRef->{"shy-then-bold-percentage"} .= ($shyThenBoldCount/$totalIndividuals)."  ";
    $dataRef->{"bold-then-shy-count"} .= $boldThenShyCount."  ";
    $dataRef->{"bold-then-shy-percentage"} .= ($boldThenShyCount/$totalIndividuals)."  ";

    $totalUpdates -= $totalIndividuals;

    # Save the total number of updates and individuals
    $dataRef->{"total-updates"} = $totalUpdates;
    $dataRef->{"total-individuals"} += $totalIndividuals;
    $dataRef->{"total-bold-individuals"} += $totalBoldIndividuals;
    $dataRef->{"bold-individual-count"} .= $totalBoldIndividuals."  ";
}

# ===================================================================
sub findOldestBoldCoefficientIdx
{
    my ($coefficientsRef) = @_;

    my @coefficients = \$coefficientsRef;

    # Work backwards
    my $lastBoldIdx = -1;
    for( my $i = (@coefficients - 1); $i >= 0; $i-- )
    {
        if( $coefficients[$i] > BOLDTHRESHOLD )
        {
            $lastBoldIdx = $i;
        }
        else
        {
            last;
        }
    }

    return $lastBoldIdx;
}

# ===================================================================
sub findOldestShyCoefficientIdx
{
    my ($coefficientsRef) = @_;

    my @coefficients = \$coefficientsRef;

    # Work backwards
    my $lastBoldIdx = -1;
    for( my $i = (@coefficients - 1); $i >= 0; $i-- )
    {
        if( $coefficients[$i] < SHYTHRESHOLD )
        {
            $lastBoldIdx = $i;
        }
        else
        {
            last;
        }
    }

    return $lastBoldIdx;
}

# ===================================================================
sub wasBoldThenShy
{
    my ($coefficientsRef) = @_;

    my @coefficients = @$coefficientsRef;

    # Iterate through, skipping the first one.  This is because the first one
    # is determined by the initial personality value, not experience
    my $lastBoldIdx = -1;
    my $lastShyIdx = -1;
    for( my $i = 1; $i < @coefficients; $i++ )
    {
#print "i=[$i]\n";
        if( $coefficients[$i] > BOLDTHRESHOLD )
        {
            $lastBoldIdx = $i;
        }
        elsif( $coefficients[$i] < SHYTHRESHOLD )
        {
            $lastShyIdx = $i;
        }
    }
#print "lastBoldIdx=[$lastBoldIdx] lastShyIdx=[$lastShyIdx]\n";

    # Is the last bold before the last shy?
    my $boldThenShy = 0;
    if( ($lastBoldIdx > 0)
        && ($lastShyIdx > 0)
        && ($lastBoldIdx < $lastShyIdx) )
    {
        $boldThenShy = 1;
#print "Bold then shy\n";
    }

    return $boldThenShy;
}


# ===================================================================
sub wasShyThenBold
{
    my ($coefficientsRef) = @_;

    my @coefficients = @$coefficientsRef;

#print "coefficients=[@coefficients]\n";

    # Iterate through, skipping the first one.  This is because the first one
    # is determined by the initial personality value, not experience
    my $lastBoldIdx = -1;
    my $lastShyIdx = -1;
    for( my $i = 1; $i < @coefficients; $i++ )
    {
        if( $coefficients[$i] > BOLDTHRESHOLD )
        {
            $lastBoldIdx = $i;
        }
        elsif( $coefficients[$i] < SHYTHRESHOLD )
        {
            $lastShyIdx = $i;
        }
    }
#print "lastBoldIdx=[$lastBoldIdx] lastShyIdx=[$lastShyIdx]\n";

    # Is the last shy before the last bold?
    my $shyThenBold = 0;
    if( ($lastShyIdx > 0)
        && ($lastBoldIdx > 0)
        && ($lastShyIdx < $lastBoldIdx) )
    {
        $shyThenBold = 1;
#print "Shy then bold\n";
    }

    return $shyThenBold;
}

