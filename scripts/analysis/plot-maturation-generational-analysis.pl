#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the output file
my $outputFile = shift( @ARGV );

my @directories;
my @titles;
while( @ARGV )
{
    push( @directories, shift( @ARGV ) );
    push( @titles, shift( @ARGV ) );
}


# -------------------------------------------------------------------
my $plotFile = "/tmp/generational.plot";
open( OUTPUT, "> $plotFile" ) or die "Unable to open [$plotFile]: $!\n";

print OUTPUT "set ylabel \"Fitness\"\n";
print OUTPUT "set xlabel \"Generations\"\n";
print OUTPUT "set format y \"%03.1f\"\n";

print OUTPUT "set yrange [0:1]\n";
print OUTPUT "set size 0.65,0.65\n";
print OUTPUT "set key out horiz center top\n";

print OUTPUT "set style line 1 lt 1 lw 2\n";

for( my $i = 0; $i <= $#directories; $i++ )
{
    my $fraction = $i / $#directories;
    print OUTPUT "\# fraction=[$fraction]\n";
    my $color = "#".sprintf( "%02x", ($fraction*255)).sprintf( "%02x", ($fraction*140)).sprintf( "%02x", ((1-$fraction)*255));
    my $cmd = "replot";
    if( 0 == $i )
    {
        $cmd = "plot";
    }
    print OUTPUT "$cmd \"$directories[$i]/generational-analysis.stats\" using 1:4 title \"$titles[$i]\" with lines lt 1 lw 2 lc rgb \"$color\"\n";
}

print OUTPUT "set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texmf-texlive/fonts/type1/urw/helvetic/uhvr8a.pfb\"\n";
print OUTPUT "set output \"/tmp/tmp.eps\"\n";
print OUTPUT "replot\n";

close( OUTPUT );


`gnuplot $plotFile`;
`eps2eps /tmp/tmp.eps $outputFile`;
`epstopdf $outputFile`;

