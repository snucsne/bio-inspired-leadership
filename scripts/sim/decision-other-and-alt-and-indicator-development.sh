#!/bin/sh

SEED=42

./evolve.sh \
    --params evolve/experiments/decision-development-with-other-and-alt-and-indicator.parameters \
    --logprefix decision-dev-experiment \
    --jargs -Dactivation-function-mapping-file=./cfg/activation-functions.properties \
    --runid decision-dev-experiment-$SEED \
    --statfile data/decision-dev-experiment-$SEED.stats \
    --jobcount 1 \
    --seed $SEED  >  decision-dev-experiment.out 2>&1
