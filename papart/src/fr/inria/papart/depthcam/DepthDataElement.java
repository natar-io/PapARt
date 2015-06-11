/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
