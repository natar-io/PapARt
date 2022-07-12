## PapARt Library

PapARt is a software development kit (SDK) that enables the creation of interactive projection mapping.
It is a long running project by Jeremy Laviole, created by Inria, Bordeaux University and the lastest updates
are from CATIE. 

It comes from the augmented physical drawing tools created by Jeremy Laviole, which are documented in his PhD thesis (free to read).

### Main features 

#### Unified rendering - Projection and SeeThrough 

It is possible to switch easily between AR on top of video (SeeThrough) and 
AR using Projection just by changing a few lines of code. 

#### Object tracking 

The native tracking in PapARt is based on ARToolkitPlus, nowadays it is possible to 
use higher quality marker tracking with ARUCO. 
We have a built-in support for color detection in RGB, HSV and Cie XYZ color spaces. 
The latest examples use custom circular tracking for colored stickers that provide 
position, and orientation. 

#### Arm, Hand, Finger tracking using depth cameras 

Many depth cameras are supported : Kinect, Orbbec Astra, Realsense Depth cameras (older models). 
For these depth images, we have two object detection, a simple one that detect and track 
objects over a plane. The second one is a hierarchical tracking, of arm, hand and finger detection 
and tracking. It achieves high quality results for finger tracking but it is still harder to tweak
and requires more hardware resources.

#### Integrated UI kit 

We use a fork of ControlP5 called Skatalo which is updated to handle multiple "touch" events 
instead of a single cursor and click. The elements detected and tracked can be used to activate
widgets: buttons, toggles, sliders. 

#### API at millimeter scale 

We follow the Processing APIs, using millimeters instead of pixels. 
The interesting consequence is that rendering can be adjusted following the 
hardware capabilities and projector location. 

#### Open source ecosystem 

PapARt is build on top of Processing, OpenCV, JavaCV and JavaCPP. Our calibration 
boards use the SVG format and are created using Inkscape. 

The latest updates rely on Redis, ARUCO, and other open source projects. 

This library is an outcome enabled by many open source communities. 
#### Advanced examples


We include advanced examples of uses: 

* 3D Rendering inside a secondary screen (in PapARt initial article). 
* Compatibility with Unity3D though Natar. 
* 3D Scanner using Gray Code. 
* Teaching of color blending application. 

## PapARt 1.6 - Back to monolith 

After a two years break a new version comes to life. It integrates back the Natar developments 
and builds up a new structure from the micro-service creation experience. 

The main update is the support of modern version of OpenCV : 4.5.4, and modern operating systems and machines. 
This new support is permitted by the Processing community.

* Windows 10/11 support. 
* Arch linux support. 
* OSX, M1 architecture. 

Support on other architecture and devices should be possible, notably Raspberry PI or Android. 


### *New* - Hardware production 

PapARt hardware from RealityTech will be distributed soon with a free licence (Creative Commons). 
The 3D models and sample calibrations for known hardware will be released. 

The bill of materials will be included also, with projector, camera, screen support and
recommended configuration. 

Although RealityTech is not in operation anymore, we could help with the creation of such devices 
and for research/industrial projects with PapARt you can contact us at CATIE:  j.laviole@catie.fr .

### New features 
#### Update to Processing 4 and Java 17

Processing 4 is currently in beta, it brings support of Java 17. 
We support again all major OS for this release. 
#### Integration of Natar 

Initially, Natar was the follow-up project of PapARt for larger projects.
Natar is a communication protocol for images based on Redis. It features a support of 
calibration files within Redis. 

* There are now programs to load calibration files from PapARt to Redis to use in 
other languages and SDKs, notably Unity3D. 
* PapARt can be used to load cameras feeds to Natar, RGB, IR and Depth videos. 
* Natar video feeds can be loaded into PapARt. 
* Natar pose estimators using Aruco can be used in PapARt. 

The full support and update is in progress and full tutorials are to be created. 



## PapARt 1.4.2 - Christmas 2021 Release 
### Everything is open-source 

The last bits are opened, as RealityTech stopped its AR two years ago. 
Most notably the calibration tools, used to create the hardware, are now 
available and will be documented. 

### 10 years of tabletop AR

The first public demonstrations were 2011 at the "Palais de la découverte" in Paris, a few months 
before the first paper was published. 

The first steps were getting a project projection, then it snowballed: 
 
- 2011 Rendering a 3D scene from another perspective into this projection. 
- 2012 Add shadows, lights. 
- 2012 Create Stereoscopic rendering, for stereo - drawings. 
- 2012-2013 Many tools were create do assist drawing (described in the PhD thesis). 
- 2014-2015 The library got better architecture, precision, performance and calibration. 
- 2016 Touch precision got improved. 
- 2017 Natar idea, tests on marker detectors and camera precision.
- 2017 Natar experiments, Unity version.
- 2017 Inclusion of any Linux app with projection and touch.
- 2018 Use of colored dots instead of markers. 

In 2019, it slowed down to a few customer projects, and stopped dead for two years. 

### What is next ? 

- Projection-based AR is still cool. 
- Holograms are still cool. 
- Cameras get better every year, and depth camera too. 
- Projector's sizes and prices are low enough for students projects and research.

All of the basics are there. I got quite sick of this project after 8 years on it. 
Now, new people come to projection-based AR and want to give it a go. 

You will suffer with calibration issues until the guides are perfect, or a new hardware is created/sold.

However, the tools offered by PapARt are wide enough to create a wide variety of experiences.
A few developer devices are out there (at least 4) in universities, if the projector and cameras were not salvaged 
the new guides could come handy. 

Aside the research projects, two commercial applications are in use, and a few more should be created soon.
This project comes back to life from demands in research, by students and retail use. 
I want to give it a push, maybe also ressurect the devices as a Kit to download, or buy pre-build to assemble.



## PapARt 1.4 - Release Candidate. (July 2018)

This new release brings many new features. It is now easier to place PaperScreen on the table with 
the new TableScreen class. 

The color tracking and particularly the circular tracking is quite robust and enable the creation of 
physical interfaces with a high detection rate. There will be a complete tutorial on how to create 
a mixed reality interface with touch and circle tracking. 

We work to improve the current API, as it will be part of the coming Nectar platform. The main 
motivation for Nectar to push further the possibilites of SAR with PapARt. The rendering will not
be limited to Processing for rendering with the Unity3D Nectar plugin. The plugin is in 
internal test/development phase, and is already quite promising. 

[More on the example repository, 1.4rc branch.](https://github.com/poqudrof/Papart-examples/tree/1.4rc).

#### New hosting 

The 1.4 version and development versions are hosted on [gitlab](https://forge.pole-aquinetic.net/RealityTech/PapARt). You can request access if you collaborate with RealityTech, or use RealityTech Hardware platforms. 

The 1.3 version, sister of 1.4 will be free and publicly available on github. 

## Version 1.1 and 1.2 (January 2018)

The first 2018 releases are 1.1 and 1.2.There are two major updates: 

* (1.1) ColorTracking: The library enables color tracking. The system learns to recognize and track five colors which can be used to activate buttons, or identify objects. 
* (1.2) Hand recognition and tracking is improved to segment the arm - hands and fingers. The API is in progress and will evolve. This version is distributed with RealityTech's hardware.

Other features: 

* Easier to compile thanks to the release of JavaCV/JavaCPP 1.4.  
* Support of intel Realsense cameras (SR300 and F200). 
* Support of Orbbec cameras (Astra S).
* JRubyArt support is getting extended.
* Community and commercial support is moved from the wiki to the [forum](http://forum.rea.lity.tech). 

## Version 1.0 (summer 2017)

The first big release is ready. If you want to try it out download our precompiled version from the [example repository](https://github.com/poqudrof/Papart-examples). 

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

PapARt is an open source software owned by Inria, Bordeaux University, CNRS, and RealityTech (Jeremy Laviole) distributed
under the LGPL license.
