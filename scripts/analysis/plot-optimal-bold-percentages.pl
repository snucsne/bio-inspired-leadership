#!/usr/bin/perl
use strict;
use POSIX;

# -------------------------------------------------------------------
# Get the input data file
my $dataFile = shift( @ARGV );

# Get the eps file file
my $epsFile = shift( @ARGV );

# -------------------------------------------------------------------
#  Grab the optimal bold percentages from the file
my %boldPercentages;
open( DATA, "$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

# Read each line
while( <DATA> )
{
    # Remove any other comments or whitespace
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

    my ($key,$value) = split( /\s+=\s+/, $_ );

    if( $key =~ /group-size-(\d+)\.optimal-bold-percentage/ )
    {
        my $groupSize = $1;
        $boldPercentages{$groupSize} = $value;
    }
}

close( DATA );

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/optimal-bold.r";
my $rOutputFile = $rInputFile.".out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

# -------------------------------------------------------------------
# Add the data
my $percentages;
my $groupSizes;
foreach my $groupSize (sort (keys %boldPercentages) )
{
    $percentages .= $boldPercentages{$groupSize}." ";
    $groupSizes .= $groupSize." ";
}

print INPUT "percentages <- scan()\n";
print INPUT $percentages,"\n\n";

print INPUT "groupsizes <- scan()\n";
print INPUT $groupSizes,"\n\n";

# -------------------------------------------------------------------
# Plot it
print INPUT "postscript( file=\"$epsFile\", height=5, width=5.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";

print INPUT "par(mar=c(8,8,3,3)+0.1)\n";
print INPUT "plot( groupsizes, percentages, type=\"o\", yaxt='n',\n",
            "    ylab=\"Bold Personality Percentage\", xlab=\"Group Size\",\n",
            "    ylim=c(0.1,0.5), xlim=c(0,160), col=\"#999933\",\n",
            "    lwd=2, pch=6 )\n";

print INPUT "percentlabels <- sprintf( \"%3d%%\", c(0, 10, 20, 30, 40, 50) )\n";

print INPUT "axis( 2, las=2, at=c(seq(0.0,0.5,0.1)), labels=percentlabels)\n";
print INPUT "axis( 4, las=2, at=c(seq(0.0,0.5,0.1)), labels=percentlabels)\n";

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

