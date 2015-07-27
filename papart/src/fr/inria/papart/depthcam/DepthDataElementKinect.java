/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class DepthDataElementKinect extends DepthDataElement{
    
    public Vec3D projectedPoint;
    public TouchAttributes touchAttribute;
    public boolean validPoint3D;

}
