#!/usr/bin/perl
use strict;
use Time::localtime;

# -------------------------------------------------------------------
# Get the data file
my $dataFile = shift( @ARGV );

# Get the r output file
my $rOutputFile = shift( @ARGV );

# -------------------------------------------------------------------
# Read the R output file
my %eigenCentralities;

# Open the file
open( ROUTPUT, "$rOutputFile" ) or die "Unable to open R output file [$rOutputFile]: $!\n";

# Read each line
my $gatherValues = 0;
while( <ROUTPUT> )
{
    # Remove any other comments or whitespace
#    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

#print "Line[$gatherValues]: $_\n";

    if( /XX-END-XX/ )
    {
        # We are done
        last;
    }
    elsif( $gatherValues )
    {
        my ($id, $value) = split( /\s+/, $_ );
        my $key = "Ind".sprintf("%05d", $id);
        $eigenCentralities{$key} = $value;
    }
    elsif( /XX-START-XX/ )
    {
        $gatherValues = 1;
    }
}
close( ROUTPUT );

# -------------------------------------------------------------------
# Read the data file
my $tmpOutputFile = "/tmp/temp-insert.dat";

# Open the data file
open( DATA, "$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

# Open the temp output file
open( OUTPUT, "> $tmpOutputFile" ) or die "Unable to open tmp output file [$tmpOutputFile]: $!\n";

# Read each line
while( <DATA> )
{
    # Replace the eigenvector centrality placeholder
    s/%%%(Ind\d+)-EIGENVECTOR-CENTRALITY%%%/$eigenCentralities{$1}/;

    # Print it to the output file
    print OUTPUT $_;
}

close( OUTPUT );
close( DATA );


# Move it over
`mv $dataFile $dataFile.bak`;
`mv $tmpOutputFile $dataFile`;
