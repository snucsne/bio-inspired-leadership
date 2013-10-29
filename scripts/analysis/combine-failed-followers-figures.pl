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

print "Starting...\n";

# -------------------------------------------------------------------
# Build the figure names
my $figurePrefix = $baseDir
        ."figures/indcount-"
        .$indCount
        ."-usenn-";

my $trueFig = $figurePrefix."true-mimics-vs-failedfollowers.eps";
my $falseFig = $figurePrefix."false-mimics-vs-failedfollowers.eps";

# -------------------------------------------------------------------
# Copy the figures
`cp $trueFig /tmp/true.eps`;
`cp $falseFig /tmp/false.eps`;

print "Copied...\n";

# -------------------------------------------------------------------
# Copy the latex file
`sed "s/\#\#TITLE\#\#/$title/" combined-failedfollowers-figs.tex > /tmp/combined-failedfollowers-figs.tex`;

print "Sed'ed...\n";

# -------------------------------------------------------------------
# Build the output file name
my $outputFigure = $figurePrefix."failedfollowers-combined.eps";
print "outputfigure=[$outputFigure]\n";

# -------------------------------------------------------------------
# Use LaTeX to combine them
chdir( "/tmp/" );

`latex combined-failedfollowers-figs.tex 2>&1`;
`dvips -o /tmp/combined-failedfollowers-figs.ps /tmp/combined-failedfollowers-figs.dvi 2>&1`;
`ps2eps -f /tmp/combined-failedfollowers-figs.ps 2>&1`;
`eps2eps /tmp/combined-failedfollowers-figs.eps $outputFigure 2>&1`;
`epstopdf $outputFigure 2>&1`;

