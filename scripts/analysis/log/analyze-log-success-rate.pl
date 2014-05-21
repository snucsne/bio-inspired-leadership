#!/usr/bin/perl
use strict;
use Statistics::Descriptive;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

print "#  Sims   Successes\n";

# -------------------------------------------------------------------
# Open the input file
open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";

# Read each line
my $line = 0;
my $sims = 0;
my $successes = 0;
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

    # Perform an initial pass
    /^(\w)\s+.*$/;
    my $result = $1;

    if( $result =~ /S/ )
    {
        $successes++;
    }

    print " ",sprintf("%6d", $sims ),"      ",sprintf("%6d", $successes),"\n";
}

# Close the file
close( INPUT );

