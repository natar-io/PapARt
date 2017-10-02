/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam.analysis;

import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.DepthData.DepthSelection;
import fr.inria.papart.depthcam.PixelOffset;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.isInside;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class Compute3D extends DepthRecognition {

    private DepthSelection selection;


    public Compute3D(DepthAnalysisImpl depthAnalysis) {
        super(depthAnalysis);
    }

    public void recognize(Object filter, int quality) {
        find3DTouch((PlaneAndProjectionCalibration) filter, quality);
    }

    /**
     *
     * Fills the validPointsMask3D array and validPointsList3D list. Called by
     * Simple3D.java
     *
     * @param calib incoming plane
     * @param skip3D precision
     */
    public void find3DTouch(PlaneAndProjectionCalibration calib, int skip3D) {
        // TODO: ensure that this has been computed.
//        depthData.clearValidPoints();
        selection = depthData.createSelection();
        depthData.planeAndProjectionCalibration = calib;
        depthAnalysis.doForEachPoint(skip3D, new Select3DPlaneProjection());
//        doForEachPoint(skip3D, new Select3DPointPlaneProjection()); 
    }

    class Select3DPointPlaneProjection implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeAndProjectionCalibration.hasGoodOrientation(p)) {
//                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//                depthData.projectedPoints[px.offset] = projected;

                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

                if (isInside(depthData.projectedPoints[px.offset], 0.f, 1.f, 0.1f)) {
                    selection.validPointsMask[px.offset] = true;
                    selection.validPointsList.add(px.offset);
                }
            }
        }
    }

    class Select3DPlaneProjection implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeAndProjectionCalibration.hasGoodOrientation(p)) {
//                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//                depthData.projectedPoints[px.offset] = projected;

                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

                selection.validPointsMask[px.offset] = true;
                selection.validPointsList.add(px.offset);
            }
        }
    }
    
    
    public DepthSelection getSelection() {
        return selection;
    }

}
