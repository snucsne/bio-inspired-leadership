#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# =========================================================
for DATA_DIR in $1/*indcount*;
do

    INDCOUNT=`basename $DATA_DIR`

    ./plot-bold-vs-success-percentages.pl \
            $DATA_DIR \
            $BASE_DIR/analysis/bold-vs-success-$INDCOUNT.dat \
            $BASE_DIR/figures/bold-vs-success-$INDCOUNT.eps

done

# =========================================================

