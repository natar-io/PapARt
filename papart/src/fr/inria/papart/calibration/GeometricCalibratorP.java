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
package fr.inria.papart.calibration;

import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.tracking.MarkerList;
import fr.inria.papart.tracking.MarkerSvg;
import java.util.Arrays;
import org.bytedeco.javacv.GeometricCalibrator;
import org.bytedeco.javacv.Marker;
import org.bytedeco.javacv.MarkerDetector;
import org.bytedeco.javacv.ProjectiveDevice;

/**
 *
 * @author Jeremy Laviole - laviole@rea.lity.tech
 */
public class GeometricCalibratorP extends GeometricCalibrator {

    /**
     * Create a Geometric Calibrator for a given resolution.
     *
     * @param width
     * @param height
     * @param name
     * @return
     */
    public static GeometricCalibratorP createGeometricCalibrator(int width, int height, String name) {
        ProjectiveDevice d = new ProjectiveDevice(name);
        d.imageWidth = width;
        d.imageHeight = height;
        d.setSettings(new ProjectiveDevice.CalibrationSettings());

        return new GeometricCalibratorP(d);
    }

    /**
     * Create a calibrator with default settings.
     *
     * @param projectiveDevice
     */
    public GeometricCalibratorP(ProjectiveDevice projectiveDevice) {

        // MarkerDetector.settings is not used
        super(new GeometricCalibrator.Settings(), new MarkerDetector.Settings(), null, projectiveDevice);
    }

    /**
     * Requires a ProjectiveDevice with image size set in the ProjectiveDevice.
     *
     * @param settings
     * @param projectiveDevice
     */
    public GeometricCalibratorP(Settings settings, ProjectiveDevice projectiveDevice) {
        super(settings, null, null, projectiveDevice);
    }

    /**
     *  Add the markers, both the model and detected markers.
     *
     * @param model stored in a MarkerboardSvg object.
     * @param detected Found by a camera.
     */
    public void addMarkers(MarkerList model, DetectedMarker[] detected) {

        int maxLength = Math.min(model.size(), detected.length);

//        MarkerSvg[] om = new MarkerSvg[maxLength];
//        DetectedMarker[] im = new DetectedMarker[maxLength];
        Marker[] om = new Marker[maxLength];
        Marker[] im = new Marker[maxLength];

        int k = 0;

        for (MarkerSvg m1 : model.values()) {
            for (int i = 0; i < detected.length; i++) {
                DetectedMarker m2 = detected[i];
                if (m1.getId() == m2.getId() && m2.confidence == 1) {
                    om[k] = m1.copyAsMarker();
                    im[k] = m2.copyAsMarker();
                    k++;
                    break;
                }
            }
        }

        // matching markers
        if (k > 0) {
            
            // resize the array
            if (k < maxLength) {
                om = Arrays.copyOf(om, k);
                im = Arrays.copyOf(im, k);
            }
            this.getAllObjectMarkers().add(om);
            this.getAllImageMarkers().add(im);
        }

    }

    public void clearMarkers() {
        this.getAllObjectMarkers().clear();
        this.getAllImageMarkers().clear();
    }

}
