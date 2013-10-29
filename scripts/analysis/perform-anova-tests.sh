#!/bin/bash

CURRENT=`pwd`

FIRST=$1
SECOND=$2
OUTPUTFILE=$3


cd ~/grad/research/java-util/dist-test/

echo "Comparing: [$FIRST]=>[$SECOND]"
echo "    Sending to [$OUTPUTFILE]"

echo "*********************************************" >> $OUTPUTFILE
./two-way-anova.sh \
        $FIRST \
        $SECOND \
        > $OUTPUTFILE
echo "" >> $OUTPUTFILE
echo "*********************************************" >> $OUTPUTFILE
echo "" >> $OUTPUTFILE
./randomized-two-way-anova.sh 2000 0.95 \
        $FIRST \
        $SECOND \
        >> $OUTPUTFILE
cd $CURRENT
