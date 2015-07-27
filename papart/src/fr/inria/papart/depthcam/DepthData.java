/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam;

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.PlaneCalibration;
import static fr.inria.papart.depthcam.DepthAnalysis.INVALID_COLOR;
import static fr.inria.papart.depthcam.DepthAnalysis.INVALID_POINT;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.util.ArrayList;
import java.util.Arrays;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class DepthData {
    
        /**
     * 3D points viewed by the depth camera.
     */
    public Vec3D[] depthPoints;
    public Vec3D[] normals;

    /**
     * Mask of valid Points
     */
    public boolean[] validPointsMask;

    /**
     * Color...
     */
    public int[] pointColors;

    public Connexity connexity;
    
    /**
     * List of valid points
     */
    public ArrayList<Integer> validPointsList;

    public ProjectiveDeviceP projectiveDevice;

    public int timeStamp;
    public DepthAnalysis source;
    
    public DepthData(DepthAnalysis source) {
        int width = source.getWidth();
        int height = source.getHeight();
        this.source = source;
        int size = width * height;
        depthPoints = new Vec3D[size];
        validPointsMask = new boolean[size];
        pointColors = new int[size];
        validPointsList = new ArrayList();
        connexity = new Connexity(depthPoints, width, height);
//        connexity = new Connexity(projectedPoints, width, height);
    }
    
    public DepthDataElement getElement(int i){
        DepthDataElement dde = new DepthDataElement();
        fillDepthDataElement(dde, i);
        return dde;
    }
    
    protected void fillDepthDataElement(DepthDataElement dde, int i){
        dde.pointColor = pointColors[i];
        dde.depthPoint = depthPoints[i];
        dde.validPoint = validPointsMask[i];
        dde.neighbourSum = connexity.connexitySum[i];
        dde.neighbours = connexity.connexity[i];
        dde.offset = i;
    }

    public void clear() {
        clearDepth();
        clear2D();
        clearColor();
        connexity.reset();
    }
    
    void clearColor(){
        Arrays.fill(this.pointColors, INVALID_COLOR);
    }

    void clearDepth() {
        Arrays.fill(this.depthPoints, INVALID_POINT);
    }

    void clear2D() {
        Arrays.fill(this.validPointsMask, false);
        this.validPointsList.clear();
    }

}
