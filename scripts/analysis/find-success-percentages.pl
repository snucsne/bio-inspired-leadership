#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
unless( $dataDir =~ /\/$/ )
{
    $dataDir = "$dataDir/";
}

# Get the output file
my $outputFile = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @dataFiles = grep { /\.dat$/ } readdir( DIR );
@dataFiles = sort( @dataFiles );
closedir( DIR );

#print "Found ",($#dataFiles + 1)," files\n";

# -------------------------------------------------------------------
# Search the data files
my $results = "";
foreach my $dataFile (@dataFiles)
{
    # Get the total number of simulations
    my $simulations = `grep total-simulations $dataDir$dataFile | sed 's/.* = //'`;
    chomp( $simulations );

    # Get the total number of successful simulations
    my $successes = `grep total-successful-simulations $dataDir$dataFile | sed 's/.* = //'`;
    chomp( $successes );

    $results .= ($successes/$simulations)."  ";
#    $results .= $successes."  ";

}


open( SUCCESS, "> $outputFile" ) or die "Unable to open success file [$outputFile]: $!\n";
print SUCCESS "$results\n";
close( SUCCESS );
