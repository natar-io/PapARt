/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.depthcam;

import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class DepthPoint{
    // name is colorPt -- because of processing color function to avoid problems.
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
