/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

import fr.inria.skatolo.Skatolo;
import fr.inria.skatolo.gui.Pointer;
import java.util.ArrayList;
import java.util.HashMap;
import processing.core.PVector;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class SkatoloLink {

    public static HashMap<Skatolo, ArrayList<Integer>> pointersMap = new HashMap();
    
    public static void updateTouch(TouchList touchList, Skatolo skatolo) {

        // TODO: Full integration !
        // TODO: use the pointerList ?
        ArrayList<Integer> pointers;
        if(pointersMap.containsKey(skatolo)){
            pointers = pointersMap.get(skatolo);
        }else {
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
