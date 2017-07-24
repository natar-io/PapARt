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
package fr.inria.papart.multitouch.metaphors;

import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.multitouch.tracking.TouchPointEventHandler;
import static processing.core.PApplet.acos;
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole
 */
public class TwoFingersRST extends RSTTransform {

    protected Touch[] touchs = new Touch[2];
    protected TouchList touchList;

    class TouchHandler implements TouchPointEventHandler {

        public Touch touch;
        public int id;

        public TouchHandler(int id) {
            this.id = id;
        }

        @Override
        public void delete() {
//            System.out.println("Touch " + id + " delete()" + touchs[id]);
            touchs[id] = Touch.INVALID;
        }

    }

    public TwoFingersRST(PVector size) {
        super(size);
        touchs[0] = Touch.INVALID;
        touchs[1] = Touch.INVALID;
    }

    @Override
    public void update(TouchList globalList, int currentTime) {
        TouchList touchList2D = globalList.get2DTouchs();

        touchList = touchList2D.getOldOnes(currentTime);
        twoFingerMovement();
    }

    private int minY = 0;

    public void setDisabledYZone(int min) {
        this.minY = min;
    }

    protected boolean validBounds(Touch touch) {
        return touch.position.y > minY;
    }

    private boolean getValidTouchs() {

        for (int i = 0; i < touchs.length; i++) {

            if (touchs[i] != Touch.INVALID) {
                if (isInValidTouch(touchs[i])) {
                    touchs[i].trackedSource.attachedObject = null;
//                    System.out.println("Touch " + i + " not valid anymore." + touchs[i]);
                    touchs[i] = Touch.INVALID;
                }
            }

            if (touchs[i] == Touch.INVALID) {
                touchs[i] = getNewTouch(i);
                if (touchs[i] != Touch.INVALID) {
//                    System.out.println("Touch " + i + " is new !." + touchs[i]);
                }
            }
        }

        if (touchs[0] == Touch.INVALID || touchs[1] == Touch.INVALID) {
            return false;
        }

//        System.out.println("touch speed " + touchs[0].speed.mag());

        if (touchs[0].speed.mag() > 10) {
//            System.out.println("Jump filtered !");
            return false;
        }
        if (touchs[1].speed.mag() > 10) {
//             System.out.println("Jump filtered !");
            return false;
        }

        // check other...
        return true;
    }

    private boolean isInValidTouch(Touch t) {
        return t.isGhost || t.isObject || !validBounds(t);
    }

    private Touch getNewTouch(int id) {
        for (Touch touch : touchList) {

            if (!validBounds(touch)
                    || touch.trackedSource.attachedObject != null
                    || touch.isGhost) {
                continue;
            }

            // touch without "attachment"
            if (touch.trackedSource.attachedObject == null) {
                // tag it.
                touch.trackedSource.attachedObject = new TouchHandler(id);
                return touch;
            }
        }
        return Touch.INVALID;
    }

    protected void twoFingerMovement() {

        if (!getValidTouchs()) {
            emptyUpdate();
            return;
        }

//        System.out.println("Full Update");
        // Every values needs to be divided by 2... for some reason.
        float rot = computeRotation(touchs[0], touchs[1]);
        if (!Float.isNaN(rot)) // &&  abs(rot) > PI / 90f)
        {
            addRotation(rot / 2f);
        }

        float scale = computeScale(touchs[0], touchs[1]);
        if (!Float.isNaN(scale)) //  &&  abs(scale) > 0.8)
        {
            float halfScale = (scale - 1f) / 2f + 1;
            multScale(halfScale);
        }

        PVector translate = computeTranslate(touchs[0], touchs[1]);
        translate.mult(0.5f);
        addTranslation(translate);
    }

    protected float computeRotation(Touch touch0, Touch touch1) {
        PVector currentDirection = PVector.sub(touch0.pposition, touch1.pposition);
        PVector previousDirection = PVector.sub(touch0.position, touch1.position);
        currentDirection.normalize();
        previousDirection.normalize();

        float cos = currentDirection.dot(previousDirection);
        float angle = acos(cos);

        PVector sin = currentDirection.cross(previousDirection);
        if (sin.z < 0) {
            angle = -angle;
        }
        return angle;
    }

    protected float computeScale(Touch touch0, Touch touch1) {
        PVector distT0 = touch0.pposition.get();
        distT0.sub(touch1.pposition);

        PVector distT1 = touchs[0].position.get();
        distT1.sub(touch1.position);

        return distT1.mag() / distT0.mag();
    }

    protected PVector computeTranslate(Touch touch0, Touch touch1) {
        PVector previousCenter = PVector.add(touch0.pposition, touch1.pposition);
        previousCenter.mult(0.5f);
        PVector currentCenter = PVector.add(touch0.position, touch1.position);
        currentCenter.mult(0.5f);
        PVector diff = PVector.sub(currentCenter, previousCenter);
//        diff.mult(0.5f);
        return diff;
    }

}
