#!/bin/bash


for DIR in ../../dist-test/data/conflict-complex/dir-mean-two/dir-diff*
do
    echo Processing [$DIR]
    ./build-all-rose-diagrams.pl $DIR indcount
done

