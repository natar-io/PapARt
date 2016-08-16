#!/bin/bash


echo "Removing symlinks... "

rm config.pde
rm data

echo "Regenerating symlinks... "

ln -s ../../calib/config/config.pde
ln -s ../../data
