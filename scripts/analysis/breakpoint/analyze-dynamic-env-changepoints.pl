#!/usr/bin/perl
use strict;
use Data::Dumper;
use Statistics::Descriptive;
use Scalar::Util qw(looks_like_number);
use constant BOLDTHRESHOLD => 0.85;
use constant SHYTHRESHOLD => 0.4;
use constant SIMBUFFER => 250;

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

# Get the simulation indices of environmental change
my @envChangeSimIndices;
while( @ARGV )
{
    push( @envChangeSimIndices, shift( @ARGV ) );
}


# -------------------------------------------------------------------
# Statistics configuration
my %statsConfig;
$statsConfig{"inits-to-bold"} = {
        "run" => {
                "mean" => 1,
                "min" => 1,
                "max" => 1},
        "total" => {
                "raw" => {"mean" => 1,
                          "stddev" => 1 },
                "run-mean" => {"mean" => 1,
                               "stddev" => 1},
                "run-min" => {"mean" => 1,
                              "stddev" => 1} } };
$statsConfig{"inits-to-bold-successes"} = {
        "run" => {
                "mean" => 1,
                "min" => 1,
                "max" => 1},
        "total" => {
                "raw" => {"mean" => 1,
                          "stddev" => 1 },
                "run-mean" => {"mean" => 1,
                               "stddev" => 1},
                "run-min" => {"mean" => 1,
                              "stddev" => 1} } };
$statsConfig{"inits-to-bold-successes-percentage"} = {
        "run" => {
                "mean" => 1,
                "min" => 1,
                "max" => 1},
        "total" => {
                "raw" => {"mean" => 1,
                          "stddev" => 1 },
                "run-mean" => {"mean" => 1,
                               "stddev" => 1},
                "run-min" => {"mean" => 1,
                              "stddev" => 1} } };
$statsConfig{"personality"} = {};
$statsConfig{"sims-to-bold"} = {
        "run" => {
                "mean" => 1,
                "min" => 1,
                "max" => 1},
        "total" => {
                "raw" => {"mean" => 1,
                          "stddev" => 1 },
                "run-mean" => {"mean" => 1,
                               "stddev" => 1},
                "run-min" => {"mean" => 1,
                              "stddev" => 1} } };

# -------------------------------------------------------------------
# Get the changepoint files
my $analysisDir = $dataDir."analysis/";
opendir( DIR, $analysisDir ) or die "Unable to open analysis directory [$analysisDir]: $!\n";
my @changepointFiles = grep { /^indcount-0*$indCount-changepoint-analysis-\d+.dat$/ } readdir( DIR );
@changepointFiles = sort( @changepointFiles );
closedir( DIR );


# -------------------------------------------------------------------
# Process each changepoint file
my %totalData;
my @runBoldAtAnyTimeCount;
foreach my $changepointFile (@changepointFiles)
{
    # Build the filename
    my $changepointFile = $analysisDir.$changepointFile;
#print "file=[$changepointFile]\n";

    # Read the data
    my $changepointDataRef = readChangePointFile( $changepointFile );

    # Calculate the simulations at which the environment changed
    my @envChangeSims = (0);
    foreach my $envChangeSimIndex (@envChangeSimIndices)
    {
        push( @envChangeSims, $indCount * $envChangeSimIndex );
    }
    push( @envChangeSims, $changepointDataRef->{"total-simulations"} );

    # Grab the run data file containing the original data
    my $runDataFile = `grep 'data-file' $changepointFile`;
    $runDataFile =~ s/.*\[(.*)\].*\n?/\1/;

    # Process each individual
    my %runData;
    $runData{"bold-at-any-time"} = 0;
    for( my $i = 0; $i < $indCount; $i++ )
    {
        # Get the individual's data
        my $indID = "ind".sprintf( "%05d", $i );
        processIndividual( $indID,
                \%runData,
                $changepointDataRef,
                \@envChangeSims,
                $runDataFile );
    }

    # Save the number of individuals that were bold at any time
    push( @runBoldAtAnyTimeCount, $runData{"bold-at-any-time"} );
    delete $runData{"bold-at-any-time"};

    # Iterate through every change so we can gather some interesting data
    # to contribute to the summary data
    foreach my $idx ( sort( keys %runData) )
    {
        next unless( looks_like_number($idx) );

        # Grab the total number of bold individuals
        my $totalBoldInds = eval( join( '+', @{$runData{$idx}{'bold'}} ) );
        push( @{$totalData{$idx}{'bold-count'}}, $totalBoldInds);

        # Gather some interesting data about the successes
        for( my $i = 0; $i <= $#{$runData{$idx}{'inits-to-bold-successes'}}; $i++ )
        {
            my $percentage = 0;
            if( $runData{$idx}{'inits-to-bold'}[$i] > 0 )
            {
                $percentage = $runData{$idx}{'inits-to-bold-successes'}[$i] /
                            $runData{$idx}{'inits-to-bold'}[$i];
            }
            push( @{$runData{$idx}{'inits-to-bold-successes-percentage'}},
                    $percentage );
#print "idx=[$idx]  i=[$i]  inits-to-bold=[",$runData{$idx}{'inits-to-bold'}[$i],"]\n";

            if( $runData{$idx}{'inits-to-bold'}[$i] == "" )
            {
print "*** BAILING WITH EMPTY [inits-to-bold]***\n";
exit;
            }
        }

        # Add each bit of data to the total
        foreach my $dataKey (keys %{$runData{$idx}})
        {
            next if( $dataKey =~ /^bold$/ );
            next if( (scalar @{$runData{$idx}{$dataKey}}) == 0 );

            # Add the raw data
            push( @{$totalData{$idx}{$dataKey}{'raw'}}, @{$runData{$idx}{$dataKey}} );
            my $byRunStr = " [".join(" ", @{$runData{$idx}{$dataKey}})."] ";
            $totalData{$idx}{$dataKey}{'by-run'} .= $byRunStr;

            # Add some run summary data
            my $runStats = Statistics::Descriptive::Full->new();
            $runStats->add_data( @{$runData{$idx}{$dataKey}} );
            if( $statsConfig{$dataKey}{'run'}{'count'} )
            {
                push( @{$totalData{$idx}{$dataKey}{'run-count'}},
                        $runStats->count() );
            }
            if( $statsConfig{$dataKey}{'run'}{'min'} )
            {
                push( @{$totalData{$idx}{$dataKey}{'run-min'}},
                        $runStats->min() );
            }
            if( $statsConfig{$dataKey}{'run'}{'max'} )
            {
                push( @{$totalData{$idx}{$dataKey}{'run-max'}},
                        $runStats->max() );
            }
            if( $statsConfig{$dataKey}{'run'}{'range'} )
            {
                push( @{$totalData{$idx}{$dataKey}{'run-range'}},
                        $runStats->sample_range() );
            }
            if( $statsConfig{$dataKey}{'run'}{'mean'} )
            {
                push( @{$totalData{$idx}{$dataKey}{'run-mean'}},
                        $runStats->mean() );
            }
            if( $statsConfig{$dataKey}{'run'}{'variance'} )
            {
                push( @{$totalData{$idx}{$dataKey}{'run-variance'}},
                        $runStats->variance() );
            }
            if( $statsConfig{$dataKey}{'run'}{'stddev'} )
            {
                push( @{$totalData{$idx}{$dataKey}{'run-stddev'}},
                        $runStats->standard_deviation() );
            }
        }
    }
}



# -------------------------------------------------------------------
# Dump the data to the results file
open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

# Print some important information
my $date = localtime;
print OUTPUT "# ==================================================================\n";
print OUTPUT "# DataDir:             [$dataDir]\n";
print OUTPUT "# IndCount:            [$indCount]\n";
print OUTPUT "# EnvChangeSimIndices: [",join(',',@envChangeSimIndices),"]\n";
print OUTPUT "# OutputFile:          [$outputFile]\n";
print OUTPUT "# Date:                $date\n";
print OUTPUT "# ==================================================================\n\n";



# -------------------------------------------------------------------
{
    print OUTPUT "# ------------------------------------------------------------------\n";
    print OUTPUT "# General data\n";

    print OUTPUT "bold-at-any-time.values = ",join( ' ', @runBoldAtAnyTimeCount),"\n";
    my ($min, $max, $mean, $stddev) = 0;
    if( scalar @runBoldAtAnyTimeCount > 0 )
    {
        my $stats = Statistics::Descriptive::Full->new();
        $stats->add_data( @runBoldAtAnyTimeCount );
        $min = $stats->min();
        $max = $stats->max();
        $mean = $stats->mean();
        $stddev = $stats->standard_deviation();
    }
    print OUTPUT "bold-at-any-time.min = $min\n";
    print OUTPUT "bold-at-any-time.max = $max\n";
    print OUTPUT "bold-at-any-time.mean = $mean\n";
    print OUTPUT "bold-at-any-time.stddev = $stddev\n\n";
}

# -------------------------------------------------------------------
foreach my $idx ( sort(keys %totalData) )
{
    my $printIdx = sprintf( "%02d", $idx );

    print OUTPUT "# ------------------------------------------------------------------\n";
    print OUTPUT "# Environmental change index=[$printIdx]\n";

    foreach my $dataKey ( sort( keys %{$totalData{$idx}} ) )
    {

        if( $dataKey =~ /bold-count/ )
        {
            # Bail if there isn't any data
            if( scalar @{$totalData{$idx}{$dataKey}} == 0 )
            {
                next;
            }

            # Build the output prefix
            my $prefix = "env-change.$printIdx.$dataKey.";
            
            print OUTPUT $prefix,"values = ",
                    join( ' ', @{$totalData{$idx}{$dataKey}} ),
                    "\n";
            my $stats = Statistics::Descriptive::Full->new();
            $stats->add_data( @{$totalData{$idx}{$dataKey}} );
            print OUTPUT $prefix,"min = ",$stats->min(),"\n";
            print OUTPUT $prefix,"max = ",$stats->max(),"\n";
            print OUTPUT $prefix,"mean = ",$stats->mean(),"\n";
            print OUTPUT $prefix,"stddev = ",$stats->standard_deviation(),"\n";

            print OUTPUT "\n";
            next;
        }

        foreach my $type ( sort( keys %{$totalData{$idx}{$dataKey}} ) )
        {
            # Build the prefix
            my $prefix = "env-change.$printIdx.$dataKey.";
            unless( $type =~ /raw/ )
            {
                $prefix .= "$type.";
            }

            if( $type =~ /by-run/ )
            {
                print OUTPUT $prefix,"values = ",
                        $totalData{$idx}{$dataKey}{$type},
                        "\n";
                next;
            }

            # Bail if there isn't any data
            if( scalar @{$totalData{$idx}{$dataKey}{$type}} == 0 )
            {
                print "*** BAILING DUE TO NO DATA IN",
                        (scalar @{$totalData{$idx}{$dataKey}{$type}}),
                        " ****\n";
                next;
            }


            # Print out the raw values
            print OUTPUT $prefix,"values = ",
                    join( " ", @{$totalData{$idx}{$dataKey}{$type}} ),
                    "\n";
            my $stats = Statistics::Descriptive::Full->new();
            $stats->add_data( @{$totalData{$idx}{$dataKey}{$type}} );


            if( $statsConfig{$dataKey}{'total'}{$type}{'count'} )
            {
                print OUTPUT $prefix,"count = ",
                        $stats->count(),
                        "\n";
            }
            if( $statsConfig{$dataKey}{'total'}{$type}{'min'} )
            {
                print OUTPUT $prefix,"min = ",
                        $stats->min(),
                        "\n";
            }
            if( $statsConfig{$dataKey}{'total'}{$type}{'max'} )
            {
                print OUTPUT $prefix,"max = ",
                        $stats->max(),
                        "\n";
            }
            if( $statsConfig{$dataKey}{'total'}{$type}{'mean'} )
            {
                print OUTPUT $prefix,"mean = ",
                        $stats->mean(),
                        "\n";
            }
            if( $statsConfig{$dataKey}{'total'}{$type}{'variance'} )
            {
                print OUTPUT $prefix,"variance = ",
                        $stats->variance(),
                        "\n";
            }
            if( $statsConfig{$dataKey}{'total'}{$type}{'stddev'} )
            {
                print OUTPUT $prefix,"stddev = ",
                        $stats->standard_deviation(),
                        "\n";
            }

        }
        print OUTPUT "\n";
    }
    print OUTPUT "\n";
}

close( OUTPUT );



# ===================================================================
sub readChangePointFile
{
    my ($filename ) = @_;

    # Open the file
    open( DATA, "$filename" ) or die "Unable to open changepoint file [$filename]: $!\n";

    # Read each line
    my %data;
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
            $data{"total-simulations"} = $value;
        }

        # Is it data about an individual?
        elsif( $key =~ /^(ind\d+)\.(\w+)/ )
        {
            # Yup
            my $id = $1;
            my $dataKey = $2;
            $data{"results"}{$id}{$dataKey} = $value;
        }
    }

    # Close the file
    close( DATA );

    return \%data;
}

# ===================================================================
sub processIndividual
{
    my ($indID, $runDataRef, $changepointDataRef,
        $envChangeSimsRef, $runDataFile) = @_;

    # Use the real data, not the references
    my @envChangeSims = @{$envChangeSimsRef};
#    print "====================================================================\n";
#    print Dumper($runDataRef);
#    print "---------------------------------------------------\n";

    # Get the changepoint data
    my $indUpdates = $changepointDataRef->{"results"}{$indID}{'updates'};
    my @indChangepoints = split( /\s+/,
            $changepointDataRef->{"results"}{$indID}{'changepoints'} );
    my @indPersonalities = split( /\s+/,
            $changepointDataRef->{"results"}{$indID}{'coefficients'} );

    # Check to see if the individual ever became bold
    foreach my $personality (@indPersonalities)
    {
        if( BOLDTHRESHOLD < $personality )
        {
            # Yup
            $runDataRef->{"bold-at-any-time"} += 1;
            last;
        }
    }

    # Add the starting simulation to the beginning of the change points so we can
    # have changepoints denote when the corresponding personality starts
#print "ChangePoints=[",join(' ',@indChangepoints),"]\n";
    if( $indChangepoints[0] > 0 )
    {
#print "\tInserting 0...\n";
        unshift( @indChangepoints, 0 );
    }

    # Iterate through all the environmental changes to see if the individual
    # was bold after the change
    for( my $changeIdx = 0; $changeIdx < $#envChangeSims; $changeIdx++ )
    {
        my $startSim = $envChangeSims[$changeIdx];
        my $endSim = $envChangeSims[$changeIdx+1];
#print "startSim=[$startSim]  endSim=[$endSim]\n";

        # Find the changepoints after the change but before the next change
        # and check the personality
        for( my $i = 0; $i < ($#indChangepoints + 1); $i++ )
        {
#print "\ti=[$i]\n";
            # Have we passed it?
            if( $endSim <= $indChangepoints[$i] )
            {
                # Yup, bail out
                last;
            }

            # Are we too early?
            if( ($startSim + SIMBUFFER) > $indChangepoints[$i] )
            {
                # Yup
                next;
            }

            # Is the individual bold?
            if( BOLDTHRESHOLD < $indPersonalities[$i] )
            {
#print "--------------------------------------------\n";
#print "indID=[$indID]  personality=[",$indPersonalities[$i],"] i=[$i]\n";
#my $early = ($startSim > (scalar $indChangepoints[$i])) ? 1 : 0;
#print "changeSims=[",join(",", @envChangeSims),"]  start=[$startSim]  end=[$endSim]  changepointSim=[",$indChangepoints[$i],"]  early?=[$early]\n";
                # Yup
                push( @{$runDataRef->{($changeIdx)}{'bold'}}, 1 );
                push( @{$runDataRef->{($changeIdx)}{'personality'}},  $indPersonalities[$i] );

                # Find out how many simulations and initiations it took for
                # the individual to become bold

                # In the rare situation where an individual becomes bold right on the boundary
                # (for example during the initial phase), manually bump it to the next changepoint
                my $initStatsChangePoint = $indChangepoints[$i];
                if( $startSim == $indChangepoints[$i] && ($i < $#indChangepoints) )
                {
                    $initStatsChangePoint = $indChangepoints[$i+1];
#print "increasing initStatsChangePoint=[$initStatsChangePoint]\n";
                }

                my $initStatsRef = calcInitiationStatsDuringPeriod( $startSim,
                        $endSim,
                        $indID,
                        $runDataFile );
#print Dumper($initStatsRef);
                # If we don't actually have a simulation where we went bold, 
                # the changepoint could be off a bit.  Go on to the next one.
                unless( $initStatsRef->{'sim'} =~ /\d+/ )
                {
                    next;
                }

                push( @{$runDataRef->{$changeIdx}{'inits-to-bold'}},
                    $initStatsRef->{'count'} );
                push( @{$runDataRef->{$changeIdx}{'inits-to-bold-successes'}},
                    $initStatsRef->{'successes'} );

                # Calculate the simulations until bold using the init results
                my $simsToBold = $initStatsRef->{'sim'} - $startSim;
                push( @{$runDataRef->{$changeIdx}{'sims-to-bold'}}, $simsToBold );




if( $initStatsRef->{'count'} > $simsToBold )
{
    # Something is fubar
    print "**** FUBAR INITS-TO-BOLD-DATA sims=[$simsToBold] inits=[",$initStatsRef->{'count'},"] start=[$startSim] bold-sim[",$initStatsRef->{'sim'},"] indID=[$indID] ****\n";
    print "****       file=[$runDataFile]\n";
}

                # If this isn't the first change, see if they were bold before
                # this one too
                if( $changeIdx > 1 )
                {
                    my $boldRepeatCount = 0;
                    for( my $j = 0; $j < $i; $j++ )
                    {
                        if( ($startSim > $indChangepoints[$i]) &&
                                (BOLDTHRESHOLD < $indPersonalities[$j]) )
                        {
                            # Yup, they were bold before
                            $boldRepeatCount++;
                        }
                    }
                    push( @{$runDataRef->{$changeIdx}{'bold-repeat'}}, $boldRepeatCount );
                }

                # Bail out, we got what we came for
                last;
            }
        }
    }
#    print Dumper($runDataRef);
#    print "---------------------------------------------------\n";
#    print "Done...\n";
}


# ===================================================================
sub calcInitiationStatsDuringPeriod
{
    my ($startSim, $endSim, $indID, $runDataFile) = @_;

#print "CALC: start=[$startSim]  end=[$endSim]\n";

    # Get the individual's initiation history
    my $initiationHistory = `grep -i $indID $runDataFile | grep 'initiation-history'`;
    $initiationHistory =~ s/^.*\=\s+\[(.*)\]\n?/\1/;
    my @initiations = split( /\]\s+\[/, $initiationHistory );

    # Iterate through every initiation and keep track of the number
    # during the desired period of time
    my %stats;
    foreach my $initiation (@initiations)
    {
        my ($sim, $initialPersonality, $success, $finalPersonality, $followers) = 
                split( /\s+/, $initiation );

#print "\tinitiation=[$initiation]\n";
        # Have we passed the end?
        if( $sim > $endSim )
        {
            # Yup, bail out
            last;
        }

        # Are we too early?
        if( $sim < $startSim )
        {
            # Yup
            next;
        }

        # Keep track of some stats
        $stats{'count'}++;
        $stats{'successes'} += ($success =~ /true/i) ? 1 : 0;

        # If we are actually bold, bail out
        if( BOLDTHRESHOLD < $finalPersonality )
        {
#print "sim=[$sim] inits=[",$stats{'count'},"]\n";
            # Save the simulation
            $stats{'sim'} = $sim;
            last;
        }

    }

    # Return a reference to the stats
    return \%stats;
}
