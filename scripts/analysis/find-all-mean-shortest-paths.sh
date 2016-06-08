#!/bin/bash

for INDCOUNT_DIR in $1/indcount-*;
do
    INDCOUNT=`echo $INDCOUNT_DIR | sed 's/.*indcount-//'`
#    echo INDCOUNT [$INDCOUNT]

    for MAP_DIR in $INDCOUNT_DIR/map-*;
    do
        MAP=`echo $MAP_DIR | sed 's/.*map-//'`
#        echo   MAP [$MAP]

        grep mean-shortest-path $MAP_DIR/*Ind00000.dat | awk '{print $3;}' > $1/mean-shortest-path-indcount-$INDCOUNT-map-$MAP.dat

    done
done
