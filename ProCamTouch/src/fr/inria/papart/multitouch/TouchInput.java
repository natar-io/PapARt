package fr.inria.papart.multitouch;

import fr.inria.papart.multitouchKinect.MultiTouchKinect;
import fr.inria.papart.multitouchKinect.TouchPoint;
import fr.inria.papart.Projector;
import fr.inria.papart.Screen;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

/**
 * Touch input, using a Kinect device for now.
 *
 * @author jeremylaviole
 */
public class TouchInput {

    private boolean isTouch2DActive = false;
    private boolean isTouch3DActive = false;
    private ArrayList<TouchPoint> touchPoints2D, touchPoints3D;
    private int touch2DPrecision, touch3DPrecision;
    private MultiTouchKinect mtk;
    private float touchHeight;

    public TouchInput(PApplet applet, String calibrationFile) {
        this(applet, calibrationFile, 1, 8);
        // TODO: use XML calibration file.
    }

    public TouchInput(PApplet applet, String calibrationFile, int precision2D, int precision3D) {
        mtk = new MultiTouchKinect(applet, calibrationFile);
        this.touch2DPrecision = precision2D;
        this.touch3DPrecision = precision3D;
        touchPoints2D = mtk.getTouchPoint2D();
        touchPoints3D = mtk.getTouchPoint3D();
    }

    public void startTouch(int[] depth, float touchHeight) {
        this.touchHeight = touchHeight;
        mtk.updateKinect(depth);

        // This updates the values of touchPoints2D and touchPoints3D
        mtk.find2DTouch(touch2DPrecision);
        mtk.find3DTouch(touch3DPrecision, touchHeight);
    }

    public void endTouch() {
        //     try{
        // kinectMutex.acquire();
        //     }catch(Exception e){}

        mtk.touch2DFound();
        mtk.touch3DFound();
    }

    public ArrayList<TouchPoint> getTouchPoints2D() {
        return touchPoints2D;
    }

    public ArrayList<TouchPoint> getTouchPoints3D() {
        return touchPoints3D;
    }

    public TouchElement projectTouchToScreen(Screen screen, Projector projector) {
        return projectTouchToScreen(screen, projector, true, true, true, true);
    }

    public TouchElement projectTouchToScreen(Screen screen, Projector projector, boolean is2D, boolean is3D) {
        return projectTouchToScreen(screen, projector, is2D, is3D, false, false);
    }

    public TouchElement projectTouchToScreen(Screen screen, Projector projector,
            boolean is2D, boolean is3D,
            boolean isSpeed2D, boolean isSpeed3D) {

        ArrayList<PVector> position2D = null, position3D = null, speed2D = null, speed3D = null;

        if (isSpeed2D) {
            is2D = true;
        }
        if (isSpeed3D) {
            is3D = true;
        }

        if (is2D) {
            position2D = new ArrayList<PVector>();
        }
        if (is3D) {
            position3D = new ArrayList<PVector>();
        }
        if (isSpeed2D) {
            speed2D = new ArrayList<PVector>();
        }
        if (isSpeed3D) {
            speed3D = new ArrayList<PVector>();
        }

        // TODO: version without all these allocations...
        TouchElement elem = new TouchElement();
        elem.position2D = position2D;
        elem.position3D = position3D;
        elem.speed2D = speed2D;
        elem.speed3D = speed3D;

        if (is2D && !touchPoints2D.isEmpty()) {
            for (TouchPoint tp : touchPoints2D) {

                // TODO: change this to get outside points ? 
                // Inside the window
                if (tp.v.x >= 0 && tp.v.x < 1
                        && tp.v.y >= 0 && tp.v.y < 1) {

                    PVector res, res2;
                    res = projector.projectPointer(screen, tp.v.x, tp.v.y);

                    if (isSpeed2D) {
                        res2 = (tp.oldV != null) ? projector.projectPointer(screen, tp.oldV.x, tp.oldV.y) : null;
                    } else {
                        res2 = null;
                    }

                    if (res != null) {

                        // inside the paper sheet 	      
                        if (res.x >= 0 && res.x <= 1 && res.y >= 0 && res.y <= 1) {
                            position2D.add(new PVector(res.x, res.y));
//                            position2D.add(new PVector(res.x * screen.getSize().x * screen.getScale(),
//                                        res.y* screen.getSize().y * screen.getScale()));
                        }

                        if (res2 != null) {
                            // inside the paper sheet 	      
                            if (res2.x >= 0 && res2.x <= 1 && res2.y >= 0 && res2.y <= 1) {
                                speed2D.add(new PVector(res.x - res2.x,
                                        res.y - res2.y));
                            }
                        }
                    }
                }

            }
        }

        if (is3D && !touchPoints3D.isEmpty()) {
            for (TouchPoint tp : touchPoints3D) {

                // TODO: inside necessary ??
                // Inside the window
                if (tp.v.x >= 0 && tp.v.x < 1
                        && tp.v.y >= 0 && tp.v.y < 1) {

                    PVector res, res2;
                    res = projector.projectPointer(screen, tp.v.x, tp.v.y);
//                    res = projector.projectPointer(screen, tp);
                    res.z = tp.v.z;

                    if (isSpeed3D && tp.oldV != null) {
                        res2 = projector.projectPointer(screen, tp.oldV.x, tp.oldV.y);
                        res2.z = tp.oldV.z;
//                        res2 = (tp.oldV != null) ? projector.projectPointer(screen, tp) : null;
                    } else {
                        res2 = null;
                    }

                    if (res != null) {

                        // inside the paper sheet 	      
                        if (res.x >= 0 && res.x <= 1 && res.y >= 0 && res.y <= 1) {
                            position3D.add(new PVector(res.x, res.y, tp.v.z));
                        }

                        if (res2 != null) {

                            // inside the paper sheet 	      
                            //			if(res2.x >= 0 && res2.x <= 1 && res2.y >= 0 && res2.y <= 1)
                            speed3D.add(new PVector(res.x - res2.x,
                                    res.y - res2.y,
                                    (tp.v.z - tp.oldV.z)));
                        }
                    }

                }
            }
        }


        return elem;
    }
}