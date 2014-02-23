#!/usr/bin/perl
use strict;
use POSIX;

# -------------------------------------------------------------------
# Get the data output file prefix
my $outputFilePrefix = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );

# Get the maximum indcount to compare
my $maxIndCount = shift( @ARGV );

# Get all the treatments and their titles
my @treatmentIDs;
my %treatments;
while( @ARGV )
{
    my $treatmentID = shift( @ARGV );
    push( @treatmentIDs, $treatmentID );
    $treatments{$treatmentID}{"dir"} = shift( @ARGV );
    $treatments{$treatmentID}{"desc"} = shift( @ARGV );
}


# -------------------------------------------------------------------
# Get the success percentages from each data directory
my %data;
foreach my $treatmentID (@treatmentIDs)
{
    # Read all the success percentages from the analysis directory
    my $dataDir = $treatments{$treatmentID}{"dir"}."/analysis/";
#print "dir=[$dataDir]  dataType=[$dataType]  dataSet=[$dataSet]\n";
    readSuccessPercentages( $dataDir, \%data, $treatmentID, $maxIndCount );
}


# -------------------------------------------------------------------
# Create the R input file
my $rInputFile = "/tmp/compare-success-percentages.r";
my $rOutputFile = $rInputFile.".out";
my @epsFiles = createRInput( $rInputFile,
        $outputFilePrefix."-results.dat",
        $epsFilePrefix,
        \@treatmentIDs,
        \%treatments,
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
    `./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
    `/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
    `epstopdf $epsFile`;
    my $pdfFile = $epsFile;
    $pdfFile =~ s/eps$/pdf/;
    my $title = $epsFile;
    $title =~ s/.*\/(.*)\.eps$/\1/;
    `exiftool -Title="$title" -Author="Brent E. Eskridge" $pdfFile`;
    # See the link below for more exiftool help with PDFs
    # http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/PDF.html
    # See the link below for more exiftool help with PostScript
    # http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/PostScript.html
}


# ===================================================================
sub readSuccessPercentages
{
    my ($dir, $dataRef, $treatmentID, $maxIndCount) = @_;

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

        if( $indCount <= $maxIndCount )
        {
            # Store the data
            $dataRef->{$treatmentID}{$indCount} = `cat $dir$successFile`;
        }
    }

}

# ===================================================================
sub createRInput
{
    my ($rInputFile, $resultsFile, $epsFilePrefix, $treatmentIDsRef, $treatmentsRef, $dataRef) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# =========================================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";
    print INPUT "library('igraph')\n";
    print INPUT "library('Hmisc')\n";

    # Build a standard error function
    print INPUT "stderr <- function(x) sd(x)/sqrt(length(x))\n\n";

    # ---------------------------------------------------------------
    # Add the data
    my %dataIDs;
    my @allDataIDs;
    my %treatmentIDHash;
    my %indCountHash;
    foreach my $treatmentID (@{$treatmentIDsRef})
    {
        foreach my $indCount (sort (keys %{$dataRef->{$treatmentID}} ) )
        {
            my $id = $treatmentID."indcount".$indCount;
            print INPUT "$id <- scan()\n";
            print INPUT $dataRef->{$treatmentID}{$indCount},"\n\n";
            $dataIDs{$treatmentID}{$indCount} = $id;
            push( @allDataIDs, $id );
            $indCountHash{$indCount} = 1;
        }
        $treatmentIDHash{$treatmentID} = 1;
    }
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
    foreach my $treatmentID (@{$treatmentIDsRef})
    {
        # Build the data
        my $dataFrameStr = "";
        my $meanStr = "";
        my $sdStr = "";
        my $seStr = "";
        my $labelStr = "";
        $dataPositionsStr = "";
        my $dataCount = 0;
        foreach my $indCount (sort (keys %{$dataIDs{$treatmentID}} ) )
        {
            my $id = $dataIDs{$treatmentID}{$indCount};
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
        print INPUT "# ",$treatments{$treatmentID}{"desc"}," --------------------------\n";
        my $allDataIDStr = "all".$treatmentID."data";
        print INPUT "$allDataIDStr = data.frame($dataFrameStr)\n";

        my $meansIDStr = "all".$treatmentID."means";
        print INPUT "$meansIDStr = c($meanStr)\n";
        push( @meansIDStrings, $meansIDStr );

        my $sdIDStr = "all".$treatmentID."sd";
        push( @sdIDStrings, $sdIDStr );
        print INPUT "$sdIDStr = c($sdStr)\n";

        my $seIDStr = "all".$treatmentID."se";
        push( @seIDStrings, $seIDStr );
        print INPUT "$seIDStr = c($seStr)\n";

        my $labelsIDStr = "all".$treatmentID."labels";
        print INPUT "$labelsIDStr = c($labelStr)\n";
        my $dataPositionsIDStr = "all".$treatmentID."datapositions";
        print INPUT "$dataPositionsIDStr = c(",$dataPositionsStr,")\n";


        # Build the EPS filename
#        my $treatmentIDStr = $treatmentID;
#        $treatmentIDStr =~ s/\./-/;
#        my $epsFile = $epsFilePrefix."-$treatmentIDStr.eps";
#        push( @epsFiles, $epsFile );

        # Build the plot
#        print INPUT "postscript( file=\"$epsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
#        print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
#        print INPUT "par(mgp=c(3,1,0))\n";
#        print INPUT "boxplot( $allDataIDStr, col=\"$boxColor\",ylim=c($yMin,$yMax),\n",
#                "    ylab=\"Leadership success percentage\", names=c($labelStr),\n",
#                "    xlab=\"Group size\", yaxt='n', at=$dataPositionsIDStr, boxwex=0.5, srt=-40)\n";
#        print INPUT "title( main=\"",$treatmentsRef->{$treatmentID}{"desc"},"\" )\n";
#        print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
#        print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
#
##        print INPUT "mtext( \"Shy\", side=2, line=2, adj=0.0 )\n";
##        print INPUT "mtext( \"Shy\", side=4, line=2, adj=0.0 )\n";
##        print INPUT "mtext( \"Moderate\", side=2, line=2, adj=0.5 )\n";
#        print INPUT "mtext( \"Moderate\", side=4, line=2, adj=0.5 )\n";
#        print INPUT "mtext( \"Bold\", side=2, line=2, adj=1.0 )\n";
#        print INPUT "mtext( \"Bold\", side=4, line=2, adj=1.0 )\n";
#
#        print INPUT "lines($dataPositionsIDStr,$meansIDStr,col=\"$lineColor\",lwd=2)\n";
#        print INPUT "dev.off()\n\n\n";
    }

    # ---------------------------------------------------------------
    # Build a combined plot
    my $combinedEpsFile = $epsFilePrefix."-combined.eps";
    push( @epsFiles, $combinedEpsFile );
    print INPUT "groupsizes = (c($dataPositionsStr) + 1)*5\n";
    print INPUT "groupsizes\n";
    print INPUT "postscript( file=\"$combinedEpsFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
    print INPUT "par(mgp=c(3,1,0))\n";
    my $index = 0;
    $legendPosition = "bottom";
#    my @colors = (  "#999933", "#117733", "#332288", "#882255", "#AA4499", "#88CCEE", "#CC6677", "#44AA99", "#DDCC77", "#000000" );
    my @colors = (  "#999933", "#117733", "#332288", "#9C0A2E", "#AA4499", "#88CCEE", "#CC6677", "#44AA99", "#DDCC77", "#000000" );
    my @plotSymbols = ( 1, 2, 5, 6, 0, 9, 3, 4, 7, 8 );
    my $dataSetNames = "";
    my $colorsStr = "";
    my $plotSymbolsStr = "";
    foreach my $treatmentID (@{$treatmentIDsRef})
    {
        my $misc = "ylab=\"Leadership success percentage\",  xlab=\"Group size\", \n".
                    "    ylim=c($yMin,$yMax), xlim=c((min(groupsizes)-5), (max(groupsizes)+5)), ";
        my $cmd = "plot";
        if( $index > 0 )
        {
            $misc = "";
            $cmd = "lines";
        }
        print INPUT "$cmd( (groupsizes+$index*0.5-0.5), ",$meansIDStrings[$index],", type=\"o\", \n",
                    "    lwd=2, pch=",$plotSymbols[$index],",\n",
                    "    ",$misc," col=\"",$colors[$index],"\", yaxt='n' )\n";

        print INPUT "par(fg=\"",$colors[$index],"\")\n";
        print INPUT "errbar( (groupsizes+$index*0.5-0.5), ",$meansIDStrings[$index],", \n",
                "    (",$meansIDStrings[$index],"+",$seIDStrings[$index],"), \n",
                "    (",$meansIDStrings[$index],"-",$seIDStrings[$index],"), \n",
                "    add=TRUE, lwd=0.5, pch=",$plotSymbols[$index],",\n",
                "    col=\"",$colors[$index],"\", yaxt='n' )\n";
        print INPUT "par(fg=\"black\")\n";

        # Add the name
        $dataSetNames .= "\"".$treatmentsRef->{$treatmentID}{"desc"}."\", ";
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
        "    pch=c($plotSymbolsStr), lty=1, lwd=2, bty=\"n\" )\n";
#        "    pch=c($plotSymbolsStr), lty=1, lwd=2, bty=\"n\", ncol=",(ceil((scalar @{$treatmentIDsRef}) / 3))," )\n";
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
    # Start sending output to the results file
    print INPUT "sink(\"$resultsFile\")\n";

    # ---------------------------------------------------------------
    # Add the statistics
    foreach my $treatmentID (sort (keys %dataIDs) )
    {
        foreach my $indCount (sort (keys %{$dataIDs{$treatmentID}} ) )
        {
            my $id = $dataIDs{$treatmentID}{$indCount};
            print INPUT "cat(\"# =====================================\\n\")\n";
            print INPUT "cat(\"# Treatment=[",$treatmentsRef->{$treatmentID}{"desc"},"] indCount=[$indCount] summary\\n\")\n";
            print INPUT "cat(\"summary.$treatmentID.indcount-$indCount.mean =   \", format( mean($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$treatmentID.indcount-$indCount.max =    \", format( max($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$treatmentID.indcount-$indCount.min =    \", format( min($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$treatmentID.indcount-$indCount.sd =     \", format( sd($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$treatmentID.indcount-$indCount.se =     \", format( stderr($id) ), \"\\n\")\n";
            print INPUT "cat(\"summary.$treatmentID.indcount-$indCount.length = \", format( length($id) ), \"\\n\")\n";
            print INPUT "cat(\"\\n\")\n";
        }
    }


    # ---------------------------------------------------------------
    # Add the KS tests for data with the same individual count
    foreach my $indCount (@indCounts)
    {
        for( my $i = 0; $i <= ($#{$treatmentIDsRef} - 1); $i++ )
        {
            for( my $j = $i + 1; $j <= ($#{$treatmentIDsRef}); $j++ )
            {
                my $firstTreatmentID = $treatmentIDsRef->[$i];
                my $secondTreatmentID = $treatmentIDsRef->[$j];
                print INPUT $rSpacer;
                print INPUT "cat(\"# KS IndCount [",
                        $indCount,
                        "]: [",
                        $treatmentsRef->{$firstTreatmentID}{"desc"},
                        "] vs [",
                        $treatmentsRef->{$secondTreatmentID}{"desc"},
                        "]\\n\")\n";
                printKSTestText( *INPUT,
                        $dataIDs{$firstTreatmentID}{$indCount},
                        "$firstTreatmentID-indCount-$indCount",
                        $dataIDs{$secondTreatmentID}{$indCount},
                        "$secondTreatmentID-indCount-$indCount" );
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
    print INPUT "sink()\n";

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
#return;
            print INPUT "ks <- ks.boot( ",
                    $firstID,
                    ",",
                    $secondID,
                    " )\n";
            print INPUT "cat(\"ks.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".p-value =       \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
            print INPUT "cat(\"ks.",
                    $firstKey,
                    ".vs.",
                    $secondKey,
                    ".naive-pvalue =  \", format(ks\$ks\$p.value), \"\\n\")\n";
            print INPUT "cat(\"ks.",
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

