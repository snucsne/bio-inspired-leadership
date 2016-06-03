#!/bin/bash

LOCFILE=$1
OUTPUTPREFIX=$2
PREFERREDINITIATOR=$3

STEPS=`wc -l $LOCFILE | sed -E 's/([0-9]+).*/\1/'`

TEMPGEXF="/home/brent/tmp/temp-location.gexf"

for STEP in $(seq 1 1 $STEPS)
do
    # Get a formatted version for file names
    FORMATTED_STEP=$(printf '%05d' $STEP)

    OUTPUTFILE=$OUTPUTPREFIX-$FORMATTED_STEP.svg
#    echo Output file [$OUTPUTFILE]

    # Build the GEXF file
    ./build-gexf-from-locations-log.pl \
            $LOCFILE \
            $STEP \
            $TEMPGEXF \
            $PREFERREDINITIATOR

    if [ ! -f $TEMPGEXF ];
    then
        echo Temp GEXF file [$TEMPGEXF] does not exist
        exit
    fi

    CURRENT_DIR=`pwd`
#    echo Current directory [$CURRENT_DIR]

    # Export it as an image
    cd  ~/research/projects/leader-git/dist-test/
#    TEMPDIR=`pwd`
#    echo Changed to directory [$TEMPDIR]
    ./build-gephi-initiator.sh \
            $TEMPGEXF \
            $OUTPUTFILE > /dev/null
    cd $CURRENT_DIR
#    TEMPDIR=`pwd`
#    echo Changed back to directory [$TEMPDIR]


done

