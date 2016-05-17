/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.tracking;

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.ProjectiveDeviceP;
import static fr.inria.papart.tracking.MarkerBoard.BLOCK_UPDATE;
import static fr.inria.papart.tracking.MarkerBoard.FORCE_UPDATE;
import static fr.inria.papart.tracking.MarkerBoard.NORMAL;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.tracking.MarkerList;
import fr.inria.papart.tracking.MarkerSvg;
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
        trackers = new ArrayList<>();
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
        
        PMatrix3D newPos = compute3DPos(markers, camera);

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

        }

    }

    private PMatrix3D compute3DPos(DetectedMarker[] detectedMarkers, Camera camera) {
        // We create a pair model ( markersFromSVG) -> observation (markers) 

//         markersFromSVG
        ArrayList<PVector> objectPoints = new ArrayList<PVector>();
        ArrayList<PVector> imagePoints = new ArrayList<PVector>();
        int k = 0;

        for (DetectedMarker detected : detectedMarkers) {
            if (markersFromSVG.containsKey(detected.id)) {

                PVector[] object = markersFromSVG.get(detected.id).getCorners();
                PVector[] image = detected.getCorners();
                for (int i = 0; i < 4; i++) {
//                    System.out.println("Model " + object[i] + " image " + image[i]);
                    objectPoints.add(object[i]);
                    imagePoints.add(image[i]);
                }
                k++;
            }
        }
        if (k < 1) {
            return MarkerBoard.INVALID_LOCATION;
        }

        PVector[] objectArray = new PVector[k];
        PVector[] imageArray = new PVector[k];
        objectArray = objectPoints.toArray(objectArray);
        imageArray = imagePoints.toArray(imageArray);

        ProjectiveDeviceP pdp = camera.getProjectiveDevice();
        return pdp.estimateOrientation(objectArray, imageArray);
//        return pdp.estimateOrientationRansac(objectArray, imageArray);

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
