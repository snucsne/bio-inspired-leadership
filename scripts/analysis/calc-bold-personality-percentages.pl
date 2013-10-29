#!/usr/bin/perl
use strict;
use constant BOLDTHRESHOLD => 0.6;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

# Get the output file
#my $outputFile = shift( @ARGV );

# Get the eps file
#my $epsFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @dataFiles = grep { /\.dat$/ } readdir( DIR );
@dataFiles = sort( @dataFiles );
closedir( DIR );

#print "Found ",($#dataFiles + 1)," files\n";

# -------------------------------------------------------------------
# Read the data files
my $boldPercentages;
foreach my $dataFile (@dataFiles)
{
    # Open the file
    open( DATA, "$dataDir/$dataFile" ) or die "Unable to open data file [$dataDir/$dataFile]: $!\n";

    # Get the run ID
    $dataFile =~ /var-(\d\d)-seed/;
    my $runID = $1;

    my $totalInds;
    my $totalBolds;

    # Read each line
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

        if( $key =~ /individual.*personality/ )
        {
            $totalInds++;
            if( $value > BOLDTHRESHOLD )
            {
                $totalBolds++
            }
        }
    }

    # Add the percentage
    if( 0 < $totalInds )
    {
        $boldPercentages .= sprintf( "%06.4f", ($totalBolds/$totalInds))."  ";
    }
    else
    {
        print STDERR "No personality values found in [$dataFile]\n";
    }

    close( DATA );
}

# -------------------------------------------------------------------
# Write out the results
#open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

#print OUTPUT $boldPercentages;

#close( OUTPUT );

# -------------------------------------------------------------------
# Print out the percentages
print $boldPercentages;

