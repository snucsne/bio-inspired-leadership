#!/usr/bin/perl
use strict;
use List::Util qw(sum);
use POSIX;


# -------------------------------------------------------------------
# Get the root data directory
my $rootDataDir = shift( @ARGV );

# -------------------------------------------------------------------
# Get all the data directories in the data directory
opendir( DIR, $rootDataDir ) or die "Unable to open root data directory [$rootDataDir]: $!\n";
my @dataDirs = sort( grep { /boldcount/ } readdir( DIR ) );
closedir( DIR );

# -------------------------------------------------------------------
# Calculate the mean success percentage for each directory
my %successPercentages;
foreach my $dataDir (@dataDirs)
{
    # Get the number of bold individuals
    $dataDir =~ m/boldcount-(\d+)/;
    my $boldCount = $1;

    # Get the mean success percentage
    my $successPercentage = findMeanSuccessPercentage( $rootDataDir.$dataDir );

    # Store it
    $successPercentages{$boldCount} = $successPercentage;
#    print "boldCount=[$boldCount]  success=[$successPercentage]\n";
}

# -------------------------------------------------------------------
# Sort the bold counts by success percentage
my @sortedBoldCounts = sort { $successPercentages{$b} <=> $successPercentages{$a} }
        keys %successPercentages;
#print "Sorted: @sortedBoldCounts\n";

# -------------------------------------------------------------------
# Calculate the next bold count to test
my $nextBoldCount = -1;
my $firstPlace = $sortedBoldCounts[0];
my $secondPlace = findSecondHighest( \@sortedBoldCounts );
if( ($secondPlace > 0) && ($firstPlace != $secondPlace) )
{
    # Calculate the difference
    my $diff = abs( $firstPlace - $secondPlace );
    if( $diff > 1 )
    {
        my $half = floor( $diff / 2 );
        if( $firstPlace <  $secondPlace )
        {
            $nextBoldCount = $half + $firstPlace;
        }
        else
        {
            $nextBoldCount = $half + $secondPlace;
        }
    }
}

print $nextBoldCount,"\n";


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
    my $mean = 0;
    if( @successPercentages > 0 )
    {
        $mean = sum(@successPercentages)/@successPercentages;
    }
    else
    {
        `echo $dir > error.out`;
    }
    return $mean;
}

# ===================================================================
sub findSecondHighest
{
    my ($sortedBoldRef) = @_;

    # Get the boldest
    my $firstPlace = shift( @{$sortedBoldRef} );

    # Check the next
    my $secondPlace = shift( @{$sortedBoldRef} );

    # If it isn't next to the first place, it's fine
    if( abs($firstPlace - $secondPlace) == 1 )
    {
        # OK, not fine.  Check to see if it is above or below
        my $above = 0;
        if( $secondPlace > $firstPlace )
        {
            $above = 1;
        }

        # Look to see if we find an uncheck value in the other direction
        foreach my $boldCount (@{$sortedBoldRef})
        {
            # Is this on the same side?
            unless( (($boldCount > $firstPlace) && $above)
                    || ($boldCount < $firstPlace) && !($above) )
            {
                # Nope, is it next to it?
                if( abs($boldCount - $firstPlace) > 1 )
                {
                    # Nope, use it
                    $secondPlace = $boldCount;
                }
                last;
            }
        }
    }

    #print "Second place [$secondPlace]\n";

    return $secondPlace;
}
