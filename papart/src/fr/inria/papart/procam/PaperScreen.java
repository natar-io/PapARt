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

import fr.inria.papart.tracking.MarkerBoardFactory;
import fr.inria.papart.tracking.MarkerBoardInvalid;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.tracking.ObjectFinder;
import java.util.ArrayList;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.event.KeyEvent;

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
    protected MarkerBoard markerBoard = MarkerBoardInvalid.board;
    protected Screen screen;

    protected PVector drawingSize
            = new PVector(DEFAULT_DRAWING_SIZE, DEFAULT_DRAWING_SIZE, 1);
    protected float quality = DEFAULT_RESOLUTION;

    protected boolean isDrawingOnScreen = true;
    protected boolean isDrawingOnDisplay = false;

    private boolean isInitialized = false;
    private boolean isRegistered = false;
    protected boolean isWithoutCamera = false;
    protected boolean useManualLocation = false;

    private float filteringDistance = 30;
    private float filteringFreq = 30;
    private float filteringCutoff = 4;

    private PMatrix3D manualLocation;

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

            initScreen();
            linkMarkerBoardToScreen();

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
                this.currentGraphics = screen.getGraphics();
                setup();
                tryInitTracking();
            }

            isInitialized = true;
        }

        // Needed for touch and projection operations
        screen.computeScreenPosTransform(cameraTracking);

//        assert (isInitialized);
//        if (this.isWithoutCamera || useManualLocation) {
//            return;
//        }
//        checkCorners();
    }

    private void initScreen() {
        this.screen = new Screen(parent);

        if (this.isDrawingOnScreen) {
            for (BaseDisplay display : displays) {
                display.addScreen(screen);
            }
        }

        // resolution and drawingSize are set in settings() now...
        this.screen.setScale(quality);
        this.screen.setSize(drawingSize);
    }

    private void linkMarkerBoardToScreen() {
        this.screen.linkTo(markerBoard);
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
            return;
        }

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

    private String loadKey = null, saveKey = null, trackKey = null;
    private String saveName = null;

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

    /**
     * Events coming from Processing to handle keys.
     *
     * @param e
     */
    public void keyEvent(KeyEvent e) {

        String filename = "paper-id.xml";

        if (e.isAltDown() && e.getAction() == KeyEvent.PRESS) {

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
            screen.setDrawing(true);
            PGraphicsOpenGL g = screen.getGraphics();
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
                PGraphicsOpenGL g = display.beginDrawOnScreen(this.screen);
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
        this.useManualLocation = manual;

        if (manual) {
            if (loc == null) {
//                System.err.println("Cannot set a null location.");
                loc = this.screen.getLocation(cameraTracking);
            }
            this.screen.useManualLocation(loc);
            this.manualLocation = loc.get();
        }
        if (!manual) {
            this.screen.useTracking();
            this.manualLocation = new PMatrix3D();
        }
        // TEST instead of blocking the update, just skip the use of the tracking.
//        if (this.useManualLocation) {
//            markerBoard.blockUpdate(cameraTracking, 10 * 60 * 60 * 1000); // ms
//        } else {
//            markerBoard.blockUpdate(cameraTracking, 0); // ms
//        }
    }

    /**
     * Get the 3D position of the screen.
     *
     * @return the bottom right corner of the markerboard (tracked position).
     */
    public PVector getScreenPos() {

        if (this.isWithoutCamera) {
            PMatrix3D mat = screen.getExtrinsics();
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
        screen.setDrawing(false);
        PGraphicsOpenGL pg = screen.getGraphics();
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
        PVector p = getDisplay().project(getScreen(),
                (float) parent.mouseX / (float) parent.width,
                (float) parent.mouseY / (float) parent.height);
        p.x = p.x * drawingSize.x;
        p.y = p.y * drawingSize.y;
        t.setPosition(p);
        return t;
    }

    public PGraphicsOpenGL getGraphics() {
        return currentGraphics;
    }

    public Screen getScreen() {
        return this.screen;
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
     * Add a vector to the tracked location.
     *
     * @param v in millimeters
     */
    public void setLocation(PVector v) {
        setLocation(v.x, v.y, v.z);
    }

    /**
     * Add a vector from the tracked location.
     *
     * @param x in millimeters
     * @param y in millimeters
     * @param z in millimeters
     */
    public void setLocation(float x, float y, float z) {
        screen.setTranslation(x, y, z);
    }

    /**
     * Add another transformation from the current location.
     *
     * @param matrix
     */
    public void setLocation(PMatrix3D matrix) {
        assert (isInitialized);
        screen.setTransformation(matrix);
    }

    public PVector getLocationVector() {
        PMatrix3D p;
        if (this.useManualLocation) {
            p = this.manualLocation;
        } else {
            p = screen.getLocation(cameraTracking);
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
        return this.screen.getLocation(cameraTracking);
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
                screen.getMainLocation(cameraTracking),
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

    ///////////////////////////
    // Getters and setters. 
    ///////////////////////////
    /**
     * Enable or disable rendering of this PaperScreen.
     *
     * @param drawing
     */
    public void setDrawing(boolean drawing) {
        screen.setDrawing(drawing);
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
        display.addScreen(this.screen);
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
        trackCurrentMarkerBoard();
    }

    /**
     * Assign an existing markerboard to this PaperScreen.
     */
    public void setMarkerBoard(MarkerBoard markerboard) {
        this.markerBoard = markerboard;
        linkMarkerBoardToScreen();
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
        markerBoard.setFiltering(cameraTracking, filteringFreq, filteringCutoff);
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

}
