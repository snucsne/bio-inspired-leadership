#!/bin/bash

for DATADIR in $1*indcount*;
do

    echo Calculating SNA values for [$DATADIR]

    for DATAFILE in $DATADIR/*.dat
    do

#        echo $DATAFILE
        ./calculate-sna-values.pl $DATAFILE
    done

#    tar cfz $DATADIR/bak-files.tar.gz $DATADIR/*.bak 
#    rm $DATADIR/*.bak

done
