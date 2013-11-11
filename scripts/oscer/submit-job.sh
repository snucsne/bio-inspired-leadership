#!/bin/bash

# --------------------------------------------------------------
# Arguments
# 1: Job run count
# 2: Job name
# 3: Wall clock run time (hours)
# 4: Execution directory
# 5: Job command
# --------------------------------------------------------------

date=`date`

for i in `seq 1 $1`;
do
  run=`printf "%02d" $i`
  command=`echo $5`
  bsuboutput="#!/bin/tcsh

# Generated on $date
# Arguments: $*
# Run: $run of $1

# Submission details
#BSUB -q normal
#BSUB -n 1
#BSUB -o $4/tmp/$2-$run-%J.out
#BSUB -e $4/tmp/$2-$run-%J.err
#BSUB -W $3:00
#BSUB -J $2-$run
#BSUB -u eskridge@ou.edu
#BSUB -M 2000000
#BSUB -R \"rusage[mem=2000]\"

# Job execution
cd $4
pwd
setenv runnumber $run
date
time $5
date"
  
  # Dump the job information to a temporary file and submit it
  filename="$2-$run.bsub"
  echo "$bsuboutput" > $filename
  bsub < $filename
  rm $filename
  sleep 0.3
done


