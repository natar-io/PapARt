///*
// * Part of the PapARt project - https://project.inria.fr/papart/
// *
// * Copyright (C) 2014-2016 Inria
// * Copyright (C) 2011-2013 Bordeaux University
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation, version 2.1.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General
// * Public License along with this library; If not, see
// * <http://www.gnu.org/licenses/>.
// */
//package fr.inria.papart.procam;
//
//
//import org.bytedeco.javacv.*;
//
//public class Test {
//    public static void main(String[] args) throws Exception {
//        int x = 0, y = 0, w = 1024, h = 768; // specify the region of screen to grab
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(":0.0+" + x + "," + y);
//        grabber.setFormat("x11grab");
//        grabber.setImageWidth(w);
//        grabber.setImageHeight(h);
//        grabber.start();
//        
//        CanvasFrame frame = new CanvasFrame("Screen Capture");
//        while (frame.isVisible()) {
//            frame.showImage(grabber.grab());
//        }
//        frame.dispose();
//        grabber.stop();
//    }
//}