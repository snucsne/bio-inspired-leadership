#!/bin/bash

# =========================================================
# Get the base director
BASE_DIR=$1

if [ ! -d "$BASE_DIR/data" ]; then
    mkdir -p $BASE_DIR/data
fi
if [ ! -d "$BASE_DIR/figures" ]; then
    mkdir -p $BASE_DIR/figures
fi
if [ ! -d "$BASE_DIR/analysis" ]; then
    mkdir -p $BASE_DIR/analysis
fi

# =========================================================
for IND_COUNT in 10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95 100;
do

    FORMATTED_IND_COUNT=$(printf "%03d" $IND_COUNT)

    for USE_NN_GROUP_SIZE in "true" "false";
    do

        echo Analyzing ind count [$FORMATTED_IND_COUNT] use flag [$USE_NN_GROUP_SIZE]


        # Analyze the data
        ./analyze-eigenvector-centrality-hidden.pl \
                $BASE_DIR/data/spatial-hidden-variables-sim-indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_GROUP_SIZE.dat \
                $BASE_DIR/analysis/spatial-hidden-variables-sim-indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_GROUP_SIZE.stats \
                $BASE_DIR/figures/spatial-hidden-variables-sim-indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_GROUP_SIZE
exit
        # Insert the eigenvalue centrality values back into the data file
        ./insert-eigen-centrality-values.pl \
                $BASE_DIR/data/spatial-hidden-variables-sim-indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_GROUP_SIZE.dat \
                $BASE_DIR/analysis/spatial-hidden-variables-sim-indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_GROUP_SIZE.stats

        # Convert the data to a GEXF file for display
        ./convert-hidden-vars-to-gexf.pl \
                $BASE_DIR/data/spatial-hidden-variables-sim-indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_GROUP_SIZE.dat \
                $BASE_DIR/figures/spatial-hidden-variables-sim-indcount-$FORMATTED_IND_COUNT-usenn-$USE_NN_GROUP_SIZE.gexf


    done

    # Combine the figures
    ./combine-figures-and-analysis.pl \
            $BASE_DIR \
            "Individual count [$FORMATTED_IND_COUNT]" \
            $IND_COUNT 2>&1 > /tmp/test.out

done



