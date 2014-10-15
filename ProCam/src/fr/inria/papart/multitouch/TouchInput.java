/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.procam.ARDisplay;
import fr.inria.papart.procam.BaseDisplay;
import fr.inria.papart.procam.Projector;
import fr.inria.papart.procam.Screen;
import java.util.ArrayList;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public abstract class TouchInput {

   
    
    abstract public void update();

    abstract public TouchList projectTouchToScreen(Screen screen, BaseDisplay display);

    protected PVector project(Screen screen, BaseDisplay display, float x, float y) throws Exception {
        boolean isProjector = display instanceof Projector;
        boolean isARDisplay = display instanceof ARDisplay;

        // check that the correct method is called !
        PVector paperScreenCoord;
        if (isProjector) {
            paperScreenCoord = ((Projector) display).projectPointer(screen, x, y);
        } else {
            if (isARDisplay) {
                paperScreenCoord = ((ARDisplay) display).projectPointer(screen, x, y);
            } else {
                paperScreenCoord = display.projectPointer(screen, x, y);
            }
        }
        return paperScreenCoord;
    }
}
