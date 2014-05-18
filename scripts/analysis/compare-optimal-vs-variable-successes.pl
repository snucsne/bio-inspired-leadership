#!/usr/bin/perl
use strict;
use POSIX;
use Statistics::Descriptive;

# -------------------------------------------------------------------
# Get the group size
my $groupSize = shift( @ARGV );

# Get the fixed percentage directory
my $fixedDataDir = shift( @ARGV );

# Ensure that it ends with a "/"
unless( $fixedDataDir =~ m/\/$/ )
{
    $fixedDataDir .= "/";
}

# Get the variable percentage directory
my $variableAnalysisDir = shift( @ARGV );

# Ensure that it ends with a "/"
unless( $variableAnalysisDir =~ m/\/$/ )
{
    $variableAnalysisDir .= "/";
}

# Get the output results file
my $resultsFile = shift( @ARGV );


# -------------------------------------------------------------------
# Get the fixed percentage success percentages
my %fixedSuccesses;
getBestFixedSuccessPercentages( $groupSize, $fixedDataDir, \%fixedSuccesses );


# -------------------------------------------------------------------
# Get each of the variable personality percentage files
opendir( DIR, "$variableAnalysisDir") or die "Unable to open analysis directory [$variableAnalysisDir]: $!\n";
my @variableSuccessFiles = sort( grep { /success-percentages/ } readdir( DIR ) );
closedir( DIR );

print "Found [",scalar(@variableSuccessFiles),"] files\n";

# -------------------------------------------------------------------
# Process each of the variable personality percentage files
my %variableSuccesses;
foreach my $variableSuccessFile (@variableSuccessFiles)
{
    # Get the individual count and bold count
    $variableAnalysisDir =~ /indcount-(\d+)/;
    my $indCount = $1;
    $variableSuccessFile =~ /boldcount-(\d+)/;
    my $boldCount = $1;

    # Get the percentages
    $variableSuccesses{$boldCount} = `cat $variableAnalysisDir$variableSuccessFile`;
}


# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/optimal-vs-variable.r";
my $rOutputFile = $rInputFile.".out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

# -------------------------------------------------------------------
# Import some libraries
print INPUT "library('Matching')\n";
print INPUT "library('Hmisc')\n";

# Build a standard error function
print INPUT "stderr <- function(x) sd(x)/sqrt(length(x))\n\n";

# Build a relative difference function
print INPUT "reldiff <- function(x,y)\n";
print INPUT "{\n";
print INPUT "    return( abs( (x-y) / (max( abs(x), abs(y) ) ) ) )\n";
print INPUT "}\n";
print INPUT "\n";

# -------------------------------------------------------------------
# Add the optimal data
my $optimalID = "optimal.$groupSize";
print INPUT "$optimalID <- scan()\n";
print INPUT $fixedSuccesses{"data"},"\n\n";

# -------------------------------------------------------------------
# Add the variable data
my %variableIDs;
foreach my $boldCount (sort (keys %variableSuccesses) )
{
    my $id = "variable.$groupSize.$boldCount";
    $variableIDs{$boldCount} = $id;

    print INPUT "$id <- scan()\n";
    print INPUT $variableSuccesses{$boldCount},"\n\n";
}


# -------------------------------------------------------------------
# Start sending output to the results file
print INPUT "sink(\"$resultsFile\")\n";

# -------------------------------------------------------------------
# Add the statistics for the optimal successes
print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Optimal: boldCount=[",$fixedSuccesses{"bold-count"},"] groupSize=[$groupSize] summary\\n\")\n";
print INPUT "cat(\"summary.optimal.mean = \", format( mean($optimalID) ), \"\\n\")\n";
print INPUT "cat(\"summary.optimal.max =  \", format( max($optimalID) ), \"\\n\")\n";
print INPUT "cat(\"summary.optimal.min =  \", format( min($optimalID) ), \"\\n\")\n";
print INPUT "cat(\"summary.optimal.sd =   \", format( sd($optimalID) ), \"\\n\")\n";
print INPUT "cat(\"summary.optimal.se =   \", format( stderr($optimalID) ), \"\\n\")\n";
print INPUT "cat(\"\\n\\n\")\n";

# -------------------------------------------------------------------
# Add the statistics for the variable successes
foreach my $boldCount (sort (keys %variableSuccesses) )
{
    my $id = $variableIDs{$boldCount};

    print INPUT "cat(\"# =====================================\\n\")\n";
    print INPUT "cat(\"# Variable: boldCount=[$boldCount] groupSize=[$groupSize] summary\\n\")\n";
    print INPUT "cat(\"summary.variable.$boldCount.mean =    \", format( mean($id) ), \"\\n\")\n";
    print INPUT "cat(\"summary.variable.$boldCount.max =     \", format( max($id) ), \"\\n\")\n";
    print INPUT "cat(\"summary.variable.$boldCount.min =     \", format( min($id) ), \"\\n\")\n";
    print INPUT "cat(\"summary.variable.$boldCount.sd =      \", format( sd($id) ), \"\\n\")\n";
    print INPUT "cat(\"summary.variable.$boldCount.se =      \", format( stderr($id) ), \"\\n\")\n";
    print INPUT "cat(\"summary.variable.$boldCount.reldiff = \", format( reldiff( mean($optimalID), mean($id) ) ), \"\\n\")\n";
    print INPUT "cat(\"\\n\\n\")\n";
}


# -------------------------------------------------------------------
# Add the significance testing
foreach my $boldCount (sort (keys %variableSuccesses) )
{
    my $id = $variableIDs{$boldCount};

    print INPUT "cat(\"# =====================================\\n\")\n";
    print INPUT "cat(\"# Sig Test: Optimal vs Variable boldCount=[$boldCount]\\n\")\n";
    printKSTestText( *INPUT,,
            $optimalID,
            "optimal",
            $id,
            "variable.$boldCount" );
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


# ===================================================================
sub getBestFixedSuccessPercentages
{
    my ($groupSize, $dataDir, $fixedSuccessesRef) = @_;

    # Open the directory and get all the different bold percentage dirs
    opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
    my @boldDirs = sort( grep { /boldcount/ } readdir( DIR ) );
    closedir( DIR );

    # Process each directory
    my %data;
    my %successMap;
    my @meanSuccessPercentages;
    foreach my $boldDir (@boldDirs)
    {
        # Get the bold count
        $boldDir =~ /boldcount-0*(\d+)/;
        my $boldCount = $1;
        my $boldPercentage = $1 / $groupSize;

        # Get the success percentages
        my $successPercentages = `grep total-leadership-success $dataDir/$boldDir/spatial-hidden-var-* | awk '{print \$3;}' | tr '\n' ' '`;

        # Calculate the mean
        my $stats = Statistics::Descriptive::Full->new();
        $stats->add_data( split( /\s+/, $successPercentages ) );
        my $mean = $stats->mean();

        # Store the data and the mean
        $data{$boldCount}{"mean"} = $mean;
        $data{$boldCount}{"data"} = $successPercentages;
        $successMap{$mean} = $boldCount;
        push( @meanSuccessPercentages, $mean );

#        print "bold=[$boldCount]  mean=[$mean]\n";
    }

    # Find the highest mean
    my @sortedMeans = sort { $b <=> $a } @meanSuccessPercentages;
    my $highestMean = $sortedMeans[0];
    my $highestMeanBoldCount = $successMap{ $highestMean };
#    print "Highest=[$highestMeanBoldCount]\n";

    # Save the data
    $fixedSuccessesRef->{"mean"} = $highestMean;
    $fixedSuccessesRef->{"bold-count"} = $highestMeanBoldCount;
    $fixedSuccessesRef->{"bold-percentage"} = $highestMeanBoldCount / $groupSize;
    $fixedSuccessesRef->{"data"} = $data{$highestMeanBoldCount}{"data"};
}


# ===================================================================
sub printKSTestText
{
    local *INPUT = shift;
    my ($firstID, $firstKey, $secondID, $secondKey) = @_;
            print INPUT "ks <- ks.boot( ",
                    $firstID,
                    ",",
                    $secondID,
                    " )\n";
            print INPUT "cat(\"ks.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value = \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
            print INPUT "cat(\"ks.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".naive-pvalue = \", format(ks\$ks\$p.value), \"\\n\")\n";
            print INPUT "cat(\"ks.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".statistic = \", format(ks\$ks\$statistic), \"\\n\")\n";
            print INPUT "ttest <- t.test( ",
                    $firstID,
                    ",",
                    $secondID,
                    " )\n";
            print INPUT "cat(\"t-test.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value =   \", format(ttest\$p.value), \"\\n\")\n";
            print INPUT "cat(\"t-test.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".statistic = \", format(ttest\$statistic), \"\\n\")\n";
            print INPUT "cat(\"\\n\\n\")\n";

}

