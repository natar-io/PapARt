/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.drawingapp;

import fr.inria.papart.multitouch.TouchPoint;
import java.util.ArrayList;
import java.util.List;
import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PImage;

// TODO: Button -- Center MODE || CORNER mode 
public class Button extends InteractiveZone {

    static final int BUTTON_ERROR = 2;
    static final int BUTTON_WIDTH = 40;
    static final int BUTTON_HEIGHT = 20;
    static final int BUTTON_COOLDOWN = 100; // ms
    static final int UNSELECTED = 250;
    protected static PFont buttonFont;
    protected static int buttonFontSize;
    protected PImage img = null;
    protected PImage imgSel = null;
    protected String name = null;
    protected String nameSel = null;
    private List<ButtonListener> listeners = new ArrayList<ButtonListener>();
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

    public Button(String name, int x, int y) {
        this(null, name, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public void addListener(ButtonListener bl) {
        listeners.add(bl);
    }

    @Override
    public boolean isSelected(float x, float y, TouchPoint tp) {

        if (isHidden) {
            System.err.println("Error : inactive Button updated ");
            this.setNotTouched();
            return false;
        }
        if (x == PApplet.constrain(x,
                position.x - (this.width / 2) - BUTTON_ERROR,
                position.x + (this.width / 2) + BUTTON_ERROR)
                && y == PApplet.constrain(y,
                        position.y - (this.height / 2) - BUTTON_ERROR,
                        position.y + (this.height / 2) + BUTTON_ERROR)) {

            setTouched();

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

            lastPressedTime = DrawUtils.applet.millis();

            return true;
        }

        return false;
    }

    private PGraphicsOpenGL g;

    @Override
    public void drawSelf(PGraphicsOpenGL g) {

        this.g = g;

        if (isHidden) {
            return;
        }

        checkCooldown();

        // TODO: remove this null check
        if (img != null) {
            drawImage();
        } else {
            drawText();
        }
    }

    private void drawText() {
        int fontSize = this.currentButtonFontSize > 0 ? this.currentButtonFontSize : buttonFontSize;
        g.rectMode(CENTER);
        if (isActive) {
            drawTextActive(fontSize);
        } else {
            drawTextInactive(fontSize);
        }
    }

    private void drawTextActive(int fontSize) {
        if (nameSel != null) {
            DrawUtils.drawText(g, nameSel, buttonFont, fontSize,
                    (int) position.x, (int) position.y, (int) width, (int) height);
        } else {
            g.fill(isSelected ? 255 : 0, isActive ? 255 : 0, 0);
            
            g.textFont(buttonFont, fontSize);
            g.text(name, (int) position.x, (int) position.y, (int) width, (int) height);
            
            g.noFill();
            g.stroke(180);
            g.strokeWeight(2);
            g.rect((int) position.x, (int) position.y, (int) width, (int) height);
            g.fill(255);
        }
    }

    private void drawTextInactive(int fontSize) {
        g.fill(DrawUtils.applet.color(UNSELECTED));
        DrawUtils.drawText(g, name, buttonFont, fontSize,
                (int) position.x, (int) position.y, (int) width, (int) height);
    }

    private void drawImage() {
        g.imageMode(PApplet.CENTER);
        if (isActive) {
            drawImageActive();
        } else {
            drawImageInactive();
        }
    }

    private void drawImageInactive() {
        if (imgSel == null) {
            g.tint(DrawUtils.applet.color(UNSELECTED));
        }
        g.image(img, (int) position.x, (int) position.y, (int) width, (int) height);
        g.noTint();
    }

    private void drawImageActive() {
        if (imgSel != null) {
            g.image(imgSel, (int) position.x, (int) position.y, (int) width, (int) height);
        } else {

            g.tint(DrawUtils.applet.color(100, 255, 100));
            g.image(img, (int) position.x, (int) position.y, (int) width, (int) height);
            g.noTint();
        }
    }

    protected void checkCooldown() {
        if ((DrawUtils.applet.millis() - lastPressedTime) > BUTTON_COOLDOWN) {
            setNotTouched();
            isCooldownDone = true;
            currentTP = null;
        }
    }

    public void reset() {
        isActive = false;
        currentTP = null;
        setNotTouched();
        lastPressedTime = DrawUtils.applet.millis();
        isCooldownDone = false;
    }

    public void setActive() {
        isActive = true;
    }

    public boolean isActive() {
        return this.isActive;
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

    static public PFont getFont() {
        return buttonFont;
    }

    static public void setFontSize(int size) {
        buttonFontSize = size;
    }
}
