#!/usr/bin/perl
use strict;

my $rootDir = shift( @ARGV );
my $dirPrefix = shift( @ARGV );
my $outputFile = shift( @ARGV );
my $statsOutputFile = shift( @ARGV );

# -------------------------------------------------------------------
# Create an input file
my $inputFile = "/tmp/best-of-run.r";

open( INPUT, "> $inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";
print INPUT "library('Matching')\n";

# -------------------------------------------------------------------
# Add each raw fitness data
my $dataCount = 0;
my $dataVars = "";
my $dataNames = "";
my @names;

opendir( DIR, $rootDir ) or die "Unable to open root dir [$rootDir]: $\!\n";
my @rawDataDirs = grep { /$dirPrefix/ } readdir( DIR );
closedir( DIR );

@rawDataDirs = sort( @rawDataDirs );

foreach my $dir (@rawDataDirs)
{
    my $file = $dir."/overall-raw-fitness.data";
    my $data = `cat $file`;
    print INPUT "# $file\n";
    print INPUT "data$dataCount <- scan()\n";
    print INPUT $data,"\n\n";
    $dataVars .=" data$dataCount,";
    $dataNames .=" \"$dir\",";
    push( @names, $dir );
    $dataCount++;
}
chop( $dataVars );
chop( $dataNames );

# -------------------------------------------------------------------
# Create a boxplot

print INPUT "postscript( file = \"/tmp/tmp.eps\", height=8, width=12, onefile=FALSE, pointsize=11, horizontal=FALSE, paper=\"special\" )\n";
print INPUT "par(mar=c(3,30,3,2)+0.1)\n";
print INPUT "boxplot( $dataVars, col=\"blue\", horizontal=TRUE, names=c($dataNames), las=2, ylim=c(0,1) )\n";
print INPUT "dev.off()\n\n";


# -------------------------------------------------------------------
# Get some stats
print INPUT "sink(\"$statsOutputFile\")\n";
for( my $i = 0; $i < $dataCount; $i++ )
{
    print INPUT "cat(\"=====================================\\n\")\n";
    print INPUT "cat(\"data$i: $names[$i]\\n\")\n";
    print INPUT "summary(data$i)\n";
    print INPUT "cat(\"Standard deviation\\n\")\n";
    print INPUT "sd(data$i)\n\n";
    for( my $j = $i+1; $j < $dataCount; $j++ )
    {
        if( $i != $j )
        {
            print INPUT "cat(\"----------------------------\\n\")\n";
            print INPUT "cat(\"KS: $names[$i] VS $names[$j]\\n\")\n";
#            print INPUT "ks.boot(data$i,data$j)\n";
            print INPUT "ks <- ks.boot(data$i,data$j)\n";
#            print INPUT "attributes(ks)\n";
            print INPUT "summary(ks)\n";
            
        }
    }
    print INPUT "cat(\"\\n\\n\")\n";
}
print INPUT "sink()\n";

# -------------------------------------------------------------------
# Close the input file
close( INPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $inputFile > $outputFile 2>&1`;
