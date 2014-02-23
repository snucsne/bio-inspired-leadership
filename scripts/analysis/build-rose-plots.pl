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
createRosePlot( $dirDataFile, $outputEPSFile, "2.7" );


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
    my $pdfFile = $outputEPSFile;
    $pdfFile =~ s/eps$/pdf/;
    my $title = $outputEPSFile;
    $title =~ s/.*\/(.*)\.eps$/\1/;
    `exiftool -Title="$title" -Author="Brent E. Eskridge" $pdfFile`;
    # See the link below for more exiftool help with PDFs
    # http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/PDF.html
    # See the link below for more exiftool help with PostScript
    # http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/PostScript.html
}


# ===================================================================
sub createRInput
{
    my ($rInputFile, $dirDataFile, $epsFile, $scale) = @_;

    # ---------------------------------------------------------------
    my $roseColor = "#999933";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('circular')\n";
    print INPUT "library(ggplot2)\n";

    # ---------------------------------------------------------------
    # Send the plot to an EPS file
    print INPUT "postscript( file=\"$epsFile\", height=5, width=5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";

    # ---------------------------------------------------------------
    # Create a rose plot
    print INPUT "dirs <- scan(\"$dirDataFile\")\n";
    print INPUT "degreedirs <- (dirs*180 + 360)%%360\n";
    print INPUT "circdata <- circular(degreedirs,units=\"degrees\",rotation=\"clock\",zero=pi/2)\n";
    print INPUT "rose.diag(circdata,bins=36,prop=$scale,col=\"$roseColor\",radii.scale=\"linear\")\n";

#    print INPUT "rose.diag(circdata,bins=72,prop=$scale,col=\"$roseColor\",lwd.border=0.1,)\n";
#    print INPUT "ggplot(data.frame(circdata), aes(x=factor(circdata),fill=factor(circdata))) +\n",
#            "    geom_bar(width=1) +\n",
#            "    coord_polar()\n";


    # ---------------------------------------------------------------
    print INPUT "dev.off()\n";

    close(INPUT);

}


#dirs <- scan("/tmp/tmp-directions.txt")
#degreedirs <- (dirs*180 + 360)%%360
#circdata <- circular(degreedirs,units="degrees",rotation="clock",zero=pi/2)
#circdatabins <-findInterval(circdata,(seq(0,90)/90*360))
#cdf <- data.frame(counts=circdata,bins=circdatabins)
#
#ggplot(mapping= aes(x=circdata,colour="#9C0A2E",fill="#AA0000")) +
#    geom_bar(fill="#9C0A2E",colour="black",width=0.25)+
#        scale_x_continuous(limits=c(0,360),breaks=seq(0,360,90))+
#        coord_polar(start=0)
#
#ggplot(cdf, aes(x=bins)) +
#        geom_bar(fill="#9C0A2E",colour="black",width=0.25)+
#        scale_x_continuous(limits=c(0,64))+
#        coord_polar(start=0)
# 
#
#
#       
#dev.off()
#        stat_bin() +
#
#        stat_bin(binwidth=0.5,breaks=((0:32-0.5)/32*360)) +
#
#        stat_bin(binwidth=0.5,breaks=(seq(0,90)/90*360)) +
#

