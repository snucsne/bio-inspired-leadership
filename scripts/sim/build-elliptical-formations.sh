#!/bin/bash

OUTPUT_PREFIX=$1
INITIAL_LOC=$2
LOC_INCREMENT=$3
MAJOR_AXIS=1.5
MINOR_AXIS=0.5

for RING in {2..8}
do

    ./build-formation.pl $RING \
            0 \
            1 \
            $INITIAL_LOC \
            $LOC_INCREMENT \
             0 1 1 0 0 1 > $OUTPUT_PREFIX-circle-rings-$RING.loc

done

for RING in {2..8}
do

    ./build-formation.pl $RING \
            0 \
            1 \
            $INITIAL_LOC \
            $LOC_INCREMENT \
            0 \
            $MAJOR_AXIS \
            $MINOR_AXIS \
            1 0 0 1 > $OUTPUT_PREFIX-ellipse-rings-$RING.loc

done

