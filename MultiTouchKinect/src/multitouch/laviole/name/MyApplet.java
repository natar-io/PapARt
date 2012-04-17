/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multitouch.laviole.name;

import processing.core.PApplet;

/**
 *
 * @author jeremy
 */
public class MyApplet {

    public static final int w = 640;
    public static final int h = 480;

    static PApplet pa = null;
    public static float height3D = 15f;

    static void init(PApplet applet) {
        pa = applet;
    }

    static PApplet get() {
        return pa;
    }
}
