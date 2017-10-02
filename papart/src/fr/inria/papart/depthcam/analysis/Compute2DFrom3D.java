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
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import java.util.ArrayList;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class Compute2DFrom3D extends DepthRecognition {

    private DepthSelection selection;

    public DepthSelection getSelection() {
        return selection;
    }

    public Compute2DFrom3D(DepthAnalysisImpl depthAnalysis) {
        super(depthAnalysis);
    }

    public void find2DTouchFrom3D(PlaneAndProjectionCalibration planeAndProjCalibration,
            int precision2D,
            ArrayList<TrackedDepthPoint> touchPoints,
            int precision3D) {

        selection = depthData.createSelection();
        depthData.planeAndProjectionCalibration = planeAndProjCalibration;

//        depthAnalysis.doForEachPoint(skip2D, new Select2DPlaneProjection());
// TEST only around the first point
        for (TrackedDepthPoint pt : touchPoints) {
            int offset = pt.getDepthDataAsConnectedComponent().get(0);
//            depthAnalysis.computeDepthAndDoAround(precision2D, offset, 80, new SelectAll());
            depthAnalysis.doForEachPointAround(precision2D, offset, 60, new Select2DPlaneProjection());
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

    class SelectAll implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);
            selection.validPointsMask[px.offset] = true;
            selection.validPointsList.add(px.offset);
        }
    }

    class Select2DPlaneProjection implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

//            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p)
            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p)
                    && depthData.projectedPoints[px.offset].x() != 0
                    && depthData.projectedPoints[px.offset].y() != 0
                    && depthData.projectedPoints[px.offset].z() != 0) {

                selection.validPointsMask[px.offset] = true;
                selection.validPointsList.add(px.offset);
            }

        }
    }


}
