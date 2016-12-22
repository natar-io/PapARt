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
package fr.inria.papart.apps;

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.PaperTouchScreen;

public class MyApp2DTouch extends PaperTouchScreen {

    @Override
    public void settings() {
        setDrawingSize(297, 210);
        loadMarkerBoard(Papart.markerFolder + "A4-default.svg", 297, 210);
    }

    @Override
    public void setup() {

    }

    @Override
    public void drawOnPaper() {
        background(100, 0, 0);
        fill(200, 100, 20);
        rect(10, 10, 100, 30);
        drawTouch();
    }
}
