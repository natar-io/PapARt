#!/bin/bash


mkdir libraries

echo "Get Toxiclibs"

wget https://bitbucket.org/postspectacular/toxiclibs/downloads/toxiclibs-complete-0020.zip

unzip toxiclibs-complete-0020.zip
mv audioutils libraries/
mv colorutils libraries/
mv datautils libraries/
mv simutils libraries/
mv toxiclibscore libraries/
mv toxiclibs_p5 libraries/
mv verletphysics libraries/
mv volumeutils libraries/

echo "Get SVGExtended"
wget https://github.com/Rea-lity-Tech/SVGExtended/releases/download/3.3.5/SVGExtended.tgz

tar xvzf SVGExtended.tgz 
mv SVGExtended libraries/

echo "Compile SimplePointCloud"
wget https://github.com/Rea-lity-Tech/SimplePointCloud/releases/download/0.1/SimplePointCloud.tgz
tar xvzf SimplePointCloud.tgz
mv SimplePointCloud libraries/

echo "Get Processing Video"
#wget https://github.com/processing/processing-video/releases/download/latest/video.zip

unzip video.zip
unzip video
mv video libraries/


echo "Get Processing TUIO" 
wget https://github.com/poqudrof/ProcessingTUIO/releases/download/0.5/processingTUIO.tgz

tar xvzf processingTUIO.tgz 
mv processingTUIO libraries/


echo "Get OSCP5"
wget http://www.sojamo.de/libraries/oscP5/download/oscP5-0.9.8.zip

unzip oscP5-0.9.8.zip
mv oscP5 libraries/

echo "Get Skatolo"
wget https://github.com/rea-lity-tech/Skatolo/releases/download/1.1/skatolo.tgz

tar xvzf skatolo.tgz
mv skatolo libraries/

echo "Get Gui Modes"
wget https://github.com/poqudrof/guiModes/releases/download/0.5/guiModes.tgz

tar xvzf guiModes.tgz
mv guiModes libraries/

echo "get Peasycam"
wget http://mrfeinberg.com/peasycam/peasycam_202.zip
unzip peasycam_202.zip
mv peasycam libraries/

echo "Compile reflections"
cd reflections-build
ruby createLibs.rb
cp reflections.tgz ..
cd ..
tar xvzf reflections.tgz
mv reflections libraries/

echo "Compile JavaCV"
cd javacv-build
ruby createLibs.rb
mv javacv-*.tgz ..

## Missing: OpenNI

echo "compile all"
zip -r libraries.zip libraries
