#!/bin/sh


mvn install:install-file -DlocalRepositoryPath=repo -DcreateChecksum=true -Dpackaging=jar -Dfile=org.openni.jar -DgroupId=org.openni -DartifactId=OpenNI -Dversion=2.3

mvn install:install-file -DlocalRepositoryPath=repo -DcreateChecksum=true -Dpackaging=jar -Dfile=openni-bin.jar -DgroupId=org.openni -DartifactId=OpenNI-bin -Dversion=2.3
