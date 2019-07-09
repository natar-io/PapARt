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

if [ ! -f toxiclibs-complete-0020.zip ]; then
    echo "Toxiclibs"
    wget https://bitbucket.org/postspectacular/toxiclibs/downloads/toxiclibs-complete-0020.zip
    unzip toxiclibs-complete-0020.zip
#    rm toxiclibs-complete-0020.zip
fi

if [ ! -f SVGExtended.tgz ]; then
    echo "SVGExtended"
    wget https://github.com/Rea-lity-Tech/SVGExtended/releases/download/3.3.7.2/SVGExtended.tgz
    tar xvzf SVGExtended.tgz
#    rm SVGExtended.tgz
fi

if [ ! -f SimplePointCloud.tgz ]; then
    echo "SimplePointCloud"
    wget https://github.com/Rea-lity-Tech/SimplePointCloud/releases/download/1.0/SimplePointCloud.tgz
    tar xvzf SimplePointCloud.tgz
#    rm SimplePointCloud.tgz
fi

if [ ! -f ColorConverter.tgz ]; then
    echo "ColorConverter"
    wget https://github.com/Rea-lity-Tech/ColorConverter/releases/download/0.1/ColorConverter.tgz
    tar xvzf ColorConverter.tgz
#    rm ColorConverter.tgz
fi

if [ ! -f video-2.zip ]; then
    echo "Processing Video"
    wget https://github.com/processing/processing-video/releases/download/2/video-2.zip
    unzip video-2.zip
#    rm video-2.zip
fi

if [ ! -f processingTUIO.tgz ]; then
    echo "Processing TUIO"
    wget https://github.com/poqudrof/ProcessingTUIO/releases/download/0.5/processingTUIO.tgz
    tar xvzf processingTUIO.tgz
#    rm processingTUIO.tgz
fi

if [ ! -f oscP5-0.9.8.zip ]; then
    echo "OSCP5"
    wget http://www.sojamo.de/libraries/oscP5/download/oscP5-0.9.8.zip
    unzip oscP5-0.9.8.zip
#    rm oscP5-0.9.8.zip
fi

if [ ! -f skatolo.tgz ]; then
    echo "Skatolo"
    wget https://github.com/Rea-lity-Tech/Skatolo/releases/download/1.1.1/skatolo.tgz
    tar xvzf skatolo.tgz
#    rm skatolo.tgz
fi

if [ ! -f guiModes.tgz ]; then
    echo "Gui Modes"
    wget https://github.com/poqudrof/guiModes/releases/download/0.5/guiModes.tgz
    tar xvzf guiModes.tgz
#    rm guiModes.tgz
fi

if [ ! -f peasycam_302.zip ]; then
    echo "Peasycam"
    wget http://mrfeinberg.com/peasycam/peasycam_302.zip
    unzip peasycam_302.zip
#    rm peasycam_302.zip
fi


echo "Jedis"
cd ..
wget http://central.maven.org/maven2/redis/clients/jedis/2.9.0/jedis-2.9.0.jar
cd libraries

# echo "Compile reflections"
# cd ..
# cd reflections-build
# ruby createLibs.rb
# mv reflections.tgz ../libraries
# cd ../libraries
# tar xvzf reflections.tgz
# rm reflections.tgz

# echo "Compile JavaCV"
# cd ..
# cd javacv-build
# ruby createLibs.rb
# mv javacv-*.tgz ../libraries
# cd ../libraries
# tar xvzf javacv-*.tgz
# rm javacv-*.tgz

## Missing: OpenNI
# cd ..
# echo "compile all"
# zip -r libraries.zip libraries
