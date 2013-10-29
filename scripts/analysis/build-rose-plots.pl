#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

# Get the EPS file prefix
my $outputEPSPrefix = shift( @ARGV );


# -------------------------------------------------------------------
# Get all the data files in the directory
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @dataFiles = sort( grep { /dat/ } readdir( DIR ) );
closedir( DIR );

# -------------------------------------------------------------------
# Build a plot for each data file
my $dirDataFile = "/tmp/tmp-directions.txt";
#foreach my $dataFile (@dataFiles)
#{
#    # Grab the run ID
#    $dataFile =~ /var-(\d+)-/;
#    my $id = $1;
#
#    # Build a file with just the directions
#    `grep "preferred-dir" $dataDir/$dataFile | awk '{print \$3;}' | tr '\n' '  ' > $dirDataFile`;
#
#    # Create the plot
#    my $outputEPSFile = $outputEPSPrefix."-run-$id.eps";
#    createRosePlot( $dirDataFile, $outputEPSFile, "5.0" );
#}

# -------------------------------------------------------------------
# Build a plot for all the runs combined
`grep "preferred-dir" $dataDir/*.dat | awk '{print \$3;}' | tr '\n' '  ' > $dirDataFile`;
my $outputEPSFile = $outputEPSPrefix."-combined.eps";
createRosePlot( $dirDataFile, $outputEPSFile, "2.5" );


# ===================================================================
sub createRosePlot
{
    my ($dirDataFile, $outputEPSFile, $scale) = @_;

    # ---------------------------------------------------------------
    # Create the R file
    my $rInputFile = "/tmp/tmp-rose.r";
    my $tmpEPSFile = "/tmp/tmp-rose.eps";
    createRInput( $rInputFile, $dirDataFile, $tmpEPSFile, $scale );

    # ---------------------------------------------------------------
    # Run R
    my $rOutputFile = "$rInputFile.out";
    `R --no-save < $rInputFile > $rOutputFile 2>&1`;

    # ---------------------------------------------------------------
    # Fix the EPS file and create a PDF
    my $tmpFixedEPSFile = "/tmp/tmp-rose-fixed.eps";
    `./replace-fonts-in-eps.pl $tmpEPSFile $tmpFixedEPSFile`;
    `/usr/bin/epstool --copy --bbox  $tmpFixedEPSFile $outputEPSFile`;
    `epstopdf $outputEPSFile`;
}


# ===================================================================
sub createRInput
{
    my ($rInputFile, $dirDataFile, $epsFile, $scale) = @_;

    # ---------------------------------------------------------------
    my $roseColor = "orange";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('circular')\n";

    # ---------------------------------------------------------------
    # Send the plot to an EPS file
    print INPUT "postscript( file=\"$epsFile\", height=5, width=5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";

    # ---------------------------------------------------------------
    # Create a rose plot
    print INPUT "dirs <- scan(\"$dirDataFile\")\n";
    print INPUT "degreedirs <- (dirs*180 + 360)%%360\n";
    print INPUT "circdata <- circular(degreedirs,units=\"degrees\",rotation=\"clock\",zero=pi/2)\n";
#    print INPUT "rose.diag(circdata,bins=36,prop=$scale,col=\"$roseColor\",radii.scale=\"linear\")\n";
    print INPUT "rose.diag(circdata,bins=36,prop=$scale,col=\"$roseColor\")\n";

    # ---------------------------------------------------------------
    print INPUT "dev.off()\n";

    close(INPUT);

}
