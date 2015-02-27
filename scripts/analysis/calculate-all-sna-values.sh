#!/bin/bash

for DATA_DIR in $1*indcount*;
do

    echo Calculating SNA values for [$DATA_DIR]

for MAP_DIR in $DATA_DIR/map*
do

    for DATAFILE in $MAP_DIR/*.dat
    do

#        echo $DATAFILE
        SNAFILE="/tmp/sna-values.results"
        ./calculate-sna-values.pl $DATAFILE $SNAFILE
        ./replace-sna-values.pl $DATAFILE $SNAFILE
    done

#    tar cfz $DATADIR/bak-files.tar.gz $DATADIR/*.bak 
#    rm $DATADIR/*.bak

done
done
