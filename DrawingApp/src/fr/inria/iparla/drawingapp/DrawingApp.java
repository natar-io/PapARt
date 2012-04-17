/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.iparla.drawingapp;

import fr.inria.iparla.drawingapp.shape.Shape;
import java.util.ArrayList;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class DrawingApp {

    public static ArrayList<InteractiveZone> zones = new ArrayList<InteractiveZone>();
    public static ArrayList<Drawable> drawables = new ArrayList<Drawable>();
    public static boolean isAdujusting = true;
    public static Shape currentShape = null;

    // Precise edition
    public static InteractiveZone currentIZ = null;
    public static boolean isSearchingIZ;

    public static boolean isTranslateActive = false;
    public static ButtonWidget preciseWidget = new ButtonWidget("precis.png", new PVector(0,0), 0,0, 80, 80 );
    public static PVector preciseWidgetInitPos;

    public static int imageIntensity = 80;

    public static void selectShape(Shape s) {
        if (currentShape != null) {
            currentShape.select(false);
        }
        currentShape = s;
        s.select(true);
    }

    public static void selectPositionDescriptor(InteractiveZone iz) {

        currentIZ = iz;
    }
}
