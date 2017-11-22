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
package fr.inria.papart.tracking;

import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.tracking.ObjectFinder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class MarkerBoardJavaCV extends MarkerBoard {

    private PVector[] objectPoints;
    private PVector[] imagePoints;
    private final PVector topLeft = new PVector();
    private final PVector topRight = new PVector();
    private final PVector botLeft = new PVector();
    private final PVector botRight = new PVector();

    private opencv_core.IplImage imgToFind;

    public MarkerBoardJavaCV(String fileName, float width, float height) throws Exception {
        super(fileName, width, height);
        trackers = new ArrayList<ObjectFinder>();
        imagePoints = new PVector[4];
        objectPoints = new PVector[4];

        objectPoints[0] = new PVector(0, 0, 0);
        objectPoints[1] = new PVector(width, 0, 0);
        objectPoints[2] = new PVector(width, height, 0);
        objectPoints[3] = new PVector(0, height, 0);

        Logger logger = Logger.getLogger(ObjectFinder.class.getName());
        logger.setLevel(Level.OFF);
        this.type = MarkerType.JAVACV_FINDER;
        imgToFind = cvLoadImage(this.fileName);

        // TODO: something less violent than a runtime exception... 
        if (imgToFind == null) {
            throw new Exception("MarkerBoardJavaCV: Impossible to load the image: " + this.fileName);
        }

    }

    @Override
    protected void addTrackerImpl(Camera camera) {

        ObjectFinder finder = new ObjectFinder(imgToFind);
//        finder.getSettings().setUseFLANN(true);
//        finder.getSettings().setMatchesMin(6);
        this.trackers.add(finder);
        this.transfos.add(new PMatrix3D());
    }

    private PMatrix3D compute3DPos(double[] corners, Camera camera) {

        //  double[] srcCorners = {0, 0,  w, 0,  w, h,  0, h};
        botLeft.set((float) corners[0], (float) corners[1]);
        botRight.set((float) corners[2], (float) corners[3]);
        topRight.set((float) corners[4], (float) corners[5]);
        topLeft.set((float) corners[6], (float) corners[7]);

        // check image bounds...
        if (botLeft.x < 0 || botRight.x < 0 || topLeft.x < 0 || topRight.x < 0
                || botLeft.x > camera.width() || botRight.x > camera.width() || topLeft.x > camera.width() || topRight.x > camera.width()
                || botLeft.y < 0 || botRight.y < 0 || topLeft.y < 0 || topRight.y < 0
                || botLeft.y > camera.height() || botRight.y > camera.height() || topLeft.y > camera.height() || topRight.y > camera.height()) {
            return null;
        }

//        imagePoints[0] = botLeft;
//        imagePoints[1] = botRight;
//        imagePoints[2] = topRight;
//        imagePoints[3] = topLeft;
        imagePoints[0] = topLeft;
        imagePoints[1] = topRight;
        imagePoints[2] = botRight;
        imagePoints[3] = botLeft;

//      objectPoints[0] = new PVector(0, 0, 0);
//      objectPoints[1] = new PVector(width, 0, 0);
//      objectPoints[2] = new PVector(width, height, 0);
//      objectPoints[3] = new PVector(0, height, 0);
        ProjectiveDeviceP pdp = camera.getProjectiveDevice();
        return pdp.estimateOrientation(objectPoints, imagePoints);

    }

    @Override
    protected void updatePositionImpl(int id, int currentTime, int endTime, int mode,
            Camera camera, opencv_core.IplImage img, Object globalTracking) {

//        Do not find when not needed. 
        if (this.subscribersAmount == 0
                || (mode == BLOCK_UPDATE && currentTime < endTime)) {
            return;
        }

        try {
            System.out.println("Subscribers: " + this.subscribersAmount);
            ObjectFinder finder = (ObjectFinder) trackers.get(id);

            // TODO: the  finder.find should be done ONCE per image. Not once per board.
            // Find the markers
            double[] corners = finder.find(img);

            // one use... HACK  -- Why 
            // why so evil ?
//        finder = new ObjectFinder(finder.getSettings());
//        trackers.set(id, finder);
            if (corners == null) {
                return;
            }

            PMatrix3D newPos = compute3DPos(corners, camera);

            if (newPos == null) {
                return;
            }

            PVector currentPos = new PVector(newPos.m03, newPos.m13, newPos.m23);
            if (currentPos.z < 10f || currentPos.z > 10000) {
                return;
            }

            float distance = currentPos.dist(lastPos.get(id));

//        System.out.println("Distance " + distance);
//        if (distance > 5000) // 1 meter~?
//        {
//            return;
//        }
            lastDistance.set(id, distance);
            // if the update is forced 
            if (mode == FORCE_UPDATE && currentTime < endTime) {
                update(newPos, id);
                return;
            }

            // the force and block updates are finished, revert back to normal
            if (mode == FORCE_UPDATE || mode == BLOCK_UPDATE && currentTime > endTime) {
                updateStatus.set(id, NORMAL);
            }

            // if it is a drawing mode
            if (drawingMode.get(id)) {

                if (distance > this.minDistanceDrawingMode.get(id)) {
                    update(newPos, id);

                    lastPos.set(id, currentPos);
                    updateStatus.set(id, FORCE_UPDATE);
                    nextTimeEvent.set(id, applet.millis() + MarkerBoard.updateTime);
//                    System.out.println("Next Update for x seconds");
                }

            } else {
                update(newPos, id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update(PMatrix3D newPos, int id) {

        PMatrix3D transfo = (PMatrix3D) transfos.get(id);
        fr.inria.papart.multitouch.OneEuroFilter filter[] = filters.get(id);

        if (filter == null) {
            transfo.set(newPos);
        } else {
            try {
                // Rotation
                transfo.m00 = (float) filter[0].filter(newPos.m00);
                transfo.m01 = (float) filter[1].filter(newPos.m01);
                transfo.m02 = (float) filter[2].filter(newPos.m02);
                transfo.m10 = (float) filter[3].filter(newPos.m10);
                transfo.m11 = (float) filter[4].filter(newPos.m11);
                transfo.m12 = (float) filter[5].filter(newPos.m12);
                transfo.m20 = (float) filter[6].filter(newPos.m20);
                transfo.m21 = (float) filter[7].filter(newPos.m21);
                transfo.m22 = (float) filter[8].filter(newPos.m22);

                // Translation
                transfo.m03 = (float) filter[9].filter(newPos.m03);
                transfo.m13 = (float) filter[10].filter(newPos.m13);
                transfo.m23 = (float) filter[11].filter(newPos.m23);

            } catch (Exception e) {
                System.out.println("Filtering error " + e);
            }
        }
//
//        // If z negation hack required...
//         PMatrix3D tmp = new PMatrix3D(transfo[0], transfo[1], transfo[2], transfo[3],
//                transfo[4], transfo[5], transfo[6], transfo[7],
//                transfo[8], transfo[9], transfo[10], transfo[11],
//                0, 0, 0, 1);
////         tmp.print();
//        tmp.scale(1, 1, -1);
//        transfo[11] = -transfo[11];
    }

}
