#!/bin/bash

# =========================================================

DATA_PREFIX=$1
EPS_PREFIX=$2

./plot-initial-bold-vs-success.pl \
        $DATA_PREFIX/indcount-010 \
        $EPS_PREFIX-010.eps \
        0.869356 \
        0.2

./plot-initial-bold-vs-success.pl \
        $DATA_PREFIX/indcount-020 \
        $EPS_PREFIX-020.eps \
        0.917203 \
        0.35

./plot-initial-bold-vs-success.pl \
        $DATA_PREFIX/indcount-030 \
        $EPS_PREFIX-030.eps \
        0.9359963306 \
        0.4

./plot-initial-bold-vs-success.pl \
        $DATA_PREFIX/indcount-040 \
        $EPS_PREFIX-040.eps \
        0.94408975 \
        0.45

./plot-initial-bold-vs-success.pl \
        $DATA_PREFIX/indcount-050 \
        $EPS_PREFIX-050.eps \
        0.948235 \
        0.42

./plot-initial-bold-vs-success.pl \
        $DATA_PREFIX/indcount-100 \
        $EPS_PREFIX-100.eps \
        0.9545249 \
        0.44

#pdftk ~/tmp/initial-vs-final*.pdf cat output ~/tmp/all-initial-vs-final.pdf
