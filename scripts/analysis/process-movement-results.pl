#!/usr/bin/perl
use strict;
use Data::Dumper;
use List::Util qw(sum);
#use Time::localtime;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

# Ensure that it ends with a "/"
unless( $dataDir =~ m/\/$/ )
{
    $dataDir .= "/";
}

# Get the output file
my $outputFile = shift( @ARGV );

# -------------------------------------------------------------------
# Store some configuration data
my %data;
$data{"config"}{"dataDir"} = $dataDir;
$data{"config"}{"time"} = scalar(localtime());


# -------------------------------------------------------------------
# Get the results files
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @resultsFiles = grep { /^conflict-spatial-hidden-var-\d+-seed-\d+\.dat$/ } readdir( DIR );
@resultsFiles = sort( @resultsFiles );
closedir( DIR );

#print "Processing [",($#resultsFiles + 1),"] result files...\n";

# -------------------------------------------------------------------
# Load the results from each file
foreach my $resultsFile (@resultsFiles)
{
#    print "\tLoading [$dataDir$resultsFile]...\n";
    loadResults( $dataDir.$resultsFile, \%data );
#    last;
}


# -------------------------------------------------------------------
# Process the destination reached times
processDestinationReachedTimes( \%data );

# -------------------------------------------------------------------
# Process the groups
processGroups( \%data );

# -------------------------------------------------------------------
# Dump out aggregate data
dumpAggregatedData( $outputFile, $dataDir, \%data );


# ===================================================================
sub loadResults
{
    my ($resultsFile, $dataRef ) = @_;

    # Find out the file run id
    $resultsFile =~ /conflict-spatial-hidden-var-(\d+)-seed/;
    my $fileRunID = $1;

    # Grab the number of simulations per file
    my $simsPerFile = `grep 'simulation-count' $resultsFile`;
    $simsPerFile =~ s/^.*\s+=\s+(\d+).*\R/\1/;
    my $fileOffset = ($fileRunID - 1) * $simsPerFile;

    # Grab the time limit
    my $timeLimit = `grep 'time-limit' $resultsFile`;
    $timeLimit =~ s/^.*\s+=\s+(\d+).*\R/\1/;

    # Grab the number of individuals
    my $indCount = `grep 'individual-count' $resultsFile`;
    $indCount =~ s/^.*\s+=\s+(\d+).*\R/\1/;

    # Open the file
    open( INPUT, $resultsFile ) or die "Unable to open results file [$resultsFile]: $!\n";

    # Read the file
    my %currentRunIDs;
    while( <INPUT> )
    {
        # Remove any comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Split the line up into a key and a value
        my ($simID,
                $time,
                $indID,
                $dest,
                $decisionType,
                $leader,
                $decisionData) = split( /\s+/, $_ );

        # Calculate the runID to be unique across files
        my $runID = "run-".sprintf( "%04d", ($simID + $fileOffset) );
        $dataRef->{"runs"}{$runID} = 1;
        $currentRunIDs{$runID} = 1;

        # Fix the leader if it is empty
        $leader =~ s/^-$//;

        # Clean up the time
        $time =~ s/^0+(\d+)/\1/;

        # Store the destination
        unless( exists $dataRef->{"dest"}{$runID}{$indID} )
        {
            $dataRef->{"dest"}{$runID}{$indID} = $dest;
        }
        if( exists $dataRef->{"dest"}{$indID} && $dataRef->{"dest"}{$indID} ne $dest )
        {
            print "**** Different Destination before[",
                    $dataRef->{"dest"}{$indID},
                    "]  now[$dest]\n";
        }
        $dataRef->{"dest"}{$indID} = $dest;
       
        # Did the agent reach the destination?
        if( $decisionType =~ /REACHED/i )
        {
            # Yup
            $dataRef->{"reached-raw"}{$runID}{$indID} = { "time" => $time, "dest" => $dest };
            $dataRef->{"reached"}{$runID}{$indID} = { "time" => $time, "dest" => $dest };
            $dataRef->{"reached-count"}{$runID}{$dest} += 1;
        }

        # Store the decision in the individual's history
        push( @{$dataRef->{"ind-history"}{$runID}{$indID}},
                { "type" => $decisionType, "time" => $time, "leader" => $leader } );

        # Store the full history
        push( @{$dataRef->{"full-history"}{$runID}},
                { "type" => $decisionType, "time" => $time, "ind" => $indID, "leader" => $leader } );

        # Store the time of the last event
        $dataRef->{"last-event-time"}{$runID} = $time;

    }

    close( INPUT );

    # Truncate the data
    foreach my $runID (sort( keys %currentRunIDs ) )
    {
        # Yup, find the individuals that didn't reach their destination
        foreach my $indID (sort( keys %{$dataRef->{"dest"}{$runID}} ) )
        {
            # Did they reach their destination?
            unless( exists( $dataRef->{"reached"}{$runID}{$indID} ) )
            {
                    # Use the time limit
                    my $dest = $dataRef->{"dest"}{$runID}{$indID};
                    $dataRef->{"reached"}{$runID}{$indID} = { "time" => $timeLimit, "dest" => $dest };
            }
        }
    }
}


# ===================================================================
# Process the groups
sub processGroups
{
    my ($dataRef) = @_;

    # Iterate through each run
    foreach my $runID ( sort( keys %{$dataRef->{"full-history"}} ) )
    {
        my @history = @{$dataRef->{"full-history"}{$runID}};

        # Store all the group data
        my %groupData;
        my %destinationData;
        my %currentGroupIDs;
        my %previousGroupIDs;
#        my %groupSizes;
        my @goalDestHistory;

        foreach my $eventRef (@history)
        {
            # Is it initiation?
            if( $eventRef->{"type"} =~ /INITIATION/i )
            {
                # See if they are currently with another group
                my $oldGroupID;
                if( $currentGroupIDs{ $eventRef->{"ind"} } )
                {
                    # Yup, reduce that group's size
                    $oldGroupID = $currentGroupIDs{ $eventRef->{"ind"} };
                    alterGroupSize( $eventRef, $oldGroupID, \%groupData, -1 );

                    # Save it
                    $previousGroupIDs{ $eventRef->{"ind"} } = $oldGroupID;
                }

                # Create a new group ID
                my $newGroupID = $eventRef->{"ind"}."-".$eventRef->{"time"};
                $currentGroupIDs{ $eventRef->{"ind"} } = $newGroupID;
                $groupData{$newGroupID}{"initiator"} = $eventRef->{"ind"};
                $groupData{$newGroupID}{"created"} = $eventRef->{"time"};
                $groupData{$newGroupID}{"dest"} = $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}};

                # Add the group with a size of one
                alterGroupSize( $eventRef, $newGroupID, \%groupData, 1 );

                # Set the individual's current destination
                push( @{$destinationData{$eventRef->{"ind"}}},
                        { "time" => $eventRef->{"time"},
                          "dest" => $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}} } );
                
                # Log that the individual is moving towards their current goal
                # (if they weren't before)
                my $result = 0;
                if( length($oldGroupID) == 0
                        || ($groupData{$oldGroupID}{"dest"} ne $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}}) )
                {
                    $result = 1;
                }
                push( @goalDestHistory,
                        { "time" => $eventRef->{"time"},
                          "result" => $result,
                          "ind" => $eventRef->{"ind"},
                          "type" => $eventRef->{"type"},
                          "group-dest" => $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}},
                          "old-group-dest" => $groupData{$oldGroupID}{"dest"},
                          "preferred-dest" => $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}} } );
                
            }
            elsif( $eventRef->{"type"} =~ /CANCEL/i )
            {
                # The leader left his own group
                my $groupID = $currentGroupIDs{ $eventRef->{"ind"} };
                delete( $currentGroupIDs{ $eventRef->{"ind"} } );

                # Save it
                $previousGroupIDs{ $eventRef->{"ind"} } = $groupID;

                # Reduce the groups size by 1
                alterGroupSize( $eventRef, $groupID, \%groupData, -1 );

                # Set the individual's current destination
                push( @{$destinationData{$eventRef->{"ind"}}},
                        { "time" => $eventRef->{"time"},
                          "dest" => "NONE" } );

                # The individual isn't moving towards their preferred
                # destination anymore
                push( @goalDestHistory,
                        { "time" => $eventRef->{"time"},
                          "result" => -1,
                          "ind" => $eventRef->{"ind"},
                          "type" => $eventRef->{"type"},
                          "group-dest" => "NONE",
                          "old-group-dest" => $groupData{$groupID}{"dest"},
                          "preferred-dest" => $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}} } );


            }
            elsif( $eventRef->{"type"} =~ /FOLLOW/i )
            {
                # See if they are currently with another group
                my $oldGroupID;
                if( $currentGroupIDs{ $eventRef->{"ind"} } )
                {
                    # Yup, reduce that group's size
                    $oldGroupID = $currentGroupIDs{ $eventRef->{"ind"} };
                    alterGroupSize( $eventRef, $oldGroupID, \%groupData, -1 );

                    # Save it
                    $previousGroupIDs{ $eventRef->{"ind"} } = $oldGroupID;
                }

                # Add them to the new leader's group
                my $newGroupID = $currentGroupIDs{ $eventRef->{"leader"} };
                if( $newGroupID =~ /^$/ )
                {
                    # Oops.  The initiator must have just canceled.
                    # Just use the previous group iD
                    $newGroupID = $previousGroupIDs{ $eventRef->{"leader"} };
                }

                $currentGroupIDs{ $eventRef->{"ind"} } = $newGroupID;
                alterGroupSize( $eventRef, $newGroupID, \%groupData, 1 );

                # Set the individual's current destination
                push( @{$destinationData{$eventRef->{"ind"}}},
                        { "time" => $eventRef->{"time"},
                          "dest" => $groupData{$newGroupID}{"dest"} } );

                # Is the individual now moving towards their preferred destination?
                my $result = 0;
                if( $groupData{$newGroupID}{"dest"} eq $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}} )
                {
                    # Yup, is this new?
                    if( length($oldGroupID) == 0
                            || ($groupData{$oldGroupID}{"dest"} ne $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}}) )
                    {
                        # Yup
                        $result = 1;
                    }
                }
                else
                {
                    # Nope, were they before?
                    if( length($oldGroupID) > 0
                            && ($groupData{$oldGroupID}{"dest"} eq $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}}) )
                    {
                        $result = -1;
                    }
                }

                push( @goalDestHistory,
                        { "time" => $eventRef->{"time"},
                          "result" => $result,
                          "ind" => $eventRef->{"ind"},
                          "type" => $eventRef->{"type"},
                          "group-dest" => $groupData{$newGroupID}{"dest"},
                          "old-group-dest" => $groupData{$oldGroupID}{"dest"},
                          "preferred-dest" => $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}} } );
            }
            elsif( $eventRef->{"type"} =~ /REACHED/i )
            {
                push( @goalDestHistory,
                        { "time" => $eventRef->{"time"},
                          "result" => 0,
                          "ind" => $eventRef->{"ind"},
                          "type" => $eventRef->{"type"},
                          "group-dest" => $groupData{$currentGroupIDs{ $eventRef->{"ind"} }}{"dest"},
                          "old-group-dest" => "NONE",
                          "preferred-dest" => $dataRef->{"dest"}{$runID}{$eventRef->{"ind"}} } );
            }
        }

        # Iterate through each timestep and see how many individuals are going towards their preferred direction
        my @happyIndCount;
        my $currentHappyIndCount = 0;
        my $nextGoalDestHistoryIDX = 0;
        for( my $i = 0; $i <= 20000; $i++ )
        {
            # Is the current time before the next event?
            while( ($nextGoalDestHistoryIDX <= $#goalDestHistory)
                    && ($i >= ${$goalDestHistory[$nextGoalDestHistoryIDX]}{"time"}) )
            {
                # Nope, adjust the value
                $currentHappyIndCount += ${$goalDestHistory[$nextGoalDestHistoryIDX]}{"result"};

                # Increment the index
                $nextGoalDestHistoryIDX++;
            }

            # Save the value
            push( @happyIndCount, $currentHappyIndCount );
            push( @{${$dataRef->{"happy-ind-count"}}[$i]}, $currentHappyIndCount );
        }

        # Save the number of happy individuals at the end of the simulation
        push( @{$dataRef->{"final-happy-ind-count"}}, $happyIndCount[-1] );
    }
}

# ===================================================================
# Alters a group size
sub alterGroupSize
{
    my ($eventRef, $groupID, $groupDataRef, $change) = @_;

    # Get the group
    my $currentGroupDataRef = ${$groupDataRef->{$groupID}{"size-history"}}[-1];

    # Get the current size
    my $currentSize = $currentGroupDataRef->{"size"};

    # Change it
    push( @{$groupDataRef->{$groupID}{"size-history"}},
            { "time" => $eventRef->{"time"},
              "size" => ($currentSize + $change),
              "ind"  => $eventRef->{"ind"},
              "reason" => $eventRef->{"type"} } );
}

# ===================================================================
# Organize the times required to reach the destination by the mean
# time required
sub processDestinationReachedTimes
{
    my ($dataRef) = @_;

    foreach my $runID ( sort( keys %{$dataRef->{"reached"}} ) )
    {
        my %timeToDestByDest;
        my %timeToDestByDestRaw;
        foreach my $indID ( sort( keys %{$dataRef->{"reached"}{$runID}} ) )
        {
            my $time = $dataRef->{"reached"}{$runID}{$indID}{"time"};
            my $destID = $dataRef->{"reached"}{$runID}{$indID}{"dest"};
            push( @{$dataRef->{"all-time-to-dest"}}, $time );
            push( @{$timeToDestByDest{$destID}}, $time );

            if( exists( $dataRef->{"reached-raw"}{$runID}{$indID} ) )
            {
                $time = $dataRef->{"reached-raw"}{$runID}{$indID}{"time"};
                $destID = $dataRef->{"reached-raw"}{$runID}{$indID}{"dest"};
                push( @{$dataRef->{"all-time-to-dest-raw"}}, $time );
                push( @{$timeToDestByDestRaw{$destID}}, $time );
            }
        }

        my @sortedDestIDs = sort
                {mean(@{$timeToDestByDest{$a}}) <=> mean(@{$timeToDestByDest{$b}})}
                keys %timeToDestByDest;
        $dataRef->{"sorted-dest-ids"}{$runID} = \@sortedDestIDs;

        for( my $i = 0; $i <= $#sortedDestIDs; $i++ )
        {
            push( @{$dataRef->{"time-to-dest-by-time"}{$i}}, @{$timeToDestByDest{$sortedDestIDs[$i]}} );
            $dataRef->{"reached-count-by-dest-rank"}{$i} += $dataRef->{"reached-count"}{$runID}{$sortedDestIDs[$i]};

            if( exists( $timeToDestByDestRaw{$sortedDestIDs[$i]} ) )
            {
                push( @{$dataRef->{"time-to-dest-by-time-raw"}{$i}}, @{$timeToDestByDestRaw{$sortedDestIDs[$i]}} );
            }
        }
    }
}


# ===================================================================
sub uniq { my %seen; grep !$seen{$_}++, @_ }


# ===================================================================
# Dump the aggregated data
sub dumpAggregatedData
{
    my ($outputFile, $dataDir, $dataRef) = @_;

    my $fullStats = 0;

    # Open the output file
    open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

    # Dump out configuration information
    foreach my $configKey ( sort( keys %{$dataRef->{"config"}} ) )
    {
        print OUTPUT "# $configKey = ",$dataRef->{"config"}{$configKey},"\n";
    }
    print OUTPUT "# ====================================================================\n\n";

    # Dump out the times to reach a destination
    my @timeToDest;
    my %timeToDestByDest;
    my %timeToDestByTime;
    foreach my $runID ( sort( keys %{$dataRef->{"reached"}} ) )
    {
#        my %timeToDestByDest;
#        foreach my $indID ( sort( keys %{$dataRef->{"reached"}{$runID}} ) )
#        {
#            my $time = $dataRef->{"reached"}{$runID}{$indID}{"time"};
#            my $dest = $dataRef->{"reached"}{$runID}{$indID}{"dest"};
#            push( @timeToDest, $time );
#            push( @{$timeToDestByDest{$dest}}, $time );
#        }

        # Dump out the times for this run by destination, but sort it
        # by the time to the destination
#        my @sortedDestIDs = sort
#                {mean(@{$timeToDestByDest{$a}}) <=> mean(@{$timeToDestByDest{$b}})}
#                keys %timeToDestByDest;
        for( my $i = 0; $i <= $#{$dataRef->{"sorted-dest-ids"}{$runID}}; $i++ )
        {
            my $destID = ${$dataRef->{"sorted-dest-ids"}{$runID}}[$i];

            # Print it out
            if( $fullStats )
            {
                print OUTPUT "# Time to destination: run=[$runID] destID=[$destID]\n";
                print OUTPUT "time-to-destination.rank-",
                        sprintf("%02d", $i),
                        ".dest-$destID = ",
                        join( " ", @{$dataRef->{"time-to-dest-by-time"}{$runID}{$destID}} ),
                        "\n";
            }
        }
    }

    print OUTPUT "# Time to destination ordered by mean time to destination\n";
    foreach my $destRank ( sort( keys %{$dataRef->{"time-to-dest-by-time"}} ) )
    {
        print OUTPUT "time-to-destination.rank-",
                sprintf("%02d", $destRank),
                " = ",
                join( " ", sort( {$a <=> $b} @{$dataRef->{"time-to-dest-by-time"}{$destRank}} ) ),
                "\n";
    }

    print OUTPUT "\n# Time to destination ordered by mean time to destination (not truncated)\n";
    foreach my $destRank ( sort( keys %{$dataRef->{"time-to-dest-by-time"}} ) )
    {
        print OUTPUT "raw-time-to-destination.rank-",
                sprintf("%02d", $destRank),
                " = ",
                join( " ", sort( {$a <=> $b} @{$dataRef->{"time-to-dest-by-time-raw"}{$destRank}} ) ),
                "\n";
    }


    print OUTPUT "\n# Number that reached destination ordered by mean time to destination\n";
    foreach my $destRank ( sort( keys %{$dataRef->{"reached-count-by-dest-rank"}} ) )
    {
        print OUTPUT "reached-count.rank-",
                sprintf("%02d", $destRank),
                " = ",
                $dataRef->{"reached-count-by-dest-rank"}{$destRank},
                "\n";
    }


    # Dump the full times
    print OUTPUT "\n# Time to destination (ALL=[",($#{$dataRef->{"all-time-to-dest"}}+1),"])\n";
    print OUTPUT "time-to-destination.all = ",join( " ", @{$dataRef->{"all-time-to-dest"}} ),"\n\n";

    # Dump the number of individuals moving towards their goal at each timestep
    print OUTPUT "# ====================================================================\n";
    print OUTPUT "# Number of individuals moving towards their preferred destination\n";
    print OUTPUT "#    or already arrived at their preferred destination at the end\n";
    print OUTPUT "final-happy-inds = ",join( " ", @{$dataRef->{"final-happy-ind-count"}} ),"\n\n";

    print OUTPUT "# Number of individuals moving towards their preferred destination\n";
    for( my $i = 0; $i <= 20000; $i++ )
    {
        print OUTPUT "happy-inds.",sprintf("%05d",$i)," = ",
                join( " ", @{${$dataRef->{"happy-ind-count"}}[$i]} ),"\n";
    }


    # Close the output
    close( OUTPUT );
}

# ===================================================================
# Find the mean value of an array
sub mean
{
    return sum(@_)/@_;
}

