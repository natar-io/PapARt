/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp;

import fr.inria.papart.drawingapp.shape.Arc;
import fr.inria.papart.drawingapp.shape.Bezier;
import fr.inria.papart.drawingapp.shape.Curve;
import fr.inria.papart.drawingapp.shape.Ellipse;
import fr.inria.papart.drawingapp.shape.Line;
import fr.inria.papart.drawingapp.shape.Quad;
import fr.inria.papart.drawingapp.shape.Rectangle;
import fr.inria.papart.drawingapp.shape.Shape;
import fr.inria.papart.drawingapp.shape.Triangle;
import java.util.ArrayList;
import fr.inria.papart.multitouchKinect.TouchPoint;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics3D;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class Interface {

    public DrawMode currentMode = DrawMode.NO_MODE;
    public PFont interfaceFont, paperFont;
    public InteractiveZone iz;
    public Slider slider1, imageIntensity;
    public ArrayList<InteractiveZone> interfaceZones = new ArrayList<InteractiveZone>();
    public ArrayList<Drawable> interfaceDrawables = new ArrayList<Drawable>();
    public boolean interfaceLocked = false;
    public Button gridButton, gridSliderActivator;
    public Button lockInterface1, lockInterface2;
    public Button showImage;
    public Button preciseEdit, validate, translate, okButton;
    public Button selectLast, cancel;
    public Button[] shapes = new Button[6];
    public Button newShape, Arc, Bezier, Curve, Ellipse, Line, Quad, Rectangle, Triangle;
    public Button clearAll, hideAll;
    PaperSheet paperSheet;
    public ArrayList<Shape> allShapes = new ArrayList<Shape>();
    private static final int SHAPE_BUTTON_SIZE = 35;
    public String statusText = "";
    public boolean isInterfaceLocked = false;

    public void initInterface(PApplet parent, PaperSheet paperSheet) {

        DrawUtils.applet = parent;
        this.paperSheet = paperSheet;
        interfaceFont = parent.loadFont("CenturySchL-Bold-12.vlw");
        paperFont = parent.loadFont("CenturySchL-Bold-35.vlw");

        showImage = new Button("image.jpg", 350, 300);
        clearAll = new Button("x clear everything", 220, 350);

        gridSliderActivator = new Button("Scale", - 50, 40);
        slider1 = new Slider(120, -20, 20, 20, gridSliderActivator);
        imageIntensity = new Slider(200, 230, 20, 20, showImage);

        validate = new Button("valid.png", 270, 260);
        translate = new Button("o Translate", 340, 160);
        preciseEdit = new Button("o edit Precise", 340, 80);
        okButton = new Button("OK", 340, 130);

        hideAll = new Button("Hide", 100, 350);
        lockInterface1 = new ActiveZone("lock.png", 400, 200, 20, 20);
        lockInterface2 = new ActiveZone("lock.png", 400, 100, 20, 20);

        selectLast = new Button("last", 300, 230);
        cancel = new Button("cancel", 0, 50);

//        newShape = new Button("new.png", 870, 230);
//        gridButton = new Button("grid.png", 880, 300);
        gridButton = new Button("grid.png", 280, 300);
        newShape = new Button("new.png", 270, 230);

        shapes[0] = new Button("line.png", 10, 10, SHAPE_BUTTON_SIZE, SHAPE_BUTTON_SIZE);
        shapes[1] = new Button("triangle.png", 10, 10, SHAPE_BUTTON_SIZE, SHAPE_BUTTON_SIZE);
        shapes[2] = new Button("rectangle.png", 10, 10, SHAPE_BUTTON_SIZE, SHAPE_BUTTON_SIZE);
        shapes[3] = new Button("quad.png", 10, 10, SHAPE_BUTTON_SIZE, SHAPE_BUTTON_SIZE);
        shapes[4] = new Button("ellipse.png", 10, 10, SHAPE_BUTTON_SIZE, SHAPE_BUTTON_SIZE);
//        shapes[5] = new Button("curve.png", 10, 10, SHAPE_BUTTON_SIZE, SHAPE_BUTTON_SIZE);
        shapes[5] = new Button("bezier.png", 10, 10, SHAPE_BUTTON_SIZE, SHAPE_BUTTON_SIZE);
//        shapes[7] = new Button("arc.png", 10, 10, SHAPE_BUTTON_SIZE, SHAPE_BUTTON_SIZE);

        for (int i = 0; i < shapes.length; i++) {
            shapes[i].position.x = 130 + 1.2f * i * SHAPE_BUTTON_SIZE;
            shapes[i].position.y = 130;
            shapes[i].hide();
            addInteractiveZone(shapes[i]);
        }


        cancel.hide();
        selectLast.hide();
        newShape.show();
        gridButton.show();
        showImage.show();
        clearAll.show();
        validate.hide();
        preciseEdit.hide();
        translate.hide();
        slider1.hide();
        gridSliderActivator.hide();
        okButton.hide();
        hideAll.show();
        imageIntensity.hide();

        DrawingApp.preciseWidget.hide();
        DrawingApp.drawables.add(DrawingApp.preciseWidget);
        DrawingApp.zones.add(DrawingApp.preciseWidget);

        addInteractiveZone(imageIntensity);
        addInteractiveZone(lockInterface1);
        addInteractiveZone(lockInterface2);
        addInteractiveZone(gridButton);
        addInteractiveZone(showImage);
        addInteractiveZone(gridSliderActivator);
        addInteractiveZone(slider1);
        addInteractiveZone(preciseEdit);
        addInteractiveZone(validate);
        addInteractiveZone(translate);
        addInteractiveZone(newShape);
        addInteractiveZone(clearAll);
        addInteractiveZone(okButton);
        addInteractiveZone(cancel);
        addInteractiveZone(selectLast);

        ///////////////// TEMP SHAPE TESTS ... ////////////////////
//        Bezier bezier1 = new Bezier(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//        DrawingApp.drawables.add(bezier1);
//        bezier1.show();
//        DrawingApp.selectShape(bezier1);


//        Ellipse ellipse1 = new Ellipse(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//        DrawingApp.drawables.add(ellipse1);
//        ellipse1.show();
//        DrawingApp.selectShape(ellipse1);


//        Triangle triangle1 = new Triangle(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//        DrawingApp.drawables.add(triangle1);
//        triangle1.show();
//        DrawingApp.selectShape(triangle1);


//        Quad Quad1 = new Quad(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//        DrawingApp.drawables.add(Quad1);
//        Quad1.show();
//        DrawingApp.selectShape(Quad1);

//        Line Line1 = new Line(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//        DrawingApp.drawables.add(Line1);
//        Line1.show();
//        DrawingApp.selectShape(Line1);

//        Curve Curve1 = new Curve(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//        DrawingApp.drawables.add(Curve1);
//        Curve1.show();
//        DrawingApp.selectShape(Curve1);

//        Rectangle Rectangle1 = new Rectangle(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//        DrawingApp.drawables.add(Rectangle1);
//        Rectangle1.show();
//        DrawingApp.selectShape(Rectangle1);


//        Arc Arc1 = new Arc(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//        DrawingApp.drawables.add(Arc1);
//        Arc1.show();
//        DrawingApp.selectShape(Arc1);


    }

    public void addInteractiveZone(InteractiveZone z) {
        interfaceZones.add(z);
        interfaceDrawables.add(z);
    }
    private Shape lastShape = null;
    private DrawMode newMode = DrawMode.NO_MODE;
    private int lastLock = 0;
    ArrayList<InteractiveZone> currentInterface = new ArrayList<InteractiveZone>();

    public void drawInterface(PGraphics3D graphics) {

        Button.setFont(interfaceFont);

        //        System.out.println("Current Mode " + currentMode);
//        if (currentMode == DrawMode.NO_MODE) {
//            statusText = "";
//        }

        if (lockInterface1.isSelected
                && lockInterface2.isSelected
                && DrawUtils.applet.millis() - lastLock > 1000) {
            isInterfaceLocked = !isInterfaceLocked;

            if (isInterfaceLocked) {
                for (InteractiveZone i : interfaceZones) {
                    if (!i.isHidden) {
                        i.hide();
                        currentInterface.add(i);
                    }
                }
                lockInterface1.show();
                lockInterface2.show();
                statusText = "Interface locked";
            } else {
                for (InteractiveZone i : currentInterface) {
                    i.show();
                }
                currentInterface.clear();
                statusText = "";
            }
            lastLock = DrawUtils.applet.millis();
        }

        if (gridButton.isActive) {
            gridSliderActivator.show();
            slider1.show();
        } else {
            gridSliderActivator.hide();
            slider1.hide();
        }
        

        if(showImage.isActive){
            imageIntensity.show();
            int intens = (int) (imageIntensity.position.x - 200) ;
            DrawingApp.imageIntensity = PApplet.constrain(intens, 0, 255);
//            imageIntensity.isSliding
        }else {
            imageIntensity.hide();
        }
//                if (isActive) {
//            gridSliderActivator.show();
//            slider1.show();
//        } else {
//            gridSliderActivator.hide();
//            slider1.hide();
//        }


        if (newShape.isActive) {
            if (currentMode != DrawMode.NO_MODE) {
                System.out.println("ERRRRRORRR new shape impossible");
            }
            newShape.hide();
            newShape.reset();
            cancel.show();
            startSelectShape();
            statusText = "Only the shape most on the left is selected.";
            newMode = DrawMode.SELECT_SHAPE;
        }

        //////////////// Cancel new shape ////////////////////////
        if (currentMode == DrawMode.SELECT_SHAPE && cancel.isActive) {
            stopSelectShape();
            cancel.reset();
            newShape.show();
            newShape.reset();
            validate.hide();
            newMode = DrawMode.NO_MODE;
        }


        //////////////// EDIT last shape ////////////////////////
        if (currentMode == DrawMode.NO_MODE && selectLast.isActive && lastShape != null) {
            DrawingApp.selectShape(lastShape);
            newShape.hide();
            selectLast.reset();
            startEditShape();
            newMode = DrawMode.EDIT_SHAPE;
        }

        if (clearAll.isSelected) {
            removeAllShapes();
        }

        if (hideAll.isActive) {
            for (Shape s : allShapes) {
                s.hide();
            }
        }


        if (preciseEdit.isActive) {
            startPreciseEdit();
            newMode = DrawMode.EDIT_SHAPE_PRECISE;
        }

        if (currentMode == DrawMode.EDIT_SHAPE_PRECISE) {
        }


        if (okButton.isActive) {
            newMode = DrawMode.EDIT_SHAPE;
            preciseEdit.isActive = false;
            stopPreciseEdit();
        }

        DrawingApp.isTranslateActive = translate.isActive;
        if (DrawingApp.currentShape != null) {
            DrawingApp.currentShape.setMovable(translate.isActive);
        }

        if (currentMode == DrawMode.SELECT_SHAPE) {
//            Button b = shapes[0];
//            int currentTime = DrawUtils.applet.millis();
//            int lastSelect = currentTime - b.lastPressedTime;
//            boolean isFirstOk = b.isActive;
//int last = 0;
////          b.reset();
//
//            for (int i = 1; i < shapes.length; i++) {
//                if (!shapes[i].isActive) {
//                    continue;
//                }
//
//                if (currentTime - shapes[i].lastPressedTime < lastSelect) {
//                    lastSelect = b.lastPressedTime;
//                    b = shapes[i];
//                    last = i;
//                }
//                shapes[i].reset();
//            }
//System.out.println("last "  + last);
//            b.isActive = true;
//            b.isActive = true;
        }


        //  Validation under the mode :
        if (validate.isActive) {

            validate.reset();
            switch (currentMode) {
                case NO_MODE:   // no effect ... impossible ?
                    System.out.println("ERRORRRRR !!!");
                    break;

                case EDIT_SHAPE:
                    stopEditShape();
                    newShape.show();
                    newShape.reset();
                    newMode = DrawMode.NO_MODE;
                    break;

                case SELECT_SHAPE:
                    boolean hasSelected = false;
                    for (int i = 0; i < shapes.length; i++) {
                        if (shapes[i].isActive) {
                            Shape s = null;
                            switch (i) {
                                case 0:
                                    s = new Line(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
                                    break;
                                case 1:
                                    s = new Triangle(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
                                    break;
                                case 2:
                                    s = new Rectangle(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
                                    break;
                                case 3:
                                    s = new Quad(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
                                    break;
                                case 4:
                                    s = new Ellipse(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
                                    break;
                                case 5:
                                    s = new Bezier(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
                                    break;
//                                case 6:
//                                    s = new Bezier(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//                                    break;
//                                case 7:
//                                    s = new Arc(new PVector(100 * paperSheet.scale, 100 * paperSheet.scale), paperSheet.scale);
//                                    break;
                            }
                            addShape(s);
                            lastShape = s;
                            startEditShape();
                            newMode = DrawMode.EDIT_SHAPE;
                            hasSelected = true;
                            break;
                        }
                    }

                    if (!hasSelected) {
                        newShape.show();
                        newShape.reset();
                        validate.hide();
                        newMode = DrawMode.NO_MODE;
                    }
                    stopSelectShape();
                    break;
            }
        }
// Switching to a new Mode

        if (currentMode != newMode) {
            currentMode = newMode;
        }

//        System.out.println("Current Mode --- NEW  " + currentMode);

        ///////////// Drawing ... ///////////////
        graphics.imageMode(PApplet.CENTER);
        graphics.pushMatrix();
        graphics.translate(0, 0, 0.01f);
        for (Drawable d : interfaceDrawables) {
            d.drawSelf(graphics);
        }

        if (gridButton.isActive) {
            graphics.fill(0x8ECB6D);
            DrawUtils.drawText(graphics, "grid size: " + (float) slider1.getPosition().x / 5f,
                    interfaceFont, 220, 270); // , 0, 0);
        }

        DrawUtils.drawText(graphics, statusText,
                interfaceFont, 0, -20); // , 0, 0);

        graphics.popMatrix();

        graphics.imageMode(PApplet.CORNER);
    }

    public void moveTo(PGraphics3D pgraphics3d, float x, float y) {
        pgraphics3d.translate(x, y);
        pgraphics3d.scale(-1, 1, 1);
        pgraphics3d.rotate(PApplet.PI);
    }

    public void startSelectShape() {
        for (int i = 0; i < shapes.length; i++) {
            shapes[i].show();
        }
        validate.show();
        validate.position.x = 0;
        validate.position.y = 200;
        validate.isActive = false;
        validate.reset();
    }

    public void stopSelectShape() {
        for (int i = 0; i < shapes.length; i++) {
            shapes[i].hide();
        }
        cancel.hide();
        validate.position.x = 270;
        validate.position.y = 260;
    }

    public void startEditShape() {
        validate.isActive = false;
        validate.isCooldownDone = false;
        validate.show();
        preciseEdit.show();
//        translate.show();
        newShape.hide();
        selectLast.hide();
        statusText = "Tip: You can go outside the paper sheet at your own risk.";
        DrawingApp.currentShape.setMovable(translate.isActive);
    }

    public void stopEditShape() {
        validate.hide();
        preciseEdit.hide();
        translate.hide();
        selectLast.reset();
        DrawingApp.currentShape.select(false);
        DrawingApp.currentShape.setMovable(false);
        DrawingApp.currentShape = null;
        DrawingApp.preciseWidget.hide();
        statusText = "";
        selectLast.show();
    }

    public void stopPreciseEdit() {
        System.out.println("Stop precise Edit");
        DrawingApp.currentIZ = null;
        DrawingApp.isSearchingIZ = false;
        DrawingApp.preciseWidget.hide();
        preciseEdit.show();
        preciseEdit.reset();
        validate.show();
        okButton.hide();
        okButton.reset();
    }

    public void updatePreciseEdit() {
    }

    public void startPreciseEdit() {
        System.out.println("Start precise Edit");
        DrawingApp.currentIZ = null;
        DrawingApp.isSearchingIZ = true;
        okButton.show();
        validate.hide();
        preciseEdit.hide();
        preciseEdit.reset();
    }

    public void addShape(Shape s) {
        allShapes.add(s);
        DrawingApp.drawables.add(s);
        s.show();
        DrawingApp.selectShape(s);
    }

    public void removeShape(Shape s) {
        allShapes.remove(s);
        DrawingApp.drawables.remove(s);
        s.hide();
    }

    public void removeAllShapes() {
        clearAll.reset();
        if (currentMode != DrawMode.NO_MODE) {
            System.out.println("Cannot destroy while making !!");
            return;
        }
        for (Shape s : allShapes) {
            DrawingApp.drawables.remove(s);
            s.hide();
        }
        allShapes.clear();
        selectLast.hide();
    }

    public void observeInput(float x, float y, TouchPoint tp) {

        for (InteractiveZone z : interfaceZones) {
            if (z.isSelected(x, y, tp)) {
                return;
            }
        }

        for (InteractiveZone z : DrawingApp.zones) {
            if (z.isSelected(x * paperSheet.scale, y * paperSheet.scale, tp)) {
                return;
            }
        }
    }
// void drawImage(PGraphics3D pg3d, PImage img, int x, int y, int w, int h){
//   pg3d.pushMatrix();
//   pg3d.translate(x, y);
//   pg3d.scale(-1, 1, 1);
//   pg3d.rotate(PI);
//   pg3d.image(img, 0, 0, w, h);
//   pg3d.popMatrix();
// }
// void drawText(PGraphics3D pg3d, String text, PFont font, int x, int y){
//   pg3d.pushMatrix();
//   pg3d.translate(x, y);
//   pg3d.scale(-1, 1, 1);
//   pg3d.rotate(PI);
//   pg3d.textMode(MODEL);
//   pg3d.textFont(font);
//   pg3d.text(text, 0, 0);
//   pg3d.popMatrix();
// }
}
