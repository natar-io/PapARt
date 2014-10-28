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
package fr.inria.papart.depthcam.calibration;

import java.io.FileNotFoundException;
import processing.core.PApplet;
import processing.data.XML;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public abstract class Calibration {

    static final String CALIBRATION_XML_NAME = "Calibration";

    public abstract boolean isValid();

    public abstract void addTo(XML xml);

    public abstract void replaceIn(XML xml);

    public void saveTo(PApplet parent, String fileName) {
        assert(isValid());
        XML root = new XML(Calibration.CALIBRATION_XML_NAME);
        this.addTo(root);
        parent.saveXML(root, fileName);
    }

    
    public void replaceIn(PApplet parent, String fileName) {
        assert(isValid());
        XML root = parent.loadXML(fileName);
        this.replaceIn(root);
        parent.saveXML(root, fileName);
    }

    public abstract void loadFrom(PApplet parent, String fileName);
}
