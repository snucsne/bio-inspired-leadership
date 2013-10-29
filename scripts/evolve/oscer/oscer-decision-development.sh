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

# Choose a random follow network
FOLLOWID=`choose-random.sh`
FOLLOWNET=`ls follow-network/*.ser | grep $FOLLOWID`

# Choose a random no-follow network
NOFOLLOWID=`choose-random.sh`
NOFOLLOWNET=`ls no-follow-network/*.ser | grep $NOFOLLOWID`



# Build our data directory
OTHERPART="other-$OTHERTYPE"
if [ $OTHERTYPE == "noisy" ]; then
    OTHERPART="other-$OTHERTYPE-$OTHERNOISEMULT"
fi
THISPART="$TYPE"
if [ $TYPE == "noisy" ]; then
    THISPART="$TYPE-mult-$NOISEMULT"
fi
DATADIR="data/decision-dev-with-$OTHERPART-predation-$THISPART-alt-and-ind"
mkdir $DATADIR

./evolve.sh \
    --params evolve/experiments/decision-development-with-other-and-alt-and-indicator.parameters \
    --logprefix log \
    --jargs -Dactivation-function-mapping-file=./cfg/activation-functions.properties \
    --runid decision-dev-with-other-experiment-$SEED \
    --statfile $DATADIR/decision-dev-$SEED.stats \
    --jobcount $JOBS \
    --ecargs "-p eval.problem.predation-input-type=$TYPE -p eval.problem.predation-noise-multiplier=$NOISEMULT -p eval.problem.other-network-file=$OTHERNET -p eval.problem.other-predation-input-type=$OTHERTYPE -p eval.problem.other-predation-noise-multiplier=$OTHERNOISEMULT -p eval.problem.follow-network-file=$FOLLOWNET -p eval.problem.no-follow-network-file=$NOFOLLOWNET" \
    --seed $SEED > decision-dev-$SEED.out 2>&1
