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

my $successTrueFig = $figurePrefix."true-success.eps";
my $successNNTrueFig = $figurePrefix."true-success-mimic.eps";
my $attemptsTrueFig = $figurePrefix."true-attempts.eps";
my $movementCountsTrueFig = $figurePrefix."true-movement-counts.eps";
my $successFollowersTrueFig = $figurePrefix."true-success-followers.eps";
my $failedFollowersTrueFig = $figurePrefix."true-failed-followers.eps";
my $mimicingNeighborsTrueFig = $figurePrefix."true-mimicing-neighbors.eps";

my $successFalseFig = $figurePrefix."false-success.eps";
my $successNNFalseFig = $figurePrefix."false-success-mimic.eps";
my $attemptsFalseFig = $figurePrefix."false-attempts.eps";
my $movementCountsFalseFig = $figurePrefix."false-movement-counts.eps";
my $successFollowersFalseFig = $figurePrefix."false-success-followers.eps";
my $failedFollowersFalseFig = $figurePrefix."false-failed-followers.eps";
my $mimicingNeighborsFalseFig = $figurePrefix."false-mimicing-neighbors.eps";

# -------------------------------------------------------------------
# Copy the figures
`cp $successTrueFig /tmp/spatial-hidden-vars-success-true.eps`;
`cp $successNNTrueFig /tmp/spatial-hidden-vars-success-mimic-true.eps`;
`cp $attemptsTrueFig /tmp/spatial-hidden-vars-attempts-true.eps`;
`cp $movementCountsTrueFig /tmp/spatial-hidden-vars-movement-counts-true.eps`;
`cp $successFollowersTrueFig /tmp/spatial-hidden-vars-success-followers-true.eps`;
`cp $failedFollowersTrueFig /tmp/spatial-hidden-vars-failed-followers-true.eps`;
`cp $mimicingNeighborsTrueFig /tmp/spatial-hidden-vars-mimicing-neighbors-true.eps`;


`cp $successFalseFig /tmp/spatial-hidden-vars-success-false.eps`;
`cp $successNNFalseFig /tmp/spatial-hidden-vars-success-mimic-false.eps`;
`cp $attemptsFalseFig /tmp/spatial-hidden-vars-attempts-false.eps`;
`cp $movementCountsFalseFig /tmp/spatial-hidden-vars-movement-counts-false.eps`;
`cp $successFollowersFalseFig /tmp/spatial-hidden-vars-success-followers-false.eps`;
`cp $failedFollowersFalseFig /tmp/spatial-hidden-vars-failed-followers-false.eps`;
`cp $mimicingNeighborsFalseFig /tmp/spatial-hidden-vars-mimicing-neighbors-false.eps`;


# -------------------------------------------------------------------
# Copy the latex file
`sed "s/\#\#TITLE\#\#/$title/" combined-figs.tex > /tmp/combined-figs.tex`;

# -------------------------------------------------------------------
# Build the output file name
my $outputFigure = $figurePrefix."combined.eps";

# -------------------------------------------------------------------
# Use LaTeX to combine them
chdir( "/tmp/" );

`latex combined-figs.tex 2>&1`;
`dvips -o /tmp/combined-figs.ps /tmp/combined-figs.dvi 2>&1`;
`ps2eps -f /tmp/combined-figs.ps 2>&1`;
`eps2eps /tmp/combined-figs.eps $outputFigure 2>&1`;
`epstopdf $outputFigure 2>&1`;


