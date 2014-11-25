#!/bin/sh
javac -Xlint:unchecked -O -source 1.4 -target 1.4 -cp ../library/libTUIO.jar:core.jar TUIO/*.java
jar cfm ../library/TUIO.jar manifest.inc TUIO/*.class
rm -f TUIO/*.class
