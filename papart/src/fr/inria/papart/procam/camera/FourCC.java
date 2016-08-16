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
package fr.inria.papart.procam.camera;

/**
 * A "four character code" (4CC), as used in AVI files.
 *
 * This class wraps a 32-bit value to be used as a 4CC inside an AVI file, so
 * that it is guaranteed to be valid, and it incurs no overhead if used
 * repeatedly.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FourCC {
    /**
     * Integer FourCC value.
     */
    private final int value;

    /**
     * Convert FourCC String value to int.
     *
     * @param fourcc
     *            FourCC String.
     */
    public FourCC(final String fourcc) {
        if (fourcc == null) {
            throw new NullPointerException("FourCC cannot be null");
        }
        // CHECKSTYLE:OFF MagicNumber - Magic numbers here for illustration
        if (fourcc.length() != 4) {
            throw new IllegalArgumentException(
                    "FourCC must be four characters long");
        }
        for (char c : fourcc.toCharArray()) {
            if (c < 32 || c > 126) {
                throw new IllegalArgumentException(
                        "FourCC must be ASCII printable");
            }
        }
        int val = 0;
        for (int i = 0; i < 4; i++) {
            val <<= 8;
            val |= fourcc.charAt(i);
        }
        // CHECKSTYLE:ON MagicNumber
        this.value = val;
    }

    /**
     * Return FourCC int value.
     *
     * @return int value.
     */
    public int toInt() {
        return value;
    }

    @Override
    public String toString() {
        String s = "";
        // CHECKSTYLE:OFF MagicNumber - Magic numbers here for illustration
        s += (char) ((value >> 24) & 0xFF);
        s += (char) ((value >> 16) & 0xFF);
        s += (char) ((value >> 8) & 0xFF);
        s += (char) (value & 0xFF);
        // CHECKSTYLE:ON MagicNumber
        return s;
    }
}