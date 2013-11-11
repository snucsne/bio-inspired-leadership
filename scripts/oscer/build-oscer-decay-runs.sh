#!/bin/bash

WALL_TIME=12
BASE_DIR=/home/eskridge/leader

for DECAY_TIME_MULT in 10 50 100 500 1000 2000
do

    FORMATTED_DECAY=$(printf "%04d" $DECAY_TIME_MULT)

    for CALC in edu.snu.leader.hidden.personality.ConstantDecay edu.snu.leader.hidden.personality.LinearDecay edu.snu.leader.hidden.personality.ExponentialDecay
    do

        CALC_DIR=`echo $CALC | sed 's/.*\.//' | sed 's/Decay//' | tr '[A-Z]' '[a-z]'`
        CALC_LETTER=`echo $CALC_DIR | sed -E 's/([a-z]).*/\1/'`

        for PERSONALITY in 0.2 0.5 0.8
        do

#            echo decay=[$DECAY_TIME_MULT]  calc=[$CALC]
            echo ./submit-job.sh \
                    1 \
                    $CALC_LETTER$FORMATTED_DECAY$PERSONALITY \
                    $WALL_TIME \
                    $BASE_DIR \
                    \'./perform-personality-decay.sh \
                    cfg/sim/hidden/spatial-hidden-variables-simulation-personality-decay.parameters \
                    data/$CALC_DIR/decay-mult-$FORMATTED_DECAY/ \
                    $PERSONALITY \
                    $CALC \
                    $DECAY_TIME_MULT \'

        done

#        echo ""

    done

#    echo ""

done
