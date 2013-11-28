#!/bin/bash 

SKETCHBOOK=../sketchbook

mkdir ProCamLibrary 
mkdir ProCamLibrary/library
mkdir ProCamLibrary/examples

echo "Copy Library"

# Library
cp ProCam/dist/ProCam.jar ProCamLibrary/library/ProCamLibrary.jar

echo "Copy JavaCV, OpenCV and friends"

# libs are  javaCV and javaCV cppjars
cp libs/* ProCamLibrary/library/


echo "Copy the sources" 
# copy the source also
cp -R ProCam/src ProCamLibrary/

echo "Copy Examples - Common and Calib" 
# Examples
cp -R $SKETCHBOOK/examples/* ProCamLibrary/examples/

# Source to create a new sketch
cp -R $SKETCHBOOK/common ProCamLibrary/common
cp -R $SKETCHBOOK/calib/config/config.pde ProCamLibrary/common/

# Calibration stuff
cp -R $SKETCHBOOK/calib ProCamLibrary/calib

#cp -R $SKETCHBOOK/data ProCamLibrary/data


echo "Create the archive..." 
tar -zcf ProCamLibrary.tgz ProCamLibrary


echo "Clean " 
rm -rf ProCamLibrary


echo "Creation OK" 
