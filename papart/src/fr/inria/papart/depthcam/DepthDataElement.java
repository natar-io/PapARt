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
 * @author jiii
 */
public class DepthDataElement {
    
    public int offset;
    public Vec3D depthPoint;
    public boolean validPoint;
    public int pointColor;

    public Vec3D normal;

    public byte neighbourSum;
    public byte neighbours;
    
}
