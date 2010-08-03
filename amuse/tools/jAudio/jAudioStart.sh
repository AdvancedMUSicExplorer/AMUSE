#!/bin/sh 

# Load some system properties
. $AMUSEHOME/config/system.sh

cd $AMUSEHOME/tools/jAudio

BATCH=$1

$JAVAPATH -Xmx1024m -classpath $AMUSEHOME/tools/jAudio/jhall.jar:$AMUSEHOME/tools/jAudio/mp3plugin.jar:$AMUSEHOME/tools/jAudio/tritonus_remaining-0.3.6.jar:$AMUSEHOME/tools/jAudio/tritonus_share-0.3.6.jar:$AMUSEHOME/tools/jAudio/jAudio.jar:$AMUSEHOME/lib/xerces.jar jAudioFE -b $BATCH
