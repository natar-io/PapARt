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
import java.nio.ShortBuffer;

/**
 *
 * @author Jeremy Laviole - laviole@rea.lity.tech
 */
public class RealSenseDepth implements DepthComputation {

    private final float depthRatio;

    public RealSenseDepth(float depthRatio) {
        this.depthRatio = depthRatio;
    }

    @Override
    public float findDepth(int offset, Object buffer) {
        float d = ((ShortBuffer) buffer).get(offset) * depthRatio * 1000f;
        return d;
    }
}
