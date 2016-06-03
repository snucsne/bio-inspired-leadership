#!/usr/bin/perl
use strict;
use Data::Dumper;
use Statistics::Descriptive;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

# Get the original input file name
my $originalFile = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the group size
my $groupSize = shift( @ARGV );

# Flag denoting full data should also be dumped
my $tempFlag = shift( @ARGV );
my $fullDataFlag = 0;
if( $tempFlag )
{
    $fullDataFlag = 1;
}

# -------------------------------------------------------------------
# Read the input file
my %data;
my %individuals;
my @allIndivduals;


# Open the input file
#print "Opening inputFile=[$inputFile]\n";
open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";

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

#print "LineNum=[$line]  line=[$_]\n";

    # Set up some variables
    my %leaders;

    # Save the initiator info
    push( @{$data{"final-initiators"}}, $finalInitiators );
    push( @{$data{"max-initiators"}}, $maxInitiators );

    # Get all the events from the history
    $history =~ s/\]//g;
    my (@historyEvents) = split( /\s*\[/, $history );

    # Iterate through the events in reverse order so we get the final 
    # hierarchy and ignore failed attempts
    my %processed;
    my $order = $groupSize - 1;
#    print "  Processing [",(scalar @historyEvents),"] events\n";
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
        if( $leader =~ /\*/ || $leader =~ /^$departed$/ )
        {
            # If the departed is initiating, note that in the leader
            $leader = "initiate";
        }
        else
        {
            $leaders{$departed} = $leader;
#print "  departed=[$departed] leader=[$leader]\n";
        }
        $data{"freq"}{$departed}{$leader}++;
        $data{"freq"}{$departed}{"total"}++;

        # Save the time
        my $orderStr = sprintf( "%03d", $order );
        push( @{$data{"time"}{$orderStr}}, $time );
#print "Added time: [$orderStr]=>[$time]\tevent=[$event]\n";

        # Decrement the order of the event (as related to the final hierarchy)
        $order--;
    }

    # Calculate the total elapsed time for following events
#    print "Calculating elapsed time...\n";
    my $totalElapsedTime = 0;
    foreach my $order ( sort keys %{$data{"time"}} )
    {
        $totalElapsedTime += $data{"time"}{$order}[-1];
        push( @{$data{"elapsed-time"}{$order}}, $totalElapsedTime );
#        print "Order=[$order]  Raw [",${$data{"time"}{$order}}[-1],"]  Elapsed=[$totalElapsedTime]\n";
#        print "\tTime array [",join(" ", @{$data{"time"}{$order}}),"]\n";
    }

#print Dumper (\%leaders);

    # Process the hierarchy
#    print "Processing hierarchy...\n";
    @allIndivduals = sort( keys %individuals );
    processHierarchy( \%data, \%leaders, \@allIndivduals );
#    print "\t\tDone\n";
}

# Close the file
close( INPUT );

print "  Read [$line] lines\n";

# -------------------------------------------------------------------
# Open the output file
open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

# Print out header information
print OUTPUT "# Input file: [$originalFile]\n";
my $date = localtime;
print OUTPUT "# Date:       $date\n";
print OUTPUT "# ===================================================\n\n";


# -------------------------------------------------------------------
# Iterate through all the individuals
foreach my $ind ( @allIndivduals )
{

    print OUTPUT "# ---------------------------------------------------\n";
    print OUTPUT "# Individual [$ind]\n";

    # Ensure we have a hash
    if( ref( $data{"freq"}{$ind} ) eq "HASH" )
    {
        # Iterate through all the leader individuals
        foreach my $leader( @allIndivduals )
        {

            # Does it have a count?
            if( exists( $data{"freq"}{$ind}{$leader} ) )
            {
                my $count = $data{"freq"}{$ind}{$leader};

                # Print out the frequency
#                printf( "%-10s  %-10s  %06.4f\n",
#                        $ind,
#                        $leader,
#                        ($count/($data{"freq"}{$ind}{"total"})) );
                printf(OUTPUT "frequency.%s.follow.%s = %06.4f\n",
                        $ind,
                        $leader,
                        ($count/($data{"freq"}{$ind}{"total"})) );
            }

        }

        # Print out the initiation
#        printf( "%-10s  %-10s  %06.4f\n\n",
#                $ind,
#                "initiate",
#                (($data{"freq"}{$ind}{"initiate"})/($data{"freq"}{$ind}{"total"})) );
        printf( OUTPUT "frequency.%s.initiate = %06.4f\n",
                $ind,
                ($data{"freq"}{$ind}{"initiate"})/($data{"freq"}{$ind}{"total"}) );
    }

    # Get stats on the depth and followers
    my $depthStats = Statistics::Descriptive::Full->new();
    $depthStats->add_data( @{$data{"depth"}{$ind}} );
    my $followerStats = Statistics::Descriptive::Full->new();
    $followerStats->add_data( @{$data{"followers"}{$ind}} );

    # Print out the depth and the follower counts
    if( $fullDataFlag )
    {
        print OUTPUT "depth.$ind.data = ",
            join( " ", @{$data{"depth"}{$ind}} ),
            "\n";
    }
    print OUTPUT "depth.$ind.mean = ",$depthStats->mean(),"\n";
    print OUTPUT "depth.$ind.std-dev = ",$depthStats->standard_deviation(),"\n";
    print OUTPUT "depth.$ind.max = ",$depthStats->max(),"\n";
    print OUTPUT "depth.$ind.min = ",$depthStats->min(),"\n";
    if( $fullDataFlag )
    {
        print OUTPUT "followers.$ind.data = ",
            join( " ", @{$data{"followers"}{$ind}} ),
            "\n";
    }
    print OUTPUT "followers.$ind.mean = ",$followerStats->mean(),"\n";
    print OUTPUT "followers.$ind.std-dev = ",$followerStats->standard_deviation(),"\n";
    print OUTPUT "followers.$ind.max = ",$followerStats->max(),"\n";
    print OUTPUT "followers.$ind.min = ",$followerStats->min(),"\n";
    print OUTPUT"\n";
}

# -------------------------------------------------------------------
# Print out the branching factors
print OUTPUT "# ===================================================\n";
foreach my $depth ( sort( keys %{$data{"branching"}} ) )
{
    my $stats = Statistics::Descriptive::Full->new();
    $stats->add_data( @{$data{"branching"}{$depth}} );

    my $prefix = "branching.".sprintf( "%02d", $depth );

    if( $fullDataFlag )
    {
        print OUTPUT $prefix,".data = ",
            join( " ", @{$data{"branching"}{$depth}} ),
            "\n";
    }
    print OUTPUT $prefix,".count = ",
        $stats->count(),
        "\n";
    print OUTPUT $prefix,".mean = ",
        $stats->mean(),
        "\n";
    print OUTPUT $prefix,".std-dev = ",
        $stats->standard_deviation(),
        "\n";
    print OUTPUT $prefix,".max = ",
        $stats->max(),
        "\n";
    print OUTPUT $prefix,".min = ",
        $stats->min(),
        "\n";
    print OUTPUT "\n";
}


# -------------------------------------------------------------------
# Print out the times
print OUTPUT "# ===================================================\n";
foreach my $order ( sort( keys %{$data{"time"}} ) )
{
    my $stats = Statistics::Descriptive::Full->new();
    $stats->add_data( @{$data{"time"}{$order}} );

    my $prefix = "time.$order";

    if( 0 == $order )
    {
        print OUTPUT "# Time for initiator\n";
    }
    else
    {
        print OUTPUT "# Time for follower ",sprintf( "%3d", $order),"\n";
    }

    if( $fullDataFlag )
    {
        print OUTPUT $prefix,".data = ",
            join( " ", @{$data{"time"}{$order}} ),
            "\n";
    }
    print OUTPUT $prefix,".count = ",
        $stats->count(),
        "\n";
    print OUTPUT $prefix,".mean = ",
        $stats->mean(),
        "\n";
    print OUTPUT $prefix,".std-dev = ",
        $stats->standard_deviation(),
        "\n";
    print OUTPUT $prefix,".max = ",
        $stats->max(),
        "\n";
    print OUTPUT $prefix,".min = ",
        $stats->min(),
        "\n";
    print OUTPUT "\n";
}

# -------------------------------------------------------------------
# Print out the elapsed times
print OUTPUT "# ===================================================\n";
foreach my $order ( sort( keys %{$data{"elapsed-time"}} ) )
{
    my $stats = Statistics::Descriptive::Full->new();
    $stats->add_data( @{$data{"elapsed-time"}{$order}} );

    my $prefix = "elapsed-time.$order";

    if( 0 == $order )
    {
        print OUTPUT "# Elapsed time for initiator\n";
    }
    else
    {
        print OUTPUT "# Elapsed time for follower ",sprintf( "%3d", $order),"\n";
    }

    if( $fullDataFlag )
    {
        print OUTPUT $prefix,".data = ",
            join( " ", @{$data{"elapsed-time"}{$order}} ),
            "\n";
    }
    print OUTPUT $prefix,".count = ",
        $stats->count(),
        "\n";
    print OUTPUT $prefix,".mean = ",
        $stats->mean(),
        "\n";
    print OUTPUT $prefix,".std-dev = ",
        $stats->standard_deviation(),
        "\n";
    print OUTPUT $prefix,".max = ",
        $stats->max(),
        "\n";
    print OUTPUT $prefix,".min = ",
        $stats->min(),
        "\n";
    print OUTPUT "\n";
}

# -------------------------------------------------------------------
# Calculate the max depth stats
my $maxDepthStats = Statistics::Descriptive::Full->new();
$maxDepthStats->add_data( @{$data{"maxdepth"}} );

# Print out the max depth information
print OUTPUT "# ===================================================\n";
print OUTPUT "# Max depth data\n";
if( $fullDataFlag )
{
    print OUTPUT "max-depth.data = ",
    join( " ", @{$data{"maxdepth"}} ),
        "\n";
}
print OUTPUT "max-depth.mean = ",$maxDepthStats->mean(),"\n";
print OUTPUT "max-depth.std-dev = ",$maxDepthStats->standard_deviation(),"\n";
print OUTPUT "max-depth.max = ",$maxDepthStats->max(),"\n";
print OUTPUT "max-depth.min = ",$maxDepthStats->min(),"\n";


# -------------------------------------------------------------------
# Close the output file
close( OUTPUT );

# -------------------------------------------------------------------
sub processHierarchy
{
    my ($dataRef, $leadersRef, $indRef) = @_;

    # Dereference the references
    my %leaders = %$leadersRef;
    my @individuals = @$indRef;

#print Dumper (\%leaders);

    # Process each individual
    my %followers;
    my %depth;
    my $maxDepth = 0;
    foreach my $ind (@individuals)
    {
#print "Processing [$ind]\n";
        # Put in some default values if needed
        unless( defined( $followers{$ind} ) )
        {
            $followers{$ind} = 0;
        }
        unless( defined( $depth{$ind} ) )
        {
            $depth{$ind} = 0;
        }


        # Add to the leader's follower count
        my $leader = $leaders{$ind};
        $followers{$leader}++;
#        print "\tleader=[$leader]\n";

        # Found out how deep the individual is
        while( $leader )
        {
            $leader = $leaders{$leader};
            $depth{$ind}++;
#            print "\tdepth=[",$depth{$ind},"]\n";
        }

        if( $depth{$ind} > $maxDepth )
        {
            $maxDepth = $depth{$ind};
        }
    }

    # Now that everything is calculated, store it in the data
    push( @{$dataRef->{"maxdepth"}}, $maxDepth );
    foreach my $ind (@individuals)
    {
        push( @{$dataRef->{"depth"}{$ind}}, $depth{$ind} );
        push( @{$dataRef->{"followers"}{$ind}}, $followers{$ind} );

        # Add the branching factor for the current depth
        my $formattedDepth = sprintf( "%02d", $depth{$ind} );
        push( @{$dataRef->{"branching"}{$formattedDepth}}, $followers{$ind} );

if( (0 == $depth{$ind}) && (0 == $followers{$ind}) )
{
    print "FAILURE: Ind=[$ind]\n";
print "Depth:\n";
print map { "$_ => $depth{$_}\n" } sort (keys %depth);
print "Followers:\n";
print map { "$_ => $followers{$_}\n" } sort (keys %followers);
#    print Dumper %depth;
#    print Dumper %followers;
exit;
}

#        print "Ind: id=[$ind]  depth=[",
#                $depth{$ind},
#                "]  followers=[",
#                $followers{$ind},
#                "]\n";

    }

}
# -------------------------------------------------------------------

