#!/bin/sh

# Load Amuse system properties
. $AMUSEHOME/config/system.sh

# Script parameters:
# Unique (for currently running Amuse instance) task Id
TASKID=$1

# Create directories & copy optimizer node resources
mkdir $OPTIMIZERNODE
cd $OPTIMIZERNODE
cp -rf $AMUSEHOME/config/node/optimizer/. .
mkdir $OPTIMIZERNODE/input
mkdir $OPTIMIZERNODE/input/task_$TASKID
mkdir $OPTIMIZERNODE/config
cp -f $AMUSEHOME/config/amuse.properties $OPTIMIZERNODE/config
cp -f $AMUSEHOME/config/classifierAlgorithmTable.arff $OPTIMIZERNODE/input/task_$TASKID
cp -f $AMUSEHOME/config/processorAlgorithmTable.arff $OPTIMIZERNODE/input/task_$TASKID
cp -f $AMUSEHOME/config/processorConversionAlgorithmTable.arff $OPTIMIZERNODE/input/task_$TASKID
cp -f $AMUSEHOME/config/validationAlgorithmTable.arff $OPTIMIZERNODE/input/task_$TASKID
cp -f $AMUSEHOME/config/optimizerAlgorithmTable.arff $OPTIMIZERNODE/input/task_$TASKID
mkdir $OPTIMIZERNODE/tools
cp -rf $AMUSEHOME/tools/RapidMiner4.5 $OPTIMIZERNODE/tools
mkdir $OPTIMIZERNODE/lib
cp -f $AMUSEHOME/lib/rapidminer.jar $OPTIMIZERNODE/lib
cp -f $AMUSEHOME/lib/weka.jar $OPTIMIZERNODE/lib
cp -f $AMUSEHOME/lib/log4j-1.2.14.jar $OPTIMIZERNODE/lib
cp -f $AMUSEHOME/lib/amuse-utils.jar $OPTIMIZERNODE/lib
cp -f $AMUSEHOME/lib/amuse-frame.jar $OPTIMIZERNODE/lib
cp -f $AMUSEHOME/config/node/processor/processorNode.jar $OPTIMIZERNODE/lib
cp -f $AMUSEHOME/config/node/trainer/trainerNode.jar $OPTIMIZERNODE/lib
cp -f $AMUSEHOME/config/node/classifier/classifierNode.jar $OPTIMIZERNODE/lib
cp -f $AMUSEHOME/config/node/validator/validatorNode.jar $OPTIMIZERNODE/lib
mv -f $AMUSEHOME/taskoutput/task_$TASKID.ser $OPTIMIZERNODE

# Create directories for intermediate database results
mkdir $OPTIMIZERNODE/input/task_$TASKID/Processed_Features
mkdir $OPTIMIZERNODE/input/task_$TASKID/Metrics
mkdir $OPTIMIZERNODE/input/task_$TASKID/Models
mkdir $OPTIMIZERNODE/input/task_$TASKID/processor
mkdir $OPTIMIZERNODE/input/task_$TASKID/processor/input
mkdir $OPTIMIZERNODE/input/task_$TASKID/processor/input/task_$TASKID
mkdir $OPTIMIZERNODE/input/task_$TASKID/validator
mkdir $OPTIMIZERNODE/input/task_$TASKID/validator/input
mkdir $OPTIMIZERNODE/input/task_$TASKID/validator/input/task_$TASKID
cp -f $AMUSEHOME/config/classifierAlgorithmTable.arff $OPTIMIZERNODE/input/task_$TASKID/validator/input/task_$TASKID

# Start processor node
$JAVAPATH -Djava.awt.headless=true -Xmx1800m -XX:MaxPermSize=128m -classpath tools/RapidMiner4.5/lib/xpp3.jar:tools/RapidMiner4.5/lib/colt.jar:lib/weka.jar:lib/log4j-1.2.14.jar:lib/amuse-utils.jar:lib/amuse-frame.jar:lib/rapidminer.jar:tools/RapidMiner4.5/lib/xstream.jar:lib/processorNode.jar:lib/trainerNode.jar:lib/classifierNode.jar:lib/validatorNode.jar:optimizerNode.jar amuse.nodes.optimizer.OptimizerNodeScheduler $OPTIMIZERNODE $TASKID

# DEBUG Copy the contents of validator node
#cp -r $OPTIMIZERNODE/ /home

# Copy the log and delete tmp folder
#cleanup:
cp -rf $OPTIMIZERNODE/optimizer.res $AMUSEHOME/optimizer.res
cp -rf $OPTIMIZERNODE/Amuse.log $AMUSEHOME/input/log.$LSB_JOBID
cd /tmp
rm -rf $OPTIMIZERNODE
