#!/usr/bin/perl
use strict;
use List::Util qw(sum);
use POSIX;
use Statistics::Descriptive;


# -------------------------------------------------------------------
# Get the root data directory
my $rootDataDir = shift( @ARGV );

# Ensure that it ends with a "/"
unless( $rootDataDir =~ m/\/$/ )
{
    $rootDataDir .= "/";
}
print "# --------------------------\n";
print "# rootDataDir=[$rootDataDir]\n";

# Get the number of individuals in the group
$rootDataDir =~ m/indcount-(\d+)/;
my $groupSize = $1;
print "# indCount=[$groupSize]\n";
print "# --------------------------\n";


# -------------------------------------------------------------------
# Get all the data directories in the data directory
opendir( DIR, $rootDataDir ) or die "Unable to open root data directory [$rootDataDir]: $!\n";
my @dataDirs = sort( grep { /boldcount/ } readdir( DIR ) );
closedir( DIR );

# -------------------------------------------------------------------
# Calculate the mean success percentage for each directory
my %successPercentages;
my %successData;
#print "# indCount=[$groupSize]\n";
foreach my $dataDir (@dataDirs)
{
    # Get the number of bold individuals
    $dataDir =~ m/boldcount-(\d+)/;
    my $boldCount = $1;

    # Get the mean success percentage
#    my $successPercentage = findMeanSuccessPercentage( $rootDataDir.$dataDir );
    my $successStatsRef = findMeanSuccessPercentage( $rootDataDir.$dataDir );

    # Store it
#    $successPercentages{$boldCount} = $successPercentage;
#    print "# boldCount=[$boldCount]  success=[$successPercentage]\n";
    $successPercentages{$boldCount} = $successStatsRef->{"mean"};
    $successData{$boldCount} = $successStatsRef->{"data"};
    print "# boldCount=[$boldCount]  success=[",
            sprintf( "%0.6f", $successStatsRef->{"mean"} ),
            "]  sd=[",
            sprintf( "%0.6f", $successStatsRef->{"sd"} ),
            "]  count=[",
            $successStatsRef->{"count"},"]\n";
}

# -------------------------------------------------------------------
# Sort the bold counts by success percentage
my @sortedBoldCounts = sort { $successPercentages{$b} <=> $successPercentages{$a} }
        keys %successPercentages;

#print "Sorted: @sortedBoldCounts\n";

# -------------------------------------------------------------------
# Get the top bold count
my $optimalBoldCount = $sortedBoldCounts[0];

# Print out the optimal bold information
my $trimmedBoldCount = $optimalBoldCount;
$trimmedBoldCount =~ s/^0*//;
print "group-size-$groupSize.optimal-bold-count = $trimmedBoldCount\n";
print "group-size-$groupSize.optimal-bold-percentage = ",($optimalBoldCount / $groupSize),"\n";
print "group-size-$groupSize.optimal-bold-success-percentage = ",($successPercentages{$optimalBoldCount}),"\n";
print "group-size-$groupSize.optimal-bold-data = ",($successData{$optimalBoldCount}),"\n";
print "\n\n";

# ===================================================================
sub findMeanSuccessPercentage
{
    my ($dir) = @_;

    # Open the directory and get all the data files
    opendir( DIR, $dir ) or die "Unable to open data file directory [$dir]: $!\n";
    my @dataFiles = sort( grep { /spatial-hidden-var/ } readdir( DIR ) );
    closedir( DIR );

    # Process each file
    my @successPercentages;
    foreach my $dataFile (@dataFiles)
    {
        my $success = `grep total-leadership-success $dir/$dataFile | awk '{print \$3;}' | tr -d '\n'`;
        push( @successPercentages, $success );
    }

    # Calculate the mean
    my $stats = Statistics::Descriptive::Full->new();
    $stats->add_data( @successPercentages );

    my %statResults = ( "mean" => $stats->mean(),
            "sd" => $stats->standard_deviation(),
            "count" => $stats->count(),
            "data" => join( " ", @successPercentages ) );
    return \%statResults
#    my $mean = 0;
#    if( @successPercentages > 0 )
#    {
#        $mean = sum(@successPercentages)/@successPercentages;
#    }
#    else
#    {
#        `echo $dir > error.out`;
#    }
#    return $mean;

}



