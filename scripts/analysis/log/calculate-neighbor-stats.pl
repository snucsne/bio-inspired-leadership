#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the (processed) evaluation log file
my $procLogFile = shift( @ARGV );

# Get the evaluation data file
my $dataFile = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# -------------------------------------------------------------------
# Get the nearest neighbor data
my %neighbors;
loadNeighborData( $dataFile, \%neighbors );

#my @individuals = sort keys( %neighbors );

# -------------------------------------------------------------------
# Gather the neighbor following counts
my %results;
analyzeLogData( $procLogFile, \%neighbors, \%results );


# -------------------------------------------------------------------
# Send the results to a file
open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

my $timeStr = localtime;
print OUTPUT "# Date: ",$timeStr,"\n";
print OUTPUT "# Processed log file: $procLogFile\n";
print OUTPUT "# Data file: $dataFile\n";
print OUTPUT "# =====================================================\n\n";

# Print the simulation count
my $simCount = $results{"sim-count"};
print OUTPUT "sim-count = $simCount\n\n";

# Print the neighbor information
foreach my $followerIdx (sort {$a <=> $b} keys( %{$results{"neighbor-count"}} ) )
{
    my $prefix = "follower-".sprintf( "%03d", $followerIdx );
    print OUTPUT "$prefix.neighbor-count = ",$results{"neighbor-count"}{$followerIdx},"\n";
    print OUTPUT "$prefix.neighbor-probability = ",($results{"neighbor-count"}{$followerIdx} / $simCount),"\n";
    print OUTPUT "$prefix.following-neighbor-percentage = ",($results{"following-neighbor-percentage"}{$followerIdx}/$simCount),"\n";
    print OUTPUT "\n";
}


# ===================================================================
sub loadNeighborData
{
    my ($dataFile, $neighborsRef) = @_;

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

        if( $key =~ /^individual.(Ind\d+)\.nearest-neighbors/ )
        {
            foreach my $neighbor (split( /\s+/, $value ))
            {
                $neighborsRef->{$1}{$neighbor} = 1;
                $neighborsRef->{"count"}{$1} += 1;
            }
#            $neighborsRef->{$1} = [ split( /\s+/, $value ) ];
        }
    }
}


# ===================================================================
# Analyze the log data
sub analyzeLogData
{
    my ($procLogFile, $neighborRef, $dataRef) = @_;

    # Count the number of simulations analyzed
    my $simCount = 0;

    # Open the file
    open( DATA, "$procLogFile" ) or die "Unable to open processed log file [$procLogFile]: $!\n";

    # Read each line
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

        if( $key =~ /individuals/ )
        {
            # Get the list of individual departures
            my @departures = split( /\s+/, $value );

            # The first is the initiator
            my $initiator = shift( @departures );

            # Process all the followers
            my $followerIdx = 0;
            my $neighborFollowerCount = 0;
            foreach my $follower (@departures)
            {
                # If the follower is a nearest neighbor,
                if( $neighborRef->{$initiator}{$follower} )
                {
                    # Note that the follower is a neighbor
                    $dataRef->{"neighbor-count"}{$followerIdx}++;

                    # Increment our neighbor follower count
                    $neighborFollowerCount++;
                }

                # Save the current number of following neighbors
                $dataRef->{"following-neighbor-percentage"}{$followerIdx} +=
                        ($neighborFollowerCount/$neighborRef->{"count"}{$initiator});

                # Increment our index
                $followerIdx++;
            }

            $simCount++;
        }        
    }

    $dataRef->{"sim-count"} = $simCount;
}
