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


echo "Copy the sources"
# copy the source also
cp -R papart/src $TMP/$NAME/
cp -R papart/pom.xml $TMP/$NAME/
cp -R papart/deps $TMP/$NAME/

cp -R papart/test $TMP/$NAME/

echo "Copy the JavaDoc"
cp -R papart/target/site/apidocs $TMP/$NAME/

echo "Copy the Data"
cp -R papart/data $TMP/$NAME/



echo "Download the examples"
wget https://github.com/potioc/Papart-examples/archive/master.zip
unzip master.zip

mv Papart-examples-master/apps $TMP/$NAME/examples/
mv Papart-examples-master/papart-examples $TMP/$NAME/examples/

echo "cleaning the examples"
rm master.zip
rm -rf Papart-examples-master


# Calibration stuff
cp -R sketches/calibration $TMP/$NAME/examples/calib

# Now working with github examples and apps.

# echo "Copy Examples, Calibration & Apps"
# Examples
# cp -R sketches/examples $TMP/$NAME/examples/examples
# Apps
# cp -R sketches/apps $TMP/$NAME/examples/apps

echo "Create the archive..."
cd $TMP

tar -zcf $NAME.tgz $NAME

mv $NAME.tgz  ..
cd ..


cp -r $TMP/$NAME libraries/

echo "Create full archive : Papart & Deps"
tar -zcf papart-complete.tgz libraries

echo "Clean "
rm -rf $TMP


echo "Creation OK"
