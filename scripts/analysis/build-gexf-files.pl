#!/bin/bash

# =========================================================
# Get the base directory
BASE_DIR=$1

# Get the data directory
DATA_DIR=$2

# =========================================================
# Get the indivdiual count
INDCOUNT=`echo $DATA_DIR | sed -E 's/.*(indcount-[0-9]+).*/\1/'`

if [ ! -d "$BASE_DIR/figures/gephi/$INDCOUNT" ]; then
    mkdir -p $BASE_DIR/figures/gephi/$INDCOUNT
fi

# =========================================================
for DATA_FILE in `ls $DATA_DIR/*.dat`;
do

    PREFIX=`echo $DATA_FILE | sed -E 's/.*\/(.*)\.dat/\1/'`

    # Convert the data to a GEXF file for display
    ./convert-hidden-vars-to-gexf.pl \
            $DATA_FILE \
            $BASE_DIR/figures/gephi/$INDCOUNT/$PREFIX.gexf

exit
done

