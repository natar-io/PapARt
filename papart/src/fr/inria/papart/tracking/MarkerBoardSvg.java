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

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.ProjectiveDeviceP;
import static fr.inria.papart.tracking.MarkerBoard.BLOCK_UPDATE;
import static fr.inria.papart.tracking.MarkerBoard.FORCE_UPDATE;
import static fr.inria.papart.tracking.MarkerBoard.NORMAL;
import fr.inria.papart.procam.camera.Camera;
import java.util.ArrayList;
import org.bytedeco.javacpp.opencv_core;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.data.XML;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class MarkerBoardSvg extends MarkerBoard {

    private final MarkerList markersFromSVG;

    public MarkerBoardSvg(String fileName, float width, float height) {
        super(fileName, width, height);
        // Trackers not used
//        trackers = new ArrayList<>();
        this.type = MarkerType.SVG;

        // TODO: better than getting the Papart object...
        XML xml = Papart.getPapart().getApplet().loadXML(getFileName());
        markersFromSVG = MarkerSvg.getMarkersFromSVG(xml);

    }

    @Override
    protected void addTrackerImpl(Camera camera) {
        // No specific tracker for Markers, as it is global. 
        // for now... 
        PMatrix3D tr = new PMatrix3D();
        this.transfos.add(tr);
    }

    public int MIN_ARTOOLKIT_MARKER_DETECTED = 1;

    @Override
    protected void updatePositionImpl(int id,
            int currentTime,
            int endTime,
            int mode,
            Camera camera,
            opencv_core.IplImage img,
            Object globalTracking) {

        DetectedMarker[] markers = (DetectedMarker[]) globalTracking;
        PMatrix3D newPos = DetectedMarker.compute3DPos(markers, markersFromSVG, camera);

        if (newPos == INVALID_LOCATION) {
            return;
        }
        PVector currentPos = new PVector(newPos.m03, newPos.m13, newPos.m23);

        // Cannot detect elements as close as closer than 10cm
        if (currentPos.z < 10) {
            return;
        }

        // if the update is forced 
        if (mode == FORCE_UPDATE && currentTime < endTime) {
            update(newPos, id);
            return;
        }

        // the force and block updates are finished, revert back to normal
        if (mode == FORCE_UPDATE || mode == BLOCK_UPDATE && currentTime > endTime) {
            updateStatus.set(id, NORMAL);
        }

        float distance = currentPos.dist(lastPos.get(id));
        lastDistance.set(id, distance);

        // if it is a drawing mode
        if (drawingMode.get(id)) {

            if (distance > this.minDistanceDrawingMode.get(id)) {
                update(newPos, id);
                lastPos.set(id, currentPos);
                updateStatus.set(id, FORCE_UPDATE);
                nextTimeEvent.set(id, applet.millis() + MarkerBoard.updateTime);
            }

        } else {
            update(newPos, id);
            lastPos.set(id, currentPos);
            
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

        float pageHeight = markersFromSVG.getSheetHeight();

        // Invert the scales so that it fits Inkscape's view. 
        transfo.scale(1, -1, 1);
        transfo.translate(0, -pageHeight, 0);
    }

}
