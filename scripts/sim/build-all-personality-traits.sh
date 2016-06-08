#!/bin/bash

# =========================================================
# Get the mean shortest paths directory
PATHS_DIR=$1

# =========================================================
for MSP_FILE in $PATHS_DIR/mean*.dat;
do
#    echo Processing [$MSP_FILE] ...
    ./build-personality-traits.pl $MSP_FILE

done
