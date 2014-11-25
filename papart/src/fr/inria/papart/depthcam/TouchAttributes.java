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

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class TouchAttributes {

    public static TouchAttributes NO_ATTRIBUTES = new TouchAttributes(false, false, false);
    
    protected boolean isInTouch;
    protected boolean isUnderTouch;
    protected boolean isOverTouch;

    public TouchAttributes(boolean inTouch, boolean underTouch, boolean overTouch){
        this.isInTouch = inTouch;
        this.isUnderTouch = underTouch;
        this.isOverTouch = overTouch;
    }
    
    public boolean isInTouch() {
        return isInTouch;
    }

    public void setIsInTouch(boolean isInTouch) {
        this.isInTouch = isInTouch;
    }

    public boolean isUnderTouch() {
        return isUnderTouch;
    }

    public void setIsUnderTouch(boolean isUnderTouch) {
        this.isUnderTouch = isUnderTouch;
    }

    public boolean isOverTouch() {
        return isOverTouch;
    }

    public void setIsOverTouch(boolean isOverTouch) {
        this.isOverTouch = isOverTouch;
    }

}
