/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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
package fr.inria.papart.procam;

import fr.inria.papart.calibration.HomographyCreator;
import fr.inria.papart.tracking.MarkerBoardFactory;
import fr.inria.papart.tracking.MarkerBoardInvalid;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.tracking.ObjectFinder;
import fr.inria.papart.utils.MathUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.event.KeyEvent;
import toxi.geom.Plane;
import toxi.geom.Triangle3D;

public class PaperScreen extends DelegatedGraphics {

    private static final int DEFAULT_DRAWING_SIZE = 100;
    private static final float DEFAULT_RESOLUTION = 2;

    protected PApplet parent;

    // many
    // current
//    protected BaseDisplay display;
    protected Camera cameraTracking;
    // list
    protected ArrayList<BaseDisplay> displays = new ArrayList<BaseDisplay>();
    protected BaseDisplay mainDisplay;

    // only one.
    private MarkerBoard markerBoard;
    protected PVector drawingSize
            = new PVector(DEFAULT_DRAWING_SIZE, DEFAULT_DRAWING_SIZE, 1);
    protected float quality = DEFAULT_RESOLUTION;

    protected boolean isDrawingOnScreen = true;
    protected boolean isDrawingOnDisplay = false;

    private boolean isInitialized = false;
    private boolean isRegistered = false;
    protected boolean isWithoutCamera = false;
    protected boolean useManualLocation = true; // Activated at init.

    private float filteringDistance = 30;
    private float filteringFreq = 30;
    private float filteringCutoff = 4;

    private PMatrix3D manualLocation = new PMatrix3D();

    // FROM SCREEN: TO ORGANIZE
    // The current graphics
    private PGraphicsOpenGL thisGraphics;

    // Either one, or the other is unique to this object. 
    // The other one is unique to the camera/markerboard couple. 
    private PMatrix3D extrinsics = new PMatrix3D();

    protected HomographyCalibration worldToScreen;
    private boolean isDrawing = true;

    // ID is for naming this paper screen.
    private final int id;

    public static int count = 0;

    /**
     * Create a new PaperScreen, a Papart object has to be created first. A
     * PaperScreen is a rendering / interacting space like a Processing Sketch.
     * PaperScreen usually have their own rendering (PGraphics object): this is
     * the drawOnPaper() mode. They can be rendererd directly on the Display,
     * usually for 3D effects: this is the drawAroundPaper().
     */
    public PaperScreen() {
        Papart papart = Papart.getPapart();

        if (papart == null) {
            throw new RuntimeException("Cannot create the PaperScreen, "
                    + "the Papart singleton cannot be found.");
        }

        this.parent = papart.getApplet();
        this.isWithoutCamera = papart.isWithoutCamera();
        if (!this.isWithoutCamera) {
            this.cameraTracking = papart.getPublicCameraTracking();
        }
        mainDisplay = papart.getDisplay();
        displays.add(papart.getDisplay());
        this.id = count++;
        this.markerBoard = MarkerBoardInvalid.board;
        // Default to projector graphics.
        // currentGraphics = this.display.getGraphics();
        register();
    }

    /**
     * Create a PaperScreen with a given camera and display. Instanciation
     * without the use of a Papart object.
     *
     * @param mainApplet
     * @param cam
     * @param proj
     */
    public PaperScreen(PApplet mainApplet, Camera cam, BaseDisplay proj) {
        this.parent = mainApplet;
        this.cameraTracking = cam;
        mainDisplay = proj;
        displays.add(proj);
        this.id = count++;
        this.markerBoard = MarkerBoardInvalid.board;
        register();
    }

    /**
     * Start without a camera. The paperScreen location has to be manually set.
     * This is used for debug mode.
     *
     * @param display
     */
    public PaperScreen(BaseDisplay display) {
        this.isWithoutCamera = true;
        mainDisplay = display;
        displays.add(display);
        this.id = count++;
        this.markerBoard = MarkerBoardInvalid.board;
        register();
    }

    ///////////////////////////////////////
    /// Methods to override in user code 
    ///////////////////////////////////////
    /**
     * This method must be overloaded in the child class. For example to load
     * images, 3D models etc...
     */
    protected void setup() {
        System.out.println("PaperScreen setup. You should not see this unless for debug.");
    }

    /**
     * This method must be overloaded in the child class. For example to choose
     * the rendering mode (setDrawAroundPaper, setDrawOnPaper).
     */
    protected void settings() {
        setDrawStandard();
        System.out.println("PaperScreen settings. You should not see this unless for debug.");
    }

    /**
     * Call the settings() method again, and initialize the offscreen rendering
     * if necessary. Will also re-initialize the tracking.
     */
    public void reInitialize() {
        this.isInitialized = false;
    }

    /**
     * Do not call, Automatically called by Processing. This method initialises
     * the display if necessary.
     */
    public void pre() {
        if (!isInitialized) {
            settings();
            checkInitErrors();
            // check if papart is around...

            if (this.markerBoard == MarkerBoardInvalid.board) {
                this.isWithoutCamera = true;
            }

            if (isDrawingOnDisplay) {
                for (BaseDisplay display : this.displays) {
                    if (display.hasCamera()) {
                        this.cameraTracking = display.getCamera();
                    }
                    this.currentGraphics = display.getGraphics();
                    setup();
                    tryInitTracking();
                }
            }

            if (isDrawingOnScreen) {
                for (BaseDisplay display : displays) {
                    display.addPaperScreen(this);
                }
                this.currentGraphics = getGraphics();
                setup();
                tryInitTracking();
            }

            isInitialized = true;
        }

        // Needed for touch and projection operations
        computeWorldToScreenMat(cameraTracking);

//        assert (isInitialized);
//        if (this.isWithoutCamera || useManualLocation) {
//            return;
//        }
//        checkCorners();
    }

    public boolean hasMarkerBoard() {
        return this.markerBoard == MarkerBoardInvalid.board;
    }

    public MarkerBoard getMarkerBoard() {

        // TODO: WTF  invalid
//        if (!this.hasMarkerBoard()) {
//            System.err.println("The PaperScreen " + this + " does not have a markerboard...");
//        }
        return this.markerBoard;
    }

    public PGraphicsOpenGL getGraphics() {
        if (thisGraphics == null) {
            thisGraphics = (PGraphicsOpenGL) parent.createGraphics(
                    this.getRenderingSizeX(),
                    this.getRenderingSizeY(),
                    PApplet.P3D);
        }

        return thisGraphics;
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public PVector getSize() {
        return drawingSize.copy();
    }

    /**
     * Get the rendering size in pixels.
     *
     * width = drawingSize x quality (px) = (mm) x (px/mm)
     *
     * @return width in pixels.
     */
    public int getRenderingSizeX() {
        return (int) (drawingSize.x * quality);
    }

    /**
     * Get the rendering size in pixels.
     *
     * width = drawingSize x quality (px) = (mm) x (px/mm)
     *
     * @return height in pixels.
     */
    public int getRenderingSizeY() {
        return (int) (drawingSize.y * quality);
    }

    /**
     * Use the tracking for the markerboard.
     */
    public void useTracking() {
        if (useManualLocation) {
            this.useManualLocation = false;
            this.markerBoard.subscribe();
        }
    }

    /**
     * todo: a manual location should be used for each camera ?
     *
     * @param mat
     */
    protected void useManualLocation(PMatrix3D mat) {
        this.manualLocation.set(mat);

        System.out.println("Set manual loc");
        if (!useManualLocation) {
            this.useManualLocation = true;
            this.markerBoard.unsubscribe();
        }
        // Will block the update of all markerboard...
//        markerBoard.blockUpdate(cameraTracking, 10 * 60 * 60 * 1000); // ms
    }

    protected PMatrix3D getMainLocation(Camera camera) {
        if (useManualLocation) {
            return manualLocation.get();
        }
        return markerBoard.getTransfoMat(camera).get();
    }

    /**
     * Get a copy of the overall transform (after tracking and second
     * transform).
     *
     * @param trackedLocation
     * @return
     */
    public PMatrix3D getLocation(PMatrix3D trackedLocation) {
        PMatrix3D combinedTransfos = trackedLocation.get();
        combinedTransfos.apply(extrinsics);
        return combinedTransfos;
    }

    public void computeWorldToScreenMat(Camera camera) {

        ///////////////////// PLANE COMPUTATION  //////////////////
        PVector[] paperPosCorners3D = computeCorners(camera);
        plane = new Plane(new Triangle3D(MathUtils.toVec(paperPosCorners3D[0]), MathUtils.toVec(paperPosCorners3D[1]), MathUtils.toVec(paperPosCorners3D[2])));

        HomographyCreator homography = new HomographyCreator(3, 2, 4);
        homography.addPoint(paperPosCorners3D[0], new PVector(0, 0));
        homography.addPoint(paperPosCorners3D[1], new PVector(1, 0));
        homography.addPoint(paperPosCorners3D[2], new PVector(1, 1));
        homography.addPoint(paperPosCorners3D[3], new PVector(0, 1));
        worldToScreen = homography.getHomography();
    }

    public PVector[] computeCorners(Camera camera) {
        PVector[] paperPosCorners3D = new PVector[4];
        ///////////////////// PLANE COMPUTATION  //////////////////
        PMatrix3D mat = this.getLocation(camera);

        paperPosCorners3D[0] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(drawingSize.x, 0, 0);
        paperPosCorners3D[1] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(0, drawingSize.y, 0);
        paperPosCorners3D[2] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(-drawingSize.x, 0, 0);
        paperPosCorners3D[3] = new PVector(mat.m03, mat.m13, mat.m23);
        return paperPosCorners3D;
    }
    
    /**
     * @param position in mm in the paper screen
     * @return  position in px in the cameratracking.
     */
    public PVector computePxPosition(PVector position){
       
        PVector p = position.copy();
        
        // Invert Y
        p.y = p.y - drawingSize.y;
        p.y = -p.y;
        // get a copy of the position
        PMatrix3D mat = this.getLocation(getCameraTracking()).get();
        mat.translate(p.x, p.y, 0);

        PVector pos3D = new PVector(mat.m03, mat.m13, mat.m23);
        PVector camCoord = cameraTracking.getProjectiveDevice().worldToPixelCoord(pos3D);
        return camCoord;
    }

    protected Plane plane = new Plane();

    /**
     * Get the 3D plane object from the main camera.
     *
     * @return
     */
    public Plane getPlane() {
        computeWorldToScreenMat(cameraTracking);
        return plane;
    }

    /**
     * Get a 3D plane object given a camera.
     *
     * @param camera
     * @return
     */
    public Plane getPlane(Camera camera) {
        computeWorldToScreenMat(camera);
        return plane;
    }

    public HomographyCalibration getWorldToScreen() {
        return worldToScreen;
    }
//    /**
//     * Set the main position by the tracking system. 
//     * @param cam
//     */
//    public void setTrackedLocation(Camera cam) {
//        this.blockUpdate(cam, 0); // ms
//    }

    private boolean isOpenGL = false;

    // TODO: find another name
    public boolean isOpenGL() {
        return isOpenGL;
    }

    protected void setOpenGL(boolean gl) {
        this.isOpenGL = gl;
    }

    // TODO: what about those ?
    public boolean hasExtrinsics() {
        return true;
    }

    public PMatrix3D getExtrinsics() {
        return this.extrinsics;
    }

    private boolean checkInitErrors() {
        if (parent == null) {
            String message = "This PaperScreen cannot be initialized without "
                    + "the current PApplet, use PapARt or pass it as an arugment "
                    + "to init. ";
            throw new RuntimeException(message);
        }
        return true;
    }

    /**
     * Ask the main camera to track the markerboard, and set the filtering.
     */
    private void tryInitTracking() {
        // If there is really a camera tracking.
        // There can be multiple camera tracking !!
        if (!isWithoutCamera) {
            // automatic update of the paper screen, regarding the camera.
            trackCurrentMarkerBoard();
            updateBoardFiltering();
        }
    }

    /**
     * Ask the cameraTracking to track the current markerboard.
     */
    private void trackCurrentMarkerBoard() {
        if (isWithoutCamera || this.markerBoard == MarkerBoardInvalid.board) {
            System.out.println("Cannot start the tracking with cam: " + isWithoutCamera + ", board: " + this.markerBoard);
            return;
        }

        this.useTracking();
        if (!cameraTracking.tracks(markerBoard)) {
            cameraTracking.trackMarkerBoard(markerBoard);
        }
    }

    private void register() {
        this.isRegistered = true;
        parent.registerMethod("pre", this);
        parent.registerMethod("draw", this);

        // IDEA: register to save/load automatically. 
        parent.registerMethod("keyEvent", this);

        // Do this so that the display is the last rendered.
        mainDisplay.registerAgain();
    }

    private String loadKey = "l", saveKey = "s", trackKey = "t";
    private String saveName = null;
//    private String loadKey = null, saveKey = null, trackKey = null;
//    private String saveName = null;

    public void setLoadSaveKey(String loadKey, String saveKey) {
        setLoadKey(loadKey);
        setSaveKey(saveKey);
    }

    /**
     * Set a load to save the location. It will be usable using alt-key.
     *
     * @param key
     */
    public void setLoadKey(String key) {
        this.loadKey = key;
    }

    /**
     * Set a key to save the location. It will be usable using alt-key.
     *
     * @param key
     */
    public void setSaveKey(String key) {
        this.saveKey = key;
    }

    /**
     * Set a key to activate/deactivate the trackinge. It will be usable using
     * alt-key.
     *
     * @param key
     */
    public void setTrackKey(String key) {
        this.trackKey = key;
    }

    public void setSaveName(String name) {
        this.saveName = name;
    }

    private boolean useAlt = true;

    /**
     * Use the alt modifier to save or load the files. Default is true.
     *
     * @param alt
     */
    public void useAlt(boolean alt) {
        this.useAlt = alt;
    }

    /**
     * Events coming from Processing to handle keys.
     *
     * @param e
     */
    public void keyEvent(KeyEvent e) {

        String filename = "paper-" + Integer.toString(id) + ".xml";

        if (e.isAltDown() || !useAlt) {
            if (e.getAction() == KeyEvent.PRESS) {

                if (saveName == null) {
                    System.err.println("This paperscreen does not have name ! \n Set it with setSaveName()");
                } else {
                    filename = saveName;
                }

                if (saveKey != null
                        && (e.getKey() == saveKey.charAt(0))) {
                    System.out.println("Saved to : " + filename);
                    this.saveLocationTo(filename);
                }
                if (trackKey != null
                        && (e.getKey() == trackKey.charAt(0))) {
                    this.useManualLocation(!this.useManualLocation, null);
                    String status = useManualLocation ? "ON" : "OFF";
                    System.out.println("PaperScreen: " + filename + " tracking: " + status);
                }
                if (loadKey != null
                        && (e.getKey() == loadKey.charAt(0))) {
                    this.loadLocationFrom(filename);
                    System.out.println("Loaded from: " + filename);
                }
            }
        }
    }

    /**
     * Do not override anymore Change to drawOnPaper or drawAroundPaper.
     *
     */
    public void draw() {

        if (!isInitialized) {
            return;
        }

        Camera mainCamera = cameraTracking;

        if (isDrawingOnScreen) {
            setDrawing(true);
            PGraphicsOpenGL g = getGraphics();
            this.currentGraphics = g;
            g.beginDraw();
            g.scale(quality);
            this.drawOnPaper();
            g.endDraw();
        }

        if (isDrawingOnDisplay) {
            for (BaseDisplay display : this.displays) {

                if (display.hasCamera()) {
                    this.cameraTracking = display.getCamera();
                }
                PGraphicsOpenGL g = display.beginDrawOnScreen(this);
                this.currentGraphics = g;
                this.drawAroundPaper();
                display.endDraw();
            }
        }

        cameraTracking = mainCamera;
    }

    /**
     * Method to override.
     */
    public void drawOnPaper() {
        background(0, 100, 200);
//        System.out.println("drawOnPaper default, you should not see this.");
    }

    /**
     * Method to override.
     */
    public void drawAroundPaper() {
//        System.out.println("drawAroundPaper default, you should not see this.");
    }

    /**
     * Activate/Desactivate the tracking. This is called when loadLocationFrom()
     * is called.
     *
     * @param manual true to activate, false to revert to tracking
     * @param loc forced location, or null to use the tracked location
     */
    public void useManualLocation(boolean manual, PMatrix3D loc) {
        if (manual) {
            if (loc == null) {
//                System.err.println("Cannot set a null location.");
                loc = this.getLocation(cameraTracking);
            }
            this.useManualLocation(loc);
            this.manualLocation = loc.get();
        }
        if (!manual) {
            this.useTracking();
            this.manualLocation = new PMatrix3D();
        }
        // TEST instead of blocking the update, just skip the use of the tracking.
//        if (this.useManualLocation) {
//            markerBoard.blockUpdate(cameraTracking, 10 * 60 * 60 * 1000); // ms
//        } else {
//            markerBoard.blockUpdate(cameraTracking, 0); // ms
//        }
    }

    HashMap<Integer, Integer> positionsHistory = new HashMap<Integer, Integer>();

    ////////////////////////
    //// Tracking individual markers
    ////////////////////////
    /**
     * Get individual markers, their ID should be between 800 and 1000.
     *
     * @param markerWidth
     * @return
     */
    public Map<Integer, PVector> getSingleMarkers(float markerWidth) {
        Papart papart = Papart.getPapart();
//        ArrayList<PVector> positions = new ArrayList<>();
        HashMap<Integer, PVector> positions = new HashMap<Integer, PVector>();

        DetectedMarker[] markers = papart.getMarkerList();

        for (DetectedMarker marker : markers) {

            if (marker.id < 800 || marker.id > 1000) {
                continue;
            }

            //       next if marker.confidence < 1.0
            PMatrix3D mat = papart.getMarkerMatrix(marker.id, markerWidth);

            // look at others if the position is not valid
            if (mat == null) {
                continue;
            }
            PVector pos = papart.projectPositionTo(mat, this);

            if (pos.y < 0 || pos.y > drawingSize.y
                    || pos.x < 0
                    || pos.x > drawingSize.x) {

                // Not in this paperscreen - reset history
                positionsHistory.put(marker.id, 0);
            } else {
                // update history
                Integer markerID = positionsHistory.get(marker.id);
                int history = markerID == null ? 0 : markerID;
                positionsHistory.put(marker.id, ++history);

                // If it is old enough ? ~ 10 frames 
                if (history > 10) {
                    positions.put(marker.id, pos);
                }
            }
        }
        return positions;
    }

    // TODO: use Z as the angle of the marker --- source in ruby
//    
//       # pos.x = pos.x / filter_scale
//      # pos.y = pos.y / filter_scale
//      if @marker_valid_pos[marker.id] == nil
//        filter_intens = 20.0 # freq
//##        filter_intens = 1.0 ## alpha
//        @marker_valid_pos[marker.id] =
//          {
//            :x_filter => Papartlib::OneEuroFilter.new(filter_intens),
//            :y_filter => Papartlib::OneEuroFilter.new(filter_intens),
//            :angle_filter => Papartlib::OneEuroFilter.new(filter_intens)
//
//           #  :x_filter => Papartlib::LowPassFilter.new(filter_intens, pos.x),
//           # :y_filter => Papartlib::LowPassFilter.new(filter_intens, pos.y),
//           # :angle_filter => Papartlib::LowPassFilter.new(filter_intens, pos.z)
//          }
//      else
//        pos.x = @marker_valid_pos[marker.id][:x_filter].filter(pos.x)
//        pos.y = @marker_valid_pos[marker.id][:y_filter].filter(pos.y)
//
//        pos.z = pos.z + 2 * Math::PI if pos.z < 0  if in_blue_zone(pos.x)
//#        pos.z = pos.z + 2 * Math::PI  if in_blue_zone(pos.x)
//        pos.z = @marker_valid_pos[marker.id][:angle_filter].filter(pos.z)
//        pos.z = pos.z - 2* Math::PI  if in_blue_zone(pos.x)
    /**
     * Get the main marker found, ID between 800 and 1000.
     *
     * @param markerWidth
     * @return id of the marker found, or -1 in none.
     */
    public int getMainMarker(float markerWidth) {

        int minimumAge = 10;
        int selected = -1;

        Map<Integer, PVector> singleMarkers = getSingleMarkers(markerWidth);
        for (Integer key : singleMarkers.keySet()) {
            Integer age = positionsHistory.get(key);
            if (age != null && age > minimumAge) {
                selected = key;
                minimumAge = age;
            }
        }
        return selected;
    }

    ////////////////////////
    // Location handling. //
    ////////////////////////
    /**
     * Add a vector to the tracked location. The setLocation do not stack up. It
     * replaces the previous call.
     *
     * @param v in millimeters
     */
    public void setLocation(PVector v) {
        setLocation(v.x, v.y, v.z);
    }

    /**
     * Add a vector from the tracked location.
     *
     * The setLocation do not stack up. It replaces the previous call.
     *
     * @param x in millimeters
     * @param y in millimeters
     * @param z in millimeters
     */
    public void setLocation(float x, float y, float z) {
        if (extrinsics == null) {
            extrinsics = new PMatrix3D();
        }
        extrinsics.reset();
        extrinsics.translate(x, y, z);
    }

    /**
     * Add another transformation from the current location. The setLocation do
     * not stack up. It replaces the previous call.
     *
     * @param matrix
     */
    public void setLocation(PMatrix3D matrix) {
        assert (isInitialized);
        if (extrinsics == null) {
            this.extrinsics = new PMatrix3D(matrix);
        } else {
            this.extrinsics.set(matrix);
        }
    }

    /**
     * Get a copy of the overall transform (after tracking and second
     * transform).
     *
     * @param camera
     * @return
     */
    public PMatrix3D getLocation(Camera camera) {
        // WHY invalid
//        if(markerBoard == MarkerBoardInvalid.board){
//            System.out.println("Error: no location... Invalid board");
//        };

        if ((!markerBoard.isTrackedBy(camera) && !this.useManualLocation)) {
            return extrinsics.get();
        }

        PMatrix3D combinedTransfos = getMainLocation(camera);
        combinedTransfos.apply(extrinsics);
        return combinedTransfos;
    }

    /**
     * Get the 3D position. Deprecated.
     *
     * @return the bottom right corner of the markerboard (tracked position).
     */
    @Deprecated
    public PVector getScreenPos() {
        if (this.isWithoutCamera) {
            PMatrix3D mat = getExtrinsics();
            return new PVector(mat.m03, mat.m13, mat.m23);
        } else if (mainDisplay.hasCamera()) {
            return markerBoard.getBoardLocation(cameraTracking, (ARDisplay) mainDisplay);
        } else {
            System.out.println("Could not find the screen Position for the main display.");
            System.out.println("Looking into secondary displays...");
            for (BaseDisplay display : displays) {
                if (display.hasCamera()) {
                    return markerBoard.getBoardLocation(cameraTracking, (ARDisplay) display);
                }
            }
            System.out.println("Could not find where the Screen is...");
            return new PVector();
        }
    }

    /**
     * Disable the drawing and clear the offscreen.
     */
    public void noDraw() {
        setDrawing(false);
        PGraphicsOpenGL pg = getGraphics();
        pg.beginDraw();
        pg.clear();
        pg.endDraw();
    }

    /**
     *
     * @return true when this PaperScreen is renderend in an offscreen buffer.
     */
    public boolean isDraw2D() {
        return this.isDrawingOnScreen;
    }

    /**
     * *
     * Works only in 3D mode with beginDraw3D(). Change the currernt matrix to
     * the location of another PaperScreen. This could be used for drawing
     * objects or putting lights at another PaperScreen location.
     *
     * @param paperScreen PaperScreen to go to.
     */
    public void goTo(PaperScreen paperScreen) {

        if (this.isDrawingOnScreen == true) {
            throw new RuntimeException("Impossible to draw on another board. You need to draw using beginDraw3D() to do so.");
        }

//        if (this.currentGraphics != graphics) {
//            throw new RuntimeException("The given graphics context is not valid. Use the one given by beginDraw3D().");
//        }
        // get the location of this board...
        PMatrix3D loc = this.getLocation().get();
        loc.invert();
        loc.apply(paperScreen.getLocation());

        applyMatrix(loc);
    }

    public static final PVector INVALID_VECTOR = new PVector();

    /**
     * Get the Location of a point located in another PaperScreen. Example: In
     * paperScreenA, there is an object at location (10, 10). We want to know
     * the 3D location of this point relative to this PaperScreen.
     *
     * @param paperScreen
     * @param point
     * @return
     */
    public PVector getCoordFrom(PaperScreen paperScreen, PVector point) {

        // get a copy
        PMatrix3D thisLocationInv = this.getLocation().get();
        thisLocationInv.invert();

        PMatrix3D otherLocation = paperScreen.getLocation();
        PVector cameraViewOfPoint = new PVector();
        otherLocation.mult(point, cameraViewOfPoint);

        PVector thisViewOfPoint = new PVector();
        thisLocationInv.mult(cameraViewOfPoint, thisViewOfPoint);

        if (Float.isNaN(thisViewOfPoint.x)) {
            return INVALID_VECTOR;
        }

        return thisViewOfPoint;
    }

    /**
     * Project the Mouse to this PaperScreen. You can disable it by pressing the
     * right button of the mouse. Similar to SkatoloLink "AddMouseTo".
     *
     * @return Touch to add to your touchList
     */
    public Touch createTouchFromMouse() {

        if (parent.mousePressed && (parent.mouseButton == PConstants.RIGHT)) {
            return Touch.INVALID;
        }
        Touch t = new Touch();
        // Add the mouse a pointer. 
        PVector p = getDisplay().project(this,
                (float) parent.mouseX / (float) parent.width,
                (float) parent.mouseY / (float) parent.height);
        p.x = p.x * drawingSize.x;
        p.y = p.y * drawingSize.y;
        t.setPosition(p);
        return t;
    }

    /**
     *
     * @return true if the markerboard can move through tracking.
     */
    public boolean isMoving() {
        return markerBoard.isMoving(cameraTracking);
    }

    /**
     * Force the 3D location of the markerBoard, as if it were coming from
     * tracking. You can instead add another position with setLocation().
     *
     * @param location
     */
    public void setMainLocation(PMatrix3D location) {
        markerBoard.setFakeLocation(cameraTracking, location);
    }

    /**
     * Get the 3D location of the PaperScreen (bottom-right corner). It takes
     * into account the call to setLocation().
     *
     * @return
     */
    public PVector getLocationVector() {
        PMatrix3D p;
        if (this.useManualLocation) {
            p = this.manualLocation;
        } else {
            p = getLocation(cameraTracking);
        }
        return new PVector(p.m03, p.m13, p.m23);
    }

    /**
     * Get the 3D location of the PaperScreen. This takes into account the
     * setLocation() calls.
     *
     * @return
     */
    public PMatrix3D getLocation() {
        if (this.useManualLocation) {
            return this.manualLocation;
        }
        return this.getLocation(cameraTracking);
    }

    /**
     * Save the tracked location of the PaperScreen. You still need to do the
     * setLocation after loading this.
     *
     * @param filename
     */
    public void saveLocationTo(String filename) {
        HomographyCalibration.saveMatTo(
                Papart.getPapart().getApplet(),
                getMainLocation(cameraTracking),
                filename);
    }

    /**
     * Load the tracked location of the PaperScreen. You still need to do the
     * setLocation after loading this. Calling this will disable the tracking.
     *
     * @param filename
     */
    public void loadLocationFrom(String filename) {
        PMatrix3D loc = HomographyCalibration.getMatFrom(Papart.getPapart().getApplet(), filename);
        this.useManualLocation(true, loc);
//               setMainLocation(loc);
    }

    /**
     * Load a tracked location of the PaperScreen. Calling this will disable the
     * tracking.
     *
     * @param mat
     */
    public void loadLocationFrom(PMatrix3D mat) {
        this.useManualLocation(true, mat);
//        setMainLocation(mat.get());
    }

    /**
     * Experimental - Get the ObjectFinder used in this PaperScreen.
     *
     * @return the object finder or null when not in use.
     */
    public ObjectFinder getObjectTracking() {
        if (markerBoard.useJavaCVFinder()) {
            return markerBoard.getObjectTracking(cameraTracking);
        } else {
            System.err.println("getObjectTracking is only accessible with image-based tracking.");
            return null;
        }
    }

    /**
     * Get the location in camera space of a touchPoint.
     *
     * @param t touch
     * @return the coordinates for cameraTracking.
     */
    public PVector getCameraViewOf(Touch t) {

        if (getDisplay() instanceof ProjectorDisplay) {
            ProjectorDisplay projector = (ProjectorDisplay) getDisplay();
            TrackedElement tp = t.trackedSource;
            PVector screenPos = tp.getPosition();
            PVector tablePos = projector.projectPointer3D(this, screenPos.x, screenPos.y);
            ProjectiveDeviceP pdp = cameraTracking.getProjectiveDevice();
            PVector coord = pdp.worldToPixelCoord(tablePos);
            return coord;
        } else {
// Opposite of project... ?
            return getDisplay().project(this, t.position.x, t.position.y);
        }
    }
        /**
         * Unsafe do not use unless you are sure. This will be moved to a
         * utility class.
         */
    public static PImage getImageFrom(PVector coord, PImage src, PImage dst, int radius) {
        int x = (int) coord.x;
        int y = (int) coord.y;

        dst.copy(src,
                x - radius / 2,
                y - radius / 2,
                radius,
                radius,
                0, 0,
                radius,
                radius);
        return dst;
    }

    /**
     * Get the color of a 3D point. The location of the point is from the
     * cameraTracking origin.
     *
     * @param point
     * @return
     */
    public int getColorFrom3D(PVector point) {
        return getColorFrom3D(cameraTracking, point);
    }

    /**
     * Get the color of a 3D point. The location of the point is from the
     * cameraTracking origin.
     *
     * @param point
     * @return
     */
    public int getColorFrom3D(Camera camera, PVector point) {
        return getColorAt(camera, getPxCoordinates(point));
    }

    /**
     * Get the color of a camera pixel.
     *
     * @param camera
     * @param coord in cameraTracking space.
     * @return
     */
    public int getColorAt(Camera camera, PVector coord) {
        int x = (int) coord.x;
        int y = (int) coord.y;
        ByteBuffer buff = camera.getIplImage().getByteBuffer();
        int offset = x + y * camera.width();
        return getColor(buff, offset);
    }

    /**
     * Get the color of a camera pixel.
     *
     * @param coord in cameraTracking space.
     * @return
     */
    public int getColorAt(PVector coord) {
        return getColorAt(cameraTracking, coord);
    }

    /**
     * Convert the color from Ipl to Processing RGB
     *
     * @param buff
     * @param offset
     * @return
     */
    private int getColor(ByteBuffer buff, int offset) {
        offset = offset * 3;
        return (buff.get(offset + 2) & 0xFF) << 16
                | (buff.get(offset + 1) & 0xFF) << 8
                | (buff.get(offset) & 0xFF);
    }

    /**
     * Get the camera pixel coordinates of a 3D point viewed by the camera.
     *
     * @param cameraTracking3DCoord
     * @return
     */
    public PVector getPxCoordinates(PVector cameraTracking3DCoord) {
        ProjectiveDeviceP pdp = cameraTracking.getProjectiveDevice();
        PVector coord = pdp.worldToPixelCoord(cameraTracking3DCoord);
        return coord;
    }

    /**
     * Get a square of pixels centered at the coord location of radius size.
     * WARNING: Unsafe to use, this will be updated/moved
     *
     * @param coord
     * @param cameraImage
     * @param radius
     * @return
     */
    public int[] getPixelsFrom(PVector coord, PImage cameraImage, int radius) {
        int[] px = new int[radius * radius];
        int x = (int) coord.x;
        int y = (int) coord.y;
        int minX = PApplet.constrain(x - radius, 0, cameraTracking.width() - 1);
        int maxX = PApplet.constrain(x + radius, 0, cameraTracking.width() - 1);
        int minY = PApplet.constrain(y - radius, 0, cameraTracking.height() - 1);
        int maxY = PApplet.constrain(y + radius, 0, cameraTracking.height() - 1);

        int k = 0;
        for (int j = minY; j <= maxY; j++) {
            for (int i = minX; i <= maxX; i++) {
                int offset = i + j * cameraTracking.width();
                px[k++] = cameraImage.pixels[offset];
            }
        }
        return px;
    }

    ///////////////////////////
    // Getters and setters. 
    ///////////////////////////
    /**
     * Enable or disable rendering of this PaperScreen.
     *
     * @param drawing
     */
    public void setDrawing(boolean drawing) {
        this.isDrawing = drawing;
    }

    /**
     *
     * @return the tracked MarkerBoard
     */
    public MarkerBoard getBoard() {
        return markerBoard;
    }

    /**
     *
     * @return drawingSize in millimeters.
     */
    public PVector getDrawingSize() {
        return drawingSize;
    }

    /**
     *
     * @return main camera tracking.
     */
    public Camera getCameraTracking() {
        return cameraTracking;
    }

    /**
     * PaperScreen can be rendered for multiple displays. This method add a new
     * display.
     *
     * @param display
     */
    public void addDisplay(BaseDisplay display) {
        this.displays.add(display);
        display.addPaperScreen(this);
        if (display.hasCamera()) {
            display.getCamera().trackMarkerBoard(markerBoard);
        }
    }

    /**
     *
     * @return the first display
     */
    public BaseDisplay getDisplay() {
        return displays.get(0);
    }

    /**
     *
     * @return list of all the displays
     */
    public ArrayList<BaseDisplay> getDisplays() {
        return displays;
    }

    /**
     *
     * @return the quality of rendering in px/mm.
     */
    public float getResolution() {
        return quality;
    }

    /**
     *
     * @return true when using an offscreen.
     */
    public boolean isIsDrawingOnScreen() {
        return isDrawingOnScreen;
    }

    ////////////////////////////////
    /// Methods to call in settings
    ////////////////////////////////
    /**
     * Load a Markerboard with the given configuration file and size. The
     * configuration file can end with ".svg" for an ARToolKitPlus tracking
     * technique. (faster) The configuration file can end with ".jpg" or ".png"
     * to track "images" using SURF features. (slower)
     *
     * Call from settings();
     *
     * @param configFile svg filename
     * @param width width of the markerboard in millimeters.
     * @param height height of the markerboard in millimeters.
     */
    public void loadMarkerBoard(String configFile, float width, float height) {
        this.markerBoard = MarkerBoardFactory.create(configFile, width, height);

        if (this.markerBoard == MarkerBoardInvalid.board) {
            System.out.println("Cannot create the markerboard, setting it to invalid: " + configFile);
        }
        trackCurrentMarkerBoard();
    }

    /**
     * Assign an existing markerboard to this PaperScreen.
     */
    public void setMarkerBoard(MarkerBoard markerboard) {
        this.markerBoard = markerboard;
        trackCurrentMarkerBoard();
    }

    /**
     * Set the quality (resolution) of the drawing in px/mm . e.g.: A board with
     * 100mm width and 2 resolution will have 200 pixels.
     *
     * Call from settings
     */
    public void setQuality(float quality) {
        this.quality = quality;
    }

    /**
     * Sets the drawing size in millimeters. To get the resolution you must
     * multiply the drawing size by the resolution.
     *
     * Call from settings()
     *
     * @param width in millimeters
     * @param height in millimeters
     */
    public final void setDrawingSize(float width, float height) {
        this.drawingSize.x = width;
        this.drawingSize.y = height;
    }

    /**
     * Draw in the Display, beware of the order of drawing. If you call clear()
     * you may remove rendering from other PaperScreens.
     *
     * Call from settings()
     */
    public void setDrawAroundPaper() {
        this.isDrawingOnScreen = false;
        this.isDrawingOnDisplay = true;
    }

    /**
     * Draw in an offscreen buffer. Ideal for 2D rendering. Easy to use.
     *
     * Call from settings()
     */
    public void setDrawOnPaper() {
        this.isDrawingOnScreen = true;
        this.isDrawingOnDisplay = false;
    }

    /**
     * Alias for setDrawOnPaper(). Call from settings().
     */
    public void setDrawStandard() {
        setDrawOnPaper();
    }

    /**
     * Set the filtering. Filtering has two components, one is the
     * filteringDistance that disables the tracking when the markerboard has not
     * moved much. The second component is a time filter for smooth movements.
     */
    private void updateBoardFiltering() {
        if (filteringDistance != 0) {
            markerBoard.setDrawingMode(cameraTracking, true, filteringDistance);
        } else {
            markerBoard.setDrawingMode(cameraTracking, false, 0);
        }
        if (filteringFreq == 0) {
            markerBoard.removeFiltering(cameraTracking);
        } else {
            markerBoard.setFiltering(cameraTracking, filteringFreq, filteringCutoff);
        }

    }

    /**
     * Disable the tracking if the movement is below the distance distance. Call
     * from settings()
     *
     * @param distance 0 to disable, or any distance in millimeter.
     */
    protected void setDrawingFilter(float distance) {
        if (distance <= 0) {
            distance = 0;
        }
        this.filteringDistance = distance;
    }

    /**
     * Set the filters values for the time filters. Call from settings()
     *
     * @param freq
     * @param cutOff
     */
    protected void setTrackingFilter(float freq, float cutOff) {
        this.filteringFreq = freq;
        this.filteringCutoff = cutOff;
    }

    ///////////////////
    //// VR Rendering 
    ///////////////////
    public float halfEyeDist = 10; // 2cm
    private PMatrix3D initPosM = null;

    ////////////////// 3D SPACE TO PAPER HOMOGRAPHY ///////////////
    // Version 2.0 :  (0,0) is the top-left corner.
    /**
     * Initialize VR rendering.
     *
     * @param cam
     * @param userPos
     */
    public void initDraw(Camera cam, PVector userPos) {
        initDraw(cam, userPos, 40, 5000);
    }

    /**
     * Initialize VR rendering.
     *
     * @param cam
     * @param userPos
     */
    public void initDraw(Camera cam, PVector userPos, float nearPlane, float farPlane) {
        initDraw(cam, userPos, nearPlane, farPlane, false, false);
    }

    /**
     * Init VR rendering. The VR rendering creates a 3D "screen". It is used to
     * create 3D pop-up effects.
     *
     * @param cam Rendering origin.
     * @param userPos Position of the user, relative to the PaperScreen
     * @param nearPlane Close disance for OpengL in millimeters.
     * @param farPlane Far distance for OpenGL in millimeters.
     * @param isAnaglyph Use Anaglyph.
     * @param isLeft When analygph is it left or right, ignored otherwise.
     */
    public void initDraw(Camera cam, PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft) {

        PGraphicsOpenGL graphics = getGraphics();

        if (initPosM == null) {
            this.isOpenGL = true;
            // Transformation  Camera -> Marker

            initPosM = this.getLocation(cam);

            initPosM.translate(this.getRenderingSizeX() / 2, this.getRenderingSizeY() / 2);
            // All is relative to the paper's center. not the corner. 
            initPosM.scale(-1, 1, -1);

        }

        // get the current transformation... 
        PMatrix3D newPos = this.getLocation(cam);

        newPos.translate(this.getRenderingSizeX() / 2, this.getRenderingSizeY() / 2);
        newPos.scale(-1, 1, -1);

        newPos.invert();
        newPos.apply(initPosM);

        PVector user = new PVector();

        if (isAnaglyph && isLeft) {
            userPos.add(-halfEyeDist * 2, 0, 0);
        }
        newPos.mult(userPos, user);
        PVector paperCameraPos = user;

        // Camera must look perpendicular to the screen. 
        graphics.camera(paperCameraPos.x, paperCameraPos.y, paperCameraPos.z,
                paperCameraPos.x, paperCameraPos.y, 0,
                0, 1, 0);

        // http://www.gamedev.net/topic/597564-view-and-projection-matrices-for-vr-window-using-head-tracking/
        float nearFactor = nearPlane / paperCameraPos.z;

        float left = nearFactor * (-drawingSize.x / 2f - paperCameraPos.x);
        float right = nearFactor * (drawingSize.x / 2f - paperCameraPos.x);
        float top = nearFactor * (drawingSize.y / 2f - paperCameraPos.y);
        float bottom = nearFactor * (-drawingSize.y / 2f - paperCameraPos.y);

        graphics.frustum(left, right, bottom, top, nearPlane, farPlane);
        graphics.projection.m11 = -graphics.projection.m11;

        // No detection?
        PMatrix3D transformation = this.getLocation(cam);
        if (transformation.m03 == 0 && transformation.m13 == 0 && transformation.m23 == 0) {
            resetPos();
        }
    }

    public void endDrawPerspective() {
        PGraphicsOpenGL graphics = getGraphics();
        graphics.perspective();
        graphics.camera();
        graphics.projection.m11 = -graphics.projection.m11;
    }

    public void resetPos() {
        initPosM = null;
    }

    public float getHalfEyeDist() {
        return halfEyeDist;
    }

    public void setHalfEyeDist(float halfEyeDist) {
        this.halfEyeDist = halfEyeDist;
    }

}
