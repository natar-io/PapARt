/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.multitouch.metaphors.TouchComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class TouchList extends ArrayList<Touch> {

    public void scaleBy(PVector scales) {
        for (Touch touch : this) {
            touch.scaleBy(scales);
        }
    }
    
    public void sortAlongYAxis(){
          Collections.sort(this, new TouchComparator());
    }
    
    public void removeYoungOnes(int currentTime){
        for (Iterator<Touch> it = this.iterator(); it.hasNext();) {
            Touch touch = it.next();
            if(touch.touchPoint.isYoung(currentTime)){
                it.remove();
            }
        }
    }
    
    public TouchList getOldOnes(int currentTime){
        TouchList old = new TouchList();
        for (Touch touch : this) {
            if(!touch.touchPoint.isYoung(currentTime)){
                old.add(touch);
            }
        }
        return old;
    }

    public TouchList get2DTouchs() {
        return filterByType(false);
    }

    public TouchList get3DTouchs() {
        return filterByType(false);
    }

    private TouchList filterByType(boolean get3D) {
        TouchList selectedTouch = new TouchList();

        if (get3D) {
            for (Touch touch : this) {
                if (touch.is3D) {
                    selectedTouch.add(touch);
                }
            }
        } else {
            for (Touch touch : this) {
                if (!touch.is3D) {
                    selectedTouch.add(touch);
                }
            }
        }
        return selectedTouch;
    }
}
