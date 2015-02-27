#!/usr/bin/perl
use strict;
use Data::Dumper;
use Statistics::Descriptive;

# -------------------------------------------------------------------
# Get the history file
my $historyFile = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the group size
my $groupSize = shift( @ARGV );


# -------------------------------------------------------------------
# Read the history file
my %data;
my %individuals;
my @allIndivduals;

# Open the history file
open( INPUT, "$historyFile" ) or die "Unable to open history file [$historyFile]: $!\n";

# Read each line
my $line = 0;
while( <INPUT> )
{
    $line++;

    # Remove any other comments or whitespace
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

    # Perform an initial pass
    /(\w)\s+(\d+)\s+(\d+)\s+\[(.*)/;
    my $result = $1;
    my $finalInitiators = $2;
    my $maxInitiators = $3;
    my $history = $4;

    # Only continue if the movement was successful with a single initiator
    unless( ($result =~ /S/) && (1 == $finalInitiators) )
    {
        next;
    }

    # Set up some variables
    my %leaders;

    # Get all the events from the history
    $history =~ s/\]//g;
    my (@historyEvents) = split( /\s*\[/, $history );

    # Iterate through the events in reverse order so we get the final 
    # hierarchy and ignore failed attempts
    my %processed;
    my $order = $groupSize - 1;
    foreach my $event (reverse @historyEvents)
    {
        # Pull out the details
        $event =~ /(\w+)\s+(\w+|\*+)\s+(\w)\s+(-?\d+\.\d+)/;
        my $departed = $1;
        my $leader = $2;
        my $eventType = $3;
        my $time = $4;

        # Has this individual already been processed?
        if( exists( $processed{$departed} ) )
        {
            # Yup, go on
            # Note that this happens because an individual could have been
            # an initiator or follower in a failed initiation
            next;
        }

        # Log them as an individual
        $individuals{$departed} = 1;

        # Mark this individual as processed
        $processed{$departed} = 1;

        # Store the link from departed to leader
        if( $leader =~ /\*/ )
        {
            # If the departed is initiating, note that in the leader
            $leader = "initiate";
        }
        else
        {
            $leaders{$departed} = $leader;
        }
        $data{"freq"}{$departed}{$leader}++;
        $data{"freq"}{$departed}{"total"}++;

        # Save the time
        my $orderStr = sprintf( "%03d", $order );
        push( @{$data{"time"}{$orderStr}}, $time );

        # Decrement the order of the event (as related to the final hierarchy)
        $order--;
    }

    # Calculate the total elapsed time for following events
    my $totalElapsedTime = 0;
    foreach my $order ( sort keys %{$data{"time"}} )
    {
        $totalElapsedTime += $data{"time"}{$order}[-1];
        push( @{$data{"elapsed-time"}{$order}}, $totalElapsedTime );
        print "Order=[$order]  Raw [",${$data{"time"}{$order}}[-1],"]  Elapsed=[$totalElapsedTime]\n";
    }
exit;

}

# Close the file
close( INPUT );



