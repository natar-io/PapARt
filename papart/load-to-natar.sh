#!/bin/sh

prog=fr.inria.papart.apps.ConfigurationLoader

#RGB Camera
mvn exec:java -Dexec.mainClass="${prog}" -Dexec.args="-p ${SKETCHBOOK}/libraries/PapARt/ -f data/calibration/camera.yaml -pd -o camera0:calibration"

#Depth Camera
mvn exec:java -Dexec.mainClass="${prog}" -Dexec.args="-p ${SKETCHBOOK}/libraries/PapARt/ -f data/calibration/calibration-AstraS-depth.yaml -o camera0:calibration:depth -pd"
mvn exec:java -Dexec.mainClass="${prog}" -Dexec.args="-p ${SKETCHBOOK}/libraries/PapARt/ -f data/calibration/calibration-AstraS-stereo.xml -o camera0:extrinsics:depth -m"

#Projector
mvn exec:java -Dexec.mainClass="${prog}" -Dexec.args="-p ${SKETCHBOOK}/libraries/PapARt/ -f data/calibration/projector.yaml -o projector0:calibration -pd -pr"
mvn exec:java -Dexec.mainClass="${prog}" -Dexec.args="-p ${SKETCHBOOK}/libraries/PapARt/ -f data/calibration/camProjExtrinsics.xml -o projector0:extrinsics -m -i"

#Table
mvn exec:java -Dexec.mainClass="${prog}" -Dexec.args="-p ${SKETCHBOOK}/libraries/PapARt/ -f data/calibration/tablePosition.xml -m -o camera0:table:position"
mvn exec:java -Dexec.mainClass="${prog}" -Dexec.args="-p ${SKETCHBOOK}/libraries/PapARt/ -f data/calibration/tablePosition.xml -m -o camera0:table"
