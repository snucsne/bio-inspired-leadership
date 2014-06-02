#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the root data directory
my $rootDataDir = shift( @ARGV );

# Get the output file
#my $outputFile = shift( @ARGV );

# Get the eps file
my $epsFile = shift( @ARGV );

# Get the original results
my $optimalMean = shift( @ARGV );
my $optimalBold = shift( @ARGV );
#my $personality02Mean = shift( @ARGV );
#my $personality05Mean = shift( @ARGV );
#my $personality08Mean = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the bold personality percentage files
opendir( DIR, "$rootDataDir/analysis/") or die "Unable to open analysis directory [$rootDataDir/analysis/]: $!\n";
my @boldFiles = sort( grep { /success-percentages/ } readdir( DIR ) );
closedir( DIR );

# -------------------------------------------------------------------
# Process each of the bold personality percentage files
my %data;
foreach my $boldFile (@boldFiles)
{
    # Get the individual count and bold count
    $rootDataDir =~ /indcount-(\d+)/;
    my $indCount = $1;
    $boldFile =~ /boldcount-(\d+)/;
    my $boldCount = $1;

#    print "indCount=[$indCount] boldCount=[$boldCount] file=[$boldFile]\n";

    # Get the percentages
    $data{$indCount}{$boldCount} = `cat $rootDataDir/analysis/$boldFile`;
}

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/initial-bold-vs-success.r";
my $rOutputFile = $rInputFile.".out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

print INPUT "library('Hmisc')\n";

# -------------------------------------------------------------------
# Add the data
my $dataFrameStr;
my $meanStr;
my $sdStr;
my $labels;
my $dataPositionsStr = "";
my $indCountLabel;
foreach my $indCount (sort (keys %data) )
{
    foreach my $boldCount (sort (keys %{$data{$indCount}}) )
    {
        my $id = "indcount.$indCount.boldcount.$boldCount";
        print INPUT "$id <- scan()\n";
        print INPUT $data{$indCount}{$boldCount},"\n\n";

        $dataFrameStr .= "$id=$id, ";
        $meanStr .= "mean($id), ";
        $sdStr .= "sd($id), ";
        $indCountLabel = $indCount;
        $indCountLabel =~ s/^0+//;
        $labels .= "\"$indCountLabel\", ";
        my $boldCountLabel = $boldCount;
        $boldCountLabel =~ s/^0+//;
        $dataPositionsStr .= (($boldCountLabel/$indCountLabel)).", ";
    }
}

# Clean up the strings
$dataFrameStr =~ s/, $//;
$labels =~ s/, $//;
$meanStr =~ s/, $//;
$sdStr =~ s/, $//;
$dataPositionsStr =~ s/, $//;


# -------------------------------------------------------------------
print INPUT "alldata = data.frame($dataFrameStr)\n\n";
print INPUT "meanvalues = c($meanStr)\n\n";
print INPUT "sdvalues = c($sdStr)\n\n";
print INPUT "datalabels = c($labels)\n\n";
print INPUT "datapositions = c($dataPositionsStr)\n";

# -------------------------------------------------------------------
# Plot it
my @colors = (  "#117733", "#771155", "#9C0A2E", "#332288", "#999933" );
my @plotSymbols = ( 1, 2, 5, 6 );
my @lineTypes = ( 2, 6, 4 );
my $yMin = 0.5;
my $yMax = 1.0;
my $yDiff = 0.1;
my $xMin = 0.1;
my $xMax = 0.9;
my $xDiff = 0.2;
#my $tmpEPSFile = "/tmp/initial-vs-final-bold-personalities.eps";
#my $fixedTmpEPSFile = "/tmp/fixed-initial-vs-final-bold-personalities.eps";


print INPUT "postscript( file=\"$epsFile\", height=4.5, width=5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";

print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
print INPUT "par(mgp=c(2.5,0.75,0))\n";

#print INPUT "boxplot( alldata, col=\"#DDCC77\", ylim=c($yMin,$yMax), at=datapositions,\n",
#        "    ylab=\"Bold Personality Percentage\", names=datalabels, xlab=\"Group Size\",\n",
#        "    yaxt='n', boxwex=0.5 )\n";
#print INPUT "text( (datapositions-0.2), -0.05, labels=datalabels, srt=-50, xpd=NA, adj=0 )\n";
print INPUT "plot( datapositions, meanvalues, type=\"l\", yaxt='n', xaxt='n',\n",
            "    ylab=\"\", xlab=\"Initial Bold\",\n",
            "    ylim=c($yMin,$yMax), xlim=c(0.05,0.95), col=\"",$colors[0],"\",\n",
            "    lwd=2, pch=",$plotSymbols[0]," )\n";
print INPUT "par(fg=\"",$colors[0],"\")\n";
print INPUT "par(cex=0.5)\n";
print INPUT "errbar( datapositions, meanvalues, type=\"o\", \n",
            "    (meanvalues+sdvalues), (meanvalues-sdvalues), add=TRUE,\n",
            "    lwd=1.5, yaxt='n', pch=",$plotSymbols[0],", col=\"",$colors[0],"\")\n";
print INPUT "par(cex=1)\n";
print INPUT "par(fg=\"black\")\n";

print INPUT "title(ylab=\"Leadership Success\", mgp = c(3.25, 1, 0), cex=1.5 )\n";

print INPUT "ypercentlabels <- sprintf( \"%3d%%\", 100*seq($yMin,$yMax,$yDiff) )\n";
print INPUT "xpercentlabels <- sprintf( \"%3d%%\", 100*seq($xMin,$xMax,$xDiff) )\n";

print INPUT "axis( 1, las=1, at=c(seq($xMin,$xMax,$xDiff)), labels=xpercentlabels)\n";
print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)), labels=ypercentlabels)\n";
print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)), labels=ypercentlabels)\n";

print INPUT "abline( h=$optimalMean, col=\"#333333\", lwd=1.5, lty=2 )\n";
print INPUT "abline( v=$optimalBold, col=\"#333333\", lwd=1.5, lty=2 )\n";
#print INPUT "abline( h=$personality02Mean, col=\"",$colors[1],"\", lwd=1.5, lty=",$lineTypes[0]," )\n";
#print INPUT "abline( h=$personality05Mean, col=\"",$colors[2],"\", lwd=1.5, lty=",$lineTypes[1]," )\n";
#print INPUT "abline( h=$personality08Mean, col=\"",$colors[3],"\", lwd=1.5, lty=",$lineTypes[2]," )\n";
##print INPUT "lines(datapositions,meanvalues,col=\"#882255\",lwd=2)\n";
print INPUT "dev.off()\n";


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

#`./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
#`/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
#`epstopdf $epsFile`;

