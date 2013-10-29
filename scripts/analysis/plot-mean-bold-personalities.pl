#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the root data directory
my $rootDataDir = shift( @ARGV );

# Get the output file
#my $outputFile = shift( @ARGV );

# Get the eps file
my $epsFile = shift( @ARGV );


# -------------------------------------------------------------------
# Get each of the bold personality percentage files
opendir( DIR, "$rootDataDir/analysis/") or die "Unable to open analysis directory [$rootDataDir/analysis/]: $!\n";
my @boldFiles = sort( grep { /bold-personality-percentages/ } readdir( DIR ) );
closedir( DIR );


# -------------------------------------------------------------------
# Process each of the bold personality percentage files
my %data;
foreach my $boldFile (@boldFiles)
{
    # Get the individual count
    $boldFile =~ /indcount-(\d+)/;
    my $indCount = $1;

    # Get the percentages
    $data{$indCount} = `cat $rootDataDir/analysis/$boldFile`;
}


# -------------------------------------------------------------------
# Get each of the data directories
#opendir( DIR, $rootDataDir ) or die "Unable to open root data directory [$rootDataDir]: $!\n";
#my @dataDirs = grep { /indcount-/ } readdir( DIR );
#@dataDirs = sort( @dataDirs );
#closedir( DIR );

#print "Found ",($#dataDirs + 1)," directories\n";

# -------------------------------------------------------------------
# Process each of the data dirs
#my %data;
#foreach my $dataDir (@dataDirs)
#{
    # Get the individual count
#    $dataDir =~ /indcount-(\d+)/;
#    my $indCount = $1;

    # Get the percentage of bold personalities
#    $data{$indCount} = `./calc-bold-personality-percentages.pl $rootDataDir/$dataDir/`;
#    print "IndCount[$indCount]:  ";
#    print "./calc-bold-personality-percentages.pl $rootDataDir/$dataDir/\n";
#}

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/bold-personalities.r";
my $rOutputFile = $rInputFile.".out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";


# -------------------------------------------------------------------
# Add the data
my $dataFrameStr;
my $meanStr;
my $labels;
my $dataPositionsStr = "";
foreach my $indCount (sort (keys %data) )
{
    print INPUT "indcount$indCount <- scan()\n";
    print INPUT $data{$indCount},"\n\n";

    $dataFrameStr .= "indcount$indCount=indcount$indCount, ";
    $meanStr .= "median(indcount$indCount), ";
    my $indCountLabel = $indCount;
    $indCountLabel =~ s/^0+//;
    $labels .= "\"$indCountLabel\", ";
    $dataPositionsStr .= (($indCount/5)-1).", ";
}
chop( $dataFrameStr );
chop( $dataFrameStr );
chop( $meanStr );
chop( $meanStr );
chop( $labels );
chop( $labels );
chop( $dataPositionsStr );
chop( $dataPositionsStr );

print INPUT "alldata = data.frame($dataFrameStr)\n\n";
print INPUT "meanvalues = c($meanStr)\n\n";
print INPUT "datalabels = c($labels)\n\n";

# -------------------------------------------------------------------
print INPUT "datapositions = c($dataPositionsStr)\n";
print INPUT "realdatapositions = (datapositions + 1) * 5\n";
print INPUT "realdatapositions\n";
my $namesStr;
for( my $i = 0; $i <= ($#boldFiles); $i++ )
{
    $namesStr .= "\"\",";
}
chop( $namesStr );

# -------------------------------------------------------------------
# Plot it
my $yMin = 0.0;
my $yMax = 0.5;
my $yDiff = 0.1;
my $tmpEPSFile = "/tmp/bold-personalities.eps";
my $fixedTmpEPSFile = "/tmp/fixed-bold-personalities.eps";

print INPUT "postscript( file=\"$tmpEPSFile\", height=5.5, width=6.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";

print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
print INPUT "par(mgp=c(3,1,0))\n";

print INPUT "boxplot( alldata, col=\"#DDCC77\", ylim=c($yMin,$yMax), at=datapositions,\n",
        "    ylab=\"Bold Personality Percentage\", names=datalabels, xlab=\"Group Size\",\n",
        "    yaxt='n', boxwex=0.5 )\n";
#print INPUT "text( (datapositions-0.2), -0.05, labels=datalabels, srt=-50, xpd=NA, adj=0 )\n";
print INPUT "axis( 2, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
print INPUT "axis( 4, las=2, at=c(seq($yMin,$yMax,$yDiff)))\n";
print INPUT "lines(datapositions,meanvalues,col=\"#882255\",lwd=2)\n";
print INPUT "dev.off()\n";


# -------------------------------------------------------------------
# Close the input file
close( INPUT );


# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# -------------------------------------------------------------------
# Convert to PDF
`./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
`/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
`epstopdf $epsFile`;

