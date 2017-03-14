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

import static fr.inria.papart.procam.Papart.camCalibARtoolkit;
import static fr.inria.papart.tracking.MarkerBoard.BLOCK_UPDATE;
import static fr.inria.papart.tracking.MarkerBoard.FORCE_UPDATE;
import static fr.inria.papart.tracking.MarkerBoard.NORMAL;
import fr.inria.papart.procam.camera.Camera;
import static fr.inria.papart.tracking.MarkerSvg.pixelToMm;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.ARToolKitPlus;
import org.bytedeco.javacpp.opencv_core;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class MarkerBoardARToolKitPlus extends MarkerBoard {

    private PVector markerBoardSize = new PVector();

    public MarkerBoardARToolKitPlus(String fileName, float width, float height) {
        super(fileName, width, height);
        trackers = new ArrayList<ARToolKitPlus.TrackerMultiMarker>();
        this.type = MarkerType.ARTOOLKITPLUS;
    }

//        /**
//     * These parameters control the way the toolkit warps a found
//     * marker to a perfect square. The square has size
//     * pattWidth * pattHeight, the projected
//     * square in the image is subsampled at a min of
//     * pattWidth/pattHeight and a max of pattSamples
//     * steps in both x and y direction
//     *  @param imWidth width of the source image in px
//     *  @param imHeight height of the source image in px
//     *  @param maxImagePatterns describes the maximum number of patterns that can be analyzed in a camera image.
//     *  @param pattWidth describes the pattern image width (must be 6 for binary markers)
//     *  @param pattHeight describes the pattern image height (must be 6 for binary markers)
//     *  @param pattSamples describes the maximum resolution at which a pattern is sampled from the camera image
//     *  (6 by default, must a a multiple of pattWidth and pattHeight).
//     *  @param maxLoadPatterns describes the maximum number of pattern files that can be loaded.
//     *  Reduce maxLoadPatterns and maxImagePatterns to reduce memory footprint.
//     */
//    public TrackerMultiMarker(int imWidth, int imHeight, int maxImagePatterns/*=8*/, int pattWidth/*=6*/, int pattHeight/*=6*/, int pattSamples/*=6*/,
//                int maxLoadPatterns/*=0*/) { allocate(imWidth, imHeight, maxImagePatterns, pattWidth, pattHeight, pattSamples, maxLoadPatterns); }
//  
    @Override
    protected void addTrackerImpl(Camera camera) {
        
        // create a tracker that does:
        //  - 6x6 sized marker images (required for binary markers)
        //  - samples at a maximum of 6x6 
        //  - works with luminance (gray) images
        //  - can load a maximum of 0 non-binary pattern
        //  - can detect a maximum of 8 patterns in one image
        ARToolKitPlus.TrackerMultiMarker tracker = new ARToolKitPlus.TrackerMultiMarker(camera.width(), camera.height(), 20, 6, 6, 6, 5);

        // Working in gray images. 
        int pixfmt = ARToolKitPlus.PIXEL_FORMAT_LUM;

//        switch (camera.getPixelFormat()) {
//            case BGR:
//                pixfmt = ARToolKitPlus.PIXEL_FORMAT_BGR;
//                break;
//            case RGB:
//                pixfmt = ARToolKitPlus.PIXEL_FORMAT_RGB;
//                break;
//            case ARGB: // closest, not the same.
//                pixfmt = ARToolKitPlus.PIXEL_FORMAT_ABGR;
//                break;
//            case RGBA:
//                pixfmt = ARToolKitPlus.PIXEL_FORMAT_RGBA;
//            default:
//                throw new RuntimeException("ARtoolkit : Camera pixel format unknown");
//        }
        tracker.setPixelFormat(pixfmt);
        tracker.setBorderWidth(0.125f);
        tracker.activateAutoThreshold(true);
//        tracker.activateAutoThreshold(false);
        tracker.setUndistortionMode(ARToolKitPlus.UNDIST_NONE);
        tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);

//            tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_ORIGINAL_CONT);
        tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);

        // TODO: find why  FULL RES is not working with a FULL HD image. 
        tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
//        tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_HALF_RES);

        tracker.setUseDetectLite(false);
//        tracker.setUseDetectLite(true);

        // Deal with the calibration files here...
        Camera.convertARParams(this.applet, camera.getProjectiveDevice(), camCalibARtoolkit);
        camera.setCalibrationARToolkit(camCalibARtoolkit);
        
        // Initialize the tracker, with camera parameters and marker config. 
        if (!tracker.init(camera.getCalibrationARToolkit(), this.getFileName(), 1.0f, 10000.f)) {
            System.err.println("Init ARTOOLKIT Error " + camera.getCalibrationARToolkit() + " " + this.getFileName());
        }

	//        loadSizeConfig();
//        float[] transfo = new float[16];
//        for (int i = 0; i < 3; i++) {
//            transfo[12 + i] = 0;
//        }
//        transfo[15] = 0;
        PMatrix3D tr = new PMatrix3D();
        this.trackers.add(tracker);
        this.transfos.add(tr);
    }

    private void loadSizeConfig() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(this.getFileName()));
            String[] split = lines.get(2).split("x");

            float w = Float.parseFloat(split[0].substring(7)) * pixelToMm();
            float h = Float.parseFloat(split[1]) * pixelToMm();
//            System.out.println("Width: " + w + " Heigth " + h);
            markerBoardSize.set(w, h);
        } catch (IOException ex) {
            Logger.getLogger(MarkerBoardARToolKitPlus.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int MIN_ARTOOLKIT_MARKER_DETECTED = 1;

    @Override
    protected void updatePositionImpl(int id, int currentTime, int endTime, int mode,
            Camera camera, opencv_core.IplImage img, Object globalTracking) {

        ARToolKitPlus.TrackerMultiMarker tracker = (ARToolKitPlus.TrackerMultiMarker) trackers.get(id);

//        tracker.getCamera().changeFrameSize(camera.width(), camera.height());
        // Find the markers
        tracker.calc(img.imageData());

//        System.out.println("Calc... " + tracker.getNumDetectedMarkers());
        if (tracker.getNumDetectedMarkers() < MIN_ARTOOLKIT_MARKER_DETECTED) {
            return;
        }

        ARToolKitPlus.ARMultiMarkerInfoT multiMarkerConfig = tracker.getMultiMarkerConfig();

        PVector currentPos = new PVector((float) multiMarkerConfig.trans().get(3),
                (float) multiMarkerConfig.trans().get(7),
                (float) multiMarkerConfig.trans().get(11));

        // Cannot detect elements as close as closer than 10cm
        if (currentPos.z < 10) {
            return;
        }

        // if the update is forced 
        if (mode == FORCE_UPDATE && currentTime < endTime) {
            update(multiMarkerConfig, id);
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
                update(multiMarkerConfig, id);
                lastPos.set(id, currentPos);
                updateStatus.set(id, FORCE_UPDATE);
                nextTimeEvent.set(id, applet.millis() + MarkerBoard.updateTime);
//            } else {
//                System.out.println("Not updating, because of drawing mode...");
            }

        } else {
            update(multiMarkerConfig, id);

        }

    }

    private void update(ARToolKitPlus.ARMultiMarkerInfoT multiMarkerConfig, int id) {
        fr.inria.papart.multitouch.OneEuroFilter filter[] = filters.get(id);

        PMatrix3D inputMatrix = new PMatrix3D();

        if (filter == null) {
            inputMatrix.m00 = multiMarkerConfig.trans().get(0);
            inputMatrix.m01 = multiMarkerConfig.trans().get(1);
            inputMatrix.m02 = multiMarkerConfig.trans().get(2);
            inputMatrix.m03 = multiMarkerConfig.trans().get(3);

            inputMatrix.m10 = multiMarkerConfig.trans().get(4);
            inputMatrix.m11 = multiMarkerConfig.trans().get(5);
            inputMatrix.m12 = multiMarkerConfig.trans().get(6);
            inputMatrix.m13 = multiMarkerConfig.trans().get(7);

            inputMatrix.m20 = multiMarkerConfig.trans().get(8);
            inputMatrix.m21 = multiMarkerConfig.trans().get(9);
            inputMatrix.m22 = multiMarkerConfig.trans().get(10);
            inputMatrix.m23 = multiMarkerConfig.trans().get(11);
        } else {
            try {
                inputMatrix.m00 = (float) filter[0].filter(multiMarkerConfig.trans().get(0));
                inputMatrix.m01 = (float) filter[1].filter(multiMarkerConfig.trans().get(1));
                inputMatrix.m02 = (float) filter[2].filter(multiMarkerConfig.trans().get(2));
                inputMatrix.m03 = (float) filter[3].filter(multiMarkerConfig.trans().get(3));

                inputMatrix.m10 = (float) filter[4].filter(multiMarkerConfig.trans().get(4));
                inputMatrix.m11 = (float) filter[5].filter(multiMarkerConfig.trans().get(5));
                inputMatrix.m12 = (float) filter[6].filter(multiMarkerConfig.trans().get(6));
                inputMatrix.m13 = (float) filter[7].filter(multiMarkerConfig.trans().get(7));

                inputMatrix.m20 = (float) filter[8].filter(multiMarkerConfig.trans().get(8));
                inputMatrix.m21 = (float) filter[9].filter(multiMarkerConfig.trans().get(9));
                inputMatrix.m22 = (float) filter[10].filter(multiMarkerConfig.trans().get(10));
                inputMatrix.m23 = (float) filter[11].filter(multiMarkerConfig.trans().get(11));
            } catch (Exception e) {
                System.out.println("Filtering error " + e);
            }
        }

//        inputMatrix.translate(0, height / 2, 0);
//        inputMatrix.scale(1, -1, 1);
//        inputMatrix.translate(0, -height / 2, 0);
        // Invert the scales so that it fits Inkscape's view. 
        
        inputMatrix.scale(1, -1, 1);
        inputMatrix.translate(0, -markerBoardSize.y, 0);

        PMatrix3D transfo = transfos.get(id);
        transfo.set(inputMatrix);
//    Z negation ?
//        tmp.scale(1, 1, -1);
//        transfo[11] = -transfo[11];
    }

}
