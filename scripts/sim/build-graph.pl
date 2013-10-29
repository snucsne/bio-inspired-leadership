#!/usr/bin/perl
use strict;

my $CVS = 0;

if( $CVS )
{
    print "nodedef>name VARCHAR,label VARCHAR\n";
    for( my $i = 0; $i < 23; $i++ )
    {
        print "$i, $i\n";
    }
}

my @min = ( 0, 12 );
my @max = (11, 23 );

if( $CVS )
{
    print "edgedef>node1 VARCHAR,node2 VARCHAR\n";
}

my $id = 0;
for( my $i = 0; $i < 2; $i++ )
{
    for( my $j = $min[$i]; $j < $max[$i]; $j++ )
    {
        for( my $k = $min[$i]; $k < $max[$i]; $k++ )
        {
            if( $j != $k )
            {
                if( $CVS )
                {
                    print "$j, $k\n";
                }
                else
                {
                    print "$j $k  ";
                }
                $id++;
            }
        }
    }

    my $max = $min[$i] + ($max[$i] - $min[$i])/2;
    for( my $j = $min[$i]; $j < $max; $j++ )
    {
        if( $CVS )
        {
            print "$j, $max[0]\n";
            $id++;
            print "$max[0], $j\n";
            $id++;
        }
        else
        {
            print "$j $max[0] $max[0] $j  ";
            $id += 2;
        }
    }
}

print "\n";
