# @Author: PALARD Nicolas <nclsp>
# @Date:   2019-04-25T18:24:44+02:00
# @Email:  palard@rea.lity.tech
# @Project: Natar.io
# @Last modified by:   nclsp
# @Last modified time: 2019-04-29T10:38:15+02:00
# @Copyright: RealityTech 2018-2019

#!/bin/bash

mkdir libraries
cd libraries

echo "Toxiclibs"
wget https://bitbucket.org/postspectacular/toxiclibs/downloads/toxiclibs-complete-0020.zip
unzip toxiclibs-complete-0020.zip
rm toxiclibs-complete-0020.zip

echo "SVGExtended"
wget https://github.com/natar-io/SVGExtended/releases/download/3.2.1/SVGExtended.tgz
tar xvzf SVGExtended.tgz
rm SVGExtended.tgz

echo "SimplePointCloud"
wget https://github.com/natar-io/SimplePointCloud/releases/download/1.0/SimplePointCloud.tgz
tar xvzf SimplePointCloud.tgz
rm SimplePointCloud.tgz

echo "ColorConverter"
wget https://github.com/natar-io/ColorConverter/releases/download/0.1/ColorConverter.tgz
tar xvzf ColorConverter.tgz
rm ColorConverter.tgz

echo "Processing Video"
wget https://github.com/processing/processing-video/releases/download/2/video-2.zip
unzip video-2.zip
rm video-2.zip

echo "Processing TUIO"
wget https://github.com/poqudrof/ProcessingTUIO/releases/download/0.5/processingTUIO.tgz
tar xvzf processingTUIO.tgz
rm processingTUIO.tgz

echo "OSCP5"
wget http://www.sojamo.de/libraries/oscP5/download/oscP5-0.9.8.zip
unzip oscP5-0.9.8.zip
rm oscP5-0.9.8.zip

echo "Skatolo"
wget https://github.com/natar-io/Skatolo/releases/download/1.1.1/skatolo.tgz
tar xvzf skatolo.tgz
rm skatolo.tgz

echo "Gui Modes"
wget https://github.com/poqudrof/guiModes/releases/download/0.5/guiModes.tgz
tar xvzf guiModes.tgz
rm guiModes.tgz

echo "Peasycam"
wget http://mrfeinberg.com/peasycam/peasycam_302.zip
unzip peasycam_302.zip
rm peasycam_302.zip


# echo "Jedis"
# cd ..
# wget http://central.maven.org/maven2/redis/clients/jedis/2.9.0/jedis-2.9.0.jar
# cd libraries

echo "Compile reflections"
cd ..
cd reflections-build
ruby createLibs.rb
mv reflections.tgz ../libraries
cd ../libraries
tar xvzf reflections.tgz
rm reflections.tgz

echo "Compile JavaCV"
cd ..
cd javacv-build
ruby createLibs.rb
# mv javacv-*.tgz ../libraries
# cd ../libraries
# tar xvzf javacv-*.tgz
# rm javacv-*.tgz

## Missing: OpenNI
cd ..
echo "compile all"
zip -r libraries.zip libraries
