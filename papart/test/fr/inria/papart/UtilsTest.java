/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

import fr.inria.papart.procam.Utils;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class UtilsTest extends PApplet {

    @Override
    public void setup() {
        size(200, 200);
        stroke(155, 0, 0);
    }

    @Override
    public void draw() {
        line(mouseX, mouseY, width / 2, height / 2);

    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"--present", "fr.inria.papart.UtilsTest"});
    }

}
