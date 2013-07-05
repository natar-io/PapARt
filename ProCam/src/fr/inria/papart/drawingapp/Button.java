/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp;

import fr.inria.papart.multitouchKinect.TouchPoint;
import java.util.ArrayList;
import java.util.List;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics3D;
import processing.core.PImage;

public class Button extends InteractiveZone {

<<<<<<< HEAD
    static final int BUTTON_ERROR = 5;
    static final int BUTTON_WIDTH = 40;
    static final int BUTTON_HEIGHT = 20;
    static final int BUTTON_COOLDOWN = 100; // ms
    static final int UNSELECTED = 180;
=======
    static final int BUTTON_ERROR = 2;
    static final int BUTTON_WIDTH = 40;
    static final int BUTTON_HEIGHT = 20;
    static final int BUTTON_COOLDOWN = 100; // ms
    static final int UNSELECTED = 250;
>>>>>>> 55c44c04060b63a864f69d1bcce45783636492f4
    protected static PFont buttonFont;
    protected static int buttonFontSize;
    protected PImage img = null;
    protected PImage imgSel = null;
    protected String name = null;
    protected String nameSel = null;
    private List<ButtonListener> listeners = new ArrayList<ButtonListener>();
<<<<<<< HEAD

    public Button(PImage image, int x, int y, int width, int height) {
        super(x, y, width, height);
//        name = image;
        name = "Button";
        this.img = image;
    }

    public Button(PImage img, int x, int y) {
        this(img, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    
    protected Button(PImage image, String name, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.name = name;
        this.img = image;
    }
    
    // Text only buttons 
    public Button(String name, int x, int y, int width, int height) {
        this(null, name, x, y, width, height);
    }

=======
    public Object attachedObject;
    private int currentButtonFontSize = -1;

    public Button(PImage image, int x, int y, int width, int height) {
        super(x, y, width, height);
//        name = image;
        name = "Button";
        this.img = image;
    }

    public Button(PImage imageUnSel, PImage imageSel, int x, int y, int width, int height) {
        super(x, y, width, height);
//        name = image;
        name = "Button";
        this.img = imageUnSel;
        this.imgSel = imageSel;
    }

    public Button(PImage img, int x, int y) {
        this(img, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public Button(PImage image, String name, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.name = name;
        this.img = image;
    }

    public Button(String name1, int x, int y, int width, int height, String name2) {
        super(x, y, width, height);
        this.name = name1;
        this.nameSel = name2;
    }

    // Text only buttons 
    public Button(String name, int x, int y, int width, int height) {
        this(null, name, x, y, width, height);
    }

>>>>>>> 55c44c04060b63a864f69d1bcce45783636492f4
    public Button(String name, int x, int y) {
        this(null, name, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public void addListener(ButtonListener bl) {
        listeners.add(bl);
    }

    @Override
    public boolean isSelected(float x, float y, TouchPoint tp) {
        if (isHidden) {
            isSelected = false;
            return false;
        }
        if (x == PApplet.constrain(x,
                position.x - (this.width / 2) - BUTTON_ERROR,
                position.x + (this.width / 2) + BUTTON_ERROR)
                && y == PApplet.constrain(y,
                position.y - (this.height / 2) - BUTTON_ERROR,
                position.y + (this.height / 2) + BUTTON_ERROR)) {

//            System.out.println(" x " + x + " min " + (position.x - (this.width / 2) - BUTTON_ERROR));
//            System.out.println(" x " + x + " max " + (position.x + (this.width / 2) + BUTTON_ERROR));

            if (isCooldownDone) {
                isActive = !isActive;
                currentTP = tp;
                isCooldownDone = false;

                if (isActive) {
                    for (ButtonListener bl : listeners) {
                        bl.ButtonPressed();
                    }
                } else {
                    for (ButtonListener bl : listeners) {
                        bl.ButtonReleased();
                    }
                }
            }

            isSelected = true;
            lastPressedTime = DrawUtils.applet.millis();
            return true;
        }

        isSelected = false;

        return false;
    }

    @Override
    public void drawSelf(PGraphics3D pgraphics3d) {

        if (isHidden) {
            return;
        }
        if ((DrawUtils.applet.millis() - lastPressedTime) > BUTTON_COOLDOWN) {
            isCooldownDone = true;
            currentTP = null;
        }

        // if(currentTP != null)
        //     pgraphics3d.tint(0, 153, 204, 126); 


        pgraphics3d.imageMode(PApplet.CENTER);

        if (img != null) {

            if (isActive) {

                if (imgSel != null) {
                    DrawUtils.drawImage(pgraphics3d, imgSel, (int) position.x, (int) position.y, (int) width, (int) height);
                } else {

                    pgraphics3d.tint(DrawUtils.applet.color(100, 255, 100));
                    DrawUtils.drawImage(pgraphics3d, img, (int) position.x, (int) position.y, (int) width, (int) height);
                }

            } else {

                if (imgSel == null) {
                    pgraphics3d.tint(DrawUtils.applet.color(UNSELECTED));
                }
                DrawUtils.drawImage(pgraphics3d, img, (int) position.x, (int) position.y, (int) width, (int) height);
            }
            pgraphics3d.noTint();
        } else {

            int ftSize = this.currentButtonFontSize > 0 ? this.currentButtonFontSize : buttonFontSize;

            if (isActive) {
                pgraphics3d.fill(DrawUtils.applet.color(255));

                if (nameSel != null) {
                    DrawUtils.drawText(pgraphics3d, nameSel, buttonFont, ftSize,
                            (int) position.x, (int) position.y, (int) width, (int) height);
                } else {
                    DrawUtils.drawText(pgraphics3d, name, buttonFont, ftSize,
                            (int) position.x, (int) position.y, (int) width, (int) height);
                }

            } else {
                pgraphics3d.fill(DrawUtils.applet.color(UNSELECTED));
                DrawUtils.drawText(pgraphics3d, name, buttonFont, ftSize,
                        (int) position.x, (int) position.y, (int) width, (int) height);
            }
        }

    }

    public void reset() {
        isActive = false;
        currentTP = null;
        isSelected = false;
        lastPressedTime = DrawUtils.applet.millis();
        isCooldownDone = false;
    }

    public void cooldown() { // TODO ???
        lastPressedTime = DrawUtils.applet.millis();
        isCooldownDone = false;
    }

    public PImage getImage() {
        return img;
    }

    public String getName() {
        return this.name;
    }

    public void setButtonFontSize(int size) {
        this.currentButtonFontSize = size;
    }

    static public void setFont(PFont font) {
        buttonFont = font;
    }

    static public void setFontSize(int size) {
        buttonFontSize = size;
    }
}