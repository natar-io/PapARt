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
package fr.inria.papart.multitouch.tracking;

import fr.inria.papart.multitouch.OneEuroFilter;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.detection.TouchDetection;
import fr.inria.papart.multitouch.detection.TouchDetectionDepth;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 * Tracked element with a position and speed. The position can be filtered, the
 * filtering must be called directly, it is not automatic.
 *
 * @author Jeremy Laviole
 */
public interface Trackable {

    static final int SHORT_TIME_PERIOD = 200;  // in ms

    public float distanceTo(Trackable newTp);
//    public void setPosition(Vec3D pos);

    public void setPosition(PVector pos);

    public PVector getPosition();
//    public Vec3D getPositionVec3D() {
//        return new Vec3D(position.x, position.y, position.z);
//    }

    public PVector getPreviousPosition();
//    public Vec3D getPreviousPositionVec3D();

    public PVector getSpeed();

    public void filter();

    public void filter(int updateTime);

    public void forceID(int id);

    public int getID();

    public void setCreationTime(int timeStamp);

    public int getAge(int currentTime);

    public boolean isYoung(int currentTime);

    //    public float getConfidence();
//    public void setConfidence(float confidence);
    public void setUpdated(boolean updated);

    public boolean isUpdated();

    public int lastUpdate();

    public boolean updateWith(Trackable tp);

    public void updateAlone();

    public boolean isObselete(int currentTime);

    public boolean isToRemove(int currentTime, int duration);

    public boolean isToDelete();

    public void delete(int time);

    public float getTrackingMaxDistance();
//    public TouchDetection getDetection();
//    public void setDetection(TouchDetection detection);
}
