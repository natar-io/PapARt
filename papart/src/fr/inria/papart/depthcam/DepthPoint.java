/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
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
package fr.inria.papart.depthcam;

import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class DepthPoint{
    // name is colorPt -- because of processing color function to avoid problems.
    protected int colorPt;
    protected PVector position = new PVector();
    protected PVector normal = new PVector();

    public PVector getNormal() {
        return normal;
    }

    public void setNormal(Vec3D normal) {
        if(normal == null)
            return;
        this.normal.x = normal.x;
        this.normal.y = normal.y;
        this.normal.z = normal.z;
    }
    
    public DepthPoint(){}
    
    public DepthPoint(float x, float y, float z, int col){
        this.colorPt = col;
        position.x = x;
        position.y = y;
        position.z = z;
    }
    
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
