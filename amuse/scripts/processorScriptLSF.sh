#!/bin/sh

# Load Amuse system properties
. $AMUSEHOME/config/system.sh

# Script parameters:
# Unique (for currently running Amuse instance) task Id
TASKID=$1

# Create directories & copy processor node resources
mkdir $PROCESSORNODE
cd $PROCESSORNODE
cp -rf $AMUSEHOME/config/node/processor/. .
mkdir $PROCESSORNODE/input
mkdir $PROCESSORNODE/input/task_$TASKID
mkdir $PROCESSORNODE/config
cp -f $AMUSEHOME/config/amuse.properties $PROCESSORNODE/config
cp -f $AMUSEHOME/config/processorAlgorithmTable.arff $PROCESSORNODE/input/task_$TASKID
cp -f $AMUSEHOME/config/processorConversionAlgorithmTable.arff $PROCESSORNODE/input/task_$TASKID
mkdir $PROCESSORNODE/tools
cp -rf $AMUSEHOME/tools/Normalizer $PROCESSORNODE/tools
mkdir $PROCESSORNODE/lib
cp -f $AMUSEHOME/lib/weka.jar $PROCESSORNODE/lib
cp -f $AMUSEHOME/lib/log4j-1.2.14.jar $PROCESSORNODE/lib
cp -f $AMUSEHOME/lib/amuse-utils.jar $PROCESSORNODE/lib
cp -f $AMUSEHOME/lib/amuse-frame.jar $PROCESSORNODE/lib
cp -f $FEATURELIST $PROCESSORNODE/input/task_$TASKID
mv -f $AMUSEHOME/taskoutput/task_$TASKID.ser $PROCESSORNODE

# Start processor node
$JAVAPATH -Xmx1600m -classpath lib/weka.jar:lib/log4j-1.2.14.jar:lib/amuse-utils.jar:lib/amuse-frame.jar:processorNode.jar amuse.nodes.processor.ProcessorNodeScheduler $PROCESSORNODE $TASKID

# DEBUG Copy the contents of processor node
#cp -r $PROCESSORNODE/ /home

# Copy the log and delete tmp folder
cp -rf $PROCESSORNODE/Amuse.log $AMUSEHOME/input/log.$LSB_JOBID
cd /tmp
rm -rf $PROCESSORNODE