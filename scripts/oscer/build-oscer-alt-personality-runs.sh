#!/bin/bash

WALL_TIME=12
BASE_DIR=/home/bcrouch/leader

for PERSONALITY in 0.2 0.5 0.8
do
    FORMATTED_PERSONALITY=`echo $PERSONALITY | sed 's/\.//'`

    STEP_SIZE=5
    for RUN_START in $(seq 1 $STEP_SIZE 50)
    do



        FORMATTED_RUN_START=$(printf "%02d" $RUN_START)

        echo ./submit-job.sh \
                1 \
                L$FORMATTED_PERSONALITY$FORMATTED_RUN_START \
                $WALL_TIME \
                $BASE_DIR \
                \'./perform-alt-personality-run.sh \
                cfg/sim/hidden/spatial-hidden-variables-simulation-personality.parameters \
                data/ \
                $PERSONALITY \
                linear \
                $RUN_START \
                $STEP_SIZE \
                edu.snu.leader.hidden.LinearPersonalityEventTimeCalculator \'


    done

    echo ""

done
