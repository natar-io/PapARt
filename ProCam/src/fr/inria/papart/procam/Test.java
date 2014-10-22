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
package fr.inria.papart.procam;


import org.bytedeco.javacv.*;

public class Test {
    public static void main(String[] args) throws Exception {
        int x = 0, y = 0, w = 1024, h = 768; // specify the region of screen to grab
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(":0.0+" + x + "," + y);
        grabber.setFormat("x11grab");
        grabber.setImageWidth(w);
        grabber.setImageHeight(h);
        grabber.start();
        
        CanvasFrame frame = new CanvasFrame("Screen Capture");
        while (frame.isVisible()) {
            frame.showImage(grabber.grab());
        }
        frame.dispose();
        grabber.stop();
    }
}