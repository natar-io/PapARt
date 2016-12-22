/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2016 RealityTech
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.PaperTouchScreen;
import fr.inria.skatolo.Skatolo;
import fr.inria.skatolo.gui.Pointer;
import java.util.ArrayList;
import java.util.HashMap;
import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author Jérémy Laviole 
 */
public class SkatoloLink {

    public static HashMap<Skatolo, ArrayList<Integer>> pointersMap = new HashMap();
    
    // Warning -> Must be linked to a physical screen // a Renderer. 
    // Warning -> the mouse must be "displayed" ... 
    
    public static Touch mouseTouch = new Touch(); 
  
    public static void addMouseTo(TouchList touchList, Skatolo skatolo, PaperScreen paperScreen) {
        PApplet applet = Papart.getPapart().getApplet();

        float normX = (float) applet.mouseX / (float) applet.width;
        float normY = (float) applet.mouseY / (float) applet.height;
//        PVector pointer = paperScreen.getDisplay().projectPointer(paperScreen.getScreen(), normX, normY);
        PVector pointer = paperScreen.getDisplay().project(paperScreen.getScreen(), normX, normY);
        
        System.out.println("Pointer: " + pointer);
        
        mouseTouch.setPosition(pointer.x * paperScreen.getDrawingSize().x, 
                 pointer.y * paperScreen.getDrawingSize().y, 0);
        touchList.add(mouseTouch);
    }

    public static void updateTouch(TouchList touchList, Skatolo skatolo) {

        // TODO: Full integration !
        // TODO: use the pointerList ?
        ArrayList<Integer> pointers;
        if (pointersMap.containsKey(skatolo)) {
            pointers = pointersMap.get(skatolo);
        } else {
            pointers = new ArrayList<>();
            pointersMap.put(skatolo, pointers);
        }

        for (Touch t : touchList.get2DTouchs()) {
            PVector p = t.position;
            if (t.id != TouchPoint.NO_ID) {

                if (!pointers.contains(t.id)) {
                    Pointer pointer = skatolo.addPointer(t.id);
                    pointer.setType(Pointer.Type.TOUCH);

                    pointers.add(t.id);
                    skatolo.updatePointerPress(t.id, true);
                } else {
                    skatolo.updatePointer(t.id, (int) p.x, (int) p.y);
                }

                skatolo.updatePointerPress(t.id, true);
            }
        }

        ArrayList<Integer> currentTouchIds = touchList.get2DTouchs().getIds();

        ArrayList<Integer> toDelete = (ArrayList<Integer>) pointers.clone();
        toDelete.removeAll(currentTouchIds);

        for (Integer pointerId : toDelete) {
            skatolo.removePointer(pointerId);
            pointers.remove(pointerId);
        }

    }

}
