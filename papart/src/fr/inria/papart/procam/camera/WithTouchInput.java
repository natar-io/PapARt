/*
 * Copyright (C) 2016 Jérémy Laviole - jeremy.laviole@inria.fr.
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
package fr.inria.papart.procam.camera;

import fr.inria.papart.multitouch.DepthTouchInput;
import org.bytedeco.javacpp.opencv_core.IplImage;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public interface WithTouchInput {
    
    public DepthTouchInput getTouchInput();
    public void setTouchInput(DepthTouchInput touchInput);
    public void newTouchImage();
    public void newTouchImageWithColor(IplImage image);
}
