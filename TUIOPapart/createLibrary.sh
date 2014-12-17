#!/bin/bash 

SKETCHBOOK=sketches
TMP=tmp
NAME=TUIOPapart


mkdir $TMP
mkdir $TMP/$NAME 
mkdir $TMP/$NAME/library
mkdir $TMP/$NAME/examples




echo "Copy Library"
# Library
cp target/$NAME-*.jar $TMP/$NAME/library/$NAME.jar


# echo "Copy JavaCV, OpenCV and friends"
# libs are  javaCV and javaCV cppjars
# cp libs/* $NAME/library/

echo "Copy the sources" 
# copy the source also
cp -R src $TMP/$NAME/
cp -R pom.xml $TMP/$NAME/
cp -R test $TMP/$NAME/

echo "Copy the JavaDoc" 
cp -R target/site/apidocs $TMP/$NAME/

echo "Copy the Data" 
cp -R papart/data $TMP/$NAME/

echo "Create the archive..." 
cd $TMP

tar -zcf $NAME.tgz $NAME

mv $NAME.tgz  .. 
cd .. 

echo "Clean " 
rm -rf $TMP


echo "Creation OK" 
