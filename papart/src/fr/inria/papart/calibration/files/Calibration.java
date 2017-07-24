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
package fr.inria.papart.calibration.files;

import java.io.FileNotFoundException;
import processing.core.PApplet;
import processing.data.XML;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public abstract class Calibration {

    public static final String CALIBRATION_XML_NAME = "Calibration";

    public abstract boolean isValid();

    public abstract void addTo(XML xml);
    public abstract void addTo(StringBuilder yaml);

    public abstract void replaceIn(XML xml);

    public void saveToXML(PApplet parent, String fileName) {
        assert (isValid());
        XML root = new XML(Calibration.CALIBRATION_XML_NAME);
        this.addTo(root);
        parent.saveXML(root, fileName);
    }
    public void saveToYAML(PApplet parent, String fileName) {
        assert (isValid());
        StringBuilder builder = new StringBuilder("%YAML:1.0\n");
        this.addTo(builder);
        
        parent.saveStrings(fileName, new String[] {builder.toString()});
    }

    public void saveTo(PApplet parent, String fileName) {
        if (fileName.endsWith(".xml")) {
            saveToXML(parent, fileName);
        }
        if (fileName.endsWith(".yaml")) {
            saveToYAML(parent, fileName);
        }
    }

    public void replaceIn(PApplet parent, String fileName) {
        assert (isValid());
        XML root = parent.loadXML(fileName);
        this.replaceIn(root);
        parent.saveXML(root, fileName);
    }

    public abstract void loadFrom(PApplet parent, String fileName);
}
