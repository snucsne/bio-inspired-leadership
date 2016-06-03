#!/bin/bash

# =========================================================
# Get the execution directory
EXEC_DIR=$1

# Get the base directory
BASE_DIR=$2

cd $EXEC_DIR

# =========================================================
for DATA_DIR in $BASE_DIR/*indcount*;
do

    echo Processing [$DATA_DIR]...

    for GEXF_FILE in $DATA_DIR/*.gexf;
    do

        SVG_FILE=`echo $GEXF_FILE | sed -E 's/gexf/svg/'`
        PDF_FILE=`echo $GEXF_FILE | sed -E 's/gexf/pdf/'`

        ./build-gephi.sh $GEXF_FILE $SVG_FILE > /tmp/tmp.out

        inkscape -E /tmp/gephi.eps -f $SVG_FILE
        epstopdf /tmp/gephi.eps
        mv /tmp/gephi.pdf $PDF_FILE

#exit

    done
exit
done
