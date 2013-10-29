#!/bin/bash

plotfile="/tmp/data.plot"

outputfile=$1
echo Output file [$1]
datafile=$2
echo Data file [$2]
xlabel=$3
echo X Label [$3]

plotcmd="
set ylabel \"Individual Success Percentage\"
set xlabel \"$xlabel\"
set yrange [0:1]
set format y \"%3.1f\"
set ytics 0.2
#set xtics 0.2

set size 0.65,0.65
#set key out center top
unset key

set style line 1 lt 1 lw 2
set style line 2 lt 2 lw 2 

#plot \"$datafile\" using 1:(\$2-\$5):(\$2+\$5) notitle with filledcurve lc rgb \"#FFE9BF\"
#replot \"$datafile\" using 1:2 notitle with lines lt 1 lw 3 lc rgb \"#FFA500\"
plot \"$datafile\" using 1:(\$2-\$5):(\$2+\$5) notitle with filledcurve lc rgb \"#CCCCFF\"
replot \"$datafile\" using 1:2 notitle with lines lt 1 lw 3 lc rgb \"#3333FF\"

set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texmf-texlive/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > $plotfile
gnuplot $plotfile
eps2eps /tmp/tmp.eps $outputfile
epstopdf $outputfile

