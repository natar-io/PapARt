/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.papart.multitouch.metaphors;

import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class TwoFingersRST3D extends TwoFingersRST{

    public TwoFingersRST3D(PVector size) {
        super(size);
    }
    
    @Override
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
            addRotation(rot *2 );
        }

        float scale = computeScale(touch0, touch1);
        if (!Float.isNaN(scale)) //  &&  abs(scale) > 0.8)
        {
//            float halfScale = (scale - 1f) / 2f + 1;
            multScale(scale);
        }

        PVector translate = computeTranslate(touch0, touch1);
        addTranslation(translate);
    }
    
}
