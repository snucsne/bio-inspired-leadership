#!/usr/bin/perl
use strict;
use POSIX;

# -------------------------------------------------------------------
# Get the root data directory
my $rootDataDir = shift( @ARGV );

# Get the optimal data file
my $optimalDataFile = shift( @ARGV );

# Get the data type
my $dataType = shift( @ARGV );

# Get the data output file prefix
my $outputFilePrefix = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );


# -------------------------------------------------------------------
# Get all the data directories in the data directory
opendir( DIR, $rootDataDir ) or die "Unable to open root data directory [$rootDataDir]: $!\n";
my @dataDirs = sort( grep { /$dataType/ } readdir( DIR ) );
closedir( DIR );


# -------------------------------------------------------------------
# Get the bold percentages from each data directory
my %data;
my @dataSetOrder;
foreach my $dataDir (@dataDirs)
{
    # Get the data set identifier
    $dataDir =~ /$dataType-(.*)/;
    my $dataSet = $1;

    # Read all the bold percentages from the analysis directory
    $dataDir = $rootDataDir.$dataDir."/analysis/";
#print "dir=[$dataDir]  dataType=[$dataType]  dataSet=[$dataSet]\n";
    readBoldPercentages( $dataDir, \%data, $dataSet );
    push( @dataSetOrder, $dataSet );
}

# -------------------------------------------------------------------
# Get the optimal bold percentages
my %optimalData;

# Open the file
open( OPTIMAL, "$optimalDataFile" ) or die "Unable to open optimal data file [$optimalDataFile]: $!\n";

# Read each line
while( <OPTIMAL> )
{
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Split the line into key and value
        my ($key,$value) = split( /\s+=\s+/, $_ );
        $key =~ m/group-size-(\d+).*optimal-bold-(\w+)/;
        my $indCount = $1;
        my $dataType = $2;

        if( $key =~ /bold-percentage/ )
        {
            $optimalData{$indCount} = $value;
        }
}

# Close the file
close( OPTIMAL );

# -------------------------------------------------------------------
# Create the R input file
my $rInputFile = "/tmp/compare-bold-percentages.r";
my $rOutputFile = $rInputFile.".out";
my @epsFiles = createRInput( $rInputFile,
        $outputFilePrefix."-results.dat",
        $epsFilePrefix,
        $dataType,
        \@dataSetOrder,
        \%data,
        \%optimalData );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# -------------------------------------------------------------------
# Clean up the EPS files
my $tmpEPSFile = "/tmp/tmp.eps";
my $fixedTmpEPSFile = "/tmp/fixed-tmp.eps";
foreach my $epsFile (@epsFiles)
{
    `cp $epsFile $tmpEPSFile`;
    `./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
    `/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
    `epstopdf $epsFile`;
}


# ===================================================================
sub readBoldPercentages
{
    my ($dir, $dataRef, $dataSet) = @_;

    # Open the directory and find all the bold percentages files
    opendir( DIR, $dir ) or die "Unable to open analysis directory [$dir]: $!\n";
    my @boldFiles = sort( grep { /bold-personality-percentages/ } readdir( DIR ) );
    closedir( DIR );

#print "Reading bold percentages dir=[$dir] dataset=[$dataSet] ...\n";

    # Process each file
    foreach my $boldFile (@boldFiles)
    {
        # Get the number of individuals
        $boldFile =~ /indcount-(\d+)/;
        my $indCount = $1;

        # Store the data
        $dataRef->{$dataSet}{$indCount} = `cat $dir$boldFile`;
    }

}

# ===================================================================
sub createRInput
{
    my ($rInputFile,
            $resultsFile,
            $epsFilePrefix,
            $dataType,
            $dataSetOrderRef,
            $dataRef,
            $optimalDataRef) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# =====================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";
    print INPUT "library('igraph')\n";
    print INPUT "library('Hmisc')\n";

    # Build a relative difference function
    print INPUT "reldiff <- function(x,y)\n";
    print INPUT "{\n";
    print INPUT "    return( abs( (x-y) / (max( abs(x), abs(y) ) ) ) )\n";
    print INPUT "}\n\n";

    # Build a standard error function
    print INPUT "stderr <- function(x) sd(x)/sqrt(length(x))\n\n";

    # Build a function to handle the t-test error in case the data is constant
    print INPUT "try_default <- function (expr, default = NA) {\n";
    print INPUT "  result <- default\n";
    print INPUT "  tryCatch(result <- expr, error = function(e) {})\n";
    print INPUT "  result\n";
    print INPUT "}\n";

    print INPUT "failwith <- function(default = NULL, f, ...) {\n";
    print INPUT "  function(...) try_default(f(...), default)\n";
    print INPUT "}\n";

    print INPUT "tryttest <- function(...) failwith(NA, t.test(...))\n";
    print INPUT "tryttestpvalue <- function(...) {\n";
    print INPUT "    obj<-try(t.test(...), silent=TRUE)\n";
    print INPUT "    if (is(obj, \"try-error\")) return(NA) else return(obj\$p.value)\n";
    print INPUT "}\n";


    # ---------------------------------------------------------------
    # Add the optimal values
#    my $fullOptimalData;
#    foreach my $indCount (sort (keys %{$optimalDataRef} ) )
#    {
#        my $id = "optimal".$indCount;
#        print INPUT "$id <- scan()\n";
#        print INPUT $optimalDataRef->{$indCount},"\n\n";
#        $fullOptimalData .= $optimalDataRef->{$indCount}." ";
#    }
#    
#    print INPUT "optimal <- scan()\n";
#    print INPUT $fullOptimalData,"\n\n";


    # ---------------------------------------------------------------
    # Add the data
    my %dataIDs;
    my %relDiffDataIDs;
    my @allDataIDs;
    my %dataSetHash;
    my %indCountHash;
    foreach my $dataSet (@{$dataSetOrderRef})
    {
        # Sanitize the data set
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;

        foreach my $indCount (sort (keys %{$dataRef->{$dataSet}} ) )
        {
            my $id = $dataType.$cleanDataSet."indcount".$indCount;
            my $relDiffID = "reldiff".$id;
            print INPUT "$id <- scan()\n";
            print INPUT $dataRef->{$dataSet}{$indCount},"\n\n";
#            print INPUT "$relDiffID = reldiff( optimal$indCount, mean($id) )\n\n";
            $dataIDs{$dataSet}{$indCount} = $id;
            push( @allDataIDs, $id );
            $relDiffDataIDs{$dataSet}{$indCount} = $relDiffID;
            $indCountHash{$indCount} = 1;
        }
        $dataSetHash{$dataSet} = 1;
    }
    my @dataSets = @{$dataSetOrderRef};
    my @indCounts = sort( keys( %indCountHash ) );

    print INPUT "indcounts <- scan()\n @indCounts\n\n";

    # ---------------------------------------------------------------
    # Add the plots
    my @epsFiles;
    
    # Have a plot for each data set
    my $yMin = 0.0;
    my $yMax = 0.5;
    my $yDiff = 0.1;
    my $lineColor = "#882255";
    my $boxColor = "#DDCC77";
#    my $lineColor = "blue";
#    my $boxColor = "orange";
    my $dataPositionsStr = "";
    my @meansIDStrings;
    my @relDiffIDStrings;
    foreach my $dataSet (@dataSets)
    {
        # Build the data
        my $dataFrameStr = "";
        my $meanStr = "";
        my $labelStr = "";
        my $relDiffStr = "";
        $dataPositionsStr = "";
        my $dataCount = 0;
        foreach my $indCount (sort (keys %{$dataIDs{$dataSet}} ) )
        {
            my $id = $dataIDs{$dataSet}{$indCount};
            $dataFrameStr .= "$id=$id, ";
            $meanStr .= "median($id), ";
            my $indCountLabel = $indCount;
            $indCountLabel =~ s/^0+//;
            $labelStr .= "\"$indCountLabel\", ";
            $relDiffStr .= $relDiffDataIDs{$dataSet}{$indCount}.", ";
            $dataPositionsStr .= (($indCount/5)-1).", ";
            
            $dataCount++;
        }
        chop( $dataFrameStr );
        chop( $dataFrameStr );
        chop( $meanStr );
        chop( $meanStr );
        chop( $labelStr );
        chop( $labelStr );
        chop( $relDiffStr );
        chop( $relDiffStr );
        chop( $dataPositionsStr );
        chop( $dataPositionsStr );

        # Create some handy R variables
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;
        print INPUT "# $dataType => $dataSet --------------------------\n";
        my $allDataIDStr = "all".$dataType.$cleanDataSet."data";
        print INPUT "$allDataIDStr = data.frame($dataFrameStr)\n";
        my $meansIDStr = "all".$dataType.$cleanDataSet."means";
        print INPUT "$meansIDStr = c($meanStr)\n";
        push( @meansIDStrings, $meansIDStr );
        my $labelsIDStr = "all".$dataType.$cleanDataSet."labels";
        print INPUT "$labelsIDStr = c($labelStr)\n";
#        my $relDiffIDStr = "all".$dataType.$cleanDataSet."reldiffs";
#        print INPUT "$relDiffIDStr = c($relDiffStr)\n";
#        push( @relDiffIDStrings, $relDiffIDStr );
        my $dataPositionsIDStr = "all".$dataType.$cleanDataSet."datapositions";
        print INPUT "$dataPositionsIDStr = c(",$dataPositionsStr,")\n";


        # Build the EPS filename
        my $dataSetStr = $dataSet;
        $dataSetStr =~ s/\./-/;
        my $epsFile = $epsFilePrefix."-$dataType-$dataSetStr.eps";
        push( @epsFiles, $epsFile );

        # Build the plot
        print INPUT "postscript( file=\"$epsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
        print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
        print INPUT "par(mgp=c(3,1,0))\n";
        print INPUT "boxplot( $allDataIDStr, col=\"$boxColor\",ylim=c($yMin,$yMax),\n",
                "    ylab=\"Bold percentage\", names=c($labelStr),\n",
                "    xlab=\"Group size\", yaxt='n', at=$dataPositionsIDStr, boxwex=0.5, srt=-40)\n";
        print INPUT "title( main=\"",ucfirst($dataType)," $dataSet\" )\n";
        print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
        print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
        print INPUT "lines($dataPositionsIDStr,$meansIDStr,col=\"$lineColor\",lwd=2)\n";
        print INPUT "dev.off()\n\n\n";
    }

    # ---------------------------------------------------------------
    # Build a combined plot
    my $combinedEpsFile = $epsFilePrefix."-$dataType-combined.eps";
    push( @epsFiles, $combinedEpsFile );
    print INPUT "groupsizes = (c($dataPositionsStr) + 1)*5\n";
    print INPUT "groupsizes\n";
    print INPUT "postscript( file=\"$combinedEpsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
    print INPUT "par(mgp=c(3,1,0))\n";
    my $index = 0;
#    my @colors = ( "#332288", "#117733", "#999933" );
    my @colors = ( "#882255", "#332288", "#117733", "#999933" );
#    my @colors = ( "#AAAAAA", "#332288", "#117733", "#999933" );
    my @plotSymbols = ( 1, 2, 5, 6 );
    my $dataSetNames = "";
    my $colorsStr = "";
    my $plotSymbolsStr = "";
    my @combinedDataSets = ();
#    my @combinedDataSets = ( "optimal" );
    push( @combinedDataSets, @dataSets );
#    unshift( @meansIDStrings, "optimal" );
    foreach my $dataSet (@combinedDataSets)
    {
        my $misc = "ylab=\"Bold personality percentage\",  xlab=\"Group size\", \n".
                    "    ylim=c($yMin,$yMax), xlim=c((min(groupsizes)-5), (max(groupsizes)+5)), ";
        my $cmd = "plot";
        if( $index > 0 )
        {
            $misc = "";
            $cmd = "lines";
        }
        print INPUT "$cmd( groupsizes, ",$meansIDStrings[$index],", type=\"o\", \n",
                    "    lwd=2, pch=",$plotSymbols[$index],",\n",
                    "    ",$misc," col=\"",$colors[$index],"\", yaxt='n' )\n";

        if( $dataSet =~ /optimal/i )
        {
            $dataSetNames .= "\"Fixed distribution\", ";
        }
        else
        {
            my $name = "bold";
            if( $dataSet =~ /0.2/ )
            {
                $name = "shy";
            }
            elsif( $dataSet =~ /0.5/ )
            {
                $name = "moderate";
            }
            $dataSetNames .= "\"Initial $dataType $name\", ";
#            $dataSetNames .= "\"".ucfirst($dataType)." $dataSet\", ";
        }
        $colorsStr .= "\"".$colors[$index]."\", ";
        $plotSymbolsStr .= $plotSymbols[$index].", ";

        $index++;
    }
    chop( $dataSetNames );
    chop( $dataSetNames );
    chop( $colorsStr );
    chop( $colorsStr );
    chop( $plotSymbolsStr );
    chop( $plotSymbolsStr );
    print INPUT "legend( \"bottom\", c($dataSetNames), col=c($colorsStr), \n",
        "    pch=c($plotSymbolsStr), lty=1, lwd=2, bty=\"n\", ncol=",(ceil($#dataSets / 2))," )\n";


    print INPUT "templabels <- c(seq($yMin,$yMax,$yDiff)) * 100\n";
    print INPUT "percentagelabels <- paste(templabels, \"%\", sep=\"\" )\n";
    print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)), labels=percentagelabels)\n";
    print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)), labels=percentagelabels)\n";
#    print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
#    print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";

    print INPUT "dev.off()\n\n\n";



    # ---------------------------------------------------------------
    # Build a combined plot of relative differences plot
#    my $combinedEpsFile = $epsFilePrefix."-$dataType-rel-diff-combined.eps";
#    push( @epsFiles, $combinedEpsFile );
#    print INPUT "groupsizes = (c($dataPositionsStr) + 1)*5\n";
#    print INPUT "groupsizes\n";
#    print INPUT "postscript( file=\"$combinedEpsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
#    print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
#    print INPUT "par(mgp=c(3,1,0))\n";
#    $yMin = 0;
#    $yMax = 1.0;
#    $yDiff = 0.2;
#    $index = 0;
#    @colors = ( "#332288", "#117733", "#999933" );
#    @plotSymbols = ( 2, 5, 6 );
#    $dataSetNames = "";
#    $colorsStr = "";
#    $plotSymbolsStr = "";
#    @combinedDataSets = ( "optimal" );
#    push( @combinedDataSets, @dataSets );
#    unshift( @meansIDStrings, "optimal" );
#    foreach my $dataSet (@dataSets)
#    {
#        my $misc = "ylab=\"Absolute relative difference\", xlab=\"Group size\", \n".
#                    "    ylim=c($yMin,$yMax), xlim=c((min(groupsizes)-5), (max(groupsizes)+5)), ";
#        my $cmd = "plot";
#        if( $index > 0 )
#        {
#            $misc = "";
#            $cmd = "lines";
#        }
#        print INPUT "$cmd( groupsizes, ",$relDiffIDStrings[$index],", type=\"o\", \n",
#                    "    lwd=2, pch=",$plotSymbols[$index],",\n",
#                    "    ",$misc," col=\"",$colors[$index],"\", yaxt='n' )\n";
#
#        if( $dataSet =~ /optimal/i )
#        {
#            $dataSetNames .= "\"Fixed distribution\", ";
#        }
#        else
#        {
#            $dataSetNames .= "\"".ucfirst($dataType)." $dataSet\", ";
#        }
#        $colorsStr .= "\"".$colors[$index]."\", ";
#        $plotSymbolsStr .= $plotSymbols[$index].", ";
#
#        $index++;
#    }
#    chop( $dataSetNames );
#    chop( $dataSetNames );
#    chop( $colorsStr );
#    chop( $colorsStr );
#    chop( $plotSymbolsStr );
#    chop( $plotSymbolsStr );
#    print INPUT "legend( \"top\", c($dataSetNames), col=c($colorsStr), \n",
#        "    pch=c($plotSymbolsStr), lty=1, lwd=2, bty=\"n\", ncol=",(ceil($#dataSets / 2))," )\n";
#    print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
#    print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
#    print INPUT "dev.off()\n\n\n";


    # ---------------------------------------------------------------
    # Build a combined plot of the raw numbers
#    my $combinedEpsFile = $epsFilePrefix."-$dataType-combined-raw.eps";
#    push( @epsFiles, $combinedEpsFile );
#    print INPUT "groupsizes = (c($dataPositionsStr) + 1)*5\n";
#    print INPUT "groupsizes\n";
#    print INPUT "postscript( file=\"$combinedEpsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
#    print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
#    print INPUT "par(mgp=c(3,1,0))\n";
#    $index = 0;
#    $yMin = 0.0;
#    $yMax = 80;
#    $yDiff = 20;
#    @colors = ( "#882255", "#332288", "#117733", "#999933" );
##    @colors = ( "#AAAAAA", "#332288", "#117733", "#999933" );
#    @plotSymbols = ( 1, 2, 5, 6 );
#    $dataSetNames = "";
#    $colorsStr = "";
#    $plotSymbolsStr = "";
#    @combinedDataSets = ( "optimal" );
#    push( @combinedDataSets, @dataSets );
##    unshift( @meansIDStrings, "optimal" );
#    foreach my $dataSet (@combinedDataSets)
#    {
#        my $misc = "ylab=\"Bold personality count\",  xlab=\"Group size\", \n".
#                    "    ylim=c($yMin,$yMax), xlim=c((min(groupsizes)-5), (max(groupsizes)+5)), ";
#        my $cmd = "plot";
#        if( $index > 0 )
#        {
#            $misc = "";
#            $cmd = "lines";
#        }
#        print INPUT "$cmd( groupsizes, (",$meansIDStrings[$index]," * indcounts), type=\"o\", \n",
#                    "    lwd=2, pch=",$plotSymbols[$index],",\n",
#                    "    ",$misc," col=\"",$colors[$index],"\", yaxt='n' )\n";
#
#        if( $dataSet =~ /optimal/i )
#        {
#            $dataSetNames .= "\"Fixed distribution\", ";
#        }
#        else
#        {
#            $dataSetNames .= "\"".ucfirst($dataType)." $dataSet\", ";
#        }
#        $colorsStr .= "\"".$colors[$index]."\", ";
#        $plotSymbolsStr .= $plotSymbols[$index].", ";
#
#        $index++;
#    }
#    chop( $dataSetNames );
#    chop( $dataSetNames );
#    chop( $colorsStr );
#    chop( $colorsStr );
#    chop( $plotSymbolsStr );
#    chop( $plotSymbolsStr );
#    print INPUT "legend( \"top\", c($dataSetNames), col=c($colorsStr), \n",
#        "    pch=c($plotSymbolsStr), lty=1, lwd=2, bty=\"n\", ncol=",(ceil($#dataSets / 2))," )\n";
#    print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
#    print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
#    print INPUT "dev.off()\n\n\n";


    # ---------------------------------------------------------------
    # Start sending output to the results file
#    print INPUT "sink(\"$resultsFile\")\n";

    # ---------------------------------------------------------------
    # Add the statistics
    foreach my $dataSet (sort (keys %dataIDs) )
    {
        # Sanitize the data set
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;

        foreach my $indCount (sort (keys %{$dataIDs{$dataSet}} ) )
        {
            my $id = $dataIDs{$dataSet}{$indCount};
            print INPUT "cat(\"# =====================================\\n\")\n";
            print INPUT "cat(\"# $dataType=[$dataSet]  indCount=[$indCount] summary\\n\")\n";
            print INPUT "cat(\"summary.$dataType-$dataSet.indcount-$indCount.mean = \", format( mean($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$dataType-$dataSet.indcount-$indCount.max =  \", format( max($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$dataType-$dataSet.indcount-$indCount.min =  \", format( min($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$dataType-$dataSet.indcount-$indCount.sd =   \", format( sd($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$dataType-$dataSet.indcount-$indCount.se =   \", format( stderr($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$dataType-$dataSet.indcount-$indCount.length = \", format( length($id) ), \"\\n\")\n";
            print INPUT "cat(\"\\n\")\n";
        }
    }

    # ---------------------------------------------------------------
    # Add the relative difference between the optimum and each data set
#    foreach my $dataSet (sort (keys %dataIDs) )
#    {
#        # Sanitize the data set
#        my $cleanDataSet = $dataSet;
#        $cleanDataSet =~ s/\.//g;
#        $cleanDataSet =~ s/-//g;
#
#        print INPUT "cat(\"# =====================================\\n\")\n";
#        print INPUT "cat(\"# Relative difference to optimum:  $dataType=[$dataSet]\\n\")\n";
#
#        foreach my $indCount (sort (keys %{$dataIDs{$dataSet}} ) )
#        {
#            my $relDiffID = $relDiffDataIDs{$dataSet}{$indCount};
#            print INPUT "cat(\"rel-diff.$dataType-$cleanDataSet.indcount-$indCount =  \", format( $relDiffID ), \"\\n\")\n";
#        }
#        print INPUT "cat(\"\\n\")\n";
#    }    

    # ---------------------------------------------------------------
    # Add the KS tests for data with the same individual count
    foreach my $indCount (@indCounts)
    {
        for( my $i = 0; $i <= ($#dataSets - 1); $i++ )
        {
            for( my $j = $i + 1; $j <= ($#dataSets); $j++ )
            {
                my $firstDataSet = $dataSets[$i];
                my $secondDataSet = $dataSets[$j];
                print INPUT $rSpacer;
                print INPUT "cat(\"# KS IndCount [",
                        $indCount,
                        "]: ",
                        ucfirst($dataType),
                        "=[",
                        $dataSets[$i],
                        "] vs ",
                        ucfirst($dataType),
                        "=[",
                        $dataSets[$j],
                        "]\\n\")\n";
                printKSTestText( *INPUT,
                        $dataIDs{$firstDataSet}{$indCount},
                        "$dataType-$firstDataSet-indCount-$indCount",
                        $dataIDs{$secondDataSet}{$indCount},
                        "$dataType-$secondDataSet-indCount-$indCount" );
            }
        }
    }

    # ---------------------------------------------------------------
    # Add the KS tests for data with the same data set
    foreach my $dataSet (sort (keys %dataIDs) )
    {
        for( my $i = 0; $i <= ($#indCounts - 1); $i++ )
        {
            for( my $j = $i + 1; $j <= ($#indCounts); $j++ )
            {
                my $firstIndCount = $indCounts[$i];
                my $secondIndCount = $indCounts[$j];
#                print INPUT $rSpacer;
#                print INPUT "cat(\"# KS ",
#                        ucfirst( $dataType ),
#                        " [",
#                        $dataSet,
#                        "]: IndCount=[",
#                        $indCounts[$i],
#                        "] vs IndCount=[",
#                        $indCounts[$j],
#                        "]\\n\")\n";
#                printKSTestText( *INPUT,
#                        $dataIDs{$dataSet}{$firstIndCount},
#                        "$dataType-$dataSet-indCount-$firstIndCount",
#                        $dataIDs{$dataSet}{$secondIndCount},
#                        "$dataType-$dataSet-indCount-$secondIndCount" );
            }
        }
    }


    # ---------------------------------------------------------------
    # Stop sending output to the results file
#    print INPUT "sink()\n";

    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );

    # ---------------------------------------------------------------
    # Return the EPS files built
    return @epsFiles;
}

# ===================================================================
sub printKSTestText
{
    local *INPUT = shift;
    my ($firstID, $firstKey, $secondID, $secondKey) = @_;
return;
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
            print INPUT "ttest.p.value <- tryttestpvalue(",
                    $firstID,
                    ",",
                    $secondID,
                    ")\n";
            print INPUT "cat(\"t-test.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value =   \", format(ttest.p.value), \"\\n\")\n";
#            print INPUT "cat(\"t-test.",
#                    $firstKey,
#                    ".vs.",
#                    $secondKey,
#                    ".statistic = \", format(ttest\$statistic), \"\\n\")\n";
            print INPUT "cat(\"\\n\")\n";

}

