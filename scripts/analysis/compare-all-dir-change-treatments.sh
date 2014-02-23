#!/bin/bash

# =========================================================
# Get the data output prefix
DATA_OUTPUT_PREFIX=$1

# Get the figure output prefix
FIGURE_OUTPUT_PREFIX=$2

# Get the max indcount to compare
MAX_IND_COUNT=50

# =========================================================
for PERSONALITY in 0.2 0.5 0.8
do

    SHORTPER=`echo $PERSONALITY | sed -E 's/\.//'`

    ./compare-success-percentages-between-diff-treatments.pl \
            $DATA_OUTPUT_PREFIX-personality-$PERSONALITY \
            $FIGURE_OUTPUT_PREFIX-personality-$PERSONALITY \
            $MAX_IND_COUNT \
            basenoper \
            ../../dist-test/data/dir-change/baseline/ \
            "No personality" \
            baseper$SHORTPER \
            ../../dist-test/data/dir-change/baseline-personality/personality-$PERSONALITY/ \
            "No direction change, no decay" \
            dirchangeper$SHORTPER \
            ../../dist-test/data/dir-change/modify-reward/personality-$PERSONALITY/ \
            "Direction change, no decay" \
            dirchangedecay0100per$SHORTPER \
            ../../dist-test/data/dir-change/modify-reward-momentum-decay-0100/personality-$PERSONALITY/ \
            "Direction change, decay rate 100xN" \
            dirchangedecay0500per$SHORTPER \
            ../../dist-test/data/dir-change/modify-reward-momentum-decay-0500/personality-$PERSONALITY/ \
            "Direction change, decay rate 500xN"

done
