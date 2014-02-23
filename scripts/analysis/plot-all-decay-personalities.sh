#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1


# =========================================================
for TYPE in momentum constant exponential linear;
do
    for MULT in $BASE_DIR/$TYPE/decay-mult-*;
    do

        for PERSONALITY in $MULT/personality-0.*;
        do

            ./analyze-all-personality-results.sh $PERSONALITY/
#read -p "Press [Enter] to continue..."
#exit
        done
    done
done

