#!/bin/bash

for NOISE in "05" "10" "15"; do

    ./perform-anova-tests.sh \
            ~/research/projects/data/leader/simple-v2/simple-dev-predation-noisy-mult-0.$NOISE/full-generational-data.csv \
            ~/research/projects/data/leader/simple-v2/simple-dev-with-other-noisy-0.$NOISE-predation-noisy-mult-0.$NOISE/full-generational-data.csv \
            ~/research/projects/data/leader/simple-v2/leader-noise-$NOISE-anova.txt

done

./perform-anova-tests.sh \
        ~/research/projects/data/leader/simple-v2/simple-dev-predation-accurate/full-generational-data.csv \
        ~/research/projects/data/leader/simple-v2/simple-dev-with-other-accurate-predation-accurate/full-generational-data.csv \
        ~/research/projects/data/leader/simple-v2/leader-noise-00-anova.txt

./perform-anova-tests.sh \
        ~/research/projects/data/leader/simple-v2/simple-dev-predation-accurate/full-generational-data.csv \
        ~/research/projects/data/leader/simple-v2/decision-dev-with-other-accurate-predation-random-alt-and-ind/full-generational-data.csv \
        ~/research/projects/data/leader/simple-v2/decision-anova.txt


