#!/bin/bash

# Get the name of the current program
progname=`basename $0`

# Usage/help statement
print_help()
{
    cat <<'EOF'
Usage: $progname [options]

Options:
  -h, --help           Print this help message
      --oscer          Use OSCER logging configuration
      --debug          Use DEBUG logging configuration
      --params         Evolutionary parameters
      --seed           Random seed
      --logprefix      Logfile prefix
      --runid          Run ID
      --jargs          Arguments used by the JVM
      --mem            The amount of memory to give the JVM

      --ecargs         Arguments used by the evolutionary environment
      --config         Evaluation configuration
      --jobcount       Number of evolutionary runs to perform
      --statfile       Output file for evolutionary statistics
      --compressstats  Compress evolutionary statistics
      --statclass      Java class to use for statistics logging
EOF
}

# Ensure we have options
if [ $# = 0 ] ; then
    print_help
    exit 1
fi

# Command line argument listing
SHORTOPTS="h"
LONGOPTS="params:,statfile:,compressstats,statclass:,config:,seed:,logprefix:,runid:,jobcount:,jargs:,ecargs:,debug,help,oscer,mem:"

# Process the command line arguments
OPTS=`getopt -a -o $SHORTOPTS --long $LONGOPTS -n "$progname" -- "$@"`


# Parse the command line options
eval set -- "$OPTS"
while [ $# -gt 0 ]; do
    case $1 in
        -h|--help)
            print_help ; exit 0 ;;
        --debug)
            LOG_CONFIG=log-config/log4j-config-debug.xml ; shift ;;
        --oscer)
            LOG_CONFIG=log-config/log4j-oscer-config.xml ; shift ;;
        --mem)
            MEMORYSIZE=$2 ; shift 2 ;;
        --params)
            PARAMS=$2 ; shift 2 ;;
        --config)
            EVAL_CONFIG=$2 ; shift 2 ;;
        --seed)
            SEED=$2 ; shift 2 ;;
        --logprefix)
            LOGNAME_PREFIX=$2 ; shift 2 ;;
        --runid)
            RUNID=$2 ; shift 2 ;;
        --jobcount)
            JOBCOUNT=$2 ; shift 2 ;;
        --statfile)
            STATFILE=$2 ; shift 2 ;;
        --compressstats)
            COMPRESSSTATS=true ; shift ;;
        --statclass)
            STATCLASS=$2 ; shift 2 ;;
        --jargs)
            JVM_ARGS="${JVM_ARGS} $2" ; shift 2 ;;
        --ecargs)
            EC_ARGS=$2 ; shift 2  ;;
        --)
            shift ;;
        *)
            echo "Unknown evolve option: $1" ; exit 1 ;;
    esac
done


