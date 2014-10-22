/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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
