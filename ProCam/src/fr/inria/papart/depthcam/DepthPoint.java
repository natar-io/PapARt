/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.papart.depthcam;

import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class DepthPoint{
    // name is colorPt -> because of processing color function to avoid problems.
    protected int colorPt;
    protected PVector position = new PVector();

    
    public int getColor() {
        return colorPt;
    }

    public void setColor(int color) {
        this.colorPt = color;
    }
    
    public PVector getPosition(){
        return this.position;
    }

    public Vec3D getPositionVec3D(){
        return new Vec3D(position.x, position.y, position.z);
    }
    
    public void setPosition(Vec3D pos){
        this.position.set(pos.x, pos.y, pos.z);
    }
    
    public void setPosition(PVector pos){
        this.position.set(pos);
    }
    
}
