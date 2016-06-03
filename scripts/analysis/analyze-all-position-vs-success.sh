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

    ./analyze-position-vs-success.pl \
            $DATA_DIR/ \
            $DATA_DIR/analysis/position-vs-success-$INDCOUNT-correlations.dat \
            $DATA_DIR/figures/position-vs-success-$INDCOUNT-
    pdftk $DATA_DIR/figures/position-vs-success-*.pdf \
            cat output \
            $DATA_DIR/figures/all-position-vs-success-$INDCOUNT-figures.pdf
done

