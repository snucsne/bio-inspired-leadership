#!/bin/bash

# =========================================================

./plot-initial-vs-final-bold-percentages.pl \
        ~/research/projects/leader/dist-test/data/personality-stability/indcount-010 \
        ~/tmp/initial-vs-final-010.eps 0.1 0.1 0.112

./plot-initial-vs-final-bold-percentages.pl \
        ~/research/projects/leader/dist-test/data/personality-stability/indcount-020 \
        ~/tmp/initial-vs-final-020.eps 0.087 0.122 0.251

./plot-initial-vs-final-bold-percentages.pl \
        ~/research/projects/leader/dist-test/data/personality-stability/indcount-030 \
        ~/tmp/initial-vs-final-030.eps 0.154664 0.184678 0.31867

./plot-initial-vs-final-bold-percentages.pl \
        ~/research/projects/leader/dist-test/data/personality-stability/indcount-040 \
        ~/tmp/initial-vs-final-040.eps 0.2385 0.248 0.366

./plot-initial-vs-final-bold-percentages.pl \
        ~/research/projects/leader/dist-test/data/personality-stability/indcount-050 \
        ~/tmp/initial-vs-final-050.eps 0.2928 0.2956 0.3896

./plot-initial-vs-final-bold-percentages.pl \
        ~/research/projects/leader/dist-test/data/personality-stability/indcount-100 \
        ~/tmp/initial-vs-final-100.eps 0.397 0.3998 0.4498

pdftk ~/tmp/initial-vs-final*.pdf cat output ~/tmp/all-initial-vs-final.pdf
