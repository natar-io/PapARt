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

import tech.lity.rea.nectar.markers.DetectedMarker;
import tech.lity.rea.nectar.markers.MarkerList;
import tech.lity.rea.nectar.camera.Camera;
import tech.lity.rea.nectar.camera.CameraNectar;
import tech.lity.rea.nectar.camera.SubCamera;
import org.bytedeco.javacpp.opencv_core;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.data.JSONArray;
import tech.lity.rea.nectar.tracking.MarkerBoard;
import tech.lity.rea.nectar.tracking.MarkerBoardSvg;

/**
 *
 * @author Jérémy Laviole - laviole@rea.lity.tech
 */
public class MarkerBoardSvgNectar extends MarkerBoard {

    private final MarkerList markersFromSVG;

    public MarkerBoardSvgNectar(String name, CameraNectar cam) {
        super(200, 200);
        this.fileName = name;
        this.type = MarkerType.SVG;

        JSONArray markersJson = JSONArray.parse(cam.get("markerboards:" + name));
        markersFromSVG = MarkerList.createFromJSON(markersJson);
    }


    @Override
    protected void addTrackerImpl(Camera camera) {
        // No specific tracker for Markers, as it is global. 
        // for now... 
        PMatrix3D tr = new PMatrix3D();
        this.transfos.add(tr);
    }

    @Override
    protected void updatePositionImpl(int id,
            int currentTime,
            int endTime,
            int mode,
            Camera camera,
            opencv_core.IplImage img,
            Object globalTracking) {

        // Get markers here
        DetectedMarker[] markers;

        markers = (DetectedMarker[]) globalTracking;

        if (camera instanceof SubCamera) {
            SubCamera sub = (SubCamera) camera;
            Camera main = sub.getMainCamera();
            if (main instanceof CameraNectar) {
                markers = ((CameraNectar) main).getMarkers();
            }
        } else {
            if (camera instanceof CameraNectar) {
                markers = ((CameraNectar) camera).getMarkers();
            }
        }

        PMatrix3D newPos = DetectedMarker.compute3DPos(markers, markersFromSVG, camera);

        if (newPos == INVALID_LOCATION) {
            return;
        }
        PVector currentPos = new PVector(newPos.m03, newPos.m13, newPos.m23);

        // Cannot detect elements as close as closer than 10cm
        if (currentPos.z < 10) {
            return;
        }

        update(newPos, id);

    }

    private void update(PMatrix3D newPos, int id) {
        PMatrix3D transfo = (PMatrix3D) transfos.get(id);

        transfo.set(newPos);

//        float pageHeight = this.height;
        float pageHeight = markersFromSVG.getSheetHeight();

        // Invert the scales so that it fits Inkscape's view. 
        transfo.scale(1, -1, 1);
        transfo.translate(0, -pageHeight, 0);
    }

}
