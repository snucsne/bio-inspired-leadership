#!/bin/bash

OUTPUT_PREFIX=$1

YMAX=0.015
YTICS=0.005

PLOT_FILE=/tmp/personality.plot

DEFAULT_OUTPUT=$OUTPUT_PREFIX"-default.eps"

plotcmd="
set xlabel \"Departed individuals\"
set ylabel \"\"
set xrange [0:10]
set yrange [0:$YMAX]
set format y \"%5.3f\"
set ytics $YTICS

set size 0.65,0.62
set key out horiz center top

plot 0.1 title \" \" lc rgb \"#FFFFFF\"
replot 0.009/(1+(x/2)**2.3) title \"Default cancellation rate\" with lines lt 1 lw 3 lc rgb \"#CC6677\"
replot 1/(162.3+75.4*(10-x)/x) title \"Default following rate\" with lines lt 2 lw 3 lc rgb \"#332288\"


set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texlive/texmf-dist/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > $PLOT_FILE
gnuplot $PLOT_FILE
/usr/bin/epstool --copy --bbox /tmp/tmp.eps $DEFAULT_OUTPUT
epstopdf $DEFAULT_OUTPUT


FOLLOWING_OUTPUT=$OUTPUT_PREFIX"-following.eps"

plotcmd="
set xlabel \"Departed individuals\"
set ylabel \"\"
set xrange [0:10]
set yrange [0:$YMAX]
set format y \"%5.3f\"
set ytics $YTICS

set size 0.65,0.62
set key out horiz center top

plot 2/(162.3+75.4*(10-x)/x) title \"k=2\" with lines lw 3 lc rgb \"#117733\"
replot 1/(162.3+75.4*(10-x)/x) title \"Default/k=1\" with lines lw 3 lc rgb \"#332288\"
replot 0.1/(162.3+75.4*(10-x)/x) title \"k=0.1\" with lines lw 3 lt 5 lc rgb \"#44AA99\"


set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texlive/texmf-dist/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > $PLOT_FILE
gnuplot $PLOT_FILE
/usr/bin/epstool --copy --bbox /tmp/tmp.eps $FOLLOWING_OUTPUT
epstopdf $FOLLOWING_OUTPUT



CANCELING_OUTPUT=$OUTPUT_PREFIX"-canceling.eps"

plotcmd="
set xlabel \"Departed individuals\"
set ylabel \"\"
set xrange [0:10]
set yrange [0:$YMAX]
set format y \"%5.3f\"
set ytics $YTICS

set size 0.65,0.62
set key out horiz center top

plot 2*0.009/(1+(x/2)**2.3) title \"k=2\" with lines lt 2 lw 3 lc rgb \"#882255\"
replot 0.009/(1+(x/2)**2.3) title \"Default/k=1\" with lines lt 1 lw 3 lc rgb \"#CC6677\"
replot 0.1*0.009/(1+(x/2)**2.3) title \"k=0.1\" with lines lt 5 lw 3 lc rgb \"#999933\"

set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texlive/texmf-dist/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > $PLOT_FILE
gnuplot $PLOT_FILE
/usr/bin/epstool --copy --bbox /tmp/tmp.eps $CANCELING_OUTPUT
epstopdf $CANCELING_OUTPUT
