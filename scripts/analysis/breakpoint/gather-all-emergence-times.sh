#!/bin/bash

# =========================================================
# Get the root director
ROOT_DIR=$1


# =========================================================
for INDCOUNT in 10 15 20 25 30 40 50 60
do
    echo Processing indcount [$INDCOUNT]

    ./gather-emergence-breakpoint-times.pl \
            $ROOT_DIR \
            $INDCOUNT

done
