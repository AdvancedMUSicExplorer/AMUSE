#!/bin/sh

# Load Amuse system properties
. $AMUSEHOME/config/system.sh

# Script parameters:
# Unique (for currently running Amuse instance) task Id
TASKID=$1

# Create directories & copy trainer node resources
mkdir $TRAINERNODE
cd $TRAINERNODE
cp -rf $AMUSEHOME/config/node/trainer/. .
mkdir $TRAINERNODE/input
mkdir $TRAINERNODE/input/task_$TASKID
mkdir $TRAINERNODE/config
cp -f $AMUSEHOME/config/amuse.properties $TRAINERNODE/config
cp -f $AMUSEHOME/config/classifierAlgorithmTable.arff $TRAINERNODE/input/task_$TASKID
mkdir $TRAINERNODE/tools
cp -rf $AMUSEHOME/tools/Yale $TRAINERNODE/tools
mkdir $TRAINERNODE/lib
cp -f $AMUSEHOME/lib/yale.jar $TRAINERNODE/lib
cp -f $AMUSEHOME/lib/weka.jar $TRAINERNODE/lib
cp -f $AMUSEHOME/lib/log4j-1.2.14.jar $TRAINERNODE/lib
cp -f $AMUSEHOME/lib/amuse-utils.jar $TRAINERNODE/lib
cp -f $AMUSEHOME/lib/amuse-frame.jar $TRAINERNODE/lib
cp -f $AMUSEHOME/lib/xstream.jar $TRAINERNODE/lib
mv -f $AMUSEHOME/taskoutput/task_$TASKID.ser $TRAINERNODE

# Start trainer node
$JAVAPATH -Xmx1800m -classpath tools/Yale/lib/colt.jar:lib/weka.jar:lib/log4j-1.2.14.jar:lib/amuse-utils.jar:lib/amuse-frame.jar:lib/yale.jar:lib/xstream.jar:trainerNode.jar amuse.nodes.trainer.TrainerNodeScheduler $TRAINERNODE $TASKID

# DEBUG Copy the contents of trainer node
#cp -r $TRAINERNODE/ /home/scripts

# Copy the log and delete tmp folder
cp -rf $TRAINERNODE/Amuse.log $AMUSEHOME/input/log.$LSB_JOBID
cd /tmp
rm -rf $TRAINERNODE