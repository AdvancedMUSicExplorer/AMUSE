#!/bin/sh

# Load some system properties
. $AMUSEHOME/config/system.sh

#FOLDER=$1
#cd $FOLDER
#set YALE_HOME=$EXTRACTORNODE
#$JAVAPATH -Xmx1800m -classpath $EXTRACTORNODE/Yale/lib/yale.jar edu.udo.cs.yale.YaleCommandLine yaleBaseModified512.xml
#cd ..


#cd $AMUSEHOME/tools/Yale
#set YALE_HOME=$AMUSEHOME/tools/Yale

YALE_HOME=$1
BATCH=$2

echo $YALE_HOME

echo $BATCH

cd $YALE_HOME


$JAVAPATH -Xmx1200m -classpath $YALE_HOME/lib/yale.jar edu.udo.cs.yale.YaleCommandLine $BATCH

#cd ..

