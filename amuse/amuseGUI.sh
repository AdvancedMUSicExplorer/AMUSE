#!/bin/sh

export AMUSEHOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

java -javaagent:lib/jar-loader.jar -classpath lib/amuse-gui.jar:lib/amuse-frame.jar:lib/amuse-utils.jar:config/node/extractor/extractorNode.jar:config/node/processor/processorNode.jar:config/node/trainer/trainerNode.jar:config/node/classifier/classifierNode.jar:config/node/validator/validatorNode.jar:config/node/optimizer/optimizerNode.jar:lib/jama.jar:lib/jl1.0.jar:lib/launcher.jar:lib/log4j-1.2.14.jar:lib/miglayout-3.7-swing.jar:lib/rapidminer.jar:lib/tritonus_share-0.3.6.jar:lib/vldocking.jar:lib/xerces.jar:lib/weka.jar:lib/xpp3.jar:lib/xstream.jar:lib/mp3plugin.jar:lib/tritonus_remaining-0.3.6.jar:lib/jAudio.jar:lib/jhall.jar:lib/jar-loader.jar:lib/rapidminer_dependencies/* amuse.scheduler.gui.controller.WizardController
