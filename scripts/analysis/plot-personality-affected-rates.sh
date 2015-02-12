#!/bin/bash

OUTPUT_PREFIX=$1

YMAX=0.015
YTICS=0.005

#BOLDCOLOR="#882255"
BOLDCOLOR="#117733"
DEFAULTCOLOR="#000000"
#SHYCOLOR="#117733"
SHYCOLOR="#882255"

FOLLOWCOLOR="#332288"
CANCELCOLOR="#aa4499"

PLOT_FILE=/tmp/personality.plot

DEFAULT_OUTPUT=$OUTPUT_PREFIX"-default.eps"

plotcmd="
set xlabel \"Departed individuals\"
set ylabel \"Probability\"
set xrange [0:10]
set yrange [0:$YMAX]
set format y \"%5.3f\"
set ytics $YTICS

set size 0.65,0.62
set key out horiz center top

plot 0.1 title \"               \" lc rgb \"#FFFFFF\"
replot 0.009/(1+(x/2)**2.3) title \"Cancel\" with lines lt 1 lw 3 lc rgb \"$BOLDCOLOR\"
replot 1/(162.3+75.4*(10-x)/x) title \"Follow\" with lines lt 2 lw 3 lc rgb \"$SHYCOLOR\"


#set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texlive/texmf-dist/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set terminal postscript eps enhanced color \"Arial,17\" fontfile \"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > $PLOT_FILE
gnuplot $PLOT_FILE
/usr/bin/epstool --copy --bbox /tmp/tmp.eps $DEFAULT_OUTPUT
epstopdf $DEFAULT_OUTPUT


FOLLOWING_OUTPUT=$OUTPUT_PREFIX"-following.eps"

plotcmd="
set xlabel \"Departed individuals\"
set ylabel \"Probability\"
set xrange [0:10]
set yrange [0:$YMAX]
set format y \"%5.3f\"
set ytics $YTICS

set size 0.65,0.62
set key out horiz center top

plot 1.90515/(162.3+75.4*(10-x)/x) title \"Shy\" with lines lw 3 lt 2 lc rgb \"$SHYCOLOR\"
replot 1/(162.3+75.4*(10-x)/x) title \"Default / Moderate\" with lines lt 1 lw 3 lc rgb \"$DEFAULTCOLOR\"
replot 0.09485/(162.3+75.4*(10-x)/x) title \"Bold\" with lines lw 3 lt 5 lc rgb \"$BOLDCOLOR\"


#set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texlive/texmf-dist/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set terminal postscript eps enhanced color \"Arial,17\" fontfile \"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > $PLOT_FILE
gnuplot $PLOT_FILE
/usr/bin/epstool --copy --bbox /tmp/tmp.eps $FOLLOWING_OUTPUT
epstopdf $FOLLOWING_OUTPUT



CANCELING_OUTPUT=$OUTPUT_PREFIX"-canceling.eps"

plotcmd="
set xlabel \"Departed individuals\"
set ylabel \"Probability\"
set xrange [0:10]
set yrange [0:$YMAX]
set format y \"%5.3f\"
set ytics $YTICS

set size 0.65,0.62
set key out horiz center top

plot 1.90515*0.009/(1+(x/2)**2.3) title \"Shy\" with lines lt 2 lw 3 lc rgb \"$SHYCOLOR\"
replot 0.009/(1+(x/2)**2.3) title \"Default / Moderate\" with lines lt 1 lw 3 lc rgb \"$DEFAULTCOLOR\"
replot 0.09485*0.009/(1+(x/2)**2.3) title \"Bold\" with lines lt 5 lw 3 lc rgb \"$BOLDCOLOR\"

#set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texlive/texmf-dist/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set terminal postscript eps enhanced color \"Arial,17\" fontfile \"/usr/share/fonts/truetype/msttcorefonts/Arial.ttf\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > $PLOT_FILE
gnuplot $PLOT_FILE
/usr/bin/epstool --copy --bbox /tmp/tmp.eps $CANCELING_OUTPUT
epstopdf $CANCELING_OUTPUT
