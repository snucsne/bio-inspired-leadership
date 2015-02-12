#!/usr/bin/perl
use strict;
use POSIX;

# -------------------------------------------------------------------
# Get the root data directory
my $rootDataDir = shift( @ARGV );

# Get the data type
my $dataType = shift( @ARGV );

# Get the data type title
my $dataTypeTitle = shift( @ARGV );

# Get the data output file prefix
my $outputFilePrefix = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );


# -------------------------------------------------------------------
# What kind of data are we dealing with?


# Get all the data directories in the data directory
opendir( DIR, $rootDataDir ) or die "Unable to open root data directory [$rootDataDir]: $!\n";
my @dataDirs = sort( grep { /$dataType/ } readdir( DIR ) );
closedir( DIR );

# Is there a baseline?
if( -d "$rootDataDir/baseline/" )
{
    unshift( @dataDirs, "baseline" );
}


# -------------------------------------------------------------------
# Get the success percentages from each data directory
my %data;
my @dataSetOrder;
foreach my $dataDir (@dataDirs)
{
    # Get the data set identifier
    my $dataSet = "None";
    if( $dataDir =~ m/$dataType-(.*)/ )
    {
#        $dataDir =~ /$dataType-(.*)/;
         $dataSet = $1;
    }
#    if( ($dataSet eq "") && ($dataDir =~ /baseline/) )
#    {
#        $dataSet = "None";
#    }

    # Read all the success percentages from the analysis directory
    $dataDir = $rootDataDir.$dataDir."/analysis/";
#print "dir=[$dataDir]  dataType=[$dataType]  dataSet=[$dataSet]\n";
    readSuccessPercentages( $dataDir, \%data, $dataSet );
    push( @dataSetOrder, $dataSet );
}


# -------------------------------------------------------------------
# Create the R input file
my $rInputFile = "/tmp/compare-success-percentages.r";
my $rOutputFile = $rInputFile.".out";
my @epsFiles = createRInput( $rInputFile,
        $outputFilePrefix."-results.dat",
        $epsFilePrefix,
        $dataType,
        $dataTypeTitle,
        \@dataSetOrder,
        \%data );

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
#    `./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
#    `/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
    `/usr/bin/epstool --copy --bbox $tmpEPSFile $epsFile`;
    `epstopdf $epsFile`;
}


# ===================================================================
sub readSuccessPercentages
{
    my ($dir, $dataRef, $dataSet) = @_;

    # Open the directory and find all the success percentages files
    opendir( DIR, $dir ) or die "Unable to open success percentage directory [$dir]: $!\n";
    my @successFiles = sort( grep { /all-success-percentages/ } readdir( DIR ) );
    closedir( DIR );

#print "Reading success percentages dir=[$dir] dataset=[$dataSet] ...\n";

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
    my ($rInputFile, $resultsFile, $epsFilePrefix, $dataType, $dataTypeTitle, $dataSetOrderRef, $dataRef) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# =====================================\\n\")\n";

    # Sanitize the datatype
    my $cleanDataType = $dataType;
    $cleanDataType =~ s/\.//g;
    $cleanDataType =~ s/-//g;

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";
    print INPUT "library('igraph')\n";
    print INPUT "library('Hmisc')\n";

    print INPUT "library(extrafont)\n";
    print INPUT "loadfonts(device = \"postscript\")\n";

    # Build a standard error function
    print INPUT "stderr <- function(x) sd(x)/sqrt(length(x))\n\n";

    # ---------------------------------------------------------------
    # Add the data
    my %dataIDs;
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
            my $id = $cleanDataType.$cleanDataSet."indcount".$indCount;
            print INPUT "$id <- scan()\n";
            print INPUT $dataRef->{$dataSet}{$indCount},"\n\n";
            $dataIDs{$dataSet}{$indCount} = $id;
            push( @allDataIDs, $id );
            $indCountHash{$indCount} = 1;
        }
        $dataSetHash{$dataSet} = 1;
    }
    my @dataSets = @{$dataSetOrderRef};
    my @indCounts = sort( keys( %indCountHash ) );

    # ---------------------------------------------------------------
    # Add the plots
    my @epsFiles;
    
    # Have a plot for each data set
    my $yMin = 0.0;
    my $yMax = 1.0;
    my $yDiff = 0.25;
    my $lineColor = "#882255";
    my $boxColor = "#DDCC77";
    my $legendPosition = "top";
#    my $lineColor = "blue";
#    my $boxColor = "orange";
    my $dataPositionsStr = "";
    my @meansIDStrings;
    my @sdIDStrings;
    my @seIDStrings;
    foreach my $dataSet (@dataSets)
    {
        # Build the data
        my $dataFrameStr = "";
        my $meanStr = "";
        my $sdStr = "";
        my $seStr = "";
        my $labelStr = "";
        $dataPositionsStr = "";
        my $dataCount = 0;
        foreach my $indCount (sort (keys %{$dataIDs{$dataSet}} ) )
        {
            my $id = $dataIDs{$dataSet}{$indCount};
            $dataFrameStr .= "$id=$id, ";
            $meanStr .= "mean($id), ";
            $sdStr .= "sd($id), ";
            $seStr .= "stderr($id), ";
            my $indCountLabel = $indCount;
            $indCountLabel =~ s/^0+//;
            $labelStr .= "\"$indCountLabel\", ";
            $dataPositionsStr .= (($indCount/5)-1).", ";
            
            $dataCount++;
        }
        $dataFrameStr =~ s/, $//;
        $meanStr =~ s/, $//;
        $sdStr =~ s/, $//g;
        $seStr =~ s/, $//g;
        $labelStr =~ s/, $//;
        $dataPositionsStr =~ s/, $//;

        # Create some handy R variables
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;
        print INPUT "# $dataType => $dataSet --------------------------\n";
        my $allDataIDStr = "all".$cleanDataType.$cleanDataSet."data";
        print INPUT "$allDataIDStr = data.frame($dataFrameStr)\n";

        my $meansIDStr = "all".$cleanDataType.$cleanDataSet."means";
        print INPUT "$meansIDStr = c($meanStr)\n";
        push( @meansIDStrings, $meansIDStr );

        my $sdIDStr = "all".$dataType.$cleanDataSet."sd";
        push( @sdIDStrings, $sdIDStr );
        print INPUT "$sdIDStr = c($sdStr)\n";

        my $seIDStr = "all".$dataType.$cleanDataSet."se";
        push( @seIDStrings, $seIDStr );
        print INPUT "$seIDStr = c($seStr)\n";

        my $labelsIDStr = "all".$cleanDataType.$cleanDataSet."labels";
        print INPUT "$labelsIDStr = c($labelStr)\n";
        my $dataPositionsIDStr = "all".$cleanDataType.$cleanDataSet."datapositions";
        print INPUT "$dataPositionsIDStr = c(",$dataPositionsStr,")\n";


        # Build the EPS filename
        my $dataSetStr = $dataSet;
        $dataSetStr =~ s/\./-/;
        my $epsFile = $epsFilePrefix."-$dataType-$dataSetStr.eps";
        push( @epsFiles, $epsFile );

        # Build the plot
#        print INPUT "postscript( file=\"$epsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
        print INPUT "postscript( file=\"$epsFile\", height=5.5, width=6.83, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
        print INPUT "par(mar=c(4,4.5,1,3)+0.1)\n";
        print INPUT "par(mgp=c(3,0.8,0))\n";
        print INPUT "boxplot( $allDataIDStr, col=\"$boxColor\",ylim=c($yMin,$yMax),\n",
                "    ylab=\"Leadership success percentage\", names=c($labelStr),\n",
                "    xlab=\"Group size\", yaxt='n', at=$dataPositionsIDStr, boxwex=0.5, srt=-40)\n";
        print INPUT "title( main=\"$dataTypeTitle $dataSet\" )\n";
        print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
        print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";

#        print INPUT "mtext( \"Shy\", side=2, line=2, adj=0.0 )\n";
#        print INPUT "mtext( \"Shy\", side=4, line=2, adj=0.0 )\n";
#        print INPUT "mtext( \"Moderate\", side=2, line=2, adj=0.5 )\n";
#        print INPUT "mtext( \"Moderate\", side=4, line=2, adj=0.5 )\n";
#        print INPUT "mtext( \"Bold\", side=2, line=2, adj=1.0 )\n";
#        print INPUT "mtext( \"Bold\", side=4, line=2, adj=1.0 )\n";

        print INPUT "lines($dataPositionsIDStr,$meansIDStr,col=\"$lineColor\",lwd=2)\n";
        print INPUT "dev.off()\n\n\n";
    }

    # ---------------------------------------------------------------
    # Build a combined plot
    my $combinedEpsFile = $epsFilePrefix."-$dataType-by-$dataType-combined.eps";
    push( @epsFiles, $combinedEpsFile );
    print INPUT "groupsizes = (c($dataPositionsStr) + 1)*5\n";
    print INPUT "groupsizes\n";
#    print INPUT "postscript( file=\"$combinedEpsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "postscript( file=\"$combinedEpsFile\", height=5.5, width=6.83, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "par(mar=c(4,4.5,1,3)+0.1)\n";
    print INPUT "par(mgp=c(3,0.8,0))\n";
    my $index = 0;
#    $legendPosition = "bottom";
#    my @colors = (  "#999933", "#117733", "#332288", "#882255", "#AA4499", "#88CCEE", "#CC6677", "#44AA99", "#DDCC77", "#000000" );
#    my @colors = (  "#999933", "#117733", "#332288", "#9C0A2E", "#AA4499", "#88CCEE", "#CC6677", "#44AA99", "#DDCC77", "#000000" );
    my @colors = (  "#882255", "#999933", "#AA4499", "#332288", "#114477", "#774411", "#117733", "#44AA99", "#DDCC77", "#000000" );
    my @plotSymbols = ( 1, 6, 0, 2, 7, 3, 5, 4, 9, 8 );
    my $dataSetNames = "";
    my $colorsStr = "";
    my $plotSymbolsStr = "";
    foreach my $dataSet (@dataSets)
    {
        my $misc = "ylab=\"Leadership success percentage\",  xlab=\"Group size\", \n".
                    "    ylim=c($yMin,$yMax), xlim=c((min(groupsizes)-5), (max(groupsizes)+5)), ";
        my $cmd = "plot";
        if( $index > 0 )
        {
            $misc = "";
            $cmd = "lines";
        }
        print INPUT "$cmd( (groupsizes), ",$meansIDStrings[$index],", type=\"o\", \n",
                    "    lwd=2, pch=",$plotSymbols[$index],",\n",
                    "    ",$misc," col=\"",$colors[$index],"\", yaxt='n' )\n";

        print INPUT "par(fg=\"",$colors[$index],"\")\n";
        print INPUT "errbar( (groupsizes), ",$meansIDStrings[$index],", \n",
                "    (",$meansIDStrings[$index],"+",$seIDStrings[$index],"), \n",
                "    (",$meansIDStrings[$index],"-",$seIDStrings[$index],"), \n",
                "    add=TRUE, lwd=0.5, pch=",$plotSymbols[$index],",\n",
                "    col=\"",$colors[$index],"\", yaxt='n' )\n";
        print INPUT "par(fg=\"black\")\n";

        if( $dataSet =~ /None/i )
        {
            $dataSetNames .= "\"None\", ";
        }
        else
        {
            $dataSetNames .= "\"$dataTypeTitle $dataSet\", ";
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
    print INPUT "legend( \"$legendPosition\", \n",
        "    c($dataSetNames), \n",
        "    col=c($colorsStr), \n",
        "    pch=c($plotSymbolsStr), lty=1, lwd=2, bty=\"n\", ncol=",(ceil($#dataSets / 3))," )\n";
#print "# dataSets=[$#dataSets] ceil=[",(ceil($#dataSets / 3)),"]\n";
    print INPUT "templabels <- c(seq($yMin,$yMax,$yDiff)) * 100\n";
    print INPUT "percentagelabels <- paste(templabels, \"%\", sep=\"\" )\n";
    print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)), labels=percentagelabels)\n";
    print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)), labels=percentagelabels)\n";
#    print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
#    print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";

#    print INPUT "mtext( \"Shy\", side=2, line=, adj=0.0 )\n";
#    print INPUT "mtext( \"Shy\", side=4, line=2, adj=0.0 )\n";
#    print INPUT "mtext( \"Moderate\", side=2, line=2, adj=0.5 )\n";
#    print INPUT "mtext( \"Moderate\", side=4, line=2, adj=0.5 )\n";
#    print INPUT "mtext( \"Bold\", side=2, line=2, adj=1.0 )\n";
#    print INPUT "mtext( \"Bold\", side=4, line=2, adj=1.0 )\n";

    print INPUT "dev.off()\n\n\n";


    # ---------------------------------------------------------------
    # Have a plot for each individual count
    @meansIDStrings = ();
    my $labelStr = "";
    foreach my $indCount (@indCounts)
    {
        # Build the data
        my $dataFrameStr = "";
        my $meanStr = "";
        $labelStr = "";
        $dataPositionsStr = "";
        my $dataCount = 0;
        foreach my $dataSet (@dataSets)
        {
            my $id = $dataIDs{$dataSet}{$indCount};
            $dataFrameStr .= "$id=$id, ";
            $meanStr .= "median($id), ";
            $labelStr .= "\"$dataSet\", ";
            $dataPositionsStr .= $dataCount.", ";
            $dataCount++;
        }
        chop( $dataFrameStr );
        chop( $dataFrameStr );
        chop( $meanStr );
        chop( $meanStr );
        chop( $labelStr );
        chop( $labelStr );
        chop( $dataPositionsStr );
        chop( $dataPositionsStr );

        # Create some handy R variables
        print INPUT "# IndCount => $indCount --------------------------\n";
        my $allDataIDStr = "allindcount".$indCount."data";
        print INPUT "$allDataIDStr = data.frame($dataFrameStr)\n";
        my $meansIDStr = "allindcount".$indCount."means";
        print INPUT "$meansIDStr = c($meanStr)\n";
        push( @meansIDStrings, $meansIDStr );
        my $labelsIDStr = "allindcount".$indCount."labels";
        print INPUT "$labelsIDStr = c($labelStr)\n";
        my $dataPositionsIDStr = "allindcount".$indCount."datapositions";
        print INPUT "$dataPositionsIDStr = seq(1,",$dataCount,")\n";

        print INPUT "$dataPositionsIDStr\n";
        print INPUT "$meansIDStr\n";

        # Build the EPS filename
        my $epsFile = $epsFilePrefix."-indcount-$indCount.eps";
        push( @epsFiles, $epsFile );

        # Build the plot
#        print INPUT "postscript( file=\"$epsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
        print INPUT "postscript( file=\"$epsFile\", height=5.5, width=6.83, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
        print INPUT "par(mar=c(4,4.5,1,3)+0.1)\n";
        print INPUT "par(mgp=c(3,0.8,0))\n";
        print INPUT "boxplot( $allDataIDStr, col=\"$boxColor\",ylim=c($yMin,$yMax),\n",
                "    ylab=\"Leadership success percentage\", names=c($labelStr),\n",
                "    xlab=\"$dataTypeTitle\", yaxt='n')\n";
        print INPUT "title( main=\"Group size $indCount\" )\n";
        print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
        print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
        print INPUT "lines(seq(1,$dataCount),$meansIDStr,col=\"$lineColor\",lwd=2)\n";
        print INPUT "dev.off()\n\n\n";
    }


    # ---------------------------------------------------------------
    # Build another combined plot
    my $combinedEpsFile = $epsFilePrefix."-$dataType-by-indcount-combined.eps";
    push( @epsFiles, $combinedEpsFile );
    print INPUT "datasets = (c($dataPositionsStr) + 1)\n";
#    print INPUT "postscript( file=\"$combinedEpsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "postscript( file=\"$combinedEpsFile\", height=5.5, width=6.83, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "par(mar=c(4,4.5,1,3)+0.1)\n";
    print INPUT "par(mgp=c(3,0.8,0))\n";
    my $index = 0;
    $dataSetNames = "";
    $colorsStr = "";
    $plotSymbolsStr = "";
    foreach my $indCount (@indCounts)
    {
        my $misc = "ylab=\"Leadership success percentage\",  xlab=\"$dataTypeTitle\", \n".
                    "    ylim=c($yMin,$yMax), ";
        my $cmd = "plot";
        if( $index > 0 )
        {
            $misc = "";
            $cmd = "lines";
        }
        print INPUT "$cmd( datasets, ",$meansIDStrings[$index],", type=\"o\", \n",
                    "    lwd=2, pch=",$plotSymbols[($index % ($#plotSymbols))],",\n",
                    "    ",$misc," col=\"",$colors[($index % ($#colors))],"\", yaxt='n', xaxt='n' )\n";

        my $cleanedIndCount = $indCount;
        $cleanedIndCount =~ s/^0*//;
        $dataSetNames .= "\"Size $cleanedIndCount\", ";
        $colorsStr .= "\"".$colors[($index % ($#colors))]."\", ";
        $plotSymbolsStr .= $plotSymbols[($index % ($#plotSymbols))].", ";

        $index++;
    }
    chop( $dataSetNames );
    chop( $dataSetNames );
    chop( $colorsStr );
    chop( $colorsStr );
    chop( $plotSymbolsStr );
    chop( $plotSymbolsStr );
    print INPUT "legend( \"$legendPosition\", c($dataSetNames), col=c($colorsStr), \n",
        "    pch=c($plotSymbolsStr), lty=1, lwd=2, bty=\"n\", ncol=",(ceil($#indCounts / 4))," )\n";
#print "# indcounts=[$#indCounts] ceil=[",(ceil($#indCounts / 3)),"]\n";
    print INPUT "axis( 1, las=1, labels=c($labelStr), at=datasets)\n";
    print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
    print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
    print INPUT "dev.off()\n\n\n";


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
            print INPUT "cat(\"\\n\")\n";

}

