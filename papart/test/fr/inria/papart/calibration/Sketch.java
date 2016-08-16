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
package fr.inria.papart.calibration;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */

import processing.core.PApplet;

public class Sketch extends PApplet {

    @Override
    public void setup() {
        size(200, 200);
        stroke(155, 0, 0);
    }

    @Override
    public void draw() {
        line(mouseX, mouseY, width / 2, height / 2);
    }
}