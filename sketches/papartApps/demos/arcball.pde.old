/** By Martin Prout */


public  int XAXIS = 0;
public  int YAXIS = 1;
public  int ZAXIS = 2;
public  int FREE = -1;


import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import processing.core.PApplet;
import processing.core.PVector;

class ArcBall {

    private float center_x;
    private float center_y;
    private float radius;
    private PVector v_down;
    private PVector v_drag;
    private Quat q_now;
    private Quat q_down;
    private Quat q_drag;
    private PVector[] axisSet;
    private int axis;
    //    private final PApplet parent;

    /**
     *
     * @param parent
     * @param center_x
     * @param center_y
     * @param radius
     */
    public ArcBall(float center_x, float center_y, float radius) {
	//        this.parent = parent;
        // this.parent.registerMouseEvent(this);
        // this.parent.registerKeyEvent(this);
        this.center_x = center_x;
        this.center_y = center_y;
        this.radius = radius;
        this.v_down = new PVector();
        this.v_drag = new PVector();
        this.q_now = new Quat();
        this.q_down = new Quat();
        this.q_drag = new Quat();
        this.axisSet = new PVector[]{new PVector(1.0F, 0.0F, 0.0F), new PVector(0.0F, 1.0F, 0.0F), new PVector(0.0F, 0.0F, 1.0F)};
        axis = FREE; // no constraints...
    }

    /**
     * Default centered arcball and half width
     *
     * @param parent
     */
    public ArcBall(float width, float height) {
        // this.parent = parent;
        // this.parent.registerMouseEvent(this);
        // this.parent.registerKeyEvent(this);
        this.center_x = width * 0.5F;
        this.center_y = height * 0.5F;
        this.radius = width * 0.5F;
        this.v_down = new PVector();
        this.v_drag = new PVector();
        this.q_now = new Quat();
        this.q_down = new Quat();
        this.q_drag = new Quat();
        this.axisSet = new PVector[]{new PVector(1.0F, 0.0F, 0.0F), new PVector(0.0F, 1.0F, 0.0F), new PVector(0.0F, 0.0F, 1.0F)};
        axis = FREE; 
    }


    /**
     * mouse event to register
     * @param e
     */
    public void mouseEvent(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        switch (e.getID()) {
            case (MouseEvent.MOUSE_PRESSED):
                v_down = mouse2sphere(x, y);
                q_down.set(q_now);
                q_drag.reset();
                break;
            case (MouseEvent.MOUSE_DRAGGED):
                v_drag = mouse2sphere(x, y);
                q_drag.set(PVector.dot(v_down, v_drag), v_down.cross(v_drag));
                break;
            default:
        }
    }

    public void customEvent(float x, float y, boolean isNew){

	// Pressed required ?!

	if(isNew){
	    v_down = mouse2sphere(x, y);
	    q_down.set(q_now);
	    q_drag.reset();
	} else {
	    v_drag = mouse2sphere(x, y);
	    q_drag.set(PVector.dot(v_down, v_drag), v_down.cross(v_drag));
	}

    }

    /**
     * key event to register
     * @param e
     */
    public void keyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            switch (e.getKeyChar()) {
                case 'x':
                    constrain(XAXIS);
                    break;
                case 'y':
                    constrain(YAXIS);
                    break;
                case 'z':
                    constrain(ZAXIS);
                    break;
            }
        }
        if (e.getID() == KeyEvent.KEY_RELEASED) {
            constrain(FREE);
        }
    }


    /**
     * Needed to call this in sketch
     */
    public void update(PGraphicsOpenGL g) {
        q_now = Quat.mult(q_drag, q_down);
        applyQuat2Matrix(q_now, g);
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public PVector mouse2sphere(float x, float y) {
        PVector v = new PVector();
        v.x = (x - center_x) / radius;
        v.y = (y - center_y) / radius;
        float mag = v.x * v.x + v.y * v.y;
        if (mag > 1.0F) {
            v.normalize();
        } else {
            v.z = (float)Math.sqrt(1.0 - mag);
        }
        return (axis == FREE) ? v : constrainVector(v, axisSet[axis]);
    }

    /**
     *
     * @param vector
     * @param axis
     * @return
     */
    public PVector constrainVector(PVector vector, PVector axis) {
        PVector res = PVector.sub(vector, PVector.mult(axis, PVector.dot(axis, vector)));
        res.normalize();
        return res;
    }

    /**
     *
     * @param axis
     */
    public void constrain(int axis) {
        this.axis = axis;
    }

    /**
     *
     * @param q
     */
    public void applyQuat2Matrix(Quat q, PGraphicsOpenGL g) {
        // instead of transforming q into a matrix and applying it...
        float[] aa = q.getValue();
        g.rotate(aa[0], aa[1], aa[2], aa[3]);
    }
}

