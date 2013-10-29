#!/bin/bash


for DIR in ~/research/projects/data/leader/simple-v2/simple-dev*; do

    echo Processing [$DIR]
    ./calc-mean-fitness.pl $DIR simple #> /tmp/tmp.out

done

exit

./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-predation-accurate/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-predation-random/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-predation-zero/ > /tmp/tmp.out

for noise in 0.05 0.10 0.15 0.20 0.30 0.40 0.50 0.60 0.70 0.80 0.90 1.0; do
    ./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-predation-noisy-mult-$noise/ > /tmp/tmp.out
done


./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-accurate-predation-accurate/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-accurate-predation-random/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.05-predation-noisy-mult-0.10/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.05-predation-random/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.10-predation-accurate/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.10-predation-noisy-mult-0.05/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.10-predation-random/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.15-predation-random/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-random-predation-accurate/ > /tmp/tmp.out


./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-accurate-predation-off-alt/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-accurate-predation-random-alt/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.10-predation-noisy-mult-0.15/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.15-predation-noisy-mult-0.10/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.20-predation-random/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.30-predation-random/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.40-predation-random/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-noisy-0.50-predation-random/ > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-random-predation-random/ > /tmp/tmp.out

./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-accurate-predation-noisy-mult-0.05 > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-accurate-predation-noisy-mult-0.10 > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-accurate-predation-noisy-mult-0.15 > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-random-predation-noisy-mult-0.05 > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-random-predation-noisy-mult-0.10 > /tmp/tmp.out
./calc-mean-fitness.pl ~/research/projects/data/leader/simple-dev-with-other-random-predation-noisy-mult-0.15 > /tmp/tmp.out

