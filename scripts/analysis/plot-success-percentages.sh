#!/bin/bash

DATA_DIR=$1

BASELINE_OUTPUT_DATA_FILE=$2

TRUE_OUTPUT_DATA_FILE=$DATA_DIR/analysis/success-percentages-usenn-true.dat
FALSE_OUTPUT_DATA_FILE=$DATA_DIR/analysis/success-percentages-usenn-false.dat
OUTPUT_EPS_FILE=$DATA_DIR/figures/success-percentages.eps

plotcmd="
set xlabel \"Individuals\"

set ylabel \"Success Frequency\"
set yrange [0:0.5]

set size 0.65,0.65
set key out center top

set style line 1 lt 1 lw 2
set style line 2 lt 2 lw 2 

plot \"$TRUE_OUTPUT_DATA_FILE\" using 1:2 title \"Local communication N = N_{c}\" with lines lw 3 lc rgb \"#FFA500\"
replot \"$FALSE_OUTPUT_DATA_FILE\" using 1:2 title \"Local communication N = G\" with lines lw 3 lc rgb \"#3333FF\"
replot \"$BASELINE_OUTPUT_DATA_FILE\" using 1:2 title \"Global communication\" with lines lw 3 lc rgb \"#000000\"

set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texmf-texlive/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > /tmp/success-percentages.plot
gnuplot /tmp/success-percentages.plot
eps2eps /tmp/tmp.eps $OUTPUT_EPS_FILE
epstopdf $OUTPUT_EPS_FILE

