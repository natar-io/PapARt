/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.utils.MathUtils;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import java.util.Arrays;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class TouchDetectionLargeColor extends TouchDetectionColor {

// set by calling function
    public TouchDetectionLargeColor(WithSize imgSize) {
        super(imgSize);
    }

    public class CheckColorPoint implements PointValidityCondition {

        private int inititalPoint;

        public void setInitialPoint(int offset) {
            this.inititalPoint = offset;
        }

        @Override
        public boolean checkPoint(int offset, int currentPoint) {

            // TODO: not sure if the distance is relevant in this context.
            int x1 = offset % imgSize.getWidth();
            int y1 = (int) (offset / imgSize.getWidth());

            int x2 = currentPoint % imgSize.getWidth();
            int y2 = (int) (currentPoint / imgSize.getWidth());

            float dist = PVector.dist(new PVector(x1, y1), new PVector(x2, y2));

            return !assignedPoints[offset] // not assigned  
                    && segmentedImage[offset] != -1 // is a color/compo.
                    && dist < calib.getMaximumDistance();
        }
    }

}
