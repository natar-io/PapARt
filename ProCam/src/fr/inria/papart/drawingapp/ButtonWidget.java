/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp;

import fr.inria.papart.multitouchKinect.TouchPoint;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PImage;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class ButtonWidget extends Button {

    protected PVector absPos;
    protected PVector prevPos;
    protected int BUTTON_ERROR_2 = 40;
    protected static final float PRECISE_SPEED = 0.15f;
    private static ButtonWidget currentSelected = null;

    public ButtonWidget(PImage image, PVector abs, int x, int y, int width, int height) {
        super(image, x, y, width, height);
        absPos = abs;
    }

    public ButtonWidget(PImage image, PVector abs, int x, int y) {
        super(image, x, y);
        absPos = abs;
    }
    
    public ButtonWidget(String name, PVector abs, int x, int y, int width, int height) {
        super(name, x, y, width, height);
        absPos = abs;
    }

    public ButtonWidget(String name, PVector abs, int x, int y) {
        super(name, x, y);
        absPos = abs;
    }


    @Override
    public void drawSelf(PGraphicsOpenGL pgraphics3d) {
        if (isHidden) {
            return;
        }

        // FIXME: removal of DrawingApp class, what happens to this ?
//        if (DrawingApp.preciseWidget == this) {
//
////            System.out.println(pgraphics3d + "  I am precise Widget at : " + position.x + "  " + position.y + " from : " + absPos.x + " " + absPos.y);
//            pgraphics3d.imageMode(PApplet.CENTER);
////            pgraphics3d.ellipse((int) (position.x + absPos.x), (int) (position.y + absPos.y), (int) width * 0.2f, (int) height * 0.2f);
//
//            DrawUtils.drawImage(pgraphics3d, img, (int) (position.x + absPos.x), (int) (position.y + absPos.y), (int) width, (int) height);
//            return;
//        }
//
//
//        if (DrawingApp.currentIZ == this) {
//            //        DrawingApp.currentIZ != null && DrawingApp.currentIZ != this;
//            ButtonWidget p = DrawingApp.preciseWidget;
//            PVector old = DrawingApp.preciseWidgetInitPos;
//            position.x += PRECISE_SPEED * (p.position.x - old.x);
//            position.y += PRECISE_SPEED * (p.position.y - old.y);
//            old.x = p.position.x;
//            old.y = p.position.y;
////            pgraphics3d.ellipse((int) (position.x + absPos.x), (int) (position.y + absPos.y), (int) width, (int) height);
//        }


        if ((DrawUtils.applet.millis() - lastPressedTime) > BUTTON_COOLDOWN) {
            isCooldownDone = true;
//            currentTP = null;
        }

        if (img != null) {
            pgraphics3d.imageMode(PApplet.CENTER);
            if (isActive) {
                DrawUtils.drawImage(pgraphics3d, img, (int) (position.x + absPos.x), (int) (position.y + absPos.y), (int) width, (int) height);
            } else {
                pgraphics3d.tint(DrawUtils.applet.color(UNSELECTED));
                DrawUtils.drawImage(pgraphics3d, img, (int) (position.x + absPos.x), (int) (position.y + absPos.y), (int) width, (int) height);
                pgraphics3d.noTint();
            }
        } else {
            if (isActive) {
                pgraphics3d.tint(DrawUtils.applet.color(255));
            } else {
                pgraphics3d.tint(DrawUtils.applet.color(UNSELECTED));
            }

            DrawUtils.drawText(pgraphics3d, "   " + name, buttonFont, (int) (position.x + absPos.x), (int) (position.y + absPos.y)); //, (int) width, (int) height);
            pgraphics3d.noTint();
        }
    }

    public PVector getAbsPos() {
        return absPos;
    }
}
