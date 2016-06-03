#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# =========================================================
# Get the indcount directory
for IND_DIR in $BASE_DIR/indcount-*;
do

    # Go through each map
#    echo $IND_DIR
    for MAP_DIR in $IND_DIR/map-*;
    do
        ./build-gexf-files.pl $BASE_DIR/ $MAP_DIR/
#        echo $MAP_DIR
    done

done
