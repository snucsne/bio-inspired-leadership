#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# =========================================================
for DATA_DIR in $1/*indcount*;
do

    ./calc-optimal-bold-percentage.pl $DATA_DIR

done

# =========================================================

