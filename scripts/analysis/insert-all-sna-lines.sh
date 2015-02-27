#!/bin/bash

for DATA_DIR in $1*indcount*;
do
#    echo Data dir [$DATA_DIR]

for MAP_DIR in $DATA_DIR/map*
do

    for DATA_FILE in $MAP_DIR/*.dat;
    do

#        echo Processing [$DATA_FILE]
        ./insert-sna-lines.pl $DATA_FILE

    done
done
done

