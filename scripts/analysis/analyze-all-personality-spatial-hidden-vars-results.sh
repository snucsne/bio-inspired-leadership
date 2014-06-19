#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

if [ ! -d "$BASE_DIR/figures" ]; then
    mkdir -p $BASE_DIR/figures
fi
if [ ! -d "$BASE_DIR/figures/gephi" ]; then
    mkdir -p $BASE_DIR/figures/gephi
fi
if [ ! -d "$BASE_DIR/analysis" ]; then
    mkdir -p $BASE_DIR/analysis
fi

# =========================================================
for DATA_DIR in $1/*indcount*;
do

#    PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9]+-usenn-(false|true))/\1/'`
    PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9])/\1/'`

    ./analyze-spatial-hidden-vars-results.pl \
            $DATA_DIR \
            $BASE_DIR/analysis/$PREFIX.stats \
            $BASE_DIR/figures/$PREFIX > /tmp/analyze.out

    for DATA_FILE in $DATA_DIR/*.dat;
    do
#        echo Processing [$DATA_FILE]

        FILE_PREFIX=`echo $DATA_FILE | sed -E 's/.*([0-9][0-9]-seed-[0-9]+).dat/\1/'`


#        echo Sending to [$BASE_DIR/figures/gephi/$PREFIX/spatial-hidden-var-$FILE_PREFIX.gexf]
        if [ ! -d "$BASE_DIR/figures/gephi/$PREFIX" ]; then
            mkdir -p $BASE_DIR/figures/gephi/$PREFIX
        fi

        # Convert the data to a GEXF file for display
        ./convert-hidden-vars-to-gexf.pl \
                $DATA_FILE \
                $BASE_DIR/figures/gephi/$PREFIX/spatial-hidden-var-$FILE_PREFIX.gexf
    done
done

# =========================================================

