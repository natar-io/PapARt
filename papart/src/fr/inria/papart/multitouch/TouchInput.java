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
package fr.inria.papart.multitouch;

import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.display.ARDisplay;

import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import java.util.ArrayList;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public abstract class TouchInput {

    public static PVector NO_INTERSECTION = new PVector();

    public boolean isReady() {
        return true;
    }

    abstract public void update();

    abstract public TouchList projectTouchToScreen(PaperScreen paperScreen, BaseDisplay display);

    protected boolean computeOutsiders = false;

    public void computeOutsiders(boolean outsiders) {
        this.computeOutsiders = outsiders;
    }

}
