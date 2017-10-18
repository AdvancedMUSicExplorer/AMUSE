#!/bin/sh

export AMUSEHOME=/home/workspace/amuse

java -classpath lib/amuse-gui.jar:lib/amuse-frame.jar:lib/amuse-utils.jar:config/node/extractor/extractorNode.jar:config/node/processor/processorNode.jar:config/node/trainer/trainerNode.jar:config/node/classifier/classifierNode.jar:config/node/validator/validatorNode.jar:config/node/optimizer/optimizerNode.jar:lib/jl1.0.jar:lib/launcher.jar:lib/log4j-1.2.14.jar:lib/miglayout-3.7-swing.jar:lib/rapidminer.jar:lib/tritonus_share-0.3.6.jar:lib/vldocking.jar:lib/weka.jar:lib/xpp3.jar:lib/xstream.jar:lib/mp3plugin.jar:lib/tritonus_remaining-0.3.6.jar amuse.scheduler.gui.controller.WizardController
