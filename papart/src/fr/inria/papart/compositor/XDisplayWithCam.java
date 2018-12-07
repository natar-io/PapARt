/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.compositor;

import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CannotCreateCameraException;
import processing.core.PApplet;

/**
 *
 * @author realitytech
 */
public class XDisplayWithCam extends XDisplay {

    public XDisplayWithCam(int width, int height) {
        super(width, height);
    }

    private Camera Xcamera = null;

    public Camera getCamera(PApplet applet) {
        if (Xcamera == null) {
            Xcamera = createCamera(applet);
        }
        return Xcamera;
    }

    protected Camera createCamera(PApplet applet) {
        Camera Xcamera = null;
        Xcamera = CameraFactory.createCamera(Camera.Type.FFMPEG, name() + ".0+0,0", "x11grab");
        Xcamera.setSize(this.getWidth(), this.getHeight());
        Xcamera.setParent(applet);
        Xcamera.start();
        return Xcamera;
    }

}
