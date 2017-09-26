/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech 
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
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.depthcam.analysis.DepthComputation;

/**
 *
 * @author Jeremy Laviole
 */
public class Kinect360Depth implements DepthComputation {

    @Override
    public float findDepth(int offset, Object buffer) {
        float d = (((byte[]) buffer)[offset * 2] & 0xFF) << 8
                | (((byte[]) buffer)[offset * 2 + 1] & 0xFF);

        return d;
    }
}
