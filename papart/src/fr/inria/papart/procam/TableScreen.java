/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2018 RealityTech
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
package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.TrackedView;
import fr.inria.papart.procam.display.BaseDisplay;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole
 */
public class TableScreen extends PaperTouchScreen {

    protected PVector translation;
    protected float rotation = 0f;
    protected PMatrix3D location;

    public TableScreen(float width, float height) {
        this(new PVector(), width, height);
    }

    public TableScreen(float x, float y, float width, float height) {
        this(new PVector(x, y), new PVector(width, height));
    }

    public TableScreen(PVector loc, float width, float height) {
        this(loc, new PVector(width, height));
    }

    public TableScreen(PVector loc, PVector size) {
        super();
        setDrawOnPaper();
        setDrawingSize(size.x, size.y);
        setLocation(loc);
    }

    /**
     * Change the rotation of the screen.
     *
     * @param r in radians
     */
    public void setRotation(float r) {
        this.rotation = r;
        updateLocation();
    }

    /**
     * Change the location relative to the table.
     *
     * @param v in millimeters
     */
    @Override
    public void setLocation(PVector v) {
        setLocation(v.x, v.y, v.z);
    }

    /**
     * Change the location relative to the table.
     *
     * @param x in millimeters
     * @param y in millimeters
     * @param z in millimeters
     */
    @Override
    public void setLocation(float x, float y, float z) {
        this.translation = new PVector(x, y, z);
        updateLocation();
    }

    private void updateLocation() {
        this.location = table.get();
        this.location.translate(translation.x, translation.y, translation.z);
        this.location.rotate(rotation);
        this.useManualLocation(this.location);
        this.computeWorldToScreenMat(cameraTracking);
    }
    
    @Override
    public PMatrix3D getLocation(PMatrix3D trackedLocation) {
        if(getDisplay() instanceof BaseDisplay){
            return getLocationOnTable();
        }
        return super.getLocation(trackedLocation);
    }
        
    public PMatrix3D getLocationOnTable() {
        PMatrix3D m = new PMatrix3D(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        m.translate(translation.x, translation.y, translation.z);
        m.rotate(rotation);
        return m;
    }

}
