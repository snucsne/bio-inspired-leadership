#!/usr/bin/perl
use strict;

use List::Util qw( min max );

# -------------------------------------------------------------------
# Get the data file
my $meanShortestPathsFile = shift( @ARGV );

# -------------------------------------------------------------------
# Open it
open( FILE, "$meanShortestPathsFile" ) or die "Unable to open mean shortest path file [$meanShortestPathsFile]: $!\n";

my @meanShortestPaths;

# Read it
while( <FILE> )
{
    # Remove any other comments or whitespace
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

    # The only thing left is the mean shortest path
    push( @meanShortestPaths, $_ );
}

# Min and max mean shortest path
my $minMeanShortestPath = min @meanShortestPaths;
my $maxMeanShortestPath = max @meanShortestPaths;

# The range of values
my $range = $maxMeanShortestPath - $minMeanShortestPath;

print "# Date generated: ",scalar(localtime()),"\n";
print "# Min mean shortest path [$minMeanShortestPath]\n";
print "# Max mean shortest path [$maxMeanShortestPath]\n";
print "# Range of value [$range]\n";
print "# =============================================================\n\n";

print "#    MSP       Bold    Sociable    Exploratory     Escape\n";

# Process each individual
foreach my $meanShortestPath (@meanShortestPaths)
{
    my $centrality = 1 - ($meanShortestPath - $minMeanShortestPath) / $range;

#    printf( "%08.5f   %07.5f\n", $meanShortestPath, $targetValue );
#    print $meanShortestPath,"\t",$targetValue,"\n";   

    # Calculate personality traits
    my $bold = 0.8 * $centrality + 0.1;
    my $sociable = 0.8 * $centrality + 0.1;
    my $exploratory = 1 - (0.8 * $centrality + 0.1);
    my $escape = 0.9;

    printf( "%08.5f    %07.5f     %07.5f        %07.5f    %07.5f\n",
            $meanShortestPath,
            $bold,
            $sociable,
            $exploratory,
            $escape );
}


