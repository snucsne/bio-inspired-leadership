#!/bin/bash

./calc-mean-simple-activity.pl ../../dist-test/data > test.out

./make-activity-plots.sh ../../dist-test/data 0.6 0.6

./mencoder.sh ../../dist-test/data/activity-for-predation ../../dist-test/data/activity
