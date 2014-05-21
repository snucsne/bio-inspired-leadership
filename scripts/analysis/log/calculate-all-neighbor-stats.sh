#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# =========================================================
TMP_LOG=/tmp/leader-history.log
TMP_LOG_DETAILS=/tmp/leader-history-details.dat
for DATA_DIR in $1/*indcount*;
do

    # Grab the individual count
    IND_COUNT=`echo $DATA_DIR | sed -E 's/.*indcount-([0-9]+)/\1/'`

    echo Processing IndCount=[$IND_COUNT]


    # Process each results data file
    for DATA_FILE in $DATA_DIR/*.dat;
    do
        RUN_ID=`echo $DATA_FILE | sed -E 's/.*-var-([0-9]+)-.*/\1/'`

        # Grab the mean number of simulations needed for bold
        # individuals to emerge
        BOLD_STATS=$BASE_DIR/analysis/indcount-$IND_COUNT-breakpoint-analysis-$RUN_ID-stats.dat
        MEAN_SIMS=`grep mean $BOLD_STATS | awk '{print $3;}'`
        #echo   run=[$RUN_ID]  mean=[$MEAN_SIMS]

        # Extract the log details after the mean
        LOG_GZ=`echo $DATA_FILE | sed -E 's/\.dat/\.log.gz/'`
        #echo logGZ=[$LOG_GZ]
        gunzip -c $LOG_GZ > $TMP_LOG
        ./extract-log-details.pl $TMP_LOG S $MEAN_SIMS > $TMP_LOG_DETAILS

        # Calculate the neighbor stats
        NEIGHBOR_FILE=$BASE_DIR/analysis/indcount-$IND_COUNT-run-$RUN_ID-neighbor-stats.dat
        #echo neighborFile=[$NEIGHBOR_FILE]
        ./calculate-neighbor-stats.pl $TMP_LOG_DETAILS $DATA_FILE $NEIGHBOR_FILE

    done
done

rm $TMP_LOG
rm $TMP_LOG_DETAILS
# =========================================================

