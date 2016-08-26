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
