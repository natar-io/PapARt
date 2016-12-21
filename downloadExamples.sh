#!/bin/bash

echo "Download the examples"
wget https://github.com/potioc/Papart-examples/archive/master.zip
unzip master.zip

echo "cleaning the examples"
rm master.zip
