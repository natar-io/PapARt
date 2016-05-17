#!/bin/bash

SKETCHBOOK=sketches
TMP=tmp
NAME=PapARt
OUTPUT_NAME=PapARt-public

## NAME must match
## fr.inria.papart.procam.Utils.LibraryName = "ProCam";


mkdir $TMP
mkdir $TMP/$NAME
mkdir $TMP/$NAME/library
mkdir $TMP/$NAME/examples


echo "Cleaning previous versions"
rm -rf libraries/$NAME
echo "Create archive of depedencies"
tar -zcf libs.tgz libraries


echo "Cleaning sketches"
sh $SKETCHBOOK/clean.sh

echo "Copy Library"
# Library
cp papart/target/$NAME.jar $TMP/$NAME/library/$NAME.jar


# echo "Copy JavaCV, OpenCV and friends"
# libs are  javaCV and javaCV cppjars
# cp libs/* $NAME/library/

echo "Copy the deps & tests"
cp -R papart/deps $TMP/$NAME/
cp -R papart/test $TMP/$NAME/

echo "Copy the JavaDoc"
cp -R papart/target/site/apidocs $TMP/$NAME/

echo "Copy the Data"
cp -R papart/data $TMP/$NAME/


echo "Copy Examples, Calibration & Apps"
# Examples
cp -R sketches/examples $TMP/$NAME/examples/examples

# Calibration stuff
cp -R sketches/calibration $TMP/$NAME/examples/calib

# Apps
cp -R sketches/apps $TMP/$NAME/examples/apps


echo "Create the archive..."
cd $TMP

tar -zcf $OUTPUT_NAME.tgz $NAME

mv $OUTPUT_NAME.tgz  ..
cd ..

echo "Clean "
rm -rf $TMP


echo "Creation OK"
