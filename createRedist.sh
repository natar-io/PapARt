#!/bin/bash 

SKETCHBOOK=../sketchbook

mkdir ProCamLibrary 
mkdir ProCamLibrary/library
mkdir ProCamLibrary/examples
mkdir ProCamLibrary/examples/examples
mkdir ProCamLibrary/examples/calib
mkdir ProCamLibrary/examples/apps

echo "Copy Library"

# Library
cp ProCam/dist/ProCam.jar ProCamLibrary/library/ProCamLibrary.jar
cp -R ProCam/dist/lib ProCamLibrary/library/

# echo "Copy JavaCV, OpenCV and friends"
# libs are  javaCV and javaCV cppjars
# cp libs/* ProCamLibrary/library/


echo "Copy the sources" 
# copy the source also
cp -R ProCam/src ProCamLibrary/

echo "Copy the JavaDoc" 
cp -R ProCam/dist/javadoc ProCamLibrary/

echo "Copy the Data" 
cp -R ProCam/data ProCamLibrary/


echo "Copy Examples, Calibration & Apps" 
# Examples
cp -R $SKETCHBOOK/papartExamples/* ProCamLibrary/examples/examples/

# Calibration stuff
cp -R $SKETCHBOOK/papartCalibration/* ProCamLibrary/examples/calib/

# Apps 
cp -R $SKETCHBOOK/papartApps/* ProCamLibrary/examples/apps/


echo "Create the archive..." 
tar -zcf ProCamLibrary.tgz ProCamLibrary


echo "Clean " 
rm -rf ProCamLibrary


echo "Creation OK" 
