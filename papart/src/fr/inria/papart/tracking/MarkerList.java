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
package fr.inria.papart.tracking;

import java.util.HashMap;
import processing.core.PVector;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class MarkerList extends HashMap<Integer, MarkerSvg> {

    private final PVector sheetSize = new PVector();

    public float getSheetHeight() {
        return sheetSize.y;
    }

    public void setSheetSize(PVector size) {
        setSheetSize(size.x, size.y);
    }

    public void setSheetSize(float x, float y) {
        this.sheetSize.x = x;
        this.sheetSize.y = y;
    }

}
