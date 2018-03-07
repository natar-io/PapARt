package fr.inria.papart.procam;

import fr.inria.papart.multitouch.detection.CalibratedStickerTracker;
import fr.inria.papart.procam.camera.TrackedView;
import org.bytedeco.javacpp.helper.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;

/**
 * The MainScreen is a planar screen on the table. It is facing the user and
 * uses no MarkerBoard but the tableposition variable.
 *
 * @author Jeremy Laviole
 */
public class MainScreen extends PaperTouchScreen {

    public static float WIDTH = 800, HEIGHT = 500;
    public CalibratedStickerTracker stickerTracker;

    @Override
    public void settings() {
        // Test with this...
        setDrawOnPaper();
        setDrawingSize(WIDTH, HEIGHT);
        setQuality(2f);
    }

    TrackedView circleView;
    protected PMatrix3D location;

    @Override
    public void setup() {
        // View on all of the image
        circleView = createViewForCircle(15); // 8mm
        
        stickerTracker = new CalibratedStickerTracker(this, 8);
        stickerTracker.initTouchDetection();
        
        location = table.get();
        location.translate(-WIDTH / 2, -HEIGHT / 2);
//        this.setLocation(location);
        this.useManualLocation(location);
    }

    private TrackedView createViewForCircle(int circleDiameter) {
        TrackedView view = new TrackedView(this);

        int w = (int) (this.drawingSize.x / (float) circleDiameter * 5);
        int h = (int) (this.drawingSize.y / (float) circleDiameter * 5);

        // We need to scale the circles to 5 pixels 
        view.setImageWidthPx(w);
        view.setImageHeightPx(h);
        view.init();

        conv = parent.createImage(w, h, PApplet.RGB);
        eroded = parent.createImage(w, h, PApplet.RGB);

        return view;
    }
    

    public boolean cap = false;
    PImage conv, eroded;

    public void captureView() {
        PImage out = circleView.getViewOf(cameraTracking);

        if (out != null) {
            //	    image(out, 120, 40, picSize, picSize);

            PImage img = out;
            int xstart = 0;
            int ystart = 0;
            int xend = out.width;
            int yend = out.height;
            int matrixsize = 5;
            conv.loadPixels();
            // Begin our loop for every pixel in the smaller image
            for (int x = xstart; x < xend; x++) {
                for (int y = ystart; y < yend; y++) {
                    int c = convolution(x, y, matrix5conv, matrixsize, img);
                    int loc = x + y * img.width;
                    conv.pixels[loc] = c;
                }
            }
            // ready for rendering
            conv.updatePixels();
            //	    image(conv, 0, 0, 300/2, 200/2);

            eroded.loadPixels();

            noStroke();

            for (int x = xstart; x < xend; x++) {
                for (int y = ystart; y < yend; y++) {
                    int c = erosion(x, y, matrix3erode, 3, conv);
                    int loc = x + y * img.width;
                    eroded.pixels[loc] = c;
                    if (red(eroded.pixels[loc]) >= max
                            || green(eroded.pixels[loc]) >= max
                            || blue(eroded.pixels[loc]) >= max) {
                        fill(eroded.pixels[loc], 150);
//                        float dx = (float) x / (float) w * drawingSize.x;
//                        float dy = (float) y / (float) h * drawingSize.y;
//                        ellipse(dx, dy, 10, 10);
                    }
                }
            }
            eroded.updatePixels();
            //	    image(eroded, 300/2, 0, 300/2, 200/2);

            if (cap) {
                cap = false;
                opencv_imgcodecs.cvSaveImage(parent.sketchPath() + "/out-ipl.png", circleView.getIplViewOf(cameraTracking));
                out.save(parent.sketchPath() + "/out.png");
                conv.save(parent.sketchPath() + "/conv.png");
                eroded.save(parent.sketchPath() + "/eroded.png");
                parent.println("Image saved");
            }
        }
    }

    ////// Erosion and convolution tests... 
    float[][] matrix5conv = {{3, 1, -1, 1, 3},
    {1, -2, -2, -2, 1},
    {-1, -2, -3, -2, -1},
    {1, -2, -2, -2, 1},
    {3, 1, -1, 1, 3}};

    float[][] matrix3erode = {{1, 1, 1},
    {1, 1, 1},
    {1, 1, 1}};

    int max = 70;

    int erosion(int x, int y, float[][] matrix, int matrixsize, PImage img) {
        float rtotal = 0.0f;
        float gtotal = 0.0f;
        float btotal = 0.0f;

        int off = x + img.width * y;

        int rinit = (int) max;
        int ginit = (int) max;
        int binit = (int) max;
        // int rinit = (int) red(img.pixels[off]);
        // int ginit = (int) green(img.pixels[off]);
        // int binit = (int) blue(img.pixels[off]);

        int matSum = 0;
        for (int i = 0; i < matrixsize; i++) {
            for (int j = 0; j < matrixsize; j++) {
                matSum += matrix[i][j];
            }
        }

        int vSumr = 0, vSumg = 0, vSumb = 0;
        int offset = matrixsize / 2;
        for (int i = 0; i < matrixsize; i++) {
            for (int j = 0; j < matrixsize; j++) {
                // What pixel are we testing
                int xloc = x + i - offset;
                int yloc = y + j - offset;
                int loc = xloc + img.width * yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = PApplet.constrain(loc, 0, img.pixels.length - 1);

                // Calculate erosion
                if (matrix[i][j] == 1 && red(img.pixels[loc]) >= rinit) {
                    vSumr++;
                }

                if (matrix[i][j] == 1 && green(img.pixels[loc]) >= ginit) {
                    vSumg++;
                }

                if (matrix[i][j] == 1 && blue(img.pixels[loc]) >= binit) {
                    vSumb++;
                }
            }
        }
        // Make sure RGB is within range
        rtotal = vSumr == matSum ? rinit : 0;
        gtotal = vSumg == matSum ? ginit : 0;
        btotal = vSumb == matSum ? binit : 0;

        // Return the resulting color
        return color(rtotal, gtotal, btotal);
    }

    int convolution(int x, int y, float[][] matrix, int matrixsize, PImage img) {
        float rtotal = 0.0f;
        float gtotal = 0.0f;
        float btotal = 0.0f;
        int offset = matrixsize / 2;
        for (int i = 0; i < matrixsize; i++) {
            for (int j = 0; j < matrixsize; j++) {
                // What pixel are we testing
                int xloc = x + i - offset;
                int yloc = y + j - offset;
                int loc = xloc + img.width * yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = PApplet.constrain(loc, 0, img.pixels.length - 1);
                // Calculate the convolution
                rtotal += (red(img.pixels[loc]) * matrix[i][j]);
                gtotal += (green(img.pixels[loc]) * matrix[i][j]);
                btotal += (blue(img.pixels[loc]) * matrix[i][j]);
            }
        }
        // Make sure RGB is within range
        rtotal = PApplet.constrain(rtotal / 4f, 0, 255);
        gtotal = PApplet.constrain(gtotal / 4f, 0, 255);
        btotal = PApplet.constrain(btotal / 4f, 0, 255);
        // Return the resulting color
        return color(rtotal, gtotal, btotal);
    }

}
