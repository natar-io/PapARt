## PapARt Library

PapARt is a software development kit (SDK) that enables the creation of interactive projection mapping. Today, it is developed by the Inria Potioc team. It comes from the augmented physical drawing tools created by Jeremy Laviole, which are documented in his PhD thesis (free to read).

## Version 1.0 (summer 2017)

The first big release is ready. If you want to try it out download our precompiled version from the [example repository](https://github.com/poqudrof/Papart-examples). 

## Version 1.1 and 1.2 (January 2018)

The first 2018 release will be directly to 1.2. This label is explained by two major updates: 

* ColorTracking: The library enables color tracking. The system learns to recognize and track five colors which can be used to activate buttons, or identify objects. 
* Hand recognition and tracking is improved to segment the arm - hands and fingers. The API is in progress and will evolve 
with the 1.2.x versions. 

Other features: 

* Easier to compile thanks to the release of JavaCV/JavaCPP 1.4.  
* Support of intel Realsense cameras (SR300 and F200). 
* Support of Orbbec cameras (Astra S).
* JRubyArt support is getting extended.
* Community support is moved from the wiki to the [forum](http://forum.rea.lity.tech). 

## Version 1.3 (April - June 2018)

* Unity3D client: You will be able to create applications with Unity3D with a dedicated plugin.
* Touch Tracking will get new features: object detection (not a hand), and mid-air touch will be back. 
* Offline mode API. The library will be easier to use without the dedicated hardware.


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

PapARt is an open source software owned by Inria, Bordeaux University, CNRS, and RealityTech distributed
under the LGPL license.
