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
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.TrackedElement;
import fr.inria.papart.utils.MathUtils;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class TouchDetectionColor extends TouchDetection {

// set by calling function
    public TouchDetectionColor(WithSize imgSize) {
        super(imgSize);
        currentPointValidityCondition = new CheckColorPoint();
    }

    public static final byte INVALID_COLOR = -1;
    private byte[] segmentedImage;
    private byte[] segmentedImageCopy;
    private int erosionLevel;

    private void setErosionLevel(int erosionLevel) {
        this.erosionLevel = erosionLevel;
    }

    public class CheckColorPoint implements PointValidityCondition {

        @Override
        public boolean checkPoint(int offset, int currentPoint) {

            // TODO: not sure if the distance is relevant in this context.
            int x1 = offset % imgSize.getWidth();
            int y1 = (int) (offset / imgSize.getWidth());

            int x2 = currentPoint % imgSize.getWidth();
            int y2 = (int) (currentPoint / imgSize.getWidth());

            float dist = PVector.dist(new PVector(x1, y1), new PVector(x2, y2));

            return !assignedPoints[offset] // not assigned  
                    && segmentedImage[offset] == segmentedImage[currentPoint] // is the same color
                    && dist < calib.getMaximumDistance();
        }
    }

    public ArrayList<TrackedElement> compute(byte[] segmentedImage, int timestamp) {
        return compute(segmentedImage, timestamp, 0);
    }

    public ArrayList<TrackedElement> compute(byte[] segmentedImage, int timestamp, int erosionLevel) {
        this.setSegmentedImage(segmentedImage);
        this.setCurrentTime(timestamp);
        this.setErosionLevel(erosionLevel);

        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();
        ArrayList<TrackedElement> colorPoints = this.createTouchPointsFrom(connectedComponents);
        return colorPoints;
    }

    @Override
    protected void setSearchParameters() {
        this.toVisit.clear();

        for (int i = 0; i < segmentedImage.length; i++) {
            if (segmentedImage[i] != INVALID_COLOR) {
                toVisit.add(i);
            }
        }

        if (erosionLevel > 0 && segmentedImageCopy == null) {
            segmentedImageCopy = new byte[segmentedImage.length];
        }
        // Do an erosion to remove the useless elements

        for (int i = 0; i < erosionLevel; i++) {
            MathUtils.erodePoints2(toVisit,
                    segmentedImage, segmentedImageCopy,
                    calib.getPrecision(), INVALID_COLOR, imgSize);
        }

    }

//    protected abstract void setSearchParameters();
    protected ArrayList<TrackedElement> createTouchPointsFrom(ArrayList<ConnectedComponent> connectedComponents) {
        ArrayList<TrackedElement> touchPoints = new ArrayList<TrackedElement>();
        for (ConnectedComponent connectedComponent : connectedComponents) {

            if (connectedComponent.size() < calib.getMinimumComponentSize()) {
                continue;
            }

            TrackedElement tp = createTouchPoint(connectedComponent);
            touchPoints.add(tp);
        }
        return touchPoints;
    }

    @Override
    protected TrackedElement createTouchPoint(ConnectedComponent connectedComponent) {
        Vec3D meanProj = connectedComponent.getMean(imgSize);
        TrackedElement tp = new TrackedElement();
        tp.setDetection(this);
        tp.setPosition(meanProj);
        tp.setCreationTime(this.currentTime);
        tp.setConfidence(connectedComponent.size());

        // TODO: re-enable this one day ?
//        tp.setConnectedComponent(connectedComponent);
// EXPERIMENTAL, check if a copy is necessary
        tp.setSource(connectedComponent);
        return tp;
    }

    public byte[] getSegmentedImage() {
        return segmentedImage;
    }

    public void setSegmentedImage(byte[] segmentedImage) {
        this.segmentedImage = segmentedImage;
    }

}
