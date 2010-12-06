#!/bin/sh 

# Load some system properties
. $AMUSEHOME/config/system.sh

cd $AMUSEHOME/tools/ChromaToolbox

$MATLABHOME -nodisplay -nosplash -nojvm -r "ChromaToolboxBaseModified('$1','$2')" -logfile "$AMUSEHOME/tools/ChromaToolbox/ChromaToolbox.log"
