#!/bin/bash

outputDir=$1
echo Output directory [$outputDir]

rFile="/tmp/contour.R"
outFile="/tmp/r.out"
height=480
width=480
pointsize=20

for participation in 0.00 0.10 0.20 0.30 0.40 0.50 0.60 0.70 0.80 0.90 1.00
do
    echo Processing participation [$participation]

        # COLOR
        outputFile=$outputDir/assertiveness-following-participation-$participation.eps
        cmd="
x <- seq(0.0, 1.0, length=100)
y <- seq(0.0, 1.0, length=100)

xexp <- (0.25 + $participation * 1.25)
xexp

z <- outer( x, y, function(x,y) (x^(xexp))*(y^(0.5)))


postscript( file = \"/tmp/tmp.eps\", height=4, width=4, onefile=FALSE, pointsize=11, horizontal=FALSE, paper=\"special\" )

par(mar=c(4,4,2,1)+0.1)

redgreenpal <- colorRampPalette( c( \"#66DD66\", \"#DD6666\") )
image(x,y,z,col=redgreenpal(100),xlab=\"Difference\",ylab=\"Assertiveness\", font.main=1, main=\"Conflict for Group Participation=[$participation]\")
contour(x,y,z,add=TRUE,levels=c(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9))

#my.colors <- function(n) c( rgb(0.6,0.6,1.0), rgb(1.0, 0.6, 0.6), rainbow(n-2) )
#filled.contour.no.legend(x,y,z,levels=c(0,1,10),main=expression(paste( S[p],\" = $sp  and  Order = $order\")), font.main=1,color=my.colors,xlab=expression(paste(P[c])),ylab=expression(paste(P[m])))
#contour(x,y,z,add=TRUE,xlab=expression(paste(P[c])),ylab=expression(paste(P[m])) )

dev.off()
"

    echo -e "$cmd" > $rFile
    R --no-save < $rFile > $outFile
    ./replace-fonts-in-eps.pl /tmp/tmp.eps $outputFile
    epstopdf $outputFile


done

