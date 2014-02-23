#!/usr/bin/perl
use strict;
use Cwd;

# ===================================================================
# Get the base directory
my $baseDir = shift( @ARGV );
unless( $baseDir =~ /\/$/ )
{
    $baseDir = "$baseDir/";
}

# ===================================================================
# Get the current directory
my $cwd = getcwd();

# ===================================================================
# Handy variables
my @decayTypes = ( "constant", "linear", "exponential", "momentum" );
my @combinedPDFs;
my $baseTexFile = "decay-plot-comparison.tex";
my $tmpTexFile = "/tmp/$baseTexFile";
my $tmpDVIFile = "/tmp/decay-plot-comparison.dvi";
my $tmpPSFile = "/tmp/decay-plot-comparison.ps";
my $tmpEPSFile = "/tmp/decay-plot-comparison.eps";

my $tmpConstantFigure = "/tmp/constant-figure.eps";
my $tmpLinearFigure = "/tmp/linear-figure.eps";
my $tmpExponentialFigure = "/tmp/exponential-figure.eps";
my $tmpMomentumFigure = "/tmp/momentum-figure.eps";


# ===================================================================
# Get all the decay multipliers
my $refDir = $baseDir."/".$decayTypes[0];
opendir( DIR, $refDir ) or die "Unable to open decay directory [$refDir]: $!\n";
my @decayMultDirs = sort( grep { /decay-mult/ } readdir( DIR ) );
closedir( DIR );

my @decayMultipliers;
foreach my $decayMultDir (@decayMultDirs)
{
    $decayMultDir =~ /decay-mult-(\d+)/;
    push( @decayMultipliers, $1 );
}
print "Found ",($#decayMultipliers + 1), " decay multipliers...\n";

# ===================================================================
# Get all the personality values
$refDir = $refDir."/".$decayMultDirs[0];
opendir( DIR, $refDir ) or die "Unable to open decay multiplier directory [$refDir]: $!\n";
my @personalityDirs = sort( grep { /personality/ } readdir( DIR ) );
closedir( DIR );

my @personalities;
foreach my $personalityDir (@personalityDirs)
{
    $personalityDir =~ /personality-(.+)/;
    push( @personalities, $1 );
}
print "Found ",($#personalities + 1), " personalities...\n";

# ===================================================================
# Get all the individual counts
$refDir = $refDir."/".$personalityDirs[0];
opendir( DIR, $refDir ) or die "Unable to open personality directory [$refDir]: $!\n";
my @indCountDirs = sort( grep { /indcount/ } readdir( DIR ) );
closedir( DIR );

my @indCounts;
foreach my $indCountDir (@indCountDirs)
{
    $indCountDir =~ /indcount-(.+)/;
    push( @indCounts, $1 );
}
print "Found ",($#indCounts + 1), " ind counts...\n";

# ===================================================================
# Build the combined success comparisons
foreach my $decayMultiplier (@decayMultipliers)
{
    # Build the title
    my $title = "Success: t=[".$decayMultiplier.' \$\\\\\\times N\$'."]";
    my $filename = $baseDir."figures/success-by-personality-$decayMultiplier.eps";

    # Replace the title in the LaTeX file
    `sed "s/\#\#TITLE\#\#/$title/" $baseTexFile > $tmpTexFile`;
            
    # Copy the appropriate figures
    `cp $baseDir/constant/decay-mult-$decayMultiplier/figures/success-comparison-personality-by-personality-combined.eps $tmpConstantFigure`;
    `cp $baseDir/linear/decay-mult-$decayMultiplier/figures/success-comparison-personality-by-personality-combined.eps $tmpLinearFigure`;
    `cp $baseDir/exponential/decay-mult-$decayMultiplier/figures/success-comparison-personality-by-personality-combined.eps $tmpExponentialFigure`;
    `cp $baseDir/momentum/decay-mult-$decayMultiplier/figures/success-comparison-personality-by-personality-combined.eps $tmpMomentumFigure`;

    # Build the combined PDF
    chdir( "/tmp/" );
    `latex $tmpTexFile 2>&1`;
    `dvips -o $tmpPSFile $tmpDVIFile 2>&1`;
    `ps2eps -f $tmpPSFile 2>&1`;
    `/usr/bin/epstool --copy --bbox $tmpEPSFile $filename 2>&1`;
    `epstopdf $filename`;
    $filename =~ s/eps/pdf/;
    push( @combinedPDFs, $filename );
    print( "\tBuilt [$filename]\n" );
    chdir( $cwd );
}

# ===================================================================
# Build the representative personality histories
foreach my $decayMultiplier (@decayMultipliers)
{
    foreach my $personality (@personalities)
    {
        foreach my $indCount (@indCounts)
        {
            # Build the title
            my $title = "Personality History: t=[".$decayMultiplier.' \$\\\\\\times N\$] \$p\$='."[$personality] ".'\$N\$'."=[$indCount]";
            my $filename = $baseDir."figures/personality-history-$decayMultiplier-$personality-$indCount.eps";

#            print "Processing [$title]\n";

            # Replace the title in the LaTeX file
            `sed "s/\#\#TITLE\#\#/$title/" $baseTexFile > $tmpTexFile`;
            
            # Copy the appropriate figures
            `cp $baseDir/constant/decay-mult-$decayMultiplier/personality-$personality/figures/indcount-$indCount-personality-run-01-history.eps $tmpConstantFigure`;
            `cp $baseDir/linear/decay-mult-$decayMultiplier/personality-$personality/figures/indcount-$indCount-personality-run-01-history.eps $tmpLinearFigure`;
            `cp $baseDir/exponential/decay-mult-$decayMultiplier/personality-$personality/figures/indcount-$indCount-personality-run-01-history.eps $tmpExponentialFigure`;
            `cp $baseDir/momentum/decay-mult-$decayMultiplier/personality-$personality/figures/indcount-$indCount-personality-run-01-history.eps $tmpMomentumFigure`;

            # Build the combined PDF
            chdir( "/tmp/" );
            `latex $tmpTexFile 2>&1`;
            `dvips -o $tmpPSFile $tmpDVIFile 2>&1`;
            `ps2eps -f $tmpPSFile 2>&1`;
            `/usr/bin/epstool --copy --bbox $tmpEPSFile $filename 2>&1`;
            `epstopdf $filename`;
            $filename =~ s/eps/pdf/;
            push( @combinedPDFs, $filename );
            print( "\tBuilt [$filename]\n" );
            chdir( $cwd );
        }
    }
}

# ===================================================================
# Make one giant PDF
my $allPDFsStr = join( " ", @combinedPDFs );
my $allCombinedPDF = $baseDir."figures/all-decay-figures-combined.pdf";
`pdftk $allPDFsStr cat output $allCombinedPDF`;

