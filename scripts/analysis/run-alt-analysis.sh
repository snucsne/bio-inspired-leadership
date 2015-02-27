#!/bin/bash

./analyze-all-personality-results.sh /home/brent/research/projects/data/alt-personality/per-to-k-sigmoid-slope-05/personality-0.5
./analyze-all-personality-results.sh /home/brent/research/projects/data/alt-personality/per-to-k-sigmoid-slope-15/personality-0.5
./analyze-all-personality-results.sh /home/brent/research/projects/data/alt-personality/per-to-k-sigmoid-slope-20/personality-0.5
./analyze-all-personality-results.sh /home/brent/research/projects/data/alt-personality/per-to-k-linear/personality-0.5

cp ~/research/projects/data/alt-personality/per-to-k-sigmoid-slope-05/personality-0.5/figures/indcount-020-personality-run-13-history.* \
    ~/research/pub-git/bio2013-personality/figures/histories/sigmoid-05-personality-0.5/

cp ~/research/projects/data/alt-personality/per-to-k-sigmoid-slope-07/personality-0.5/figures/indcount-020-personality-run-12-history.* \
    ~/research/pub-git/bio2013-personality/figures/histories/sigmoid-07-personality-0.5/

cp ~/research/projects/data/alt-personality/per-to-k-sigmoid-slope-15/personality-0.5/figures/indcount-020-personality-run-18-history.* \
    ~/research/pub-git/bio2013-personality/figures/histories/sigmoid-15-personality-0.5/

cp ~/research/projects/data/alt-personality/per-to-k-sigmoid-slope-20/personality-0.5/figures/indcount-020-personality-run-24-history.* \
    ~/research/pub-git/bio2013-personality/figures/histories/sigmoid-20-personality-0.5/

cp ~/research/projects/data/alt-personality/per-to-k-linear/personality-0.5/figures/indcount-020-personality-run-40-history.* \
    ~/research/pub-git/bio2013-personality/figures/histories/linear-personality-0.5/

