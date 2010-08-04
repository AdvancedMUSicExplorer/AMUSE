#!/bin/sh 

# Load some system properties
. $AMUSEHOME/config/system.sh

cd $AMUSEHOME/tools/MIRToolbox

$MATLABHOME -nodisplay -nosplash -nojvm -r "MIRToolboxBaseModified('$1','$2')" -logfile "$AMUSEHOME/tools/MIRToolbox/MIRToolbox.log"
