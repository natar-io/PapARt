/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.panel;

import fr.inria.papart.depthcam.Kinect;
import fr.inria.papart.procam.Papart;
import javax.swing.JFrame;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author jiii
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

    public class PFrame extends JFrame {

        public PFrame(int width, int height) {
            setBounds(100, 100, width, height);
            secondApplet = new SecondApplet();
            add(secondApplet);
            secondApplet.init();
            this.setVisible(true);
        }
    }

    public class SecondApplet extends PApplet {

        PImage localImage, localImage2;

        public void setup() {
//            size(800, 600, "fr.inria.papart.panel.PGraphicsOpenGLExtension");
            size(800, 600, OPENGL);
        }

        public void draw() {
            background(100, random(50) + 50, 100);
            println("draw in second..");

            Kinect kinect = Papart.getPapart().getKinect();
        
            
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
