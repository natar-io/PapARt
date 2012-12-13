#!/bin/bash

#PROCESSING_DIR=~/Documents/processing-1.5.1
#PROCESSING_LIB_DIR=$PROCESSING_DIR/lib
#PROCESSING_LIBS_DIR=$PROCESSING_DIR/modes/java/libraries
PAPART_DIR=~/Documents/papart
#PAPART_GLOBAL_LIBS=/usr/local/libs/papart/
SK_DIR=~/Documents/sketchbook
SK_LIBS_DIR=$SK_DIR/libraries
SK_LIBS=("DrawingApp" "GLGraphics" "Homography" "MultiTouchKinect" "MyJavaCVProcessing" "OBJModel" "ProCam" "ProCamTouch")

LIBS_TO_COPY=($SK_DIR/)

cd $SK_LIB_DIR

for i in 0 1 2 3 4 5 6 7
do
    echo linking $SK_LIBS_DIR/${SK_LIBS[$i]}
    echo with $PAPART_DIR/${SK_LIBS[$i]}/dist/${SK_LIBS[$i]}.jar
    rm -f $SK_LIBS_DIR/${SK_LIBS[$i]}/${SK_LIBS[$i]}.jar
    ln -s $PAPART_DIR/${SK_LIBS[$i]}/dist/${SK_LIBS[$i]}.jar $SK_LIBS_DIR/${SK_LIBS[$i]}/
done

