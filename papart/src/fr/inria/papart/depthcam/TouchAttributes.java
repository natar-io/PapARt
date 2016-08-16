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

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
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
