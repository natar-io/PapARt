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
 * @author Jeremy Laviole
 */
public class PixelOffset {

    public final int x, y, offset;
    public static int width;

    public PixelOffset(int x, int y, int offset) {
        this.x = x;
        this.y = y;
        this.offset = offset;
    }

    private static PixelOffset[] offsets;

    public static void initStaticMode(int width, int height) {
        offsets = new PixelOffset[width * height];
        int off = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                offsets[off] = new PixelOffset(x, y, off);
                off++;
            }
        }
    }

    public static PixelOffset get(int offset) {
        return offsets[offset];
    }

}
