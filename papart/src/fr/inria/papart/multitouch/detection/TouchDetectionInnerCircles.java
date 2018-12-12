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
import static fr.inria.papart.multitouch.detection.CalibratedColorTracker.colorFinderLAB;
import static fr.inria.papart.multitouch.detection.CalibratedColorTracker.colorFinderLABError;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.utils.MathUtils;
import tech.lity.rea.utils.WithSize;
import java.util.ArrayList;
import java.util.Arrays;
import processing.core.PImage;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class TouchDetectionInnerCircles extends TouchDetection {

// set by calling function
    public TouchDetectionInnerCircles(WithSize imgSize) {
        super(imgSize);
        currentPointValidityCondition = new CheckColorPoint();
    }

    public static final byte INVALID_COLOR = -2;
    public static final byte UNKNOWN_COLOR = -1;
    protected byte[] localSegmentedImage = null;
    protected byte[] segmentedImage;
    protected byte[] segmentedImageCopy;
    protected int erosionLevel;

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
                    && (segmentedImage[offset] != INVALID_COLOR)
                    //                    || segmentedImage[currentPoint] == UNKNOWN_COLOR)
                    // is the same color/compo or unknown
                    && dist < calib.getMaximumDistance();
        }
    }

    public byte[] createInputArray() {
        localSegmentedImage = new byte[imgSize.getSize()];
        return localSegmentedImage;
    }

    public void resetInputArray() {
        Arrays.fill(localSegmentedImage, TouchDetectionInnerCircles.INVALID_COLOR);
    }

    ColorReferenceThresholds references[];
    PImage mainImg;

    /**
     * Simple element to accept maximum of 4 connected points and reject the
     * rest.
     *
     * @param timestamp
     * @param references
     * @return
     */
    public ArrayList<TrackedElement> compute(int timestamp,
            ColorReferenceThresholds references[],
            PImage mainImg,
            float scale) {
        this.segmentedImage = localSegmentedImage;
        this.references = references;
        this.mainImg = mainImg;
        
        setCurrentTime(timestamp);
        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();
        ArrayList<TrackedElement> colorPoints = this.createTouchPointsFrom(connectedComponents, scale);
        return colorPoints;
    }

    @Override
    protected void setSearchParameters() {
        this.toVisit.clear();

        mainImg.loadPixels();
        for (int i = 0; i < segmentedImage.length; i++) {
            if (segmentedImage[i] != INVALID_COLOR) {
                // Check the color here ? 
                // The index can set the color... 

                int c = mainImg.pixels[i];
                float minError = 30; // Use std error !
                int currentID = -1;

                for (byte id = 0; id < references.length; id++) {
                    float currentError = colorFinderLABError(c, references[id]);

                    float err =  references[id].AThreshold + references[id].BThreshold + references[id].LThreshold;
//                    boolean smallError = colorFinderLAB(c, references[id], err);
                    
                    if ( currentError < minError) {
//                    if (smallError && currentError < minError) {
                        minError = currentError;
                        currentID = id;
                    }
                }

                if (currentID != -1) {
                    segmentedImage[i] = (byte) (currentID);
                    toVisit.add(i);
                }

            }
        }
    }

//    protected abstract void setSearchParameters();
    protected ArrayList<TrackedElement> createTouchPointsFrom(
            ArrayList<ConnectedComponent> connectedComponents,
            float scale) {
        ArrayList<TrackedElement> touchPoints = new ArrayList<TrackedElement>();
        for (ConnectedComponent connectedComponent : connectedComponents) {

            if (connectedComponent.size() < calib.getMinimumComponentSize()) {
                continue;
            }

            // Check if it matches a color.
            byte selectedID = UNKNOWN_COLOR;
            boolean ambiguous = false;

            // TODO: number of colors 
            // WARNING: magic number
            int[] colors = new int[6];
            for (int idx : connectedComponent) {
                byte candidate = segmentedImage[idx];
                if (candidate > 0) {
                    colors[candidate]++;
                }
            }
            int max = 0;
            int id = 0;
            for (int i = 0; i < colors.length; i++) {
                if (colors[i] > max && colors[i] > UNKNOWN_COLOR) {
                    max = colors[i];
                    id = i;
                }
            }
            selectedID = (byte) id;
            
            if (id > 0) {
                for (int i = 1; i < colors.length; i++) {
                    if (i != id && colors[i] != 0) {
                        ambiguous = true;
                    }
                }
            }

//            for (int idx : connectedComponent) {
//                byte candidate = segmentedImage[idx];
//                if (candidate != UNKNOWN_COLOR) {
//                    if (selectedID != UNKNOWN_COLOR && candidate != selectedID) {
////                        System.out.println("Color attribution conflict: " + candidate + " " + selectedID);
//                        ambiguous = true;
//                    }
//                    selectedID = segmentedImage[idx];
//                }
//            }

            if (!ambiguous && selectedID != UNKNOWN_COLOR) {
                for (int idx : connectedComponent) {
//                    ColorReferenceThresholds ref = references[selectedID - 1];
                    ColorReferenceThresholds ref = references[selectedID];
                    int selectedColor = mainImg.pixels[idx];

                   
//                    ref.updateReference(selectedColor);
                }
            }

//            if (selectedColor == UNKNOWN_COLOR) {
//                System.out.println("Unknown color...");
//                continue;
//            }
            TrackedElement tp = createTouchPoint(connectedComponent);
            tp.getPosition().set(tp.getPosition().x / scale, tp.getPosition().y / scale);
            // We attach the colorID here.
            tp.attachedValue = selectedID;
            touchPoints.add(tp);
        }
        return touchPoints;
    }

    /**
     * @param connectedComponent
     */
    @Override
    protected TrackedElement createTouchPoint(ConnectedComponent connectedComponent
    ) {
        Vec3D meanProj = connectedComponent.getMean(imgSize);
        TrackedElement tp = new TrackedElement();
        tp.setDetection(this);
        tp.setPosition(meanProj);
        tp.setCreationTime(this.currentTime);
        tp.setConfidence(connectedComponent.size());
        return tp;
    }

    public byte[] getSegmentedImage() {
        return segmentedImage;
    }

    public void setSegmentedImage(byte[] segmentedImage) {
        this.segmentedImage = segmentedImage;
    }

}
