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
package fr.inria.papart.calibration.files;

import fr.inria.papart.calibration.files.Calibration;
import processing.core.PApplet;
import processing.data.XML;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class PlanarTouchCalibration extends Calibration {

    static final String PLANAR_TOUCH_CALIBRATION_XML_NAME = "PlanarTouchCalibration";

    static final String MAX_DIST_XML_NAME = "MaxDistance";
    static final String MAX_DIST_INIT_XML_NAME = "MaxDistanceInit";
    static final String MIN_COPO_SIZE_XML_NAME = "MinConnectedCompoSize";
    static final String MIN_HEIGHT_XML_NAME = "MinHeight";
    static final String MAX_RECURSION_XML_NAME = "MaxRecursion";
    static final String NORMAL_FILTER_XML_NAME = "NormalFilter";
    static final String TRACKING_FORGET_TIME_XML_NAME = "TrackingForgetTime";
    static final String TRACKING_MAX_DIST_TIME_XML_NAME = "TrackingMaxDist";
    static final String SEARCH_DEPTH_XML_NAME = "SearchDepth";
    static final String PRECISION_XML_NAME = "Precision";
    static final String TEST1_XML_NAME = "Test1";
    static final String TEST2_XML_NAME = "Test2";
    static final String TEST3_XML_NAME = "Test3";
    static final String TEST4_XML_NAME = "Test4";
    static final String TEST5_XML_NAME = "Test5";

    // Variable parameters... going to a specific class for saving.  
    private float maximumDistance = 10f;    // in mm
    private float maximumDistanceInit = 50f;    // in mm
    private float minimumHeight = 1; // mm
    private float normalFilter = 1; // normalized distance to angle

    private int minimumComponentSize = 3;   // in px
    private int searchDepth = 10;
    private int maximumRecursion = 500;
    private int precision = 2; // pixels

    // tracking
    private int trackingForgetTime = 250; // ms 
    private float trackingMaxDistance = 30; // in mm

    // Testing
    private float test1 = 1;
    private float test2 = 1;
    private float test3 = 1;
    private float test4 = 1;
    private float test5 = 1;

    public String toString() {
        return "maxD: " + maximumDistance + " test1 " + test1
                + " test2 " + test2 + " test3 " + test3
                + "test4 " + test4 + " test5 " + test5;
    }

    // TODO: implement this !
    @Override
    public boolean isValid() {
        return true;
    }

    private XML createXML() {
        XML root = new XML(PLANAR_TOUCH_CALIBRATION_XML_NAME);
        setIn(root);
        return root;
    }

    private void setIn(XML xml) {
        xml.setFloat(MAX_DIST_XML_NAME, maximumDistance);
        xml.setFloat(MAX_DIST_INIT_XML_NAME, maximumDistanceInit);
        xml.setFloat(MIN_HEIGHT_XML_NAME, minimumHeight);
        xml.setFloat(NORMAL_FILTER_XML_NAME, normalFilter);

        xml.setInt(MIN_COPO_SIZE_XML_NAME, minimumComponentSize);
        xml.setInt(MAX_RECURSION_XML_NAME, maximumRecursion);
        xml.setInt(SEARCH_DEPTH_XML_NAME, searchDepth);
        xml.setInt(PRECISION_XML_NAME, precision);

        xml.setInt(TRACKING_FORGET_TIME_XML_NAME, trackingForgetTime);
        xml.setFloat(TRACKING_MAX_DIST_TIME_XML_NAME, trackingMaxDistance);

        xml.setFloat(TEST1_XML_NAME, test1);
        xml.setFloat(TEST2_XML_NAME, test2);
        xml.setFloat(TEST3_XML_NAME, test3);
        xml.setFloat(TEST4_XML_NAME, test4);
        xml.setFloat(TEST5_XML_NAME, test5);
    }

    private void getFrom(XML xml) {
        maximumDistance = xml.getFloat(MAX_DIST_XML_NAME);
        maximumDistanceInit = xml.getFloat(MAX_DIST_INIT_XML_NAME);
        minimumComponentSize = xml.getInt(MIN_COPO_SIZE_XML_NAME);
        minimumHeight = xml.getFloat(MIN_HEIGHT_XML_NAME);
        maximumRecursion = xml.getInt(MAX_RECURSION_XML_NAME);

        searchDepth = xml.getInt(SEARCH_DEPTH_XML_NAME);
        precision = xml.getInt(PRECISION_XML_NAME);

        normalFilter = xml.getFloat(NORMAL_FILTER_XML_NAME);
        trackingForgetTime = xml.getInt(TRACKING_FORGET_TIME_XML_NAME);
        trackingMaxDistance = xml.getFloat(TRACKING_MAX_DIST_TIME_XML_NAME);

        test1 = xml.getFloat(TEST1_XML_NAME);
        test2 = xml.getFloat(TEST2_XML_NAME);
        test3 = xml.getFloat(TEST3_XML_NAME);
        test4 = xml.getFloat(TEST4_XML_NAME);
        test5 = xml.getFloat(TEST5_XML_NAME);
    }

    public void setTo(PlanarTouchCalibration calib) {
        this.maximumDistance = calib.maximumDistance;
        this.maximumDistanceInit = calib.maximumDistanceInit;
        this.maximumRecursion = calib.maximumRecursion;
        this.minimumComponentSize = calib.minimumComponentSize;
        this.minimumHeight = calib.minimumHeight;

        this.searchDepth = calib.searchDepth;
        this.precision = calib.precision;

        this.normalFilter = calib.normalFilter;
        this.trackingForgetTime = calib.trackingForgetTime;
        this.trackingMaxDistance = calib.trackingMaxDistance;

        // test
        this.test1 = calib.test1;
        this.test2 = calib.test2;
        this.test3 = calib.test3;
        this.test4 = calib.test4;
        this.test5 = calib.test5;
    }

    @Override
    public void addTo(XML xml) {
        xml.addChild(createXML());
    }

    @Override
    public void replaceIn(XML xml) {
        XML element = xml.getChild(PLANAR_TOUCH_CALIBRATION_XML_NAME);
        setIn(element);
    }

    @Override
    public void loadFrom(PApplet parent, String fileName) {
        XML root = parent.loadXML(fileName);
        XML planarTouchCalibNode = root.getChild(PLANAR_TOUCH_CALIBRATION_XML_NAME);
        getFrom(planarTouchCalibNode);
    }

    public float getMaximumDistance() {
        return maximumDistance;
    }

    public void setMaximumDistance(float maximumDistance) {
        this.maximumDistance = maximumDistance;
    }

    public float getMaximumDistanceInit() {
        return maximumDistanceInit;
    }

    public void setMaximumDistanceInit(float maximumDistanceInit) {
        this.maximumDistanceInit = maximumDistanceInit;
    }

    public int getMinimumComponentSize() {
        return minimumComponentSize;
    }

    public void setMinimumComponentSize(int minimumComponentSize) {
        this.minimumComponentSize = minimumComponentSize;
    }

    public float getMinimumHeight() {
        return minimumHeight;
    }

    public void setMinimumHeight(float minimumHeight) {
        this.minimumHeight = minimumHeight;
    }

    public int getMaximumRecursion() {
        return maximumRecursion;
    }

    public void setMaximumRecursion(int maximumRecursion) {
        this.maximumRecursion = maximumRecursion;
    }

    public int getTrackingForgetTime() {
        return trackingForgetTime;
    }

    public void setTrackingForgetTime(int trackingForgetTime) {
        this.trackingForgetTime = trackingForgetTime;
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    /**
     * Number of neighbours seen at a time.
     *
     * @param searchDepth
     */
    public void setSearchDepth(int searchDepth) {
        this.searchDepth = searchDepth;
    }

    public int getPrecision() {
        return precision;
    }

    /**
     * Precision is how much we divide the resolution on X and Y. A precision of
     * 1 is the best, 2 or 3 is OK for touch, 4 or 5 is OK for a hovering hand
     *
     * @param precision
     */
    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public float getTrackingMaxDistance() {
        return this.trackingMaxDistance;
    }

    public void setTrackingMaxDistance(float trackDistance) {
        this.trackingMaxDistance = trackDistance;
    }

    public float getNormalFilter() {
        return normalFilter;
    }

    public void setNormalFilter(float normalFilter) {
        this.normalFilter = normalFilter;
    }

    public float getTest1() {
        return test1;
    }

    public void setTest1(float test1) {
        this.test1 = test1;
    }

    public float getTest2() {
        return test2;
    }

    public void setTest2(float test2) {
        this.test2 = test2;
    }

    public float getTest3() {
        return test3;
    }

    public void setTest3(float test3) {
        this.test3 = test3;
    }

    public float getTest4() {
        return test4;
    }

    public void setTest4(float test4) {
        this.test4 = test4;
    }

    public float getTest5() {
        return test5;
    }

    public void setTest5(float test5) {
        this.test5 = test5;
    }

    @Override
    public void addTo(StringBuilder yaml) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
