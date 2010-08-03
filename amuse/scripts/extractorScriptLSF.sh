#!/bin/sh

# Load Amuse system properties
. $AMUSEHOME/config/system.sh

# Script parameters:
# Unique (for currently running Amuse instance) task Id
TASKID=$1

# Create directories & copy extractor node resources
mkdir $EXTRACTORNODE
cd $EXTRACTORNODE
cp -rf $AMUSEHOME/config/node/extractor/. .
mkdir $EXTRACTORNODE/input
mkdir $EXTRACTORNODE/input/task_$TASKID
mkdir $EXTRACTORNODE/config
cp -f $AMUSEHOME/config/amuse.properties $EXTRACTORNODE/config
cp -f $AMUSEHOME/config/featureExtractorToolTable.arff $EXTRACTORNODE/input/task_$TASKID
mkdir $EXTRACTORNODE/tools
cp -rf $AMUSEHOME/tools $EXTRACTORNODE
mkdir $EXTRACTORNODE/lib
cp -f $AMUSEHOME/lib/weka.jar $EXTRACTORNODE/lib
cp -f $AMUSEHOME/lib/log4j-1.2.14.jar $EXTRACTORNODE/lib
cp -f $AMUSEHOME/lib/amuse-utils.jar $EXTRACTORNODE/lib
cp -f $AMUSEHOME/lib/amuse-frame.jar $EXTRACTORNODE/lib
cp -f $AMUSEHOME/lib/jl1.0.jar $EXTRACTORNODE/lib
cp -f $AMUSEHOME/lib/tritonus_share-0.3.6.jar $EXTRACTORNODE/lib
cp -f $AMUSEHOME/lib/tritonus_remaining-0-1.3.6.jar $EXTRACTORNODE/lib
mkdir $EXTRACTORNODE/lib/plugins
cp -rf $AMUSEHOME/lib/plugins $EXTRACTORNODE/lib
mv -f $AMUSEHOME/taskoutput/task_$TASKID.ser $EXTRACTORNODE

# Start extractor node
$JAVAPATH -Xmx1800m -classpath lib/weka.jar:lib/jl1.0.jar:lib/tritonus_share-0.3.6.jar:lib/tritonus_remaining-0-1.3.6.jar:lib/log4j-1.2.14.jar:lib/amuse-utils.jar:lib/amuse-frame.jar:extractorNode.jar amuse.nodes.extractor.ExtractorNodeScheduler $EXTRACTORNODE $TASKID

# DEBUG Copy the contents of extractor node
#cp -r $EXTRACTORNODE/ /home

# Copy the log and delete tmp folder
cp -r $EXTRACTORNODE/Amuse.log $AMUSEHOME/taskinput/log.$LSB_JOBID
cd /tmp
rm -rf $EXTRACTORNODE