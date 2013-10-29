#!/bin/bash

plotfile="/tmp/data.plot"

echo Args: $@

inputfile=$1
echo Input file [$1]
outputfile=$2
echo Output file [$2]
xlabel=$3
echo X label [$3]
ylabel=$4
echo Y label [$4]
maxvalue=$5
echo Max value [$5]
minvalue=$6
echo Min value [$6]
title=$7
echo Title [$7]

if [ -n "$title" ]; then
    fulltitle="set title \"$title\""
fi


plotcmd="
$fulltitle
set xlabel \"$xlabel\"
set xrange [0:1]

set ylabel \"$ylabel\"
set yrange [0:1]

set cblabel \"Activity\"
set cbrange [$minvalue:$maxvalue]

set size 0.75,0.75
set view map
unset key

set palette defined (0 \"green\", 0.5 \"blue\", 1 \"red\")
set terminal postscript eps enhanced color \"NimbusSanL-Regu,17\" fontfile \"/usr/share/texmf-texlive/fonts/type1/urw/helvetic/uhvr8a.pfb\"
set output \"/tmp/tmp.eps\"

splot \"$inputfile\" using 1:2:3 with image"

echo -e "$plotcmd" > $plotfile
gnuplot $plotfile
eps2eps /tmp/tmp.eps $outputfile
epstopdf $outputfile
