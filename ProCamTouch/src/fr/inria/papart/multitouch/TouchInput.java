
import fr.inria.papart.Projector;
import fr.inria.papart.Screen;
import fr.inria.papart.multitouch.TouchElement;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import multitouch.laviole.name.MultiTouchKinect;
import multitouch.laviole.name.TouchPoint;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

/** 
 * Touch input, using a Kinect device for now. 
 * @author jeremylaviole
 */
class TouchInput {

    private boolean isTouch2DActive = false;
    private boolean isTouch3DActive = false;
    private ArrayList<TouchPoint> touchPoints2D, touchPoints3D;
    private int touch2DPrecision, touch3DPrecision;
    private MultiTouchKinect mtk;
    private float touchHeight;

    public TouchInput(PApplet applet, String calibrationFile) {
        this(applet, calibrationFile, 3, 7);
        // TODO: use XML calibration file.
    }

    public TouchInput(PApplet applet, String calibrationFile, int precision2D, int precision3D) {
        mtk = new MultiTouchKinect(applet, calibrationFile);
        this.touch2DPrecision = precision2D;
        this.touch3DPrecision = precision3D;
        touchPoints2D = mtk.getTouchPoint2D();
        touchPoints3D = mtk.getTouchPoint3D();
    }

    void startTouch(int[] depth, int touchHeight) {

        this.touchHeight = touchHeight;
        mtk.updateKinect(depth);
        ArrayList<TouchPoint> touchPoints;

        // This updates the values of touchPoints2D and touchPoints3D
        mtk.find2DTouch(touch2DPrecision);
        mtk.find3DTouch(touch3DPrecision, touchHeight);
    }

    void endTouch() {
        //     try{
        // kinectMutex.acquire();
        //     }catch(Exception e){}

        mtk.touch2DFound();
        mtk.touch3DFound();
    }

    TouchElement projectTouchToScreen(Screen screen, Projector projector, boolean is2D, boolean is3D) {
        return projectTouchToScreen(screen, projector, true, true, false, false);
    }

    TouchElement projectTouchToScreen(Screen screen, Projector projector) {
        return projectTouchToScreen(screen, projector, true, true, true, true);
    }

    TouchElement projectTouchToScreen(Screen screen, Projector projector, 
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

                    ReadonlyVec3D res, res2;
                    res = screen.projectPointer(projector, tp.v.x, tp.v.y);

                    if (isSpeed2D) {
                        res2 = (tp.oldV != null) ? screen.projectPointer(projector, tp.oldV.x, tp.oldV.y) : null;
                    } else {
                        res2 = null;
                    }


                    if (res != null) {
                        Vec3D transfo = screen.applyProjPaper(res);
                        transfo.x /= transfo.z;
                        transfo.y /= transfo.z;

                        // inside the paper sheet 	      
                        if (transfo.x >= 0 && transfo.x <= 1 && transfo.y >= 0 && transfo.y <= 1) {
                            position2D.add(new PVector(transfo.x, transfo.y));
                        }

                        if (res2 != null) {
                            Vec3D transfo2 = screen.applyProjPaper(res2);
                            transfo2.x /= transfo2.z;
                            transfo2.y /= transfo2.z;

                            // inside the paper sheet 	      
                            if (transfo2.x >= 0 && transfo2.x <= 1 && transfo2.y >= 0 && transfo2.y <= 1) {
                                speed2D.add(new PVector(transfo.x - transfo2.x,
                                        transfo.y - transfo2.y));
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

                    ReadonlyVec3D res, res2;
                    res = screen.projectPointer(projector, tp.v.x, tp.v.y);

                    if (isSpeed3D) {
                        res2 = (tp.oldV != null) ? screen.projectPointer(projector, tp.oldV.x, tp.oldV.y) : null;
                    } else {
                        res2 = null;
                    }

                    if (res != null) {
                        Vec3D transfo = screen.applyProjPaper(res);
                        transfo.x /= transfo.z;
                        transfo.y /= transfo.z;

                        // inside the paper sheet 	      
                        if (transfo.x >= 0 && transfo.x <= 1 && transfo.y >= 0 && transfo.y <= 1) {
                            position3D.add(new PVector(transfo.x, transfo.y, 1000 * tp.v.z * touchHeight));
                        }

                        if (res2 != null) {
                            Vec3D transfo2 = screen.applyProjPaper(res2);
                            transfo2.x /= transfo2.z;
                            transfo2.y /= transfo2.z;

                            // inside the paper sheet 	      
                            //			if(transfo2.x >= 0 && transfo2.x <= 1 && transfo2.y >= 0 && transfo2.y <= 1)
                            speed3D.add(new PVector(transfo.x - transfo2.x,
                                    transfo.y - transfo2.y,
                                    1000 * (tp.v.z - tp.oldV.z) * touchHeight));
                        }
                    }

                }
            }
        }


        return elem;
    }
    
}