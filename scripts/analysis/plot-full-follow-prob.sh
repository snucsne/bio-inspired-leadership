#!/bin/bash

plotfile="/tmp/data.plot"

plotcmd="
set xlabel \"Follower\"
set xrange [0:10]

set ylabel \"Probability {/Symbol l} to join\"
set format y \"%4.2f\"

set size 0.65,0.65
set key out horiz center top

plot (x <= 9 ? (1/162.3)+((1/74.5)*x**2.3)/(2**2.3+x**2.3) : 1/0 ) title \"Per individual\" with lines lw 3 lc rgb \"#FFA500\"
replot (x <= 9 ? (10-x)*((1/162.3)+((1/74.5)*x**2.3)/(2**2.3+x**2.3)) : 1/0 ) title \"Any individual\" with lines lw 3 lc rgb \"#3333FF\"
set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texmf-texlive/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"
replot"

echo -e "$plotcmd" > $plotfile
gnuplot $plotfile
./replace-fonts-in-eps.pl /tmp/tmp.eps /tmp/fixed-tmp.eps
/usr/bin/epstool --copy --bbox  /tmp/fixed-tmp.eps /tmp/tmp.eps
epstopdf /tmp/tmp.eps
