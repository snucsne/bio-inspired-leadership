#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

# Create a temp output file
my $outputFile = "/tmp/modularity.tmp";
open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]:$!\n";

# Open the file
open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";


# Read each line
while( <INPUT> )
{
    # Print it back out
    print OUTPUT $_;

    # Does it match the aggregate data line?
    if( /\# Aggregate data/ )
    {
        # Yup, add the modularity line
        print OUTPUT "modularity = %%%MODULARITY%%%\n";
    }
}
close( OUTPUT );
close( INPUT );

`mv $outputFile $inputFile`;

