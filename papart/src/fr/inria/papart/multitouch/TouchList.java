/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
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

import fr.inria.papart.multitouch.tracking.TouchComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole
 */
public class TouchList extends ArrayList<Touch> {

    public void scaleBy(PVector scales) {
        for (Touch touch : this) {
            touch.scaleBy(scales);
        }
    }

    public void addOffset(PVector offset) {
        for (Touch touch : this) {
            touch.addOffset(offset);
        }
    }

    public void sortAlongYAxis() {
        Collections.sort(this, new TouchComparator());
    }

    public void removeYoungOnes(int currentTime) {
        for (Iterator<Touch> it = this.iterator(); it.hasNext();) {
            Touch touch = it.next();
            if (touch.trackedSource.isYoung(currentTime)) {
                it.remove();
            }
        }
    }

    public TouchList getOldOnes(int currentTime) {
        TouchList old = new TouchList();
        for (Touch touch : this) {
            if (!touch.trackedSource.isYoung(currentTime)) {
                old.add(touch);
            }
        }
        return old;
    }

    public Touch getTouchWithId(int id) {
        for (Touch touch : this) {
            if (touch.id == id) {
                return touch;
            }
        }
        return Touch.INVALID;
    }

    public boolean containsTouchWithId(int id) {
        for (Touch touch : this) {
            if (touch.id == id) {
                return true;
            }
        }
        return false;
    }
    
    public ArrayList<Integer> getIds(){
        ArrayList<Integer> ids = new ArrayList<>();
        for(Touch t : this){
            ids.add(t.id);
        }
        
        return ids;
    }

    public TouchList get2DTouchs() {
        return filterByType(false);
    }

    public TouchList get3DTouchs() {
        return filterByType(true);
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

    public void invertY(PVector drawingSize) {
        for (Touch touch : this) {
            touch.invertY(drawingSize.y);
        }
    }
    
    public String toString(){
        return super.toString();
    }

}
