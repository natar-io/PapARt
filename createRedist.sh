#!/bin/bash

SKETCHBOOK=sketches
TMP=tmp
NAME=PapARt

## NAME must match
## fr.inria.papart.procam.Utils.LibraryName = "PapARt"

mkdir $TMP
mkdir $TMP/$NAME
mkdir $TMP/$NAME/library
mkdir $TMP/$NAME/examples


echo "Cleaning previous versions"
rm -rf libraries/$NAME
echo "Create archive of depedencies"
tar -zcf libs.tgz libraries

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

echo "Create the archive..."
cd $TMP

tar -zcf $NAME.tgz $NAME

mv $NAME.tgz  ..
cd ..

echo "Clean "
rm -rf $TMP

echo "Creation OK"
