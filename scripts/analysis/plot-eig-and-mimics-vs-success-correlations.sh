#!/bin/bash

DATA_DIR=$1
USE_NN_FLAG=$2

EIG_OUTPUT_DATA_FILE=$DATA_DIR/analysis/eig-vs-success-correlation.dat
MIMICS_OUTPUT_DATA_FILE=$DATA_DIR/analysis/mimics-vs-success-correlation.dat
OUTPUT_EPS_FILE=$DATA_DIR/figures/eig-and-mimics-vs-success-correlation.eps

plotcmd="
set xlabel \"Individuals\"

set ylabel \"Correlation\"
set yrange [0.5:1]

set size 0.65,0.65
set key out center top

set style line 1 lt 1 lw 2
set style line 2 lt 2 lw 2 

plot \"$EIG_OUTPUT_DATA_FILE\" using 1:2 title \"Eigenvector centrality\" with lines lw 3 lc rgb \"#FFA500\"
replot \"$MIMICS_OUTPUT_DATA_FILE\" using 1:2 title \"Mimicking neighbors\" with lines lw 3 lc rgb \"#3333FF\"

set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texmf-texlive/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > /tmp/eig-and-mimics-vs-success-correlation.plot
gnuplot /tmp/eig-and-mimics-vs-success-correlation.plot
eps2eps /tmp/tmp.eps $OUTPUT_EPS_FILE
epstopdf $OUTPUT_EPS_FILE

