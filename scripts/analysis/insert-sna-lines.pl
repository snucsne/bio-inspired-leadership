#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

# Create a temp output file
my $outputFile = "/tmp/sna.tmp";
open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]:$!\n";

# Open the file
open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";


# Read each line
while( <INPUT> )
{
    # Does it contain the sna-todo key?
#    if( /sna-todo/ )
    if( /betweenness/ )
    {
        # Yup, grab the individual ID
#        $_ =~ /^individual\.(.+)\.sna-todo/;
        $_ =~ /^individual\.(.+)\.betweenness/;
        my $id = $1;

# Replace the bogus ID in the TAG
s/\%\%(Ind)+(\d+)/\%\%$id/;
print OUTPUT $_;


        # Print out the new SNA keys
        print OUTPUT "individual.$id.alpha-centrality = %%%$id-ALPHA-CENTRALITY%%%\n";
        print OUTPUT "individual.$id.degree = %%%$id-DEGREE%%%\n";
        print OUTPUT "individual.$id.closeness = %%%$id-CLOSENESS%%%\n";
        print OUTPUT "individual.$id.transitivity = %%%$id-TRANSITIVITY%%%\n";
        print OUTPUT "individual.$id.pagerank = %%%$id-PAGERANK%%%\n";
        print OUTPUT "individual.$id.kleinberg = %%%$id-KELINBERG%%%\n";
        print OUTPUT "individual.$id.out-degree = %%%$id-OUT-DEGREE%%%\n";
        print OUTPUT "individual.$id.in-degree = %%%$id-IN-DEGREE%%%\n";
    }
    elsif( /EIGENVECTOR/ )
    {
        # Yup, grab the individual ID
        $_ =~ /^individual\.(.+)\./;
        my $id = $1;
        
        # Replace the bogus ID in the TAG
        s/\%\%(Ind)+(\d+)/\%\%$id/;
        print OUTPUT $_;
    }
    else
    {
        print OUTPUT $_;
    }
}

close( OUTPUT );
close( INPUT );

`mv $outputFile $inputFile`;
