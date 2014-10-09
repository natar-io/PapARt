/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

import java.util.Comparator;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class TouchPointComparator implements Comparator {

    static final PVector origin = new PVector(0, 0, 0);

    @Override
    public int compare(Object tp1, Object tp2) {
        PVector pos1 = ((TouchPoint) tp1).getPosition();
        PVector pos2 = ((TouchPoint) tp2).getPosition();

        if (pos2.dist(origin) < pos1.dist(origin)) {
            return 1;
        }
        return -1;
    }
}
