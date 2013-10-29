#!/usr/bin/perl
use strict;

while( @ARGV )
{
    my $dir = shift( @ARGV );
    my $output = `cd $dir/analysis/;cat *success-percentage.dat > success-percentages.dat`;
#    chomp($output);
#    print "output=[$output]\n";
}
