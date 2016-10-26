# PapARt Library

PapARt is a software development kit (SDK) that enables the creation of interactive projection mapping. Today, it is developed by the Inria Potioc team. It comes from the augmented physical drawing tools created by Jeremy Laviole, which are documented in his PhD thesis (free to read).

## Features

[![](https://github.com/poqudrof/PapARt/blob/master/video_screenshot.png?raw=true)](https://youtu.be/bMwKVOuZ9EA)

It enables the creation of Augmented Reality (AR) applications in [Processing](https://processing.org/). 
Like most AR toolkit, it is vision based and it detects the world using color cameras. 
In addition to this, PapARt also uses a depth camera. 

We generally use pre-calibrated (intrinsics parameters) and PapARt enables the extrinsic calibration: how cameras are located relatively from one to another. It also provides simple and unprecise tools to create intrinsic calibration. 

It uses tracking libraries such as ARToolkit and OpenCV, and can be extended. 
The strength of this library is the creation of interactive projection (also called spatial augmented reality in research). 
In addition to cameras, PapARt calibrates the projector’s extrinsics to create projector/camera systems also called ProCams. 

Interactivity is increased thanks to an object and hand tracking enabled by the depth camera.

More information about the research project here: 
#### https://project.inria.fr/papart/

## Examples

The examples are in a [dedicated repository](https://github.com/poqudrof/Papart-examples). 
PapARt is large library, and work with many different systems:
- webcams and professionnal cameras ([PointGrey](https://www.ptgrey.com/) cameras). 
- depthCameras: [Kinect Xbox360](https://github.com/OpenKinect/libfreenect), [Kinect xbox one](https://github.com/OpenKinect/libfreenect2), Intel [Realsense](https://github.com/IntelRealSense/librealsense).  
- Projector/camera/depth camera systems (the main purpose of the library).  

## How to contribute

The open source release is new (end of August 2016), feel free to fork, star, and file issues for this sources. 
You can contribute your examples to the [example repository](https://github.com/poqudrof/Papart-examples) to 
share your creations with the community. 

### Copyright note

PapARt is an open source software owned by Inria, Bordeaux University and CNRS, distributed
under the LGPL license.
