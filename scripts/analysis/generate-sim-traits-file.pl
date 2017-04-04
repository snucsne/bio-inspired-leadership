#!/usr/bin/perl
use strict;
use strict;
use Math::Erf::Approx -all => { -prefix => 'math_' };
use Statistics::Descriptive::Weighted;

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
@dataFiles = sort( grep { /^position.*\.dat$/ } readdir( DIR ) );
closedir( DIR );

#print "Found ",(scalar @dataFiles)," files\n";


# -------------------------------------------------------------------
my $mu = 1.0;
my $sig = 0.28;

my $distFile = "/home/brent/research/projects/leader/dist-test/data/map-position-analysis/metric/analysis/all-mean-dist.dat";
my @distValues;
my @weights;
open( INPUT, "$distFile" ) or die "Unable to open input file [$distFile]: $!\n";
while( <INPUT> )
{
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;
    push( @distValues, $_ );
    push( @weights, 1 );
}
close( INPUT );

my $stat = Statistics::Descriptive::Weighted::Full->new();
$stat->add_data( \@distValues, \@weights );


# -------------------------------------------------------------------
# Process each data file
foreach my $dataFile (@dataFiles)
{
    # Create a variable in which data is stored
    my %data;

    # Open the file
    open( INPUT, "$dataDir/$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

#print "file: $dataDir/$dataFile\n";

    # Get the map id
    $dataFile =~ /map-(\d+)/;
    my $mapID = $1;

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
#            my $erfValue = 0.5 * math_erfc( -1 * (log($value) - $mu)/($sig * sqrt(2)) );
#            $erfValue = 0.1 + 0.8 * $erfValue;
#            my $activityValue = ensureValidTraitValue( $erfValue );

            my $activityValue = ensureValidTraitValue( $stat->cdf( $value ) );
            $data{$id}{'activity'} = $activityValue;
#printf( "%6.4f      %6.4f\n", $value, $activityValue );
#printf( "%6.4f      %6.4f      %6.4f      %6.4f\n", $value, $erfValue, $activityValue );
        }
        elsif( $dataKey =~ /mean-resultant-vector.length/ )
        {
            my $fearfulValue = ensureValidTraitValue( $value );
            $data{$id}{'fearful'} = $fearfulValue;
        }
    }

    # Close the file
    close( INPUT );

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

        printf( OUTPUT "%6.4f      %6.4f      %6.4f      %6.4f\n",
                $activityValue,
                $fearfulValue,
                $boldValue,
                (1 - $boldValue) );
    }

    close( OUTPUT );
#exit;
}


# ===================================================================
# Ensure the trait value is within the appropriate bounds
sub ensureValidTraitValue
{
    my ($traitValue) = @_;

    # Set up our min and max values
    my $minTraitValue = 0.1;
    my $maxTraitValue = 0.9;
    my $traitDiff = $maxTraitValue - $minTraitValue;

    $traitValue = $minTraitValue + $traitDiff
            * ($traitValue - $minTraitValue);

    if( $traitValue < $minTraitValue )
    {
        $traitValue = $minTraitValue;
    }
    elsif( $traitValue > $maxTraitValue )
    {
        $traitValue = $maxTraitValue;
    }

    return $traitValue;
}
