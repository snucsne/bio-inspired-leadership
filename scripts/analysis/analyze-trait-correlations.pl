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
# Create all the arrays to hold the trait values
my %traits;
$traits{'bold_shy'} = [];
$traits{'active_lazy'} = [];
$traits{'social_solitary'} = [];
$traits{'fearful_assertive'} = [];

# -------------------------------------------------------------------
# Process each data file
foreach my $dataFile (@dataFiles)
{

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

        # Is it an individual trait line?
        if( $key =~ /personality-trait/ )
        {
            # Yup
            my ($trash, $id, $alsoTrash, $trait ) = split( /\./, $key );
#print "    Found id=[$id] trait=[$trait] value=[$value]\n";
            push( @{$traits{$trait}}, $value );
        }
    }

    # Close the file
    close( INPUT );
}


# -------------------------------------------------------------------

print "bold <- scan()\n";
print join( " ", @{$traits{'bold_shy'}} ),"\n\n";
print "activity <- scan()\n";
print join( " ", @{$traits{'active_lazy'}} ),"\n\n";
print "fearful <- scan()\n";
print join( " ", @{$traits{'fearful_assertive'}} ),"\n\n";
print "social <- scan()\n";
print join( " ", @{$traits{'social_solitary'}} ),"\n\n";

print "traitsdf = data.frame( bold, activity, fearful, social )\n";
print "cor(traitsdf)\n";
print "pairs(traitsdf)\n";

