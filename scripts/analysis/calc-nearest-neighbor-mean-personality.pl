#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data file
my $dataFile = shift( @ARGV );


# -------------------------------------------------------------------
# Read the data file
open( DATA, "$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

my %personality;
my %neighbors;

# Read each line
while( <DATA> )
{
    # Remove any other comments or whitespace
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

    my ($key,$value) = split( /\s+=\s+/, $_ );

    if( $key =~ /individual\.(.*)\.personality/ )
    {
        $personality{$1} = $value;
    }
    elsif( $key =~ /individual\.(.*)\.nearest-neighbors/ )
    {
        $neighbors{$1} = [ split( /\s+/, $value ) ];
    }
}

close( DATA );

# -------------------------------------------------------------------
foreach my $ind (sort (keys %personality))
{
    my @neighborPersonalities;
    foreach my $neighbor (@{$neighbors{$ind}})
    {
        push( @neighborPersonalities, $personality{$neighbor} );
    }

    print "Ind=[$ind]   personality=[",
            $personality{$ind},
            "]   average=[",
            average( \@neighborPersonalities ),
            "]   stddev=[",
            stdev( \@neighborPersonalities ),
            "]\n";

    foreach my $neighbor (@{$neighbors{$ind}})
    {
#        print "    Neighbor=[$neighbor]   personality=[".$personality{$neighbor}."]\n";
    }
#    print "\n";
}



# -------------------------------------------------------------------
# http://edwards.sdsu.edu/labsite/index.php/kate/302-calculating-the-average-and-standard-deviation
sub average{
        my($data) = @_;
        if (not @$data) {
                die("Empty array\n");
        }
        my $total = 0;
        foreach (@$data) {
                $total += $_;
        }
        my $average = $total / @$data;
        return $average;
}

# -------------------------------------------------------------------
sub stdev{
        my($data) = @_;
        if(@$data == 1){
                return 0;
        }
        my $average = &average($data);
        my $sqtotal = 0;
        foreach(@$data) {
                $sqtotal += ($average-$_) ** 2;
        }
        my $std = ($sqtotal / (@$data-1)) ** 0.5;
        return $std;
}

