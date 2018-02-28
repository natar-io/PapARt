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

import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.PaperTouchScreen;
import tech.lity.rea.skatolo.Skatolo;
import tech.lity.rea.skatolo.gui.Pointer;
import java.util.ArrayList;
import java.util.HashMap;
import processing.core.PApplet;
import processing.core.PVector;
import tech.lity.rea.skatolo.gui.Pointer.Type;

/**
 *
 * @author Jérémy Laviole
 */
public class SkatoloLink {

    public static HashMap<Skatolo, HashMap<Pointer.Type, ArrayList<Integer>>> pointersMap = new HashMap();

    // Warning -> Must be linked to a physical screen // a Renderer. 
    // Warning -> the mouse must be "displayed" ... 
    public static Touch mouseTouch = new Touch();
//    public static int mouseTouchID = -30;

    public static void addMouseTo(TouchList touchList, Skatolo skatolo, PaperScreen paperScreen) {
        PApplet applet = Papart.getPapart().getApplet();

        // Use this ?
//        Touch touchFromMouse = paperScreen.createTouchFromMouse();
//        touchList.add(touchFromMouse);
//        mouseTouch = touchFromMouse;
        /// Compute here or there ?
        float normX = (float) applet.mouseX / (float) applet.width;
        float normY = (float) applet.mouseY / (float) applet.height;
        PVector pointer = paperScreen.getDisplay().project(paperScreen, normX, normY);
        mouseTouch.setPosition(pointer.x, pointer.y, 0);
        mouseTouch.setPosition(pointer.x * paperScreen.getDrawingSize().x,
                pointer.y * paperScreen.getDrawingSize().y, 0);
        touchList.add(mouseTouch);
    }

    /**
     * UpdateTouch, legacy all are fingers.
     *
     * @param touchList
     * @param skatolo
     */
    public static void updateTouch(TouchList touchList, Skatolo skatolo) {

        // TODO: Full integration !
        // TODO: use the pointerList ?
        ArrayList<Integer> pointers;
        if (pointersMap.containsKey(skatolo)) {
            pointers = pointersMap.get(skatolo).get(Pointer.Type.FINGER);
        } else {
            pointers = new ArrayList<>();

            HashMap<Pointer.Type, ArrayList<Integer>> types = new HashMap<>();
            types.put(Pointer.Type.FINGER, pointers);
            pointersMap.put(skatolo, types);
        }

        // 2D touchs. 
        for (Touch t : touchList.get2DTouchs()) {

            // valid ones -> should be checked before.
            if (t.id != TrackedDepthPoint.NO_ID) {

                // It is new for Skatolo
                if (!pointers.contains(t.id)) {
                    Pointer pointer = skatolo.addPointer(t.id);

                    pointer.setType(Pointer.Type.FINGER);
                    pointers.add(t.id);
                }
                // Simple update
                PVector p = t.position;
                skatolo.updatePointer(t.id, (int) p.x, (int) p.y);

                skatolo.updatePointerPress(t.id, true);
            }
        }

        ArrayList<Integer> currentTouchIds = touchList.get2DTouchs().getIds();
        ArrayList<Integer> toDelete = (ArrayList<Integer>) pointers.clone();
        toDelete.removeAll(currentTouchIds);

        // Remove the ones that disappeared
        for (ArrayList<Integer> knownIDs : pointersMap.get(Type.FINGER).values()) {
            if (!touchList.get2DTouchs().contains(knownIDs)) {
                toDelete.remove(knownIDs);
            }
        }

        for (Integer pointerId : toDelete) {
            // WARNING -1 -1 
            skatolo.updatePointer(pointerId, (int) -1, (int) -1);
            skatolo.updatePointerPress(pointerId, true);

            skatolo.removePointer(pointerId);
            pointers.remove(pointerId);

        }

    }

    public static void updateTouch(TouchList touchList, Skatolo skatolo,
            Pointer.Type type) {

        // TODO: Full integration !
        // TODO: use the pointerList ?
        ArrayList<Integer> pointers;
        if (pointersMap.containsKey(skatolo)) {
            pointers = pointersMap.get(skatolo).get(type);
        } else {
            pointers = new ArrayList<>();
            HashMap<Pointer.Type, ArrayList<Integer>> types = new HashMap<>();
            types.put(type, pointers);
            pointersMap.put(skatolo, types);
        }

        // 2D touchs. 
        for (Touch t : touchList) {

            // It is new for Skatolo
            if (!pointers.contains(t.id)) {
                Pointer pointer = skatolo.addPointer(t.id);
                pointer.setType(type);
                pointers.add(t.id);
            }
            // Simple update
            PVector p = t.position;
            skatolo.updatePointer(t.id, (int) p.x, (int) p.y);

            skatolo.updatePointerPress(t.id, true);
        }

        ArrayList<Integer> currentTouchIds = touchList.get2DTouchs().getIds();
        ArrayList<Integer> toDelete = (ArrayList<Integer>) pointers.clone();
        toDelete.removeAll(currentTouchIds);

        // Remove the ones that disappeared
        for (ArrayList<Integer> knownIDs : pointersMap.get(type).values()) {
            if (!touchList.get2DTouchs().contains(knownIDs)) {
                toDelete.remove(knownIDs);
            }
        }

        for (Integer pointerId : toDelete) {
            // WARNING -1 -1 
            skatolo.updatePointer(pointerId, (int) -1, (int) -1);
            skatolo.updatePointerPress(pointerId, true);

            skatolo.removePointer(pointerId);
            pointers.remove(pointerId);

        }

    }

}
