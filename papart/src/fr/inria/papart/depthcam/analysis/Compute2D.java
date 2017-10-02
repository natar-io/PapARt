/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam.analysis;

import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.DepthData.DepthSelection;
import fr.inria.papart.depthcam.PixelOffset;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.isInside;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class Compute2D extends DepthRecognition {

    private DepthSelection selection;

    public Compute2D(DepthAnalysisImpl depthAnalysis) {
        super(depthAnalysis);
    }

    /**
     *
     * Fills the validPointsMask array and validPointsList list. Called by
     * Simple2D.java
     *
     * @param calib incoming plane
     * @param skip2D precision
     */
    public void find2DTouch(PlaneAndProjectionCalibration calib, int skip2D) {
        // TODO: ensure that this has been computed.
//         depthData.clearValidPoints();
        selection = depthData.createSelection();
        depthData.planeAndProjectionCalibration = calib;
        depthAnalysis.doForEachPoint(skip2D, new Select2DPlaneProjection());

        depthAnalysis.doForEachPoint(skip2D, new SetNormalRelative());
//        doForEachPoint(skip2D, new Select2DPointPlaneProjection());
    }

    class SetNormalRelative implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
//            depthData.connexity.compute(px.x, px.y);
            if (depthData.normals[px.offset] != null) {
                depthData.normals[px.offset].sub(depthData.planeAndProjectionCalibration.getPlane().normal);
            }
        }
    }

    class Select2DPointPlaneProjection implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p)) {

//                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//                depthData.projectedPoints[px.offset] = projected;
                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

                if (isInside(depthData.projectedPoints[px.offset], 0.f, 1.f, 0.0f)) {
                    selection.validPointsMask[px.offset] = true;
                    selection.validPointsList.add(px.offset);
                }
            }
        }
    }

    class Select2DPlaneProjection implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p)
                    && depthData.projectedPoints[px.offset].x() != 0
                    && depthData.projectedPoints[px.offset].y() != 0
                    && depthData.projectedPoints[px.offset].z() != 0) {

//                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//                depthData.projectedPoints[px.offset] = projected;
//                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);
//                if (isInside(depthData.projectedPoints[px.offset], 0.f, 1.f, 0.0f)) {
                selection.validPointsMask[px.offset] = true;
                selection.validPointsList.add(px.offset);
//                }
            }

        }
    }

    class Select2DPointPlaneProjectionNormal implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p)) {

//                System.out.println("Distance " + (depthData.planeAndProjectionCalibration.getPlane().normal).distanceTo(depthData.normals[px.offset]));
                float normalDistance = (depthData.planeAndProjectionCalibration.getPlane().normal).distanceTo(depthData.normals[px.offset]);
//                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//                depthData.projectedPoints[px.offset] = projected;
                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

                // TODO: tweak the 0.3f
                if (isInside(depthData.projectedPoints[px.offset], 0.f, 1.f, 0.0f)
                        && normalDistance > 0.3f) {
                    selection.validPointsMask[px.offset] = true;
                    selection.validPointsList.add(px.offset);
                }
            }
        }
    }

    class Select2DPointPlaneProjectionSR300Error implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            float error = Math.abs(p.x / 50f) + p.z / 400f;

            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p, error)) {
//                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
//                depthData.projectedPoints[px.offset] = projected;
                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

                if (isInside(depthData.projectedPoints[px.offset], 0.f, 1.f, 0.0f)) {
                    selection.validPointsMask[px.offset] = true;
                    selection.validPointsList.add(px.offset);
                }
            }
        }
    }

    class Select2DPointOverPlane implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeCalibration.hasGoodOrientation(p)) {
                selection.validPointsMask[px.offset] = true;
                selection.validPointsList.add(px.offset);
            }
        }
    }

    class Select2DPointOverPlaneDist implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            if (depthData.planeCalibration.hasGoodOrientationAndDistance(p)) {
                selection.validPointsMask[px.offset] = true;
                selection.validPointsList.add(px.offset);
            }
        }
    }

    class Select2DPointCalibratedHomography implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {

            PVector projected = new PVector();
            PVector init = new PVector(p.x, p.y, p.z);

            depthData.homographyCalibration.getHomographyInv().mult(init, projected);

            // TODO: Find how to select the points... 
            if (projected.z > 10 && projected.x > 0 && projected.y > 0) {
                selection.validPointsMask[px.offset] = true;
                selection.validPointsList.add(px.offset);
            }
        }
    }

    public DepthData.DepthSelection getSelection() {
        return selection;
    }

}
