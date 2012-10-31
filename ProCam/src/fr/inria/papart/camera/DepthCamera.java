/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.camera;

/**
 *
 * @author jeremy
 */
public class DepthCamera extends OpenCVCamera {

    int[] depthArray = null;

    public DepthCamera(int i) {
        if (i == 0) {
            grabber = new OpenCVDepthFrameGrabber(OpenCVDepthFrameGrabber.DepthCameraType.XTION);
        }
        try {
            grabber.start();

        } catch (Exception e) {
            System.out.println("Error while starting the depth camera : " + e);
        }

    }

    public DepthCamera(OpenCVDepthFrameGrabber.DepthCameraType type) {
        grabber = new OpenCVDepthFrameGrabber(type);

        try {
            grabber.start();

        } catch (Exception e) {
            System.out.println("Error while starting the depth camera : " + e);
        }
    }

    public void setVideoType() {
    }

    @Override
    public void grab() {

        try {
//            grabber.trigger();
            iimg = grabber.grab();

            // Image drawing
            if (copyToProcessing) {
                java.nio.ShortBuffer buff1 = iimg.getShortBuffer();

                pimg.loadPixels();

                for (int i = 0; i < iimg.width() * iimg.height(); i++) {
                    pimg.pixels[i] = buff1.get(i);
//                    int offset = i * 3;
//                    pimg.pixels[i] = (buff1.get(offset + 2) & 0xFF) << 16
//                            | (buff1.get(offset + 1) & 0xFF) << 8
//                            | (buff1.get(offset) & 0xFF);
                }
                pimg.updatePixels();
            }

        } catch (Exception e) {
            System.out.println("Exception in depth image grabbing " + e);
            e.printStackTrace();
        }
    }

    protected void createDepthArray() {
        if (depthArray == null) {
            this.depthArray = new int[grabber.getImageWidth() * grabber.getImageHeight()];
        }
    }

    public int[] grabAsArray() {
        if (depthArray == null) {
            createDepthArray();
        }

        try {
            iimg = grabber.grab();
            java.nio.ShortBuffer buff1 = iimg.getShortBuffer();

//            short[] tmpArray =  buff1.asReadOnlyBuffer().array();
            
            // TODO: optimizations ? 
            for(int i= 0; i < depthArray.length; i++){
                depthArray[i] = buff1.get(i);
//                
//                System.out.print(depthArray[i] +" ");
//                if( i % 30 == 0)
//                    System.out.println("");
            }

            
            return depthArray;
            
            // TODO: return as shorts
//java.nio.ShortBuffer buff1 = iimg.getShortBuffer();
//            return buff1.array();

        } catch (Exception e) {
            System.out.println("Exception in depth image grabbing (array) " + e);
            e.printStackTrace();
            return null;
        }

    }
}
