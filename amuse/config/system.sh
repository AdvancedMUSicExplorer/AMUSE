#!/bin/sh

### Path to Amuse home
export AMUSEHOME=/home/workspace/amuse

### Path to Java
export JAVAPATH="/usr/share/jdk/1.6/bin/java"

### Path to Matlab
export MATLABHOME="/home/matlab/R2009b/bin/matlab"

### Path to Matlab Amuse script (see Matlab help for the explanation of MATLABPATH environment variable)
export MATLABPATH="$AMUSEHOME/tools/MatlabFeatures"

### Path to extractor node
export EXTRACTORNODE="/tmp/$USER.extractor$LSB_JOBID"

### Path to processor node
export PROCESSORNODE="/tmp/$USER.processor$LSB_JOBID"

### Path to classification trainer node
export TRAINERNODE="/tmp/$USER.trainer$LSB_JOBID"

### Path to classifier node
export CLASSIFIERNODE="/tmp/$USER.classifier$LSB_JOBID"

### Path to classifier validation node
export VALIDATORNODE="/tmp/$USER.validator$LSB_JOBID"

### Path to optimization node
export OPTIMIZERNODE="/tmp/$USER.optimizer$LSB_JOBID"

### Maximal size of extractor node input wave file 
export MAXWAVESIZE="25000000"

### Split at this number of seconds
export MAXSECONDS="480"