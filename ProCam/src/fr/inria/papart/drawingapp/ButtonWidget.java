/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp;

import fr.inria.papart.multitouchKinect.TouchPoint;
import processing.core.PApplet;
import processing.core.PGraphics3D;
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

    public ButtonWidget(String image, PVector abs, int x, int y, int width, int height) {
        super(image, x, y, width, height);
        absPos = abs;
    }

    public ButtonWidget(String image, PVector abs, int x, int y) {
        super(image, x, y);
        absPos = abs;
    }

    @Override
    public boolean isSelected(float x, float y, TouchPoint tp) {
        if (isHidden) {
            return false;
        }

//        DrawingApp.currentIZ != null && DrawingApp.currentIZ != this;
        if (DrawingApp.currentIZ != null) {
            if (DrawingApp.currentIZ == this) {

//             System.out.println("Close ?" + tp.isCloseToPlane());
//                ButtonWidget p = DrawingApp.preciseWidget;
//                PVector old = DrawingApp.preciseWidgetInitPos;
//                position.x += PRECISE_SPEED * (p.position.x - old.x);
//                position.y += PRECISE_SPEED * (p.position.y - old.y);
//                old.x = p.position.x;
//                old.y = p.position.y;
                return false;
            }
            if (this != DrawingApp.preciseWidget) {
                return false;
            }
        }


        if (x == PApplet.constrain(x, position.x + absPos.x - (BUTTON_WIDTH / 2) - BUTTON_ERROR_2, position.x + absPos.x + (BUTTON_WIDTH / 2) + BUTTON_ERROR_2)
                && y == PApplet.constrain(y, position.y + absPos.y - (BUTTON_HEIGHT / 2) - BUTTON_ERROR_2, position.y + absPos.y + (BUTTON_HEIGHT / 2) + BUTTON_ERROR_2)) {


            if (currentSelected == null) {
                currentSelected = this;
            }

            if (DrawingApp.isSearchingIZ) {
                DrawingApp.isSearchingIZ = false;
                DrawingApp.preciseWidget.position.x = position.x;
                DrawingApp.preciseWidget.position.y = position.y;
                DrawingApp.preciseWidget.absPos.x = absPos.x;
                DrawingApp.preciseWidget.absPos.y = absPos.y;
                DrawingApp.preciseWidget.show();

                DrawingApp.preciseWidgetInitPos = new PVector(position.x, position.y);
                DrawingApp.currentIZ = this;
                System.out.println("Psecise Widget selectionned");
                return true;
            }

//            if (currentSelected == this) {
                position.x = x - absPos.x;
                position.y = y - absPos.y;
//            }
            
            return true;
        }
//        else {
//            if (currentSelected == this) {
//                currentSelected = null;
//            }
//        }

        return false;

    }

    @Override
    public void drawSelf(PGraphics3D pgraphics3d) {
        if (isHidden) {
            return;
        }

        if (DrawingApp.preciseWidget == this) {

//            System.out.println(pgraphics3d + "  I am precise Widget at : " + position.x + "  " + position.y + " from : " + absPos.x + " " + absPos.y);
            pgraphics3d.imageMode(PApplet.CENTER);
//            pgraphics3d.ellipse((int) (position.x + absPos.x), (int) (position.y + absPos.y), (int) width * 0.2f, (int) height * 0.2f);

            DrawUtils.drawImage(pgraphics3d, img, (int) (position.x + absPos.x), (int) (position.y + absPos.y), (int) width, (int) height);
            return;
        }


        if (DrawingApp.currentIZ == this) {
            //        DrawingApp.currentIZ != null && DrawingApp.currentIZ != this;
            ButtonWidget p = DrawingApp.preciseWidget;
            PVector old = DrawingApp.preciseWidgetInitPos;
            position.x += PRECISE_SPEED * (p.position.x - old.x);
            position.y += PRECISE_SPEED * (p.position.y - old.y);
            old.x = p.position.x;
            old.y = p.position.y;
//            pgraphics3d.ellipse((int) (position.x + absPos.x), (int) (position.y + absPos.y), (int) width, (int) height);
        }


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
