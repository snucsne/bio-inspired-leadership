#!/bin/bash

for DATA_DIR in $1*indcount*;
do
#    echo Data dir [$DATA_DIR]

    for DATA_FILE in $DATA_DIR/*.dat;
    do

#        echo Processing [$DATA_FILE]
        ./insert-sna-lines.pl $DATA_FILE

    done
done

