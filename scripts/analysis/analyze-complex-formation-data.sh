#!/bin/bash

# =========================================================
# Get the base director
BASE_DIR=$1

if [ ! -d "$BASE_DIR/figures" ]; then
    mkdir -p $BASE_DIR/figures
fi
if [ ! -d "$BASE_DIR/analysis" ]; then
    mkdir -p $BASE_DIR/analysis
fi

# =========================================================
for FILE in `ls $BASE_DIR/data/`;
do

    TYPE=`echo $FILE | sed -E 's/.*formation-(.+)\.dat/\1/'`
    PREFIX=`echo $FILE | sed 's/\.dat//'`

    # Analyze the data
    ./analyze-eigenvector-centrality-hidden.pl \
            $BASE_DIR/data/$FILE \
            $BASE_DIR/analysis/$PREFIX.stats \
            $BASE_DIR/figures/$PREFIX

    # Insert the eigenvalue centrality values back into the data file
    ./insert-eigen-centrality-values.pl \
            $BASE_DIR/data/$FILE \
            $BASE_DIR/analysis/$PREFIX.stats

    # Convert the data to a GEXF file for display
    ./convert-hidden-vars-to-gexf.pl \
            $BASE_DIR/data/$FILE \
            $BASE_DIR/figures/$PREFIX.gexf

done



