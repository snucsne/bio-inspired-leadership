#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the output file
my $outputFile = shift( @ARGV );

# Get the global communication file
my $globalFile = shift( @ARGV );
my $globalData = `cat $globalFile`;

# Get the N = Nc file
my $nnSizeFile = shift( @ARGV );
my $nnSizeData = `cat $nnSizeFile`;

# Get the N = G file
my $groupSizeFile = shift( @ARGV );
my $groupSizeData = `cat $groupSizeFile`;

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/success-percentages.r";
my $rOutputFile = "/tmp/success-percentages.r.out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
print INPUT "library('Matching')\n";
print INPUT "library('igraph')\n";

print INPUT "cohens.d <- function (x, y) {(mean(x)-mean(y))/sqrt((var(x)+var(y))/2) }\n";


# -------------------------------------------------------------------
# Add the global percentages
print INPUT "# Global percentages\n";
print INPUT "global <- scan()\n";
print INPUT $globalData,"\n";
print INPUT "\n\n";

# -------------------------------------------------------------------
# Add the NN size percentages
print INPUT "# NN size percentages\n";
print INPUT "nnsize <- scan()\n";
print INPUT $nnSizeData,"\n";
print INPUT "\n\n";

# -------------------------------------------------------------------
# Add the group size percentages
print INPUT "# Group size percentages\n";
print INPUT "groupsize <- scan()\n";
print INPUT $groupSizeData,"\n";
print INPUT "\n\n";

# -------------------------------------------------------------------
# Output statistics file
print INPUT "sink(\"$outputFile\")\n";

# -------------------------------------------------------------------
print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Global summary\\n\")\n";
print INPUT "cat(\"summary.global.mean = \", format( mean(global) ), \"\\n\")\n";
print INPUT "cat(\"summary.global.sd = \", format( sd(global) ), \"\\n\")\n";
print INPUT "cat(\"summary.global.var = \", format( var(global) ), \"\\n\")\n";
print INPUT "cat(\"\\n\")\n";

# -------------------------------------------------------------------
print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# NN size summary\\n\")\n";
print INPUT "cat(\"summary.nnsize.mean = \", format( mean(nnsize) ), \"\\n\")\n";
print INPUT "cat(\"summary.nnsize.sd = \", format( sd(nnsize) ), \"\\n\")\n";
print INPUT "cat(\"summary.nnsize.var = \", format( var(nnsize) ), \"\\n\")\n";
print INPUT "cat(\"\\n\")\n";

# -------------------------------------------------------------------
print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Group size summary\\n\")\n";
print INPUT "cat(\"summary.groupsize.mean = \", format( mean(groupsize) ), \"\\n\")\n";
print INPUT "cat(\"summary.groupsize.sd = \", format( sd(groupsize) ), \"\\n\")\n";
print INPUT "cat(\"summary.groupsize.var = \", format( var(groupsize) ), \"\\n\")\n";
print INPUT "cat(\"\\n\")\n";


# -------------------------------------------------------------------
print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Test: global vs nnsize\\n\")\n";
print INPUT "ks <- ks.boot(global,nnsize)\n";
print INPUT "cat(\"ks.global-vs-nnsize.p-value = \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
print INPUT "cat(\"ks.global-vs-nnsize.naive-pvalue = \", format(ks\$ks\$p.value), \"\\n\")\n";
print INPUT "cat(\"ks.global-vs-nnsize.statistic = \", format(ks\$ks\$statistic), \"\\n\")\n";
print INPUT "cohen <- cohens.d(global,nnsize)\n";
print INPUT "cat(\"cohen.global-vs-nnsize.d = \", format(cohen), \"\\n\")\n";
print INPUT "percentDiff <- (mean(global) - mean(nnsize))/mean(global)\n";
print INPUT "cat(\"percent-diff.global-vs-nnsize = \", format(percentDiff), \"\\n\")\n";
print INPUT "cat(\"\\n\")\n";


# -------------------------------------------------------------------
print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Test: global vs groupsize\\n\")\n";
print INPUT "ks <- ks.boot(global,groupsize)\n";
print INPUT "cat(\"ks.global-vs-groupsize.p-value = \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
print INPUT "cat(\"ks.global-vs-groupsize.naive-pvalue = \", format(ks\$ks\$p.value), \"\\n\")\n";
print INPUT "cat(\"ks.global-vs-groupsize.statistic = \", format(ks\$ks\$statistic), \"\\n\")\n";
print INPUT "cohen <- cohens.d(global,groupsize)\n";
print INPUT "cat(\"cohen.global-vs-groupsize.d = \", format(cohen), \"\\n\")\n";
print INPUT "percentDiff <- (mean(global) - mean(groupsize))/mean(global)\n";
print INPUT "cat(\"percent-diff.global-vs-groupsize = \", format(percentDiff), \"\\n\")\n";
print INPUT "cat(\"\\n\")\n";


# -------------------------------------------------------------------
print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Test: nnsize vs groupsize\\n\")\n";
print INPUT "ks <- ks.boot(nnsize,groupsize)\n";
print INPUT "cat(\"ks.nnsize-vs-groupsize.p-value = \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
print INPUT "cat(\"ks.nnsize-vs-groupsize.naive-pvalue = \", format(ks\$ks\$p.value), \"\\n\")\n";
print INPUT "cat(\"ks.nnsize-vs-groupsize.statistic = \", format(ks\$ks\$statistic), \"\\n\")\n";
print INPUT "cohen <- cohens.d(nnsize,groupsize)\n";
print INPUT "cat(\"cohen.nnsize-vs-groupsize.d = \", format(cohen), \"\\n\")\n";
print INPUT "percentDiff <- (mean(groupsize) - mean(nnsize))/((mean(groupsize) + mean(nnsize))/2)\n";
print INPUT "cat(\"percent-diff.groupsize-vs-nnsize = \", format(percentDiff), \"\\n\")\n";
print INPUT "cat(\"\\n\")\n";


# -------------------------------------------------------------------
# Close the input file
close( INPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

