#!/bin/bash

for DIR in ~/research/projects/leader/dist-test/data/conflict-complex/dir-mean-*/dir*
do
    echo Processing [$DIR]
    ./run-all-hidden-var-analysis.sh $DIR

done

