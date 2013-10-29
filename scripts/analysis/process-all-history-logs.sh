#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

ALL_DATA_FLAG=$2

TEMP_LOG="/tmp/tmp.log"
TEMP_LOG_GZ="$TEMP_LOG.gz"

# =========================================================
# Process each data directory
for DATA_DIR in $1/*indcount*;
do

    echo Processing [$DATA_DIR]...

    GROUP_SIZE=`echo $DATA_DIR | sed -r 's/^.*indcount-([0-9]+).*$/\1/'`

    for LOG_FILE in $DATA_DIR/*.log.gz;
    do

#        echo Processing [$LOG_FILE]...

        OUTPUT_FILE=`echo $LOG_FILE | sed -E 's/\.log.gz/-processed-log.stat/'`

        # Clean up
        if [ -e $TEMP_LOG ]
        then
            rm $TEMP_LOG
        fi

        # Copy the gzipped file to a temp location and uncompress it
        cp $LOG_FILE $TEMP_LOG_GZ
        gunzip $TEMP_LOG_GZ

        # Process it
        ./process-history-log.pl $TEMP_LOG $LOG_FILE $OUTPUT_FILE $GROUP_SIZE $ALL_DATA_FLAG

    done
done
