#!/bin/bash


echo "Removing symlinks... "

rm config.pde
rm data

# force remove the data folder ?
rm -rf data

echo "Regenerating symlinks... "

ln -s $SKETCHBOOK/common/config.pde
ln -s $SKETCHBOOK/data
