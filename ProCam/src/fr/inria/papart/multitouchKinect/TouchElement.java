/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import fr.inria.papart.multitouchKinect.TouchPoint;
import java.util.ArrayList;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class TouchElement {
    
    public ArrayList<PVector> position2D;
    public ArrayList<PVector> position3D;
    public ArrayList<PVector> speed2D;
    public ArrayList<PVector> speed3D;

    public ArrayList<TouchPoint> points2D;
    public ArrayList<TouchPoint> points3D;
}
