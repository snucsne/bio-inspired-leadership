#!/bin/bash

JOBS=$1
echo Using job count [$JOBS]
TYPE=$2
echo Using predation type [$TYPE]
NOISEMULT=$3
echo Using predation noise multiplier [$NOISEMULT]
OTHERTYPE=$4
echo Using others predation type [$OTHERTYPE]
OTHERNOISEMULT=$5
echo Using others predation noise multiplier [$OTHERNOISEMULT]
OTHERNETDIR=$6
echo Using other network directory [$OTHERNETDIR]
SEED=$RANDOM
echo Using random seed [$RANDOM]

# Choose a random network to use as the other individual
OTHERID=`choose-random.sh`
OTHERNET=`ls $OTHERNETDIR/*.ser | grep $OTHERID`

# Build our data directory
OTHERPART="other-$OTHERTYPE"
if [ $OTHERTYPE == "noisy" ]; then
    OTHERPART="other-$OTHERTYPE-$OTHERNOISEMULT"
fi
THISPART="$TYPE"
if [ $TYPE == "noisy" ]; then
    THISPART="$TYPE-mult-$NOISEMULT"
fi
DATADIR="data/simple-dev-with-$OTHERPART-predation-$THISPART-alt"
mkdir $DATADIR

./evolve.sh \
    --params evolve/experiments/simple-development-with-other-and-alt.parameters \
    --logprefix log \
    --jargs -Dactivation-function-mapping-file=./cfg/activation-functions.properties \
    --runid simple-dev-with-other-experiment-$SEED \
    --statfile $DATADIR/simple-dev-with-other-$OTHERTYPE-predation-$TYPE-$SEED.stats \
    --jobcount $JOBS \
    --ecargs "-p eval.problem.predation-input-type=$TYPE -p eval.problem.predation-noise-multiplier=$NOISEMULT -p eval.problem.other-network-file=$OTHERNET -p eval.problem.other-predation-input-type=$OTHERTYPE -p eval.problem.other-predation-noise-multiplier=$OTHERNOISEMULT" \
    --seed $SEED > simple-dev-with-other-$OTHERTYPE-predation-$TYPE-$SEED.out 2>&1
