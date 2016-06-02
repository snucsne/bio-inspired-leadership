# Overview of Analysis Scripts
This is a basic overview of the scripts used to analyze the results produced by the simulations.  Note that this is a basic overview and a work in progress.  Many of the scripts are customized to work in our development environment and naming conventions, and will require work to be used elsewhere.  We hope to add more documentation describing both in the future.

## Base directory
The base directory is the root directory containing all the results.  To simplify the remaining commands, a shell variable containing the directory is used.  The path can either be absolute or relative
```bash
$BASE_DIR=~/research/projects/leader-git/dist-test/data/local/
```

## Insert extra SNA lines
The first step is to insert additional lines for social network analysis (SNA) values.  This is done separately in an attempt to minimize the re-running of simulations to order to add extra SNA values.  Note that this doesn't calculate the values, it simply adds the necessary key-value pair lines to the result files.
```bash
./insert-all-sna-lines.sh $BASE_DIR/
```

## Calculate all the SNA values
After the extra SNA lines have been added, the values can be calculated.  This script uses R to calculate the values and then inserts them back into the results files.
```bash
./calculate-all-sna-values.sh $BASE_DIR/
```

## Analyze the results
This script parses the results, calculates values such as individual and group level success rates, and analyzes the results for statistically significant differences and correlations.  It creates plots of these results and creates GEXF files for use in Gephi to create plots of the communication network.
```bash
./analyze-all-personality-spatial-hidden-vars-results.sh $BASE_DIR/
```

## Analyze the history logs
If history logs of all the simulations performed were generated, this script analyzes them.  It uncompresses a single log at a time and processes it.
```bash
./process-all-history-logs.sh $BASE_DIR/ 
```
