#!/bin/bash

for DATA_DIR in $1*indcount*;
do
    echo Processing [$DATA_DIR]

    if [ ! -d "$DATA_DIR/figures" ]; then
        mkdir -p $DATA_DIR/figures
    fi
    if [ ! -d "$DATA_DIR/analysis" ]; then
        mkdir -p $DATA_DIR/analysis
    fi

    INDCOUNT=`echo $DATA_DIR | sed -E 's/.*indcount-([0-9]+).*/\1/'`

    ./analyze-network-position-role.pl \
            $DATA_DIR/ \
            $DATA_DIR/analysis/network-position-$INDCOUNT-correlations.dat \
            $DATA_DIR/figures/network-position-$INDCOUNT-

    pdftk $DATA_DIR/figures/network-position-*.pdf \
            cat output \
            $DATA_DIR/figures/all-network-position-$INDCOUNT-figures.pdf

done

