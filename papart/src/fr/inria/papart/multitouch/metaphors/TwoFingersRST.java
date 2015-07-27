/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.multitouch.metaphors;

import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import static processing.core.PApplet.acos;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class TwoFingersRST extends RSTTransform {

    public TwoFingersRST(PVector size) {
        super(size);
    }

    @Override
    public void update(TouchList touchList, int currentTime) {
        TouchList touchList2D = touchList.get2DTouchs();
        TouchList oldList = touchList2D.getOldOnes(currentTime);
        if (oldList.size() >= 2) {
            twoFingerMovement(oldList);
        } else {
            emptyUpdate();
        }
    }

    private int minY = 0;

    public void setDisabledYZone(int min) {
        this.minY = min;
    }

    protected boolean validBounds(Touch touch) {
        return touch.position.y > minY;
    }

    protected void twoFingerMovement(TouchList touchList2D) {

        assert (touchList2D.size() >= 2);
        Touch touch0 = touchList2D.get(0);
        Touch touch1 = touchList2D.get(1);

        if (!validBounds(touch0) || !validBounds(touch1)) {
            emptyUpdate();
            return;
        }

        // Every values needs to be divided by 2... for some reason.
        float rot = computeRotation(touch0, touch1);
        if (!Float.isNaN(rot)) // &&  abs(rot) > PI / 90f)
        {
            addRotation(rot / 2f);
        }

        float scale = computeScale(touch0, touch1);
        if (!Float.isNaN(scale)) //  &&  abs(scale) > 0.8)
        {
            float halfScale = (scale - 1f) / 2f + 1;
            multScale(halfScale);
        }

        PVector translate = computeTranslate(touch0, touch1);
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

        PVector distT1 = touch0.position.get();
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
