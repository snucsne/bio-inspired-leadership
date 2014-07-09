#!/bin/bash

BASEDIR=~/research/projects/data/alt-personality
#BASEDIR=~/research/projects/leader-git/dist-test/data/

for SLOPEDIR in $BASEDIR/per-to-k-sigmoid*/
do
    for PERSONALITYDIR in $SLOPEDIR/personality*/;
    do
        echo Processing [$PERSONALITYDIR]
#        ./analyze-all-personality-spatial-hidden-vars-results.sh $PERSONALITYDIR
#        ./analyze-all-personality-results.sh $PERSONALITYDIR
        ./calc-all-bold-personality-percentages.sh $PERSONALITYDIR
    done
done
