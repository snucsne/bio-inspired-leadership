#!/usr/bin/perl
use strict;
use POSIX;

my $twoPI = 2 * 3.14159;
my $maxPoints = 20;
my $maxRings = 3;
my $minorAxis = 0.5;

my $pointTotal = 0;
my $ringPointTotal = 0;
my $radius = 1;
my $pointsInRing = 2;

for( my $ring = 1; $ring <= $maxRings; $ring++ )
{
    print "# Ring=[$ring] points=[$pointsInRing]\n";
    my $angleDelta = $twoPI / $pointsInRing;
    my $angleOffset = 0;
    if( ($ring % 2) == 0 )
    {
        $angleOffset = $angleDelta / 2;
    }
    for( my $i = 0; $i < $pointsInRing; $i++ )
    {
        my $x = $ring * cos( $angleDelta * $i + $angleOffset );
        my $y = $minorAxis * $ring * sin( $angleDelta * $i + $angleOffset );

        printf( "%+05.3f  %+05.3f\n", $x, $y );
        $pointTotal++;
    }

    $pointsInRing = floor( 2 * $pointsInRing );
}
print "# Point total [$pointTotal]\n";
