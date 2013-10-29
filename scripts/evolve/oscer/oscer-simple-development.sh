#!/bin/bash

JOBS=$1
echo Using job count [$JOBS]
TYPE=$2
echo Using predation type [$TYPE]
NOISEMULT=$3
echo Using predation noise multiplier [$NOISEMULT]
SEED=$RANDOM
echo Using random seed [$RANDOM]


DATADIR="data/simple-dev-predation-$TYPE"
if [ $TYPE == "noisy" ]; then
    DATADIR="data/simple-dev-predation-$TYPE-mult-$NOISEMULT"
fi
mkdir $DATADIR

./evolve.sh \
    --params evolve/experiments/simple-development.parameters \
    --logprefix log \
    --jargs -Dactivation-function-mapping-file=./cfg/activation-functions.properties \
    --runid simple-dev-experiment-$SEED \
    --statfile $DATADIR/simple-dev-predation-$TYPE-$SEED.stats \
    --jobcount $JOBS \
    --ecargs "-p eval.problem.predation-input-type=$TYPE -p eval.problem.predation-noise-multiplier=$NOISEMULT" \
    --seed $SEED > simple-dev-predation-$TYPE-$SEED.out 2>&1
