#!/bin/sh

# Load some system properties
. $AMUSEHOME/config/system.sh

#FOLDER=$1
#cd $FOLDER
#set YALE_HOME=$EXTRACTORNODE
#$JAVAPATH -Xmx1800m -classpath $EXTRACTORNODE/Yale/lib/yale.jar edu.udo.cs.yale.YaleCommandLine yaleBaseModified512.xml
#cd ..


cd $AMUSEHOME/tools/Yale

BATCH=$1
set YALE_HOME=$AMUSEHOME/tools/Yale

$JAVAPATH -Xmx2500m -classpath $AMUSEHOME/tools/Yale/lib/yale.jar edu.udo.cs.yale.YaleCommandLine $BATCH

#cd ..

