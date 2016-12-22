#!/bin/bash

BRANCH=realsense

echo "Download the examples"
wget https://github.com/potioc/Papart-examples/archive/$BRANCH.zip
unzip $BRANCH.zip

mv Papart-examples-$BRANCH Papart-examples

echo "cleaning the examples"
rm $BRANCH.zip
