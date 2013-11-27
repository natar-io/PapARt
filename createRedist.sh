#!/bin/bash 

SKETCHBOOK=../sketchbook

mkdir ProCamLibrary 
mkdir ProCamLibrary/library
mkdir ProCamLibrary/examples

cp ProCam/dist/ProCam.jar ProCamLibrary/library/ProCamLibrary.jar
cp -R ProCam/src ProCamLibrary/src/

cp -R $SKETCHBOOK/examples/* ProCamLibrary/examples/
cp -R $SKETCHBOOK/common ProCamLibrary/common
cp -R $SKETCHBOOK/calib ProCamLibrary/examples/calib
#cp -R $SKETCHBOOK/data ProCamLibrary/data

tar -zcf ProCamLibrary.tgz ProCamLibrary

rm -rf ProCamLibrary


echo "Creation OK" 
