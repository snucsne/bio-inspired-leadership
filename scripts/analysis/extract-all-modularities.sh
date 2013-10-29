#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# =========================================================
for DATA_DIR in $BASE_DIR*indcount*;
do

    echo `grep modularity $DATA_DIR/*.dat | awk '{print $3;}' | tr '\n' '  '` > $DATA_DIR/modularities.txt

done
