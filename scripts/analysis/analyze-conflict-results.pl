#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the base directory
my $baseDir = shift( @ARGV );
unless( $baseDir =~ /\/$/ )
{
    $baseDir = "$baseDir/";
}

# Get the output file
my $outputFile = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );


# -------------------------------------------------------------------
# Get each of the conflict directories
opendir( DIR, $baseDir ) or die "Unable to open base directory [$baseDir]: $!\n";
my @conflictDirs = grep { /^conflict-?\.??/ } readdir( DIR );
@conflictDirs = sort( @conflictDirs );
closedir( DIR );

print "Found ",($#conflictDirs + 1)," conflict directories\n";

# -------------------------------------------------------------------
# Process each conflict directory
my %data;
foreach my $conflictDir (@conflictDirs)
{
    # Get the amount of conflict
    $conflictDir =~ /conflict-(\d\.\d\d)/;
    my $conflict = $1;

    # Get the directories for the different individual counts
    my $fullConflictDir = $baseDir.$conflictDir."/";
    opendir( DIR, $fullConflictDir ) or die "Unable to open conflict director [$fullConflictDir]: $!\n";
    my @indCountDirs = grep { /^indcount/ } readdir( DIR );
    @indCountDirs = sort( @indCountDirs );
    closedir( DIR );

    # Process each individual count
    foreach my $indCountDir (@indCountDirs)
    {
        # Get the individual count
        $indCountDir =~ /indcount-(\d+)/;
        my $indCount = $1;

        my $dataDir = $fullConflictDir.$indCountDir;
        
        # Grab the leadership success percentage
        my $successPercentage = `grep "total-leadership-success" $dataDir/*.dat | sed 's/.*=//' | tr '\n' ' '`;
        $successPercentage =~ s/^\s+//;
        $successPercentage =~ s/\s+$//;
        $data{$conflict}{$indCount} = $successPercentage;
#        print "conflict=[$conflict]  indCount=[$indCount]  $successPercentage\n";
    }
}

# -------------------------------------------------------------------
# Get the conflict and indCount values
my @conflictValues = keys( %data );
@conflictValues = sort( @conflictValues );
my @indCountValues = keys( %{$data{$conflictValues[0]}} );
@indCountValues = sort( @indCountValues );

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/conflict.r";
my $rOutputFile = "/tmp/conflict.r.out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

print INPUT "library(ggplot2)\n";

# -------------------------------------------------------------------
# Read in the summarySE function and send it to the file
my $summarySEFunction = `cat summary-se-function.r`;
print INPUT "\n".$summarySEFunction."\n\n";


# -------------------------------------------------------------------
# Dump the data for each treatment
my $fullData = "conflict  indcount  success\n";
foreach my $conflict (@conflictValues)
{
    foreach my $indCount (@indCountValues)
    {
        my $successValues = $data{$conflict}{$indCount};
        print INPUT buildDataID( $conflict, $indCount )." <- scan()\n";
        print INPUT $successValues;
        print INPUT "\n\n";

        # Split the success values
        my @values = split( /\s+/, $successValues );
        foreach my $value (@values)
        {
            $fullData .= sprintf( "%04.2f  %03d  %07.5f\n",
                    $conflict,
                    $indCount,
                    $value );
        }
    }
}

# -------------------------------------------------------------------
# Dump the data frame of the full data
print INPUT "df <- read.table( header=T, con <- textConnection('\n";
print INPUT $fullData;
print INPUT "'))\nclose(con)\n";
#print INPUT "df\$conflict <- factor(df\$conflict)\n";
print INPUT "df\$indcount <- factor(df\$indcount)\n";


# -------------------------------------------------------------------
# Summarize the data
print INPUT "dfc <- summarySE( df, measurevar=\"success\", groupvars=c(\"conflict\", \"indcount\") )\n\n";
print INPUT "dfc\n\n";

# -------------------------------------------------------------------
# Plot it
my $tmpREPSFile = "/tmp/conflict.eps";
print INPUT "postscript( file = \"$tmpREPSFile\", height=3.75, width=5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
print INPUT "par(mar=c(6.1,6.1,1.1,1.1))\n";
print INPUT "plot <- ggplot( dfc, aes(x=conflict, y=success, colour=indcount, group=indcount)) + \n";
print INPUT "geom_line() + \n";
print INPUT "labs(colour=\"Ind Count\") + \n";
#print INPUT "geom_point(size=3, shape=21, fill=\"white\") + \n";
print INPUT "scale_y_continuous(name=\"Leadership Success\", limits=c(0,1), breaks=seq(0,1,0.2)) + \n";
print INPUT "scale_x_continuous(name=\"Conflict\", limits=c(0,1), breaks=seq(0,1,0.2)) + \n";
print INPUT "theme_bw() + \n";
print INPUT "opts( axis.title.x = theme_text(size = 12, vjust = -0.25)) + \n";
print INPUT "opts( axis.title.y = theme_text(size = 12, vjust = 0.25, angle = 90)) + \n";
print INPUT "opts(legend.position=c(0.95,0.95), legend.justification=c(1,1))\n";
print INPUT "plot\n";
print INPUT "dev.off()\n";

# -------------------------------------------------------------------
# Close the input file
close( INPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;



# ===================================================================
sub buildDataID
{
    my ($conflict, $indCount) = @_;

    return "conflict".$conflict."ind".$indCount;
}



