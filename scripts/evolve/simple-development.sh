#!/bin/sh

SEED=42

./evolve.sh \
    --params evolve/experiments/simple-development.parameters \
    --logprefix simple-dev-experiment \
    --jargs -Dactivation-function-mapping-file=./cfg/activation-functions.properties \
    --runid simple-dev-experiment-$SEED \
    --statfile data/simple-dev-experiment-$SEED.stats \
    --jobcount 1 \
    --seed $SEED \
    --debug > simple-dev-experiment.out 2>&1
