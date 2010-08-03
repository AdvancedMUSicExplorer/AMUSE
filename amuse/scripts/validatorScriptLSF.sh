#!/bin/sh

# Load Amuse system properties
. $AMUSEHOME/config/system.sh

# Script parameters:
# Unique (for currently running Amuse instance) task Id
TASKID=$1

# Create directories & copy validator node resources
mkdir $VALIDATORNODE
cd $VALIDATORNODE
cp -rf $AMUSEHOME/config/node/validator/. .
mkdir $VALIDATORNODE/input
mkdir $VALIDATORNODE/input/task_$TASKID
mkdir $VALIDATORNODE/config
cp -f $AMUSEHOME/config/amuse.properties $VALIDATORNODE/config
cp -f $AMUSEHOME/config/classifierAlgorithmTable.arff $VALIDATORNODE/input/task_$TASKID
cp -f $AMUSEHOME/config/validationAlgorithmTable.arff $VALIDATORNODE/input/task_$TASKID
mkdir $VALIDATORNODE/tools
cp -rf $AMUSEHOME/tools/RapidMiner4.5 $VALIDATORNODE/tools
mkdir $VALIDATORNODE/lib
cp -f $AMUSEHOME/lib/rapidminer.jar $VALIDATORNODE/lib
cp -f $AMUSEHOME/lib/weka.jar $VALIDATORNODE/lib
cp -f $AMUSEHOME/lib/log4j-1.2.14.jar $VALIDATORNODE/lib
cp -f $AMUSEHOME/lib/amuse-utils.jar $VALIDATORNODE/lib
cp -f $AMUSEHOME/lib/amuse-frame.jar $VALIDATORNODE/lib
cp -f $AMUSEHOME/config/node/trainer/trainerNode.jar $VALIDATORNODE/lib
cp -f $AMUSEHOME/config/node/classifier/classifierNode.jar $VALIDATORNODE/lib
mv -f $AMUSEHOME/taskoutput/task_$TASKID.ser $VALIDATORNODE

# Start validator node (headless mode is required for RapidMiner)
$JAVAPATH -Djava.awt.headless=true -Xmx1600m -classpath tools/RapidMiner4.5/lib/xpp3.jar:tools/RapidMiner4.5/lib/colt.jar:lib/weka.jar:lib/log4j-1.2.14.jar:lib/amuse-utils.jar:lib/amuse-frame.jar:lib/rapidminer.jar:tools/RapidMiner4.5/lib/xstream.jar:lib/trainerNode.jar:lib/classifierNode.jar:validatorNode.jar amuse.nodes.validator.ValidatorNodeScheduler $VALIDATORNODE $TASKID

# DEBUG Copy the contents of validator node
#cp -r $VALIDATORNODE/ /home/scripts

# Copy the log and delete tmp folder
cp -rf $VALIDATORNODE/Amuse.log $AMUSEHOME/taskinput/log.$LSB_JOBID
cd /tmp
rm -rf $VALIDATORNODE