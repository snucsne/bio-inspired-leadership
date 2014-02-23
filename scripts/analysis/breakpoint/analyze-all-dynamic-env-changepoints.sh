#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

shift

# =========================================================
for INDCOUNTDIR in $BASE_DIR/*indcount*;
do
    INDCOUNT=`echo $INDCOUNTDIR | sed -E 's/.*indcount-([0-9]+).*/\1/'`
    SHORTINDCOUNT=`echo $INDCOUNT | sed -E 's/0?([0-9]+)/\1/'`
    echo Processing IndCount[$INDCOUNT]

    ./analyze-dynamic-env-changepoints.pl \
            $BASE_DIR \
            $SHORTINDCOUNT \
            $BASE_DIR/analysis/indcount-$INDCOUNT-dynamic-env-changepoint-results.dat \
            $*
done
