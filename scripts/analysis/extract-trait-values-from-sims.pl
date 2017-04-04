#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
$dataDir =~ /indcount-(\d+)/;
my $indCount = $1;

# Get the outputfile prefix
my $outputFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
my @dataFiles;
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
@dataFiles = sort( grep { /^spatial.*\.dat$/ } readdir( DIR ) );
closedir( DIR );

#print "Found ",(scalar @dataFiles)," files\n";

# -------------------------------------------------------------------
# Process each data file
foreach my $dataFile (@dataFiles)
{
    my %traits;

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

        # Get the data key and value
        my ($key,$value) = split( /\s+=\s+/, $_ );

#print "Testing [$_]\n";

        # Is it an individual trait line?
        if( $key =~ /personality-trait/ )
        {
            # Yup
            my ($trash, $id, $alsoTrash, $trait ) = split( /\./, $key );
            $traits{$id}{$trait} = $value;
#print "    Found id=[$id] trait=[$trait] value=[$value]\n";
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
    my $nowString = localtime;
    print OUTPUT "#               Time  [$nowString]\n";
    print OUTPUT "# ======================================================================\n\n";
    print OUTPUT "# Activity  Fearful     Bold        Social\n";

    # Dump out the data by individual ID
    foreach my $id (sort (keys %traits))
    {
        # Get the trait values
        my $boldValue = $traits{$id}{'bold_shy'};
        my $activityValue = $traits{$id}{'active_lazy'};
        my $socialValue = $traits{$id}{'social_solitary'};
        my $fearfulValue = $traits{$id}{'fearful_assertive'};

        printf( OUTPUT "%6.4f      %6.4f      %6.4f      %6.4f\n",
                $activityValue,
                $fearfulValue,
                $boldValue,
                $socialValue );
    }

    close( OUTPUT );

}

