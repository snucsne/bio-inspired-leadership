#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the EPS output file
my $epsOutputFile = shift( @ARGV );

# Get all the data files
my @dataFiles;
my @dataDescs;
while( @ARGV )
{
    push( @dataFiles, shift( @ARGV ) );
    push( @dataDescs, shift( @ARGV ) );
}

# -------------------------------------------------------------------
my $plotFile = "/tmp/many-success-percentages.plot";
my $plotEPSFile = "/tmp/many-success-percentages.eps";
open( PLOT, "> $plotFile" ) or die "Unable to open plot file [$plotFile]\n";

print PLOT "set xlabel \"Individuals\"\n";
print PLOT "set ylabel \"Success Frequency\"\n";
print PLOT "set yrange [0:1.0]\n";
print PLOT "set xrange [10:70]\n";
print PLOT "set size 0.65,0.65\n";
print PLOT "set key out center top\n";
print PLOT "set style line 1 lt 1 lw 2\n";
print PLOT "set style line 2 lt 2 lw 2\n";



# -------------------------------------------------------------------
for( my $i = 0; $i <= $#dataFiles; $i++ )
{
    my $dataFile = $dataFiles[$i];
    my $title = $dataDescs;

    my $cmd = "replot ";
    if( 0 == $i )
    {
        $cmd = "plot ";
    }
    print PLOT "$cmd \"$dataFile\" using 1:2 title \"$title\" with lines lw 3\n";
}

# -------------------------------------------------------------------
#print PLOT "set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texmf-texlive/fonts/type1/urw/helvetic/uhvr8a.pfb\"\n";
print PLOT "set terminal postscript eps enhanced color \"Arial,17\" fontfile \"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf\"\n";
print PLOT "set output \"$plotEPSFile\"\n";
print PLOT "replot\n";

close( PLOT );


# -------------------------------------------------------------------
# Plot it
`gnuplot $plotFile`;
`eps2eps $plotEPSFile $epsOutputFile`;
`epstopdf $epsOutputFile`;

