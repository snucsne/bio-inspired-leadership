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
        ."figures/spatial-hidden-variables-sim-indcount-"
        .$indCount
        ."-usenn-";

my $trueFig = $figurePrefix."true-gephi.svg";
my $falseFig = $figurePrefix."false-gephi.svg";


# -------------------------------------------------------------------
# Copy the figures
`inkscape --export-png=/tmp/gephi-true.png --export-background="white" --export-area-drawing $trueFig`;
`inkscape --export-png=/tmp/gephi-false.png --export-background="white" --export-area-drawing $falseFig`;


# -------------------------------------------------------------------
# Copy the latex file
`sed "s/\#\#TITLE\#\#/$title/" combined-gephi-figs.tex > /tmp/combined-gephi-figs.tex`;

# -------------------------------------------------------------------
# Build the output file name
my $outputFigure = $figurePrefix."combined-gephi.pdf";

# -------------------------------------------------------------------
# Use LaTeX to combine them
chdir( "/tmp/" );

`pdflatex combined-gephi-figs.tex 2>&1`;
#`dvips -o /tmp/combined-gephi-figs.ps /tmp/combined-gephi-figs.dvi 2>&1`;
#`ps2eps -f /tmp/combined-gephi-figs.ps 2>&1`;
#`eps2eps /tmp/combined-gephi-figs.eps $outputFigure 2>&1`;
#`epstopdf $outputFigure 2>&1`;
`cp combined-gephi-figs.pdf $outputFigure`;

