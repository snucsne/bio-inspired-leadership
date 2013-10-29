#!/usr/bin/perl
use strict;

# ===================================================================
# Get the root data directory
my $rootDataDir = shift( @ARGV );

# Get the output data directory
my $outputDataDir = shift( @ARGV );

# Ensure it exists
unless( -d $outputDataDir )
{
    print "Making dir [$outputDataDir]\n";
    mkdir $outputDataDir or die "Unable to make output data dir [$outputDataDir]: $!\n";
}

# Get the data type
my $dataType = shift( @ARGV );
print "Using dataType=[$dataType]\n";


# ===================================================================
# Get the global treatment directories
opendir( ROOTDIR, $rootDataDir ) or die "Unable to open root data dir [$rootDataDir]: $!\n";
my @globalDirs = sort( grep { /^global-/ } readdir( ROOTDIR ) );
closedir( ROOTDIR );

# ===================================================================
# Process each treatment
foreach my $globalDir (@globalDirs)
{
    print "Processing global dir [$rootDataDir$globalDir]\n";

    # Create the output filename
    my $treatment = $globalDir;
    $treatment =~ s/global-/success-comparison-/;
    my $outputFile = "$outputDataDir$treatment.dat";
    #print "\tSending output to [$outputFile]\n";

    # Get all the different data type treatments
    opendir( GLOBALDIR, "$rootDataDir$globalDir/" ) or die "Unable to open global dir [$rootDataDir$globalDir]: $!\n";
    my @dataDirs = sort( grep{ /$dataType/ } readdir( GLOBALDIR ) );
    closedir( GLOBALDIR );

    # ---------------------------------------------------------------
    # Get the success percentages from each data directory
    my %globalData;
    my %localData;
    foreach my $dataDir (@dataDirs)
    {
        print "  Processing data dir [$dataDir]\n";

        # Get the data set identifier
        $dataDir =~ /$dataType-(.*)/;
        my $dataSet = $1;

        # Read all the success percentages from the analysis directory
        my $globalDataDir = "$rootDataDir$globalDir/$dataDir/analysis/";
        #print "    Reading success percentages from global [$globalDataDir]\n";
        readSuccessPercentages( $globalDataDir, \%globalData, $dataSet );

        # Do the same for the local directory
        my $localDataDir = $globalDataDir;
        $localDataDir =~ s/global/local/g;
        #print "    Reading success percentages from local [$localDataDir]\n";
        readSuccessPercentages( $localDataDir, \%localData, $dataSet );
    }

    # ---------------------------------------------------------------
    # Create the R input file
    my $rInputFile = "/tmp/compare-success-between-types.r";
    my $rOutputFile = $rInputFile.".out";
    createRInput( $rInputFile,
            $outputFile,
            $dataType,
            \%globalData,
            \%localData );

    # ---------------------------------------------------------------
    # Run it
    `R --no-save < $rInputFile > $rOutputFile 2>&1`;
}


# ===================================================================
sub readSuccessPercentages
{
    my ($dir, $dataRef, $dataSet) = @_;

    # Open the directory and find all the success percentages files
    opendir( DIR, $dir ) or die "Unable to open success percentage directory [$dir]: $!\n";
    my @successFiles = sort( grep { /all-success-percentages/ } readdir( DIR ) );
    closedir( DIR );

    # Process each file
    foreach my $successFile (@successFiles)
    {
        # Get the number of individuals
        $successFile =~ /indcount-(\d+)/;
        my $indCount = $1;

        # Store the data
        $dataRef->{$dataSet}{$indCount} = `cat $dir$successFile`;
    }

}


# ===================================================================
sub createRInput
{
    my ($rInputFile, $resultsFile, $dataType, $globalDataRef, $localDataRef ) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# ==============================================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";
    print INPUT "library('igraph')\n";

    print INPUT "reldiff <- function(x,y)\n";
    print INPUT "{\n";
    print INPUT "    return( abs( (x-y) / (max( abs(x), abs(y) ) ) ) )\n";
    print INPUT "}\n";
    print INPUT "\n";

    # ---------------------------------------------------------------
    # Add the data
    my %dataIDPostfixes;
    foreach my $dataSet (sort (keys %{$globalDataRef} ) )
    {
        # Sanitize the data set
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;
        
        foreach my $indCount (sort (keys %{$globalDataRef->{$dataSet}} ) )
        {
            # Dump the global data
            my $id = "global".$dataType.$cleanDataSet."indcount".$indCount;
            print INPUT "$id <- scan()\n";
            print INPUT $globalDataRef->{$dataSet}{$indCount},"\n\n";

            # Dump the corresponding local data
            $id = "local".$dataType.$cleanDataSet."indcount".$indCount;
            print INPUT "$id <- scan()\n";
            print INPUT $localDataRef->{$dataSet}{$indCount},"\n\n";

            # Save the data ID postfix
            $id =~ s/local//g;
            $dataIDPostfixes{$dataSet}{$indCount} = $id;            
        }
    }

    # ---------------------------------------------------------------
    # Start sending output to the results file
    print INPUT "sink(\"$resultsFile\")\n";

    # ---------------------------------------------------------------
    # Add the statistics
    foreach my $dataSet (sort (keys %dataIDPostfixes) )
    {
        # Sanitize the data set
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;

        foreach my $indCount (sort (keys %{$dataIDPostfixes{$dataSet}} ) )
        {
            # Dump the data summary for both global and local
            my $id = $dataIDPostfixes{$dataSet}{$indCount};
            print INPUT $rSpacer;
            print INPUT "cat(\"# GLOBAL:  $dataType=[$dataSet]  indCount=[$indCount] summary\\n\")\n";
            print INPUT "cat(\"success.global.$dataType-$cleanDataSet.indcount-$indCount.mean = \", format( mean(global$id) ), \"\\n\")\n";
            print INPUT "cat(\"success.global.$dataType-$cleanDataSet.indcount-$indCount.max =  \", format( max(global$id) ), \"\\n\")\n";
            print INPUT "cat(\"success.global.$dataType-$cleanDataSet.indcount-$indCount.min =  \", format( min(global$id) ), \"\\n\")\n";
            print INPUT "cat(\"success.global.$dataType-$cleanDataSet.indcount-$indCount.sd =   \", format( sd(global$id) ), \"\\n\")\n";
            print INPUT "cat(\"\\n\")\n";

            print INPUT "cat(\"# LOCAL:  $dataType=[$dataSet]  indCount=[$indCount] summary\\n\")\n";
            print INPUT "cat(\"success.local.$dataType-$cleanDataSet.indcount-$indCount.mean = \", format( mean(local$id) ), \"\\n\")\n";
            print INPUT "cat(\"success.local.$dataType-$cleanDataSet.indcount-$indCount.max =  \", format( max(local$id) ), \"\\n\")\n";
            print INPUT "cat(\"success.local.$dataType-$cleanDataSet.indcount-$indCount.min =  \", format( min(local$id) ), \"\\n\")\n";
            print INPUT "cat(\"success.local.$dataType-$cleanDataSet.indcount-$indCount.sd =   \", format( sd(local$id) ), \"\\n\")\n";
            print INPUT "cat(\"\\n\")\n";

            # Use the KS test compare them
            print INPUT "cat(\"# KS-test:  $dataType=[$dataSet]  indCount=[$indCount]\\n\")\n";
            print INPUT "ks <- ks.boot( global$id, local$id )\n";
            print INPUT "cat(\"ks.$dataType-$cleanDataSet.indcount-$indCount.p-value =         \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
            print INPUT "cat(\"ks.$dataType-$cleanDataSet.indcount-$indCount.naive-p-value =   \", format( ks\$ks\$p.value), \"\\n\")\n";
            print INPUT "cat(\"ks.$dataType-$cleanDataSet.indcount-$indCount.statistic =       \", format( ks\$ks\$statistic), \"\\n\")\n";
            print INPUT "cat(\"\\n\")\n";

            # Compute the relative difference
            print INPUT "df = data.frame( global=global$id, local=local$id )\n";
            print INPUT "successreldiff = apply( df, 1, function(x,y) reldiff(x[1],x[2]))\n";
            print INPUT "meansuccessreldiff = reldiff( mean(global$id), mean(local$id) )\n";
            print INPUT "cat(\"# Relative difference:  $dataType=[$dataSet]  indCount=[$indCount]\\n\")\n";
            print INPUT "cat(\"rel-diff-of-means.$dataType-$cleanDataSet.indcount-$indCount =  \", format( meansuccessreldiff ), \"\\n\")\n";
            print INPUT "cat(\"relative-diff.$dataType-$cleanDataSet.indcount-$indCount.mean = \", format( mean(successreldiff) ), \"\\n\")\n";
            print INPUT "cat(\"relative-diff.$dataType-$cleanDataSet.indcount-$indCount.max =  \", format( max(successreldiff) ), \"\\n\")\n";
            print INPUT "cat(\"relative-diff.$dataType-$cleanDataSet.indcount-$indCount.min =  \", format( min(successreldiff) ), \"\\n\")\n";
            print INPUT "cat(\"relative-diff.$dataType-$cleanDataSet.indcount-$indCount.sd =   \", format( sd(successreldiff) ), \"\\n\")\n";
            print INPUT "cat(\"\\n\\n\")\n";
            
        }
    }

    # ---------------------------------------------------------------
    # Stop sending output to the results file
    print INPUT "sink()\n";

    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );
}

