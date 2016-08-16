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
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole
 */
public class TwoFingersRST3D extends TwoFingersRST{

    public TwoFingersRST3D(PVector size) {
        super(size);
    }
    
    
    // TODO: BROKEN -> TO CHECK
    @Override
     protected void twoFingerMovement() {

        assert (touchList.size() >= 2);
        Touch touch0 = touchList.get(0);
        Touch touch1 = touchList.get(1);

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
