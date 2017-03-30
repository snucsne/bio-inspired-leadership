#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
$dataDir =~ /indcount-(\d+)/;
my $indCount = $1;

# Get the outputfile prefix
my $outputFilePrefix = shift( @ARGV );

# Get the bold trait value
my $boldValue = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
my @dataFiles;
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
@dataFiles = sort( grep { /\.dat$/ } readdir( DIR ) );
closedir( DIR );

#print "Found ",(scalar @dataFiles)," files\n";

# -------------------------------------------------------------------
# Process each data file
foreach my $dataFile (@dataFiles)
{
    # Create a variable in which data is stored
    my %data;

    # Open the file
    open( INPUT, "$dataDir/$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

    # Get the map id
    $dataFile =~ /map-(\d+)/;
    my $mapID = $1;

    # Save the min and max activity level so we can normalize for the group
    my $minActivityValue = 10;
    my $maxActivityValue = -10;

    # Read each line
    while( <INPUT> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Get the data key and individual id
        my ($key,$value) = split( /\s+=\s+/, $_ );
        my ($id, $dataKey ) = split( /\./, $key, 2 );

        # Is it the data in which we are interested?
        if( $dataKey =~ /distance-to-neighbors.mean/ )
        {
            # Normalize the value
            my $activityValue = $value / 10; #ensureValidTraitValue( $value / 10 );
            $data{$id}{'activity'} = $activityValue;
            if( $activityValue < $minActivityValue )
            {
                $minActivityValue = $activityValue;
            }
            if( $activityValue > $maxActivityValue )
            {
                $maxActivityValue = $activityValue;
            }
        }
        elsif( $dataKey =~ /mean-resultant-vector.length/ )
        {
            # This one is already normalized
            my $fearfulValue = ensureValidTraitValue( $value );
            $data{$id}{'fearful'} = $fearfulValue;
        }
    }

    # Close the file
    close( INPUT );

    # Calculate the activity value difference
    my $activityDiff = $maxActivityValue - $minActivityValue;

    # Build the output file name
    my $outputFile = $outputFilePrefix."sim-traits-map-$mapID.dat";

    # Open the output file
    open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

    # Dump header information
    print OUTPUT "#           Data dir  [$dataDir]\n";
    print OUTPUT "#          Ind count  [$indCount]\n";
    print OUTPUT "# Output file prefix  [$outputFilePrefix]\n";
    print OUTPUT "#         Bold value  [$boldValue]\n";
    my $nowString = localtime;
    print OUTPUT "#               Time  [$nowString]\n";
    print OUTPUT "# ======================================================================\n\n";
    print OUTPUT "# Activity  Fearful     Bold        Social\n";

    # Dump out the data by individual ID
    foreach my $id (sort (keys %data))
    {
        # Get the trait values
        my $activityValue = $data{$id}{'activity'};
        my $fearfulValue = $data{$id}{'fearful'};

        # Adjust the activity value
        $activityValue = 0.1 + 0.8*( ($activityValue - $minActivityValue)
                / $activityDiff );

        printf( OUTPUT "%6.4f      %6.4f      %6.4f      %6.4f\n",
                $activityValue,
                $fearfulValue,
                $boldValue,
                (1 - $boldValue) );
    }

    close( OUTPUT );
}


# ===================================================================
# Ensure the trait value is within the appropriate bounds
sub ensureValidTraitValue
{
    my ($traitValue) = @_;

    # Set up our min and max values
    my $minTraitValue = 0.1;
    my $maxTraitValue = 0.9;

    # Ensure it is valid on the lower bound
    if( $traitValue < $minTraitValue )
    {
        $traitValue = $minTraitValue;
    }
    # And the upper bound
    elsif( $traitValue > $maxTraitValue )
    {
        $traitValue = $maxTraitValue;
    }

    return $traitValue;
}
