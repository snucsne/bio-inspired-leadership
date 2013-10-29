#!/bin/bash

for dir in ~/research/projects/data/leader/simple/simple-dev-*; do
    echo "dir=[$dir]"
    ./calc-mean-simple-activity.pl $dir > /tmp/calc-mean-simple-activity.out 2>&1
done
