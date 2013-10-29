#!/usr/bin/perl
use strict;
use Statistics::Descriptive;


# -------------------------------------------------------------------
# Define the output headers
my $fullHeader =  "# 1: Predation level\n".
        "# 2: Energy level\n".
        "# 3: Maturation level\n".
        "# 4: Other activity\n".
        "# 5: Mean activity\n".
        "# 6: Mean activity standard deviation\n".
        "# 7: Maximum activity\n".
        "# 8: Minimum activity\n";

my $noMaturationOrOtherHeader =  "# 1: Predation level\n".
        "# 2: Energy level\n".
        "# 3: Mean activity\n".
        "# 4: Mean activity standard deviation\n".
        "# 5: Maximum activity\n".
        "# 6: Minimum activity\n";

my $noEnergyOrOtherHeader =  "# 1: Predation level\n".
        "# 2: Maturation level\n".
        "# 3: Mean activity\n".
        "# 4: Mean activity standard deviation\n".
        "# 5: Maximum activity\n".
        "# 6: Minimum activity\n";

my $noEnergyOrMaturationHeader =  "# 1: Predation level\n".
        "# 2: Other activity\n".
        "# 3: Mean activity\n".
        "# 4: Mean activity standard deviation\n".
        "# 5: Maximum activity\n".
        "# 6: Minimum activity\n";

my $predMovieHeader = "# 1: Energy level\n".
        "# 2: Maturation level\n".
        "# 3: Mean activity level\n";

my $otherOnlyHeader = "# 1: Other acitivty level\n".
        "# 2: Mean activity level\n".
        "# 3: Mean activity standard deviation\n".
        "# 4: Maximum activity\n".
        "# 5: Minimum activity\n";


# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
unless( $dataDir =~ /\/$/ )
{
    $dataDir = "$dataDir/";
}
print "Data directory [$dataDir]\n";

# Get all the data files
opendir( DIR, $dataDir ) or die "Unable to open data dir [$dataDir]: $!\n";
my @dataFiles = grep { /result.*\.dat$/ } readdir ( DIR );
closedir( DIR );

print "Found ",($#dataFiles + 1)," data files\n";


# -------------------------------------------------------------------
# Process each file
my %activityLevels;
my $dataFile;
my $count = 0;
foreach $dataFile (@dataFiles)
{
    my $file = "$dataDir$dataFile";
    $count++;
    print "Data file [$count]=[$file]\n";
    
    # Open the file
    open( DATA, "$file" ) or die "Unable to open data file [$file]: $!\n";

    # Read each line
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my @tempData = split( /\s+/, $_ );
        my $predation = $tempData[0];
        my $energy = $tempData[1];
        my $maturation = $tempData[2];
        my $otherActivity = 0.0;
        my $activity = 0.0;
        if( 3 == $#tempData )
        {
            $activity = $tempData[3];
        }
        elsif( 4 == $#tempData )
        {
            $otherActivity = $tempData[3];
            $activity = $tempData[4];
        }
        else
        {
            print "ERROR: Unknown number of data columns [",($#tempData+1),"]\n";
            exit(1);
        }

        push( @{$activityLevels{$predation}{$energy}{$maturation}{$otherActivity}}, $activity );
    }

    # Close the file
    close( DATA );
}

# -------------------------------------------------------------------
# Dump the full stats

my $file = $dataDir."full-simple-development-activity.dat";

open( FULL, "> $file" ) or die "Unable to open full activity [$file]: $!\n";

print FULL $fullHeader;

# Iterate through all the predation levels
my $maxMean = -1;
foreach my $predation (sort { $a <=> $b } keys %activityLevels)
{
    foreach my $energy (sort { $a <=> $b } keys %{$activityLevels{$predation}})
    {
        foreach my $maturation (sort { $a <=> $b } keys %{$activityLevels{$predation}{$energy}})
        {
            foreach my $otherActivity (sort { $a <=> $b } keys %{$activityLevels{$predation}{$energy}{$maturation}})
            {
                # Calculate some interesting statistics
                my $stats = Statistics::Descriptive::Full->new();
                $stats->add_data( @{$activityLevels{$predation}{$energy}{$maturation}{$otherActivity}} );
                my $mean = $stats->mean();
                my $stdDev = $stats->standard_deviation();
                my $max = $stats->max();
                my $min = $stats->min();

                printf( FULL
                        "%+04.2f   %04.2f   %04.2f   %+04.2f   %04.2f   %04.2f   %04.2f   %04.2f\n",
                        $predation,
                        $energy,
                        $maturation,
                        $otherActivity,
                        $mean,
                        $stdDev,
                        $max,
                        $min );
    
                if( $mean > $maxMean )
                {
                    $maxMean = $mean;
                }
            }
        }
    }
}

print FULL "# Max mean activity level [$maxMean]\n";

close( FULL );


# -------------------------------------------------------------------
# Dump the stats without maturation
my $dumpNoMaturation = 0;
if( $dumpNoMaturation )
{
    my $file = $dataDir."no-maturation-simple-development-activity.dat";

    open( NOMAT, "> $file" ) or die "Unable to open no maturation activity [$file]: $!\n";

    print NOMAT $noMaturationOrOtherHeader;

    # Iterate through all the predation levels
    $maxMean = -1;
    foreach my $predation (sort { $a <=> $b } keys %activityLevels)
    {
        foreach my $energy (sort { $a <=> $b } keys %{$activityLevels{$predation}})
        {
            # Calculate some interesting statistics
            my $stats = Statistics::Descriptive::Full->new();

            foreach my $maturation (sort { $a <=> $b } keys %{$activityLevels{$predation}{$energy}})
            {
                foreach my $otherActivity (sort { $a <=> $b } keys %{$activityLevels{$predation}{$energy}{$maturation}})
                {
                    $stats->add_data( @{$activityLevels{$predation}{$energy}{$maturation}{$otherActivity}} );
                }
            }

            my $mean = $stats->mean();
            my $stdDev = $stats->standard_deviation();
            my $max = $stats->max();
            my $min = $stats->min();

            printf( NOMAT "%04.2f   %04.2f   %04.2f   %04.2f   %04.2f   %04.2f\n",
                    $predation,
                    $energy,
                    $mean,
                    $stdDev,
                    $max,
                    $min );

            if( $mean > $maxMean )
            {
                $maxMean = $mean;
            }
        }
    }

    print NOMAT "# Max mean activity level [$maxMean]\n";

    close( NOMAT );
}


# -------------------------------------------------------------------
# Dump the stats without energy
my $dumpNoEnergy = 0;
if( $dumpNoEnergy )
{
    my $file = $dataDir."no-energy-simple-development-activity.dat";

    open( NOENERGY, "> $file" ) or die "Unable to open no maturation activity [$file]: $!\n";

    print NOENERGY $noEnergyOrOtherHeader;

    # Iterate through all the predation levels
    $maxMean = -1;
    foreach my $predation (sort { $a <=> $b } keys %activityLevels)
    {
        foreach my $maturation (sort { $a <=> $b } keys %{$activityLevels{$predation}{"0.00"}})
        {
            # Calculate some interesting statistics
            my $stats = Statistics::Descriptive::Full->new();

            foreach my $energy (sort { $a <=> $b } keys %{$activityLevels{$predation}})
            {
                foreach my $otherActivity (sort { $a <=> $b } keys %{$activityLevels{$predation}{$energy}{$maturation}})
                {
                    $stats->add_data( @{$activityLevels{$predation}{$energy}{$maturation}{$otherActivity}} );
                }
            }

            my $mean = $stats->mean();
            my $stdDev = $stats->standard_deviation();
            my $max = $stats->max();
            my $min = $stats->min();

            printf( NOENERGY "%04.2f   %04.2f   %04.2f   %04.2f   %04.2f   %04.2f\n",
                    $predation,
                    $maturation,
                    $mean,
                    $stdDev,
                    $max,
                    $min );

            if( $mean > $maxMean )
            {
                $maxMean = $mean;
            }
        }
    }

    print NOENERGY "# Max mean activity level [$maxMean]\n";

    close( NOENERGY );
}

# -------------------------------------------------------------------
# Dump the stats without maturation or energy
my $dumpNoEnergyOrMat = 1;
if( $dumpNoEnergyOrMat )
{
    my $file = $dataDir."no-maturation-or-energy-simple-development-activity.dat";

    open( NOMAT, "> $file" ) or die "Unable to open no maturation or energy activity [$file]: $!\n";

    print NOMAT $noEnergyOrMaturationHeader;

    # Iterate through all the predation levels
    $maxMean = -1;
    foreach my $predation (sort { $a <=> $b } keys %activityLevels)
    {
        foreach my $otherActivity (sort { $a <=> $b } keys %{$activityLevels{$predation}{"0.00"}{"0.00"}})
        {
            # Calculate some interesting statistics
            my $stats = Statistics::Descriptive::Full->new();

            foreach my $energy (sort { $a <=> $b } keys %{$activityLevels{$predation}})
            {
                foreach my $maturation (sort { $a <=> $b } keys %{$activityLevels{$predation}{$energy}})
                {
                    $stats->add_data( @{$activityLevels{$predation}{$energy}{$maturation}{$otherActivity}} );
                }
            }

            my $mean = $stats->mean();
            my $stdDev = $stats->standard_deviation();
            my $max = $stats->max();
            my $min = $stats->min();

            printf( NOMAT "%+04.2f   %+04.2f   %04.2f   %04.2f   %04.2f   %04.2f\n",
                    $predation,
                    $otherActivity,
                    $mean,
                    $stdDev,
                    $max,
                    $min );

            if( $mean > $maxMean )
            {
                $maxMean = $mean;
            }
        }
    }

    print NOMAT "# Max mean activity level [$maxMean]\n";

    close( NOMAT );
}


# -------------------------------------------------------------------
# Dump the stats for the other's activity level only

my $file = $dataDir."other-only-simple-development-activity.dat";

open( NOMAT, "> $file" ) or die "Unable to open other only activity activity [$file]: $!\n";

print NOMAT $otherOnlyHeader;

# Iterate through all the other's activity levels
$maxMean = -1;
foreach my $otherActivity (sort { $a <=> $b } keys %{$activityLevels{"0.00"}{"0.00"}{"0.00"}})
{
    # Calculate some interesting statistics
    my $stats = Statistics::Descriptive::Full->new();

    foreach my $predation (sort { $a <=> $b } keys %activityLevels )
    {
        foreach my $energy (sort { $a <=> $b } keys %{$activityLevels{$predation}})
        {
            foreach my $maturation (sort { $a <=> $b } keys %{$activityLevels{$predation}{$energy}})
            {
                $stats->add_data( @{$activityLevels{$predation}{$energy}{$maturation}{$otherActivity}} );
            }
        }
    }

    my $mean = $stats->mean();
    my $stdDev = $stats->standard_deviation();
    my $max = $stats->max();
    my $min = $stats->min();

    printf( NOMAT "%+04.2f   %04.2f   %04.2f   %04.2f   %04.2f\n",
            $otherActivity,
            $mean,
            $stdDev,
            $max,
            $min );

    if( $mean > $maxMean )
    {
        $maxMean = $mean;
    }
}

print NOMAT "# Max mean activity level [$maxMean]\n";

close( NOMAT );


# -------------------------------------------------------------------
# Dump the graphs for the predation movie
my $dumpPredMovie = 0;
if( $dumpPredMovie )
{
    # Iterate through each of the predation levels
    $maxMean = -1;
    foreach my $predation (sort { $a <=> $b } keys %activityLevels)
    {
        my $file = $dataDir."activity-for-predation-$predation.dat";
        open( PREDMOV, "> $file" ) or die "Unable to open no maturation activity [$file]: $!\n";

        print PREDMOV "# Predation level [$predation]\n#\n";
        print PREDMOV $predMovieHeader;

        foreach my $energy (sort { $a <=> $b } keys %{$activityLevels{$predation}})
        {
            foreach my $maturation (sort { $a <=> $b } keys %{$activityLevels{$predation}{$energy}})
            {
                foreach my $otherActivity (sort { $a <=> $b } keys %{$activityLevels{$predation}{$energy}{$maturation}})
                {
                    # Calculate some interesting statistics
                    my $stats = Statistics::Descriptive::Full->new();
                    $stats->add_data( @{$activityLevels{$predation}{$energy}{$maturation}{$otherActivity}} );
                    my $mean = $stats->mean();
                    my $stdDev = $stats->standard_deviation();
                    my $max = $stats->max();
                    my $min = $stats->min();

                    printf( PREDMOV
                            "%04.2f   %04.2f   %04.2f   %04.2f   %04.2f   %04.2f   %04.2f\n",
                            $energy,
                            $maturation,
                            $otherActivity,
                            $mean,
                            $stdDev,
                            $max,
                            $min );

                    if( $mean > $maxMean )
                    {
                        $maxMean = $mean;
                    }
                }
            }
        }
        print PREDMOV "# Current max mean [$maxMean]\n";
        close( PREDMOV );
    }
}
