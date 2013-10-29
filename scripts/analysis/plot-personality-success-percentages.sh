#!/bin/bash

DATA_DIR=$1

BASELINE=$DATA_DIR/baseline/analysis/success-percentages.dat
PERSONALITY01=$DATA_DIR/personality-0.2/analysis/success-percentages.dat
PERSONALITY05=$DATA_DIR/personality-0.5/analysis/success-percentages.dat
PERSONALITY09=$DATA_DIR/personality-0.8/analysis/success-percentages.dat

OUTPUT_EPS_FILE=$DATA_DIR/figures/success-percentages.eps

plotcmd="
set xlabel \"Individuals\"

set ylabel \"Success Frequency\"
set yrange [0:1.0]
set xrange [10:70]

set size 0.65,0.65
set key out center top

set style line 1 lt 1 lw 2
set style line 2 lt 2 lw 2 

plot \"$PERSONALITY01\" using 1:2 title \"Personality=[0.2]\" with lines lw 3 lc rgb \"#3333FF\"
replot \"$PERSONALITY05\" using 1:2 title \"Personality=[0.5]\" with lines lw 3 lc rgb \"#33FF33\"
replot \"$PERSONALITY09\" using 1:2 title \"Personality=[0.8]\" with lines lw 3 lc rgb \"#FF3333\"
replot \"$BASELINE\" using 1:2 title \"Baseline\" with lines lw 3 lc rgb \"#000000\"

set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texmf-texlive/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > /tmp/success-percentages.plot
gnuplot /tmp/success-percentages.plot
eps2eps /tmp/tmp.eps $OUTPUT_EPS_FILE
epstopdf $OUTPUT_EPS_FILE

