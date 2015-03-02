#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );


# -------------------------------------------------------------------
# Read the input file
my %data;

# Open the file
open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";

# Read each line
while( <INPUT> )
{
    # Remove any other comments or whitespace
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

    my ($key,$value) = split( /\s+=\s+/, $_ );

    if( ($key =~ /^initiation\./)
        || ($key =~ /^individual\./) )
    {
        my ($trash, $id, $dataKey) = split( /\./, $key );

        if( $dataKey =~ /location/ )
        {
            my ($x, $y) = split( /\ /, $value );
            $data{$id}{$dataKey}{"x"} = $x;
            $data{$id}{$dataKey}{"y"} = $y;

            # While we are here, save the numerical index
            $id =~ /Ind[0]+(\d+)/;
            $data{$id}{"index"} = $1;
        }
        else
        {
            $data{$id}{$dataKey} = $value;
        }
    }
}

close( INPUT );

# -------------------------------------------------------------------
# Determine the distance between nodes
my %distancesToNode;
foreach my $ind ( sort( keys ( %data ) ) )
{
    # Get the distances to this node
    my @distances = split( /\s+/, $data{$ind}{"shortest-paths"} );
    for( my $i = 0; $i < (scalar @distances); $i++ )
    {
        my $fromIndID = "Ind".sprintf( "%05d", $i );
        $distancesToNode{$ind}{$fromIndID} = $distances[$i];
        print "From=[$fromIndID] To=[$ind] Distance=[",$distances[$i],"]\n";
    }
}
exit;


# -------------------------------------------------------------------
# Sort the individuals by closeness
my @indsByCloseness = sort { $data{$a}{"closeness"} <=> $data{$b}{"closeness"} } keys( %data );
foreach my $key (@indsByCloseness)
{
    print "Ind[$key]\tCloseness=[",$data{$key}{"closeness"},"]\n";
}

# Find the farthest periphery individuals from the most periphery individual
my $mostPeripheryInd = shift( @indsByCloseness );
print "Periphery=[$mostPeripheryInd]\n";
my @distances = split( /\s+/, $data{$mostPeripheryInd}{"shortest-paths"} );
my %distanceFactor;
foreach my $potential (@indsByCloseness)
{
    $potential =~ /0+(\d+)/;
    my $index = $1;
    $distanceFactor{$potential} = $distances[$index] / $data{$potential}{"normalized-closeness"};
#    print "Ind=[$potential]\tindex[$index]\tdistance[",$distances[$index],"]\tcloseness[",$data{$potential}{"normalized-closeness"},"]\tfactor[",$distanceFactor{$potential},"]\n";
}

# Sort in reverse order
my @ranked = sort {$distanceFactor{$b} <=> $distanceFactor{$a}} @indsByCloseness;
foreach my $potential (@ranked)
{
    $potential =~ /0+(\d+)/;
    my $index = $1;
    print "Ind=[$potential]\tindex[$index]\tdistance[",$distances[$index],"]\tcloseness[",$data{$potential}{"normalized-closeness"},"]\tfactor[",$distanceFactor{$potential},"]\tMimics=[",$data{$potential}{"mimicing-neighbor-count"},"]\n";
}

# -------------------------------------------------------------------
# Sort the individuals by the number of mimicing neighbors
my @indsByMimicingNeighborCount = sort { $data{$a}{"mimicing-neighbor-count"} <=> $data{$b}{"mimicing-neighbor-count"} } keys( %data );
foreach my $key (@indsByMimicingNeighborCount)
{
    print "Ind[$key]\tCount=[",$data{$key}{"mimicing-neighbor-count"},"]\tCloseness=[",$data{$key}{"closeness"},"]\n";
}

