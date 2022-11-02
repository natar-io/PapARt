## Compilation guide

### Clone the library. 

Clone the library or download the sources from this repository. 

### Dependencies

The compilation requires a Java Development Kit (JDK) installed and Maven. 
Maven enables automatic resolution and download of the libraries required for 
the project to compile. If the master branch does not compile, please file an issue. 
Compilation is not guaranteed on the other branches. 

### Compile the lirbray

In the downloaded folder, go to the project folder and compile it: 
```
$ cd papart/papart
$ mvn install
```

You can also generate the documentation (via Javadoc): `mvn javadoc:javadoc`.

### Package as a Processing library

Use the `create-redist` script to build the library. 
``` 
$ cd ..
$ sh create-redist.sh
```

You will obtain a file named `PapARt.tar.gz` which unpacks like any other Processing library. 


## Natar Video output 


#### Compilation: 
mvn compile -Djavacpp.platform=linux-x86_64



#### Load calibrations for Natar 


mvn exec:java -Dexec.mainClass="fr.inria.papart.apps.ConfigurationLoader" -Dexec.args=" -f data/calibration/camera.yaml -pd -o camera0:calibration"
mvn exec:java -Dexec.mainClass="fr.inria.papart.apps.ConfigurationLoader" -Dexec.args=" -f data/calibration/camera.yaml -pd -o camera0:calibration"


#### Run Natar


Video server: 
`java -jar -Xmx64m target/papart-1.6-jar-with-dependencies.jar --driver OPENCV --device-id 0 --format rgb --output camera0 --resolution 640x480 --stream`

Video Server:
`mvn exec:java -Dexec.mainClass="fr.inria.papart.apps.CameraServerImpl"  -Dexec.args=" --driver OPENCV --device-id 0 --format rgb --output camera0 --resolution 640x480 --stream"`

Video client: 
`mvn exec:java -Dexec.mainClass="fr.inria.papart.app.CameraTest"  -Dexec.args="-i camera0"`

Previous examples **Deprecated**:

```
## OpenCV camera
java -jar -Xmx64m target/nectar-camera-server-0.1-SNAPSHOT-jar-with-dependencies.jar --driver OPENCV --device-id 0 --format rgb --output camera0 --resolution 640x480 --stream --stream-set --depth-camera camera0:depth" 

## Depth camera 
java -jar -Xmx64m target/nectar-camera-server-0.1-SNAPSHOT-jar-with-dependencies.jar --driver OPENNI2 --device-id 0 --format rgb --output camera0 --resolution 640x480 --stream --depth-camera camera0:depth" 

## Play a video
java -jar -Xmx64m target/nectar-camera-server-0.1-SNAPSHOT-jar-with-dependencies.jar --driver FFMPEG --device-id "/home/ditrop/Documents/chat-fr.mp4" --format video --output video0	--stream --stream-set
```
