#!/bin/bash

# =========================================================
for DIR in $1*
do
    for DATA_DIR in $DIR/*indcount*;
    do

        PREFIX=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9])/\1/'`

#        echo Processing [$DATA_DIR] with prefix [$PREFIX]

        ./analyze-one-mean-dir.pl $DATA_DIR/ \
                $DIR/analysis/$PREFIX-dir-mean-one-analysis.dat \
                $DIR/figures/$PREFIX
    done
done
# =========================================================

