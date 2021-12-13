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
package fr.inria.papart.multitouch.tracking;

import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.detection.TouchDetection;

/**
 * Tracked element with a position and speed. The position can be filtered, the
 * filtering must be called directly, it is not automatic.
 *
 * @author Jeremy Laviole
 */
public class TrackedElement extends TrackedPosition {

    /**
     * You can attach a value to a Tracked Element, it will be passed along
     * tracking.
     */
    public int attachedValue = -1;
    /**
     * You can attach an object to a Tracked Element, it will be passed along
     * tracking.
     */
    public Object attachedObject;

    protected float confidence;

    // Element it originates from.
    protected TouchDetection detection;

    // Element it goes to 
    protected final Touch touch;

    /**
     * Create a trackedElement and force an ID to it.
     *
     * @param id
     */
    public TrackedElement(int id) {
        this();
        this.id = id;
        initTouch();
    }

    /**
     * Create a TrackedElement, a new ID will be assigned to it.
     */
    public TrackedElement() {
        super();
        touch = new Touch();
        initTouch();
    }

    private void initTouch() {
        touch.id = this.id;
        touch.trackedSource = this;
    }

    /**
     *
     * @param tp
     */
    @Override
    protected void updateAdditionalElements(TrackedElement tp) {
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    @Override
    public void delete(int time) {
        super.delete(time);
        if (this.attachedObject != null) {
            if (this.attachedObject instanceof TouchPointEventHandler) {
                ((TouchPointEventHandler) this.attachedObject).delete();
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + " Confidence: " + confidence + ". \n";
    }

    // TODO: find a way to handle in a better way.
    public boolean hasTouch() {
        return touch != null;
    }

    public Touch getTouch() {
        touch.id = this.id;
        return touch;
    }

    /**
     * Get the detection object that created this tracked element.
     *
     * @return
     */
    public TouchDetection getDetection() {
        return detection;
    }

    /**
     * Set the detection object that created this tracked element.
     *
     * @param detection
     */
    public void setDetection(TouchDetection detection) {
        this.detection = detection;
    }

    public boolean mainFinger = false;

    public void setMainFinger() {
        this.mainFinger = true;
    }

    @Override
    public boolean updateWith(Trackable tp) {
        return updateWith((TrackedElement) (tp));
    }

    @Override
    public float getTrackingMaxDistance() {
        return detection.getTrackingMaxDistance();
    }

    @Override
    public boolean isObselete(int currentTime) {
        return (currentTime - updateTime) > detection.getTrackingForgetTime();
    }

}
