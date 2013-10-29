#!/bin/bash

DATA_FILE_1=$1
TITLE_1=$2

DATA_FILE_2=$3
TITLE_2=$4

DATA_FILE_3=$5
TITLE_3=$6

OUTPUT_EPS_FILE=$7


plotcmd="
set xlabel \"Cancel constant\"

set ylabel \"Correlation\"
#set xrange [-1:-0.5]
set ytics 0.25
set format y \"%4.2f\"

set size 0.65,0.65
set key out horiz center top

set style line 1 lt 1 lw 2
set style line 2 lt 2 lw 2 

plot \"$DATA_FILE_1\" using 1:2 title \"$TITLE_1\" with lines lw 3 lc rgb \"#FFA500\"
replot \"$DATA_FILE_2\" using 1:2 title \"$TITLE_2\" with lines lw 3 lc rgb \"#3333FF\"
replot \"$DATA_FILE_3\" using 1:2 title \"$TITLE_3\" with lines lw 3 lc rgb \"#000000\"

set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texmf-texlive/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > /tmp/mimics-vs-success-correlation.plot
gnuplot /tmp/mimics-vs-success-correlation.plot
eps2eps /tmp/tmp.eps $OUTPUT_EPS_FILE
epstopdf $OUTPUT_EPS_FILE

