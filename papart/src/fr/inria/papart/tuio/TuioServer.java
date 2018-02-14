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
package fr.inria.papart.tuio;

import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import java.util.ArrayList;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author Jeremy Laviole
 */
public class TuioServer {

    private final int outPort;
    private final int inPort;

    private OscP5 oscP5;
    private NetAddress myRemoteLocation;

    private int sendID = 0;

    public TuioServer(PApplet parent, int inPort, String outAddress, int outPort) {
        this.inPort = inPort;
        this.outPort = outPort;

        oscP5 = new OscP5(this, 12000);
        myRemoteLocation = new NetAddress(outAddress, outPort);
    }

    public void send(ArrayList<TrackedDepthPoint> touchs) {
        send2D(touchs);
    }

    public void send2D(ArrayList<TrackedDepthPoint> touchs) {

        OscMessage[] messages = new OscMessage[touchs.size() + 2];
        String messageType = "/tuio/2Dcur";

        messages[0] = new OscMessage(messageType);
        messages[0].add("alive");

        int k = 1;
        for (TrackedDepthPoint tp : touchs) {
            PVector pos = tp.getPosition();
            PVector speed = tp.getSpeed();
           
//            int id = tp.getID();
//            System.out.println("Tuio OUt id " + id);
            messages[0].add(k);
            
            messages[k] = new OscMessage(messageType);

            messages[k].add("set"); /* add an int to the osc message */

            messages[k].add(k);
            messages[k].add(pos.x);
            messages[k].add(pos.y);

            messages[k].add(speed.x);
            messages[k].add(speed.y);
            messages[k].add(0f);

            k++;
        }

        messages[k] = new OscMessage(messageType);
        messages[k].add("fseq"); /* add an int to the osc message */

        messages[k].add(sendID++);

        for (OscMessage msg : messages) {
            oscP5.send(msg, myRemoteLocation);
        }

    }

    public void send25D(ArrayList<TrackedDepthPoint> touchs, float maxZ) {

        OscMessage[] messages = new OscMessage[touchs.size() + 2];

        String messageType = "/tuio/25Dcur";

        messages[0] = new OscMessage(messageType);
        messages[0].add("alive");

        int k = 1;
        for (TrackedDepthPoint tp : touchs) {
            PVector pos = tp.getPosition();
            PVector speed = tp.getSpeed();
//            int id = tp.getID();
//            messages[0].add(id);
            messages[0].add(k);
            messages[k] = new OscMessage(messageType);

            messages[k].add("set"); /* add an int to the osc message */

            messages[k].add(k);
            messages[k].add(pos.x);
            messages[k].add(pos.y);
            messages[k].add(pos.z / maxZ);

            messages[k].add(speed.x);
            messages[k].add(speed.y);
            messages[k].add(speed.z / maxZ);
            messages[k].add(0f);

            k++;
        }

        messages[k] = new OscMessage(messageType);
        messages[k].add("fseq"); /* add an int to the osc message */

        messages[k].add(sendID++);

        for (OscMessage msg : messages) {
            oscP5.send(msg, myRemoteLocation);
        }

    }

//
//    http://www.tuio.org/?specification
//    
//    2D Interactive Surface
///tuio/2Dobj set s i x y a X Y A m r
///tuio/2Dcur set s x y X Y m
///tuio/2Dblb set s x y a w h f X Y A m r
//2.5D Interactive Surface
///tuio/25Dobj set s i x y z a X Y Z A m r
///tuio/25Dcur set s x y z X Y Z m
///tuio/25Dblb set s x y z a w h f X Y Z A m r
//3D Interactive Surface
///tuio/3Dobj set s i x y z a b c X Y Z A B C m r
///tuio/3Dcur set s x y z X Y Z m
///tuio/3Dblb set s x y z a b c w h d v X Y Z A B C m r
//custom profile
///tuio/_[formatString]
///tuio/_sixyP set s i x y 0.57
//    s	Session ID (temporary object ID)	int32
//i	Class ID (e.g. marker ID)	int32
//x, y, z	Position	float32, range 0...1
//a, b, c	Angle	float32, range 0..2PI
//w, h, d	Dimension	float32, range 0..1
//f, v	Area, Volume	float32, range 0..1
//X, Y ,Z	Velocity vector (motion speed & direction)	float32
//A, B, C	Rotation velocity vector (rotation speed & direction)	float32
//m	Motion acceleration	float32
//r	Rotation acceleration	float32
//P	Free parameter	type defined by OSC message header
//    
//    
//      /tuio/2Dcur source application@address
//  /tuio/2Dcur alive s_id0 ... s_idN
//  /tuio/2Dcur set s_id x_pos y_pos x_vel y_vel m_accel
//  /tuio/2Dcur fseq f_id
// A typical TUIO bundle will contain an initial ALIVE message, followed by an 
// arbitrary number of SET messages that can fit into the actual bundle capacity
// and a concluding FSEQ message. A minimal TUIO bundle needs to contain at least the compulsory ALIVE and FSEQ messages. The FSEQ frame ID is incremented for each delivered bundle, while redundant bundles can be marked using the frame sequence ID -1.
// The optional source message can be transmitted to allow the multiplexing of several
// TUIO trackers on the client side. The application@address argument is a single string 
// that specifies th application name and any unique source address (IP, host name, MAC address).
// If sent on localhost, the second address part can be omitted, hence any 
// string without the @ separator implicitly comes from localhost.
//    
}
