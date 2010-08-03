#!/bin/sh 
# Load some system properties
. $AMUSEHOME/config/system.sh

cd $AMUSEHOME/tools/MatlabClassification

$MATLABHOME -nodisplay -nosplash -nojvm -r "matlabLDA('$1','$2','$3')" -logfile "$AMUSEHOME/tools/MatlabClassification/MatlabClassification.log"
