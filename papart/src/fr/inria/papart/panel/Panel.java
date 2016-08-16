/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.panel;

import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import javax.swing.JFrame;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

/**
 * TEST CODE.
 * @author Jeremy Laviole
 */
public class Panel {

    SecondApplet secondApplet;
    int w, h;

    PGraphicsOpenGL mainGL;

    public Panel(PApplet parent) {

        this.w = 800;
        this.h = 600;
        PFrame f = new PFrame(800, 600);

//        parent.frame.setTitle("first window");
//        f.setTitle("second window");
    }

    /**
     * Broken WITH PROCESSING 3
     */
    public class PFrame extends JFrame {

        public PFrame(int width, int height) {
            setBounds(100, 100, width, height);
            secondApplet = new SecondApplet();
//            add(secondApplet);
//            secondApplet.init();
            this.setVisible(true);
        }
    }

    public class SecondApplet extends PApplet {

        PImage localImage, localImage2;

        public void setup() {
//            size(800, 600, "fr.inria.papart.panel.PGraphicsOpenGLExtension");
            size(800, 600, OPENGL);
        }

        public void drawKinectRGB() {
            try {
                if (Papart.getPapart() != null
                        && Papart.getPapart().getKinectCamera() != null) {

                    Camera kinectRGB = Papart.getPapart().getKinectCamera();
//                    if (!kinectRGB.canBeDisplayedOn(this)) {
//                        kinectRGB.prepareToDisplayOn(this);
//                    }
//
//                    PImage img = kinectRGB.getDisplayedOn(this);
//                    image(img, 0, 0, 320, 240);
                }
            } catch (Exception e) {
            }
        }

        public void draw() {
            background(100, random(50) + 50, 100);
            println("draw in second..");

//            DepthAnalysis kinect = Papart.getPapart().getKinect();

            // Camera OK !
//            try {
//                if (Papart.getPapart() != null
//                        && Papart.getPapart().getCameraTracking() != null
//                        && Papart.getPapart().getCameraTracking().getImage() != null) {
//
//                    println("Image ready..");
//
//                    if (localImage == null) {
//                        localImage = Papart.getPapart().getCameraTracking().getPImageCopy(this);
//                    } else {
//                        Papart.getPapart().getCameraTracking().getPImageCopyTo(localImage);
//                    }
//                    image(localImage, 0, 0, 200, 200);
//                }
//            } catch (Exception e) {
//                System.out.println("Exception " + e);
//                e.printStackTrace();
//            }
            // Base Display OK !
//            try {
//                if (Papart.getPapart() != null
//                        && Papart.getPapart().getDisplay() != null
//                        && Papart.getPapart().getDisplay().getGraphics() != null) {
//
//                    println("Image ready..");
//
//                    PImage sourceImage = Papart.getPapart().getDisplay().graphics;
//                    if (localImage2 == null) {
//                        println("create local image..");
//                        localImage2 = createImage(sourceImage.width, sourceImage.height, sourceImage.format);
//                    }
//                    println("Get px copy..");
////                    int[] pxCopy = Papart.getPapart().getDisplay().getPixelsCopy();
//                    int[] pxCopy = Papart.getPapart().getDisplay().pxCopy;
//                    if(pxCopy == null)
//                          return;
//                    
//                    // get a copy before ? 
////                    sourceImage = sourceImage.get();
////                    sourceImage.loadPixels();
//                    localImage2.loadPixels();
//                    println("Copy pixels..");
//                    System.arraycopy(pxCopy, 0, localImage2.pixels, 0, pxCopy.length);
//                    println("Draw pixels..");
//                    image(localImage2, 0, 200, 200, 200);
//                }
//            } catch (Exception e) {
//                System.out.println("Exception " + e);
//                e.printStackTrace();
//            }
        }

    }

}
