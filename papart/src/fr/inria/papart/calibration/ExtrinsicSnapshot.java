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

import processing.core.PMatrix3D;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class ExtrinsicSnapshot {

    PMatrix3D mainCameraPaper = null;
    PMatrix3D projectorPaper = null;
    PMatrix3D kinectPaper = null;

    public ExtrinsicSnapshot(PMatrix3D cameraPaperCalibration,
            PMatrix3D projectorPaperCalibration,
            PMatrix3D kinectPaperCalibration) {

        if (cameraPaperCalibration != null) {
            mainCameraPaper = cameraPaperCalibration.get();
        }
        if (projectorPaperCalibration != null) {
            projectorPaper = projectorPaperCalibration.get();
        }
        if (kinectPaperCalibration != null) {
            kinectPaper = kinectPaperCalibration.get();
        }
    }

}
