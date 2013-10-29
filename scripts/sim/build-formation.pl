#!/usr/bin/perl
# -------------------------------------------------------------------
use strict;
use POSIX;
use Math::Trig;

# -------------------------------------------------------------------
use constant TWO_PI => 2 * pi;


# -------------------------------------------------------------------
# Get the command line arguments
my $ringCount = shift( @ARGV );
my $radiusIncrement = shift( @ARGV );
my $radiusMultiplier = shift( @ARGV );
my $initialLocationCount = shift( @ARGV );
my $locationCountIncrement = shift( @ARGV );
my $locationCountMultiplier = shift( @ARGV );
my $majorAxis = shift( @ARGV );
my $minorAxis = shift( @ARGV );
my $xOffset = shift( @ARGV );
my $yOffset = shift( @ARGV );
my $buildInCenterFlag = shift( @ARGV );


# Start the output
print "# -------------------------------------------------------------------\n";
print "# Circle formation:\n";
print "#   ringCount=[$ringCount]\n";
print "#   radiusIncrement=[$radiusIncrement]\n";
print "#   radiusMultiplier=[$radiusMultiplier]\n";
print "#   initialLocationCount=[$initialLocationCount]\n";
print "#   locationCountIncrement=[$locationCountIncrement]\n";
print "#   locationCountMultiplier=[$locationCountMultiplier]\n";
print "#   majorAxis=[$majorAxis]\n";
print "#   minorAxis=[$minorAxis]\n";
print "#   xOffset=[$xOffset]\n";
print "#   yOffset=[$yOffset]\n";
print "\n\n";

# Build some handy variables
my $locationsToGenerate = $initialLocationCount;
my $totalLocationsGenerated = 0;

# Do we build something at the center?
if( $buildInCenterFlag )
{
    # Yup
    print "# -------------------------------------------------------------------\n";
    print "# Center location\n";
    printf( "%+05.3f  %+05.3f\n", $xOffset, $yOffset );
    print "\n\n";
    $totalLocationsGenerated++;
}

# Build all the locations
for( my $i = 1; $i <= $ringCount; $i++ )
{
    # Build the radius
    my $radius += $i * $radiusMultiplier + $radiusIncrement;
       
    # Build the locations for this ring
    generateEllipticalRingLocations( $locationsToGenerate,
            $radius,
            $majorAxis,
            $minorAxis,
            ($i % 2),
            $xOffset,
            $yOffset );

    # Update our total
    $totalLocationsGenerated += $locationsToGenerate;
    print "# Total locations generated [$totalLocationsGenerated]\n\n";


    # Increase the number of locations to create
    $locationsToGenerate += floor( $locationCountMultiplier * $locationsToGenerate )
             + $locationCountIncrement;
}



# ===================================================================
sub generateEllipticalRingLocations
{
    my ($locationCount,
            $radius,
            $majorAxis,
            $minorAxis,
            $angleOffsetFlag,
            $xOffset,
            $yOffset) = @_;

    # Start the output
    print "# -------------------------------------------------------------------\n";
    print "# Ring:\n";
    print "#   locationCount=[$locationCount]\n";
    print "#   radius=[$radius]\n";
    print "#   angleOffsetFlag=[$angleOffsetFlag]\n";

    # Create some handy variables
    my $angleDelta = TWO_PI / $locationCount;

    # Do we offset the angle?
    my $angleOffset = 0;
    if( $angleOffsetFlag )
    {
        $angleOffset = $angleDelta / 2;
    }

    # Build all the locations
    for( my $i = 0; $i < $locationCount; $i++ )
    {
        # Build the location
        my $x = $radius * $majorAxis
                * cos( $angleDelta * $i + $angleOffset )
                + $xOffset;
        my $y = $radius * $minorAxis
                * sin( $angleDelta * $i + $angleOffset )
                + $yOffset;

        # Print it
        printf( "%+05.3f  %+05.3f\n", $x, $y );
    }
    print "\n";
}
# ===================================================================

