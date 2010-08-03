#!/bin/sh 

# Load some system properties
. $AMUSEHOME/config/system.sh

module load matlab

cd $AMUSEHOME/tools/MatlabFeatures

$MATLABHOME -nodisplay -nosplash -nojvm -r "matlabBaseModified('$1','$2')" -logfile "$AMUSEHOME/tools/MatlabFeatures/MatlabFeatures.log"

