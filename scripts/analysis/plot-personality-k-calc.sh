#!/bin/bash

OUTPUT_PREFIX=$1

PLOT_FILE=/tmp/personalityk.plot

EPS_OUTPUT=$OUTPUT_PREFIX"personality-k-calc.eps"

plotcmd="
set xlabel \"Leadership Tendency (LT) Value\"
set ylabel \"k Factor\"
set xrange [0:1]
set format y \"%3.1f\"
set format x \"%4.2f\"
set ytics 0.5
set xtics 0.25

set grid ytics lt 0 lw 1 lc rgb \"#666666\"
set grid xtics lt 0 lw 1 lc rgb \"#666666\"

set size 0.65,0.65
set key out horiz center top


plot (2 * (1/(1+exp((0.5-x)*10)))) title \"Over-initiation\" with lines lt 1 lw 3 lc rgb  \"#CC6677\"
replot (2 * (1/(1+exp((0.5-(1-x))*10)))) title \"Over-following and Under-canceling\" with lines lt 1 lw 3 lc rgb  \"#332288\"
set terminal postscript eps enhanced color \"Arial,22\" fontfile \"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf\" size 9,2.75
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > $PLOT_FILE
gnuplot $PLOT_FILE
/usr/bin/epstool --copy --bbox /tmp/tmp.eps $EPS_OUTPUT
epstopdf $EPS_OUTPUT

