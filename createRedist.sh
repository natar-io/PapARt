#!/bin/bash 

SKETCHBOOK=../sketchbook

mkdir ProCamLibrary 
mkdir ProCamLibrary/library
mkdir ProCamLibrary/examples

cp ProCam/dist/ProCam.jar ProCamLibrary/library/ProCamLibrary.jar
cp -R ProCam/src ProCamLibrary/src/

cp -R $SKETCHBOOK/examples/* ProCamLibrary/examples/

tar -zcf ProCamLibrary.tgz ProCamLibrary

rm -rf ProCamLibrary


echo "Creation OK" 
