#!/usr/bin/perl
use strict;
use Cwd;

# -------------------------------------------------------------------
# Get the base directory
my $baseDir = shift( @ARGV );
unless( $baseDir =~ /\/$/ )
{
    $baseDir = "$baseDir/";
}

# Get the title
my $title = shift( @ARGV );

# Get the individual count
my $indCount = shift( @ARGV );
$indCount = sprintf( "%03d", $indCount );


# -------------------------------------------------------------------
# Get the current directory
my $cwd = getcwd();


# -------------------------------------------------------------------
# Build the figure names
my $figurePrefix = $baseDir
        ."figures/indcount-"
        .$indCount
        ."-usenn-";


my $movementCountsTrueFig = $figurePrefix."true-movement-counts.eps";
my $eigVsSuccessTrueFig = $figurePrefix."true-eig-vs-success.eps";
#my $eigVsMimicsTrueFig = $figurePrefix."true-eig-vs-mimics.eps";
my $mimicsVsSuccessTrueFig = $figurePrefix."true-mimics-vs-success.eps";

my $movementCountsFalseFig = $figurePrefix."false-movement-counts.eps";
my $eigVsSuccessFalseFig = $figurePrefix."false-eig-vs-success.eps";
#my $eigVsMimicsFalseFig = $figurePrefix."false-eig-vs-mimics.eps";
my $mimicsVsSuccessFalseFig = $figurePrefix."false-mimics-vs-success.eps";

# -------------------------------------------------------------------
# Copy the figures
`cp $movementCountsTrueFig /tmp/spatial-hidden-vars-movement-counts-true.eps`;
`cp $eigVsSuccessTrueFig /tmp/spatial-hidden-vars-success-true.eps`;
#`cp $eigVsMimicsTrueFig /tmp/spatial-hidden-vars-mimicing-neighbors-true.eps`;
`cp $mimicsVsSuccessTrueFig /tmp/spatial-hidden-vars-success-mimic-true.eps`;

`cp $movementCountsFalseFig /tmp/spatial-hidden-vars-movement-counts-false.eps`;
`cp $eigVsSuccessFalseFig /tmp/spatial-hidden-vars-success-false.eps`;
#`cp $eigVsMimicsFalseFig /tmp/spatial-hidden-vars-mimicing-neighbors-false.eps`;
`cp $mimicsVsSuccessFalseFig /tmp/spatial-hidden-vars-success-mimic-false.eps`;



# -------------------------------------------------------------------
# Copy the latex file
`sed "s/\#\#TITLE\#\#/$title/" combined-figs.tex > /tmp/combined-figs.tex`;

# -------------------------------------------------------------------
# Build the output file name
my $outputFigure = $figurePrefix."combined.eps";
print "outputfigure=[$outputFigure]\n";

# -------------------------------------------------------------------
# Use LaTeX to combine them
chdir( "/tmp/" );

`latex combined-figs.tex 2>&1`;
`dvips -o /tmp/combined-figs.ps /tmp/combined-figs.dvi 2>&1`;
`ps2eps -f /tmp/combined-figs.ps 2>&1`;
`eps2eps /tmp/combined-figs.eps $outputFigure 2>&1`;
`epstopdf $outputFigure 2>&1`;


