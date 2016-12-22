#!/bin/bash

TMP=tmp
NAME=PapARt
VERSION=$1

## NAME must match
## fr.inria.papart.procam.Utils.LibraryName = "PapARt"

mkdir $TMP
mkdir $TMP/$NAME
mkdir $TMP/$NAME/library
mkdir $TMP/$NAME/examples

echo "Copy Library"
# Library
cp papart/target/$NAME.jar $TMP/$NAME/library/$NAME.jar

echo "Copy the sources"
# copy the source also
cp -R papart/src $TMP/$NAME/
cp -R papart/pom.xml $TMP/$NAME/
cp -R papart/test $TMP/$NAME/

echo "Copy the JavaDoc"
cp -R papart/target/site/apidocs $TMP/$NAME/

echo "Copy the Data"
cp -R papart/data $TMP/$NAME/

echo "Copy the examples"
cp -R Papart-examples/apps $TMP/$NAME/examples/
cp -R Papart-examples/papart-examples $TMP/$NAME/examples/

echo "Create the archive..."
cd $TMP

tar -zcf $NAME-$VERSION.tgz $NAME

mv $NAME-$VERSION.tgz  ..
cd ..

echo "Clean "
rm -rf $TMP

echo "Creation OK"
