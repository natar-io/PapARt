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
import org.bytedeco.javacpp.opencv_core.IplImage;
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
            IplImage colorImage,
            int offset,
            int area) {

        // Warning RESET ROI ?!
//        depthAnalysis.updateRawColor(colorImage);
        selection = depthData.createSelection();
        depthData.planeAndProjectionCalibration = planeAndProjCalibration;

        depthData.connexity.setPrecision(precision2D);

//        depthAnalysis.doForEachPoint(skip2D, new Select2DPlaneProjection());
        // Around the middle point
//        depthAnalysis.computeDepthAndDoAround(precision2D, offset, area, new SelectAll());
        // Computation of elements in a zone
        depthAnalysis.computeDepthAndDoAround(precision2D, offset, area, new Select2DPlaneProjection());
        depthAnalysis.doForEachPointAround(precision2D, offset, area, new ComputeNormal());

//        depthAnalysis.computeDepthAndDo(precision2D, new Select2DPlaneProjection());
//        depthAnalysis.doForEachPoint(precision2D, new ComputeNormal());
//        depthAnalysis.doForEachPointAround(precision2D, offset, area, new SetImageDataGRAY());
        // Add the Color Image after Contour detection
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

    protected class SetImageDataGRAY implements DepthAnalysis.DepthPointManiplation {

        public SetImageDataGRAY() {
            super();
        }

        @Override
        public void execute(Vec3D p, PixelOffset px) {
//            depthData.validPointsMask[px.offset] = true;
            depthAnalysis.setPixelColorGRAY(px.offset);
        }
    }

    class ComputeNormal implements DepthAnalysis.DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            depthData.connexity.compute(px.x, px.y);
            Vec3D normal = depthAnalysis.computeNormalImpl(depthData.depthPoints[px.offset], px);
            depthData.normals[px.offset] = normal;
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
            
            // warning ? Project again if precision is higher
            depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);

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
