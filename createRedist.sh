#!/bin/bash 

SKETCHBOOK=sketches
TMP=tmp
NAME=PapARt

## NAME must match 
## fr.inria.papart.procam.Utils.LibraryName = "ProCam";


mkdir $TMP
mkdir $TMP/$NAME 
mkdir $TMP/$NAME/library
mkdir $TMP/$NAME/examples
mkdir $TMP/$NAME/examples/examples
mkdir $TMP/$NAME/examples/calib
mkdir $TMP/$NAME/examples/apps

echo "Cleaning sketches"

sh $SKETCHBOOK/clean.sh

echo "Copy Library"
# Library
cp papart/target/papart-*.jar $TMP/$NAME/library/$NAME.jar


# echo "Copy JavaCV, OpenCV and friends"
# libs are  javaCV and javaCV cppjars
# cp libs/* $NAME/library/


echo "Copy the sources" 
# copy the source also
cp -R papart/src $TMP/$NAME/

cp -R papart/test $TMP/$NAME/

echo "Copy the JavaDoc" 
cp -R papart/target/site/apidocs $TMP/$NAME/

echo "Copy the Data" 
cp -R papart/data $TMP/$NAME/


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


echo "Create archive of depedencies"
tar -zcf libs.tgz libraries

echo "Clean " 
rm -rf $TMP


echo "Creation OK" 
