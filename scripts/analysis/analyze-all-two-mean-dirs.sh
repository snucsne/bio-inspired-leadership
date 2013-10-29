#!/bin/bash

# =========================================================
for DIR in $1*
do
    DIRDIFF=`echo $DIR | sed -E 's/.*dir-diff-([0-9]\.[0-9])-.*/\1/'`
    for DATA_DIR in $DIR/*indcount*;
    do

        PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9])/\1/'`

#        echo Processing [$DATA_DIR] with prefix [$PREFIX] and dirdiff [$DIRDIFF]

        ./analyze-two-mean-dirs.pl $DATA_DIR/ \
                $DIR/analysis/$PREFIX-dir-mean-two-analysis.dat \
                $DIR/figures/$PREFIX \
                $DIRDIFF
    done
done
# =========================================================

