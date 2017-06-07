## PapARt Library

PapARt is a software development kit (SDK) that enables the creation of interactive projection mapping. Today, it is developed by the Inria Potioc team. It comes from the augmented physical drawing tools created by Jeremy Laviole, which are documented in his PhD thesis (free to read).

## Version 1.0

The first big release is ready. If you want to try it out download our precompiled version from the [example repository](https://github.com/poqudrof/Papart-examples). 

## Version 1.1 coming soon 

A new release will match the first RealityTech Hardware. Here are the new features to expect:
* ColorTracking. Color patches will be tracked with the same tracking algorithms as depth tracking. 
* Offline mode. The library will be easier to use without the dedicated hardware. So you could code your apps before buying or building your own, and of course code in your couch or in a plane. 
* Polyglot support: PapARt will interface with [Scratch](https://github.com/LLK/scratchx), the JRubyArt examples will be updated to latest version. 
 

## Examples

This repository is for the development of the library.  
You may want to go to the **[PapARt-Examples repository](https://github.com/poqudrof/Papart-examples)** to see how to use it or discover  the features and demos. 

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

PapARt is large library, and work with many different systems:
- webcams and professionnal cameras ([PointGrey](https://www.ptgrey.com/) cameras). 
- depthCameras: [Kinect Xbox360](https://github.com/OpenKinect/libfreenect), [Kinect xbox one](https://github.com/OpenKinect/libfreenect2), Intel [Realsense](https://github.com/IntelRealSense/librealsense).  
- Projector/camera/depth camera systems (the main purpose of the library).  

## How to contribute

The open source release is new (end of August 2016), feel free to fork, star, and file issues for this sources. 
You can contribute your examples to the [example repository](https://github.com/poqudrof/Papart-examples) to 
share your creations with the community. 

## Next steps

The distribution got better, and the next steps would be to create versions on **Android** and/or on **Raspberry PI**.

### Copyright note

PapARt is an open source software owned by Inria, Bordeaux University and CNRS, distributed
under the LGPL license.
