#!/bin/bash

datadir=$1
echo Data directory [$1]
maxindactivity=$2
echo Max individual activity [$2]
maxanimactivity=$3
echo Max animation activity [$3]


./plot-activity-map.sh $datadir/no-energy-simple-development-activity.dat \
        $datadir/no-energy-simple-development-activity.eps \
        Predation \
        Maturation \
        $maxindactivity 0

./plot-activity-map.sh $datadir/no-maturation-simple-development-activity.dat \
        $datadir/no-maturation-simple-development-activity.eps \
        Predation \
        Energy \
        $maxindactivity 0

for predfile in $datadir/activity-for-predation*.dat; do

    predpngfile=`echo $predfile | sed  's/\.dat/\.png/'`
    predation=`echo $predfile | sed 's/.*predation-\(.*\)\.dat/\1/'`

    ./plot-activity-map-png.sh $predfile \
            $predpngfile \
            Energy \
            Maturation \
            $maxanimactivity 0 \
            "Predation = $predation"

done
