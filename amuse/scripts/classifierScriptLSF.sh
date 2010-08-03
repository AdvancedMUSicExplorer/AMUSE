#!/bin/sh

# Load Amuse system properties
. $AMUSEHOME/config/system.sh

# Script parameters:
# Unique (for currently running Amuse instance) task Id
TASKID=$1

echo $TASKID
echo $CLASSIFIERNODE

# Create directories & copy classifier node resources
mkdir $CLASSIFIERNODE
cd $CLASSIFIERNODE
cp -rf $AMUSEHOME/config/node/classifier/. .
mkdir $CLASSIFIERNODE/input
mkdir $CLASSIFIERNODE/input/task_$TASKID
mkdir $CLASSIFIERNODE/config
cp -f $AMUSEHOME/config/amuse.properties $CLASSIFIERNODE/config
cp -f $AMUSEHOME/config/classifierAlgorithmTable.arff $CLASSIFIERNODE/input/task_$TASKID
mkdir $CLASSIFIERNODE/tools
cp -rf $AMUSEHOME/tools/Yale $CLASSIFIERNODE/tools
mkdir $CLASSIFIERNODE/lib
cp -f $AMUSEHOME/lib/yale.jar $CLASSIFIERNODE/lib
cp -f $AMUSEHOME/lib/weka.jar $CLASSIFIERNODE/lib
cp -f $AMUSEHOME/lib/log4j-1.2.14.jar $CLASSIFIERNODE/lib
cp -f $AMUSEHOME/lib/amuse-utils.jar $CLASSIFIERNODE/lib
cp -f $AMUSEHOME/lib/amuse-frame.jar $CLASSIFIERNODE/lib
cp -f $AMUSEHOME/lib/xstream.jar $CLASSIFIERNODE/lib
mv -f $AMUSEHOME/taskoutput/task_$TASKID.ser $CLASSIFIERNODE

# Start classifier node
$JAVAPATH -classpath lib/weka.jar:lib/log4j-1.2.14.jar:lib/amuse-utils.jar:lib/amuse-frame.jar:lib/yale.jar:classifierNode.jar amuse.nodes.classifier.ClassifierNodeScheduler $CLASSIFIERNODE $TASKID

# DEBUG Copy the contents of classifier node
#cp -r $CLASSIFIERNODE/ /home/scripts

# Copy the log and delete tmp folder
cp -rf $CLASSIFIERNODE/Amuse.log $AMUSEHOME/input/log.$LSB_JOBID
cd /tmp
rm -rf $CLASSIFIERNODE