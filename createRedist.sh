#!/bin/bash 

SKETCHBOOK=../sketchbook
TMP=tmp
NAME=ProCam

## NAME must match 
## fr.inria.papart.procam.Utils.LibraryName = "ProCam";


mkdir $TMP
mkdir $TMP/$NAME 
mkdir $TMP/$NAME/library
mkdir $TMP/$NAME/examples
mkdir $TMP/$NAME/examples/examples
mkdir $TMP/$NAME/examples/calib
mkdir $TMP/$NAME/examples/apps

echo "Copy Library"

# Library
cp ProCam/dist/ProCam.jar $TMP/$NAME/library/$NAME.jar
#cp -R ProCam/dist/lib $NAME/library/

tar -zcf libs.tgz ProCam/dist/lib

# echo "Copy JavaCV, OpenCV and friends"
# libs are  javaCV and javaCV cppjars
# cp libs/* $NAME/library/


echo "Copy the sources" 
# copy the source also
cp -R ProCam/src $TMP/$NAME/

echo "Copy the JavaDoc" 
cp -R ProCam/dist/javadoc $TMP/$NAME/

echo "Copy the Data" 
cp -R ProCam/data $TMP/$NAME/


echo "Copy Examples, Calibration & Apps" 
# Examples
cp -R $SKETCHBOOK/papartExamples/* $TMP/$NAME/examples/examples/

# Calibration stuff
cp -R $SKETCHBOOK/papartCalibration/* $TMP/$NAME/examples/calib/

# Apps 
cp -R $SKETCHBOOK/papartApps/* $TMP/$NAME/examples/apps/


echo "Create the archive..." 
cd $TMP

tar -zcf $NAME.tgz $NAME

mv $NAME.tgz  .. 
cd .. 

echo "Clean " 
rm -rf $TMP


echo "Creation OK" 
