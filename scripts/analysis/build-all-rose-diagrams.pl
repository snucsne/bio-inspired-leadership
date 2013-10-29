#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the root directory
my $rootDir = shift( @ARGV );

# Get the data directory text
my $dataDirText = shift( @ARGV );

# -------------------------------------------------------------------
# Get all the data directories in the directory
opendir( DIR, $rootDir ) or die "Unable to open data directory [$rootDir]: $!\n";
my @dataDirs = sort( grep { /$dataDirText/ } readdir( DIR ) );
closedir( DIR );


# -------------------------------------------------------------------
# Process each one
foreach my $dataDir (@dataDirs)
{
    # Build the figure directory
    my $figDir = $rootDir."/figures/dirs/".$dataDir."/";
    unless( -d $figDir )
    {
        `mkdir -p $figDir`;
    }

    # Build the figure prefix
    my $cleanedDataDir = $dataDir;
    $cleanedDataDir =~ s/\./-/g;
    my $figPrefix = $figDir."$cleanedDataDir-rose";

    # Build the plots
    `./build-rose-plots.pl $rootDir/$dataDir/ $figPrefix`;
#    print "./build-rose-plot.pl $rootDir/$dataDir/ $figPrefix\n";
}
