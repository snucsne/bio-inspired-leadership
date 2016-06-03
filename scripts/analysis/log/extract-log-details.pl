#!/usr/bin/perl
use strict;
use Statistics::Descriptive;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

# Get the result
my $simResult = shift( @ARGV );

# Get the sim threshold value
my $simThreshold = shift( @ARGV );

# -------------------------------------------------------------------
# Open the input file
open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";

# Read each line
my $line = 0;
my $sims = 0;
while( <INPUT> )
{
    $line++;

    # Remove any other comments or whitespace
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

    # It is a simulation line
    $sims++;

    # Do we process it?
    next unless ($sims >= $simThreshold);

    # Perform an initial pass
    /(\w)\s+(\d+)\s+(\d+)\s+\[(.*)/;
    my $result = $1;
    my $finalInitiators = $2;
    my $maxInitiators = $3;
    my $history = $4;

    # Only continue if the movement matches our type with a single initiator
    unless( ($result =~ /$simResult/)
            && ((1 == $finalInitiators) || (0 == $finalInitiators)) )
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
    my @reversePersonalityOrder;
    my @reverseIndividualOrder;
    my @reverseTimeOrder;
    my @reverseDepartedCount;
    my @reversePotentialFollowerCount;
    foreach my $event (reverse @historyEvents)
    {
        # Pull out the details
        $event =~ /(\w+)\s+(\w+|\*+)\s+(\w)\s+(-?\d+\.\d+)\s+(\d+\.\d+)\s+(\d+)\s+(\d+)/;
        my $departed = $1;
        my $leader = $2;
        my $eventType = $3;
        my $time = $4;
        my $personality = $5;
        my $departedCount = $6;
        my $potentialFollowerCount = $7;

        # Has this individual already been processed?
        if( exists( $processed{$departed} ) )
        {
            # Yup, go on
            # Note that this happens because an individual could have been
            # an initiator or follower in a failed initiation
            next;
        }

        # Mark this individual as processed
        $processed{$departed} = 1;

        # Add the personality
        push( @reversePersonalityOrder, $personality );
        push( @reverseIndividualOrder, $departed );
        push( @reverseTimeOrder, $time );
        push( @reverseDepartedCount, $departedCount );
        push( @reversePotentialFollowerCount, $potentialFollowerCount );
    }

    my @timeOrder = reverse(@reverseTimeOrder);
    my @elapsedTimes;
    my $totalTime = 0;
    foreach my $time (@timeOrder)
    {
        $totalTime += $time;
        push( @elapsedTimes, $totalTime );
    }

    print "sim-",sprintf("%06d",$sims),".individuals = ",
            join( " ", reverse(@reverseIndividualOrder) ),"\n";
#    print "sim-",sprintf("%06d",$sims),".personalities = ",
#            join( " ", reverse(@reversePersonalityOrder) ),"\n";
    print "sim-",sprintf("%06d",$sims),".times = ",
            join( " ", reverse(@reverseTimeOrder) ),"\n";
    print "sim-",sprintf("%06d",$sims),".elapsed-times = ",
            join( " ", @elapsedTimes ),"\n";
#    print "sim-",sprintf("%06d",$sims),".departed-count = ",
#            join( " ", reverse(@reverseDepartedCount) ),"\n";
    print "sim-",sprintf("%06d",$sims),".potential-follower-count = ",
            join( " ", reverse(@reversePotentialFollowerCount) ),"\n";
    print "\n";
}

# Close the file
close( INPUT );

