#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1


# =========================================================
for ASSERT_DIR in $BASE_DIR/assert*;
do

#echo assert=[ $ASSERT_DIR ]

    if [ ! -d "$ASSERT_DIR/figures" ]; then
        mkdir -p $ASSERT_DIR/figures
    fi
    if [ ! -d "$ASSERT_DIR/figures/gephi" ]; then
        mkdir -p $ASSERT_DIR/figures/gephi
    fi
    if [ ! -d "$ASSERT_DIR/analysis" ]; then
        mkdir -p $ASSERT_DIR/analysis
    fi

    for DATA_DIR in $ASSERT_DIR/indcount*;
    do

        PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9])/\1/'`

echo     data=[ $DATA_DIR ] prefix=[ $PREFIX ]

        ./analyze-spatial-hidden-vars-results.pl \
                $DATA_DIR \
                $ASSERT_DIR/analysis/$PREFIX.stats \
                $ASSERT_DIR/figures/$PREFIX > /tmp/analyze.out
    done
done

# =========================================================

