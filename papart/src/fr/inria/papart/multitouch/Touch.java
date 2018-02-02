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

import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import processing.core.PVector;

/**
 * Public (Processing) API for a touch point. Touches are for PaperScreens
 *
 * @author Jeremy Laviole
 */
public class Touch {

    public static final Touch INVALID = new Touch();
    
    public boolean is3D;
    public boolean isGhost;

    public PVector position = new PVector();
    public PVector pposition = new PVector();
    public PVector speed = new PVector();
    
    // TODO: find a solution for this !
    // Always has a TouchPoint linked ? Not clean. 
    public TrackedElement trackedSource;
    
    @Deprecated
    public TrackedDepthPoint touchPoint;
    // TODO: switch to TrackedElement
//    public TrackedElement trackedSource;
    
    public PVector size;

    // TODO: implementation of this. 
    public boolean isObject;
    public int id;

    public void setPosition(PVector v) {
        setPosition(v.x, v.y, v.z);
    }
    
    /**
     * Legacy access to the touchPoint, use trackedSource now.
     * @return 
     */
    @Deprecated
    public TrackedElement touchPoint(){
        return trackedSource;
    }
    
    /**
     * Get the element from the tracking system. This element may store 
     * information across time. You can register events triggered when the 
     * point disappears.
     * To do so you can attach an object in the attachedObject field. If this 
     * object implements the TouchPointEventHandler interface, delete() will be  the call to 
     * called when the tracked element gets off the tracking system. 
     * 
     * -- Also:  new name of TouchPoint.
     * @return 
     */
    public TrackedElement trackedSource(){
        return trackedSource;
    }

    public void setPosition(float x, float y, float z) {
        this.pposition.set(this.position);
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
        this.speed.set(PVector.sub(this.position, this.pposition));
    }

    @Override
    public String toString() {
        return "Position " + position + " Speed " + speed + " Touch info " + trackedSource;
    }

    public void setPrevPos(PVector prevPosition) {
        pposition = prevPosition;
        speed = PVector.sub(prevPosition, position);
    }

    public void defaultPrevPos() {
        pposition = position.get();
        speed = new PVector();
    }

    public void addOffset(PVector offset) {
        position.x += offset.x;
        position.y += offset.y;
        position.z += offset.z;
    }
    
    public void scaleBy(PVector scales) {
        position.x *= scales.x;
        position.y *= scales.y;
        position.z *= scales.z;

        pposition.x *= scales.x;
        pposition.y *= scales.y;
        pposition.z *= scales.z;

        speed.x *= scales.x;
        speed.y *= scales.y;
        speed.z *= scales.z;
    }

    public void invertY(float sizeY) {
        position.y = sizeY - position.y;
        pposition.y = sizeY - pposition.y;
        speed.y = -speed.y;
    }
}
