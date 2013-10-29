#!/usr/bin/perl
use strict;
use POSIX;

# ===================================================================
# Get the data directory
my $dataDir = shift( @ARGV );

# Get the output data file
my $outputDataFile = shift( @ARGV );

# Get the output figure file
my $outputFigFile = shift( @ARGV );

my @types = ("metric", "topo");
my %colors = ( "metric" => "#EE9500",
               "topo" => "#3333FF" );

# ===================================================================
# Get the metric and topological directories
opendir( DATADIR, $dataDir ) or die "Unable to open data dir [$dataDir]: $!\n";
my @metricDirs = sort( grep { /metric$/ } readdir ( DATADIR ) );
rewinddir( DATADIR );
my @topoDirs = sort( grep { /topo$/ } readdir ( DATADIR ) );
closedir( DATADIR );


# ===================================================================
# Get the modularities for each group size
my %metricData;
foreach my $metricDir (@metricDirs)
{
    # Get the individual count
    $metricDir =~ m/indcount-(\d+)/;
    
    # Get the modularities
    $metricData{$1} = `cat $dataDir$metricDir/modularities.txt`;
}
my %topoData;
foreach my $topoDir (@topoDirs)
{
    # Get the individual count
    $topoDir =~ m/indcount-(\d+)/;
    
    # Get the modularities
    $topoData{$1} = `cat $dataDir$topoDir/modularities.txt`;
}


# ===================================================================
# Process it in R
my $rInputFile = "/tmp/modularity-analysis.r";
my $rOutputFile = "/tmp/modularity-analysis.r.out";
my $rSpacer = "cat(\"# =====================================\\n\")\n";

# -------------------------------------------------------------------
# Create the file
open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
print INPUT "library('Matching')\n";
print INPUT "library('igraph')\n\n";

# -------------------------------------------------------------------
# Add the data
my $count = 0;
foreach my $indCount ( sort keys %metricData )
{
    print INPUT "# Metric indcount=[$indCount]\n";
    print INPUT "metric$indCount <- scan()\n";
    print INPUT $metricData{$indCount},"\n\n";

    print INPUT "# Topological indcount=[$indCount]\n";
    print INPUT "topo$indCount <- scan()\n";
    print INPUT $topoData{$indCount},"\n\n";

    print INPUT "# Difference (metric - topo) indcount=[$indCount]\n";
    print INPUT "diff$indCount <- metric$indCount - topo$indCount\n";
#    print INPUT "diff$indCount\n";

    $count += 2;
}

# -------------------------------------------------------------------
# Build a description
my $date = localtime;
my $description = "# Date: $date\\n"
                 ."# Data directory: [$dataDir]\\n"
                 ."# Output data file: [$outputDataFile]\\n"
                 ."# Output figure file: [$outputFigFile]\\n";


# -------------------------------------------------------------------
# Start sending output to the output data file
print INPUT "sigs <- c()\n";
print INPUT "sink(\"$outputDataFile\")\n";
print INPUT $rSpacer;
print INPUT "cat(\"$description\")\n";
print INPUT $rSpacer;

# -------------------------------------------------------------------
# Dump summaries of the data
foreach my $indCount ( sort keys %metricData )
{
    # Metric summary
    print INPUT "cat(\"# Metric summary: indcount=[$indCount]\\n\")\n";
    print INPUT "cat(\"summary.$indCount.metric.mean = \",
            format( mean(metric$indCount) ), \"\\n\")\n";
    print INPUT "cat(\"summary.$indCount.metric.sd = \",
            format( sd(metric$indCount) ), \"\\n\")\n";
    print INPUT "cat(\"summary.$indCount.metric.var = \",
            format( var(metric$indCount) ), \"\\n\\n\")\n";

    # Topological summary
    print INPUT "cat(\"# Topological summary: indcount=[$indCount]\\n\")\n";
    print INPUT "cat(\"summary.$indCount.topo.mean = \",
            format( mean(topo$indCount) ), \"\\n\")\n";
    print INPUT "cat(\"summary.$indCount.topo.sd = \",
            format( sd(topo$indCount) ), \"\\n\")\n";
    print INPUT "cat(\"summary.$indCount.topo.var = \",
            format( var(topo$indCount) ), \"\\n\\n\")\n";

    # Difference summary
    print INPUT "cat(\"# Difference summary (metric - topo): indcount=[$indCount]\\n\")\n";
    print INPUT "cat(\"summary.$indCount.diff.mean = \",
            format( mean(diff$indCount) ), \"\\n\")\n";
    print INPUT "cat(\"summary.$indCount.diff.sd = \",
            format( sd(diff$indCount) ), \"\\n\")\n";
    print INPUT "cat(\"summary.$indCount.diff.var = \",
            format( var(diff$indCount) ), \"\\n\\n\")\n";

    # KS Test
    print INPUT "cat(\"# KS: metric$indCount vs topo$indCount\\n\")\n";
    print INPUT "ks$indCount <- ks.boot( metric$indCount, topo$indCount )\n";
    print INPUT "cat(\"ks.$indCount.p-value = \", format( ks$indCount\$ks.boot.pvalue), \"\\n\")\n";
    print INPUT "cat(\"ks.$indCount.naive-pvalue = \", format(ks$indCount\$ks\$p.value), \"\\n\")\n";
    print INPUT "cat(\"ks.$indCount.statistic = \", format(ks$indCount\$ks\$statistic), \"\\n\")\n";
    print INPUT "cat(\"\\n\\n\")\n";

    print INPUT "sigs <- c( sigs, ks$indCount\$ks.boot.pvalue );\n";
}
print INPUT "sink()\n";

# -------------------------------------------------------------------
# Build the significance identifiers
print INPUT "buildsigtext <- function(x) {\n",
            "  if( x <= 0.01 ) {\n",
            "    result <- \"**\"\n",
            "  } else if (x <= 0.05) {\n",
            "    result <- \"*\"\n",
            "  } else {\n",
            "    result <- \" \"\n",
            "  }\n",
            "  return(result)\n",
            "}\n";
print INPUT "sigtext <- lapply( sigs, buildsigtext )\n";

# -------------------------------------------------------------------
# Generate the plots
my $yMin = 0;
my $yMax = 0.6;
my $diff = ( ($yMax - $yMin) / 4);
my $xLabelPos = -0.1 * ($yMax - $yMin) + $yMin;
my $tmpEPSFile = "/tmp/modularities.eps";
my $tmpFixedEPSFile = "/tmp/modularities-fixed.eps";
print INPUT "postscript( file = \"$tmpEPSFile\", height=5, width=10, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
print INPUT "par(mar=c(8.5,4,3,3)+0.1)\n";

my $allLabels;
my $positions;
my $tickPositions;
my $i = 0;
foreach my $indCount ( sort keys %metricData )
{
    foreach my $type ( sort( keys %colors ) )
    {
        # Precalculate the positions
        my $offset = 0.5 * floor( $i / 2 );
        my $stripLoc = $i + 1 + $offset;
        my $boxLoc = $i + 1.4 + $offset;

        my $misc = "ylab=\"Modularity\", xlim=c(0.75,".($count+floor($count/2)*0.5+0.25)."), ylim=c($yMin, $yMax), yaxt='n'";
        if( $i > 0 )
        {
            $misc = "add=TRUE, xaxt='n', yaxt='n', at=($stripLoc)";
        }

        # Print a jittered stripchart
        print INPUT "stripchart( $type$indCount, method=\"jitter\",\n",
                "    pch=",(1+($i%2)),", las=2, vertical=TRUE, xaxt='n',\n",
                "    col=(c(\"$colors{$type}\")), $misc )\n";

        # Print the associated boxplot
        print INPUT "boxplot( $type$indCount, add=TRUE, at=($boxLoc),\n",
                "    border=(c(\"$colors{$type}\")), boxwex=0.5,\n",
                "    outline=FALSE, xaxt='n', yaxt='n' )\n\n";

        if( $i % 2 == 0 )
        {
            my $size = $indCount;
            $size =~ s/^0+//;
            $allLabels .= "\" Size: $size \", ";
            $positions .= ($stripLoc + 0.7).", ";
            $tickPositions .= ($stripLoc + 0.7).", ";
        }

        $i++;
    }
}

# Remove the comma and extra space
chop( $allLabels );
chop( $allLabels );
chop( $positions );
chop( $positions );
chop( $tickPositions );
chop( $tickPositions );

print INPUT "plotText <- c($allLabels)\n";
print INPUT "combinedText <- paste( plotText, sigtext )\n";


print INPUT "abline( h=c(seq($yMin, $yMax, $diff)), lty=2, col=c(\"#CCCCCC\") )\n";
print INPUT "text( c($tickPositions), $xLabelPos, col=c(\"#000000\"), labels=combinedText, srt=-40, xpd=NA, adj=0 )\n";
print INPUT "axis( 1, at=c($tickPositions), labels=c(rep(\" \", ",($count/2),")) )\n";
print INPUT "axis( 2, las=2, at=c(seq($yMin, $yMax, $diff)) )\n";
print INPUT "axis( 4, las=2, at=c(seq($yMin, $yMax, $diff)) )\n";
print INPUT "dev.off()\n";

# -------------------------------------------------------------------
close( INPUT );


# -------------------------------------------------------------------
# Run the R file
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# Fix the plots
`./replace-fonts-in-eps.pl $tmpEPSFile $tmpFixedEPSFile`;
`eps2eps $tmpFixedEPSFile $outputFigFile`;
`epstopdf $outputFigFile`;

# -------------------------------------------------------------------
# Clean up the R files
#`rm $rInputFile $rOutputFile`;

