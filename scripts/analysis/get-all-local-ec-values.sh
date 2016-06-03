#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

if [ ! -d "$BASE_DIR/figures" ]; then
    mkdir -p $BASE_DIR/figures
fi

# =========================================================
for DATA_DIR in $BASE_DIR/*indcount*;
do

    # Get the individual count
    IND_COUNT=`echo $DATA_DIR | sed -E 's/.*indcount-([0-9]+).*/\1/'`
    MAX_IND_IDX=`expr $IND_COUNT - 1`
#    echo IndCount=[$IND_COUNT] max=[$MAX_IND_IDX]

    for MAP_DIR in $DATA_DIR/*map*;
    do

        MAP_ID=`echo $MAP_DIR | sed -E 's/.*map-([0-9]+).*/\1/'`

        # Grab any dat file (we only want the neighbor listing
        DAT_FILE=`ls $MAP_DIR/*.dat | head -n 1`;

        grep "eigenvector-centrality" $DAT_FILE | sed 's/.*\s*=\s*//' | tr '\n' ' ' > $DATA_DIR/analysis/eigenvector-centrality-indcount-$IND_COUNT-map-$MAP_ID.dat

    done

    cat $DATA_DIR/analysis/eigenvector-centrality-* > $DATA_DIR/analysis/all-eigenvector-centrality-indcount-$IND_COUNT.dat
    echo -e '\n' >> $DATA_DIR/analysis/all-eigenvector-centrality-indcount-$IND_COUNT.dat

done

