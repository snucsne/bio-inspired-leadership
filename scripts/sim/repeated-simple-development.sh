#!/bin/bash

for SEED in {1..40};
do
    echo Running with seed [$SEED]

    ./evolve.sh \
        --params evolve/experiments/simple-development.parameters \
        --logprefix simple-dev-experiment \
        --jargs -Dactivation-function-mapping-file=./cfg/activation-functions.properties \
        --runid simple-dev-experiment-$SEED \
        --statfile data/simple-dev-experiment-$SEED.stats \
        --jobcount 1 \
        --seed $SEED > simple-dev-experiment.out 2>&1
done
