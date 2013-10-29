#!/bin/bash

# ** UNCOMMENT FOR EXECUTION ON OSCER **
# Use the JRE in the home directory
#PATH=$HOME/java/jre/bin:$PATH
#JAVA_PATH=$HOME/java/jre
#JAVA_HOME=$HOME/java/jre
# **************************************


# Get the host
if [ -z $HOST ]; then
  HOST=$HOSTNAME
fi

# Default values
LOG_CONFIG=log-config/log4j-config.xml
# LOG_CONFIG=log-config/log4j-oscer-config.xml
MEMORYSIZE=1024M
SEED=42
COMPRESSSTATS=false

# Put all the jar files in the lib dir in the classpath
CLASSPATH=leader.jar:./:lib/:data/:cfg/
for jarFile in lib/*.jar; do
	CLASSPATH=${jarFile}:$CLASSPATH
done

# Parse the options
source evolve-options.sh

# Build the logname
LOGNAME=$LOGNAME_PREFIX-$SEED


# Dump the configuration
echo =================================
echo EC Configuration
echo ---------------------------------
echo "                hostname: $HOST"
echo "             memory-size: $MEMORYSIZE"
echo "              log-config: $LOG_CONFIG"
echo "          logname-prefix: $LOGNAME_PREFIX"

# Build the params property
if [ -n "$PARAMS" ] ; then
  echo " evolutionary-parameters: $PARAMS"
  PARAM_PROPERTY="-file $PARAMS"
fi

# Build the random seed property
if [ -n "$SEED" ] ; then
  echo "             random-seed: $SEED"
  SEED_PROPERTY="-p seed.0=$SEED"
fi

# Build the run id property
if [ -n "$RUNID" ] ; then
  echo "                  run-id: $RUNID"
  RUNID_PROPERTY="-Drun-id=$RUNID"
fi

# Build the job count property
if [ -n "$JOBCOUNT" ] ; then
  echo "               job-count: $JOBCOUNT"
  JOBCOUNT_PROPERTY="-p jobs=$JOBCOUNT"
fi

# Build the statistics file property
if [ -n "$STATFILE" ] ; then
  echo "         statistics file: $STATFILE"
  STATFILE_PROPERTY="-p stat.file=$STATFILE"
fi

# Build the statistics compression flag
if [ -n "$COMPRESSSTATS" ] ; then
  echo "     compress statistics: $COMPRESSSTATS"
  COMPRESSSTATS_PROPERTY="-p stat.gzip=$COMPRESSSTATS"
fi

# Build the statistics class property
if [ -n "$STATCLASS" ] ; then
  echo "        statistics class: $STATCLASS"
  STATCLASS_PROPERTY="-p stat=$STATCLASS"
fi

echo "                jvm-args: $JVM_ARGS"
echo "                 ec-args: $EC_ARGS"
echo "                 logname: $LOGNAME"
echo =================================
echo ""
echo ""
echo Evolutionary output
echo ---------------------------------


# Start the application
java -cp $CLASSPATH \
        -Xmx$MEMORYSIZE \
        -server \
        -Dhostname=$HOST \
        -Djava.library.path=./lib/native \
        -Dlog4j.configuration=$LOG_CONFIG \
        $RUNID_PROPERTY \
        -Dlogname=$LOGNAME \
        $JVM_ARGS \
        ec.Evolve \
        $PARAM_PROPERTY $SEED_PROPERTY $STATFILE_PROPERTY \
        $COMPRESSSTATS_PROPERTY $STATCLASS_PROPERTY $JOBCOUNT_PROPERTY \
        $EC_ARGS

