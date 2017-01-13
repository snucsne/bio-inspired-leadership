#!/usr/bin/perl
# -------------------------------------------------------------------
use strict;

# -------------------------------------------------------------------
# Get the command line arguments
my $indCount = shift( @ARGV );
my $correctDir = shift( @ARGV );
my $incorrectDir = shift( @ARGV );
my $correctPercentage = shift( @ARGV );

# -------------------------------------------------------------------
# Calculate the total number of incorrect directions
my $totalIncorrectDirs = $indCount * (1.0 - $correctPercentage);

# Build the directions for each individual
# Start off with the correct direction
my @dirs;
for my $i (1..$indCount)
{
    $dirs[$i - 1] = $correctDir;
}

# "Incorrect" the appropriate number
my $currentIncorrectDirs = 0;
my @correctDirIDs = (0..($indCount - 1));
for my $i (1..$totalIncorrectDirs )
{
#print "Current correct: ", join( ", ", @correctDirIDs);

    # Randomly choose an ID
    my $index = int( rand( scalar @correctDirIDs ) );
    my $id = $correctDirIDs[$index];

#print "\tindex=[$index]\tid=[$id]\n";
    $dirs[$id] = $incorrectDir;

    # Remove it
    splice @correctDirIDs, $index, 1;
}

#print join( "\n", @dirs ), "\n";
for my $i (0..((scalar @dirs) - 1))
{
    printf( "%-3d   %5.2f\n", $i, $dirs[$i] );
}

