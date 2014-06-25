#!/bin/bash

WALL_TIME=12
BASE_DIR=/home/bcrouch/leader

for PERSONALITY in 0.2 0.5 0.8
do
    FORMATTED_PERSONALITY=`echo $PERSONALITY | sed 's/\.//'`

    for SLOPE_VALUE in 5 7 15 20
    do

        STEP_SIZE=10
        for RUN_START in $(seq 1 $STEP_SIZE 50)
        do

            FORMATTED_RUN_START=$(printf "%02d" $RUN_START)

            echo ./submit-job.sh \
                    1 \
                    S$FORMATTED_PERSONALITY$SLOPE_VALUE$FORMATTED_RUN_START \
                    $WALL_TIME \
                    $BASE_DIR \
                    \'./perform-sigmoid-personality-run.sh \
                    cfg/sim/hidden/spatial-hidden-variables-simulation-personality.parameters \
                    data/ \
                    $PERSONALITY \
                    sigmoid \
                    $RUN_START \
                    $STEP_SIZE \
                    edu.snu.leader.hidden.event.SigmoidPersonalityEventTimeCalculator \
                    $SLOPE_VALUE \'
        done

        echo ""

    done

    echo ""

done
