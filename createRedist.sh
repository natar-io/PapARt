#!/bin/bash 

SKETCHBOOK=../sketchbook
NAME=ProCamLibrary

## NAME must match 
## fr.inria.papart.procam.Utils.LibraryName = "ProCam";

mkdir $NAME 
mkdir $NAME/library
mkdir $NAME/examples
mkdir $NAME/examples/examples
mkdir $NAME/examples/calib
mkdir $NAME/examples/apps

echo "Copy Library"

# Library
cp ProCam/dist/ProCam.jar $NAME/library/$NAME.jar
#cp -R ProCam/dist/lib $NAME/library/

tar -zcf libs.tgz ProCam/dist/lib

# echo "Copy JavaCV, OpenCV and friends"
# libs are  javaCV and javaCV cppjars
# cp libs/* $NAME/library/


echo "Copy the sources" 
# copy the source also
cp -R ProCam/src $NAME/

echo "Copy the JavaDoc" 
cp -R ProCam/dist/javadoc $NAME/

echo "Copy the Data" 
cp -R ProCam/data $NAME/


echo "Copy Examples, Calibration & Apps" 
# Examples
cp -R $SKETCHBOOK/papartExamples/* $NAME/examples/examples/

# Calibration stuff
cp -R $SKETCHBOOK/papartCalibration/* $NAME/examples/calib/

# Apps 
cp -R $SKETCHBOOK/papartApps/* $NAME/examples/apps/


echo "Create the archive..." 
tar -zcf $NAME.tgz $NAME


echo "Clean " 
rm -rf $NAME


echo "Creation OK" 
