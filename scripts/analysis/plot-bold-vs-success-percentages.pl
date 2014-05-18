#!/usr/bin/perl
use strict;
use List::Util qw(sum);

# -------------------------------------------------------------------
# Get the root data directory
my $rootDataDir = shift( @ARGV );

# Ensure that it ends with a "/"
unless( $rootDataDir =~ m/\/$/ )
{
    $rootDataDir .= "/";
}

# Get the number of individuals in the group
$rootDataDir =~ m/indcount-(\d+)/;
my $groupSize = $1;

# Get the results output file name
my $resultsFile = shift( @ARGV );

# Get the eps output file name
my $epsFile = shift( @ARGV );

# -------------------------------------------------------------------
# Get all the data directories in the data directory
opendir( DIR, $rootDataDir ) or die "Unable to open root data directory [$rootDataDir]: $!\n";
my @dataDirs = sort( grep { /boldcount/ } readdir( DIR ) );
closedir( DIR );

# -------------------------------------------------------------------
# Grab the success percentages for each boldcount
my %successPercentages;
foreach my $dataDir (@dataDirs)
{
    # Get the number of bold individuals
    $dataDir =~ m/boldcount-(\d+)/;
    my $boldCount = $1;

    # Open the directory and get all the data files
    opendir( DIR, "$rootDataDir$dataDir" ) or die "Unable to open data file directory [$dataDir]: $!\n";
    my @dataFiles = sort( grep { /spatial-hidden-var/ } readdir( DIR ) );
    closedir( DIR );

    # Process each file
    my @successPercentages;
    foreach my $dataFile (@dataFiles)
    {
        my $success = `grep total-leadership-success $rootDataDir$dataDir/$dataFile | awk '{print \$3;}' | tr -d '\n'`;
        push( @successPercentages, $success );
    }

    # Store them
    $successPercentages{$boldCount} = \@successPercentages;
}

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/bold-vs-success.r";
my $rOutputFile = $rInputFile.".out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

print INPUT "library('Matching')\n";
print INPUT "library('Hmisc')\n";

# Build a standard error function
print INPUT "stderr <- function(x) sd(x)/sqrt(length(x))\n\n";

# -------------------------------------------------------------------
# Add the individual boldcount data
my %ids;
my @means;
my @sds;
my @boldPercentages;
foreach my $boldCount (sort (keys %successPercentages) )
{
    # Build the ID
    my $id = "bold".$boldCount."success";
    $ids{$boldCount} = $id;

    # Add the success percentages
    print INPUT "$id <- scan()\n";
    print INPUT join( " ", @{$successPercentages{$boldCount}} ),"\n\n";

    # Save the mean and standard deviation
    push( @means, "mean($id)" );
    push( @sds, "sd($id)" );

    # Calculate the bold percentage for our x axis label
    push( @boldPercentages, ($boldCount / $groupSize) );
}

# -------------------------------------------------------------------
# Add the aggregate data
print INPUT "meanvalues = c(",join( ", ", @means ),")\n";
print INPUT "sdvalues = c(",join( ", ", @sds ),")\n";
print INPUT "boldpercentages = c(",join( ", ", @boldPercentages ),")\n";
print INPUT "\n";

# -------------------------------------------------------------------
# Plot it
my $yMin = 0.0;
my $yMax = 1.0;
my $yDiff = 0.25;
my $xMin = 0.0;
my $xMax = 1.0;
my $xDiff = 0.25;
my $fgColor = "#117733";

print INPUT "postscript( file=\"$epsFile\", height=4.5, width=5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";

print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
print INPUT "par(mgp=c(2.5,0.75,0))\n";

print INPUT "plot( boldpercentages, meanvalues, type=\"o\", yaxt='n', xaxt='n',\n",
            "    ylab=\"\", xlab=\"Bold Percentage\",\n",
            "    ylim=c($yMin,$yMax), xlim=c($xMin,$xMax), col=\"$fgColor\",\n",
            "    lwd=2, pch=1 )\n";
print INPUT "par(fg=\"$fgColor\")\n";
print INPUT "errbar( boldpercentages, meanvalues, \n",
            "    (meanvalues+sdvalues), (meanvalues-sdvalues), add=TRUE,\n",
            "    lwd=1.5, yaxt='n', pch=1, col=\"$fgColor\" )\n";
print INPUT "par(fg=\"black\")\n";

print INPUT "title(ylab=\"Leadership Success\", mgp = c(3.25, 1, 0), cex=1.5 )\n";

print INPUT "abline( h=max(meanvalues), col=\"#555555\", lwd=1.5, lty=2 )\n";
print INPUT "maxx <- match( max(meanvalues), meanvalues )\n";
print INPUT "maxx\n";
print INPUT "boldpercentages[maxx]\n";
print INPUT "abline( v=boldpercentages[maxx], col=\"#555555\", lwd=1.5, lty=2 )\n";

print INPUT "boldaxispercentages <- c(seq($xMin, $xMax, $xDiff))\n";
print INPUT "boldpercentlabels <- sprintf( \"%3d%%\", boldaxispercentages*100)\n";
print INPUT "axis( 1, las=1, at=boldaxispercentages, labels=boldpercentlabels)\n";

print INPUT "successpercentages <- c(seq($yMin, $yMax, $yDiff))\n";
print INPUT "successpercentlabels <- sprintf( \"%3d%%\", successpercentages*100)\n";
print INPUT "axis( 2, las=2, at=successpercentages, labels=successpercentlabels)\n";
print INPUT "axis( 4, las=2, at=successpercentages, labels=successpercentlabels)\n";

print INPUT "dev.off()\n";

# -------------------------------------------------------------------
# Start sending output to the results file
print INPUT "sink(\"$resultsFile\")\n";

print INPUT "cat(\"# -------------------------------------\\n\")\n";
print INPUT "cat(\"# rootDataDir = [$rootDataDir]\\n\")\n";
print INPUT "cat(\"# groupSize =   [$groupSize]\\n\")\n";
print INPUT "cat(\"# resultsFile = [$resultsFile]\\n\")\n";
print INPUT "cat(\"# epsFile =     [$epsFile]\\n\")\n";
print INPUT "cat(\"# -------------------------------------\\n\\n\")\n";


# -------------------------------------------------------------------
# Dump the summary statistics
foreach my $boldCount (sort (keys %successPercentages) )
{
    # Get the ID
    my $id = $ids{$boldCount};
    my $boldPercentage = sprintf( "%04.2f", ($boldCount/$groupSize) );
    print INPUT "cat(\"# =====================================\\n\")\n";
    print INPUT "cat(\"# Bold count [$boldCount]  percentage [$boldPercentage]\\n\")\n";
    print INPUT buildDescriptionString( "summary.bold-percentage-$boldCount",
                    $id );
    print INPUT "cat(\"\\n\")\n";
}

# -------------------------------------------------------------------
# Perform the significance tests
my @boldCounts = (sort (keys %successPercentages) );
for( my $i = 0; $i <= ($#boldCounts - 1); $i++ )
{
    my $firstBoldCount = $boldCounts[$i];
    for( my $j = $i + 1; $j <= ($#boldCounts); $j++ )
    {
        my $secondBoldCount = $boldCounts[$j];
        print INPUT "cat(\"# =====================================\\n\")\n";
        print INPUT "cat(\"# KS Test: [",
                $firstBoldCount,
                "] vs [",
                $secondBoldCount,
                "]\\n\")\n";
        printKSTestText( *INPUT,
                "success.",
                $ids{$firstBoldCount},
                $firstBoldCount,
                $ids{$secondBoldCount},
                $secondBoldCount );
    }
}

# -------------------------------------------------------------------
# Stop sending output to the results file
print INPUT "sink()\n";

# -------------------------------------------------------------------
# Close the input file
close( INPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# -------------------------------------------------------------------
# Convert to PDF
my $pdfFile = $epsFile;
$pdfFile =~ s/eps$/pdf/;
my $tmpEPSFile = "/tmp/tmp.eps";
my $fixedTmpEPSFile = "/tmp/fixed-tmp.eps";
`cp $epsFile $tmpEPSFile`;
`./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
`/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
`epstopdf $epsFile`;
my $title = `basename $pdfFile`;
`exiftool -Title="$title" -Author="Brent E. Eskridge" $pdfFile`;




# ===================================================================
sub buildDescriptionString
{
    my ($dataPrefix, $id) = @_;

    my $descStr = "cat(\"$dataPrefix.mean =     \", format( mean($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.median =   \", format( median($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.max =      \", format( max($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.min =      \", format( min($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.sd =       \", format( sd($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.se =       \", format( stderr($id) ), \"\\n\")\n";
    $descStr .=   "cat(\"$dataPrefix.length =   \", format( length($id) ), \"\\n\")\n";

    return $descStr;
}


# ===================================================================
sub printKSTestText
{
    local *INPUT = shift;
    my ($keyStr, $firstID, $firstKey, $secondID, $secondKey) = @_;
#return;
            print INPUT "ks <- ks.boot( ",
                    $firstID,
                    ",",
                    $secondID,
                    " )\n";
            print INPUT "cat(\"ks.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value =       \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
            print INPUT "cat(\"ks.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".naive-pvalue =  \", format(ks\$ks\$p.value), \"\\n\")\n";
            print INPUT "cat(\"ks.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".statistic =     \", format(ks\$ks\$statistic), \"\\n\")\n";
            print INPUT "ttest <- t.test( ",
                    $firstID,
                    ",",
                    $secondID,
                    " )\n";
            print INPUT "cat(\"t-test.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value =   \", format(ttest\$p.value), \"\\n\")\n";
            print INPUT "cat(\"t-test.",
                    $keyStr,
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".statistic = \", format(ttest\$statistic), \"\\n\")\n";
            print INPUT "cat(\"\\n\")\n";

}

