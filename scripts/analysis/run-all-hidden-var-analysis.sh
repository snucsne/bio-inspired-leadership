#!/bin/bash

# Base director
BASE_DIR=$1

PERSONALITY=1

# Insert extra SNA lines
echo Inserting SNA lines...
./insert-all-sna-lines.sh $BASE_DIR/

# Insert the modularity line
echo Inserting Modularity line...
./insert-all-modularity-lines.sh $BASE_DIR/

# Calculate all the SNA values
echo Calculating SNA values...
./calculate-all-sna-values.sh $BASE_DIR/

# Analyze the results (ignore the personality in the name)
echo Analyzing results...
./analyze-all-personality-spatial-hidden-vars-results.sh $BASE_DIR/

# Build the mean success percentages file
echo Building mean success percentages file...
cat $BASE_DIR/analysis/*success-percentage.dat > $BASE_DIR/analysis/success-percentages.dat

exit

# Process the history logs
#echo Processing history logs...
#./process-all-history-logs.sh $BASE_DIR/ 0


# Analyze the network modularities
#./extract-all-modularities.sh $BASE_DIR
#./analyze-modularities.sh $BASE_DIR \
#                          $BASE_DIR/analysis/modularities.stats \
#                          $BASE_DIR/figures/modularities.eps

# Plot the success percentages
# TODO

# Is it a personality run?
if [ ! -z $PERSONALITY ]; then
    # Plot the personality histories
    ./analyze-all-personality-results.sh $BASE_DIR/

    # Plot the mean bold personality percentages
#    ./plot-mean-bold-personalities.pl $BASE_DIR \
            $BASE_DIR/figures/mean-bold-personalities.eps

fi


