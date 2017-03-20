#!/usr/bin/perl
use strict;
use POSIX;

# -------------------------------------------------------------------
# Get the parameters
my $inputFile = shift( @ARGV );
my $indCount = shift( @ARGV );
my $generation = shift( @ARGV );
my $outputFile = shift( @ARGV );

# -------------------------------------------------------------------
# Read the file
open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";

while( my $row = <INPUT> )
{
    chomp $row;

    if( ($row =~ /gen\[0+$generation/) && ($row =~ /decoded/) )
    {
        # Get the coefficients
        $row =~ /alphaF=\[(\d+\.\d+)\] betaF=\[(\d+\.\d+)\] alphaC=\[(\d+\.\d+)\] gammaC=\[(\d+\.\d+)\] epsilonC=\[(\d+\.\d+)\]/;
        my $alphaF = $1;
        my $betaF = $2;
        my $alphaC = $3;
        my $gammaC = $4;
        my $epsilonC = $5;

        print "./plot-follow-cancel-curves.pl $alphaF $betaF $alphaC $gammaC $epsilonC $indCount $outputFile\n";
    }
}

close( INPUT );
