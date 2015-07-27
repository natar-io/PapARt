/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import java.util.ArrayList;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public abstract class TouchInput {

    public static PVector NO_INTERSECTION = new PVector();

    abstract public void update();

    abstract public TouchList projectTouchToScreen(Screen screen, BaseDisplay display);

    public PVector project(Screen screen, BaseDisplay display, float x, float y) {
        boolean isProjector = display instanceof ProjectorDisplay;
        boolean isARDisplay = display instanceof ARDisplay;

        // check that the correct method is called !
        PVector paperScreenCoord;
        if (isProjector) {
            paperScreenCoord = ((ProjectorDisplay) display).projectPointer(screen, x, y);
        } else {
            if (isARDisplay) {
                paperScreenCoord = ((ARDisplay) display).projectPointer(screen, x, y);
            } else {
                paperScreenCoord = display.projectPointer(screen, x, y);
            }
        }
        return paperScreenCoord;
    }

    protected boolean computeOutsiders = false;

    public void computeOutsiders(boolean outsiders) {
        this.computeOutsiders = outsiders;
    }

}
