import fr.inria.papart.procam.ColorDetection;

public class MyApp extends PaperScreen {

  TrackedView boardView;

  ColorDetection colorDetectionInk;

  int inkColor;

  PVector captureSize = new PVector(150, 210);
  PVector origin = new PVector(0, 0);
  int picSize = 128; // Works better with power  of 2

  public void settings() {
    setDrawingSize(297, 210);
    loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", 297, 210);
  }

  public void setup() {
    boardView = new TrackedView(this);
    boardView.setCaptureSizeMM(captureSize);

    boardView.setImageWidthPx(picSize);
    boardView.setImageHeightPx(picSize);

    boardView.setTopLeftCorner(origin);

    boardView.init();

    colorDetectionInk = new ColorDetection((PaperScreen) this);
    colorDetectionInk.setCaptureSize(20, 20);
    colorDetectionInk.setPosition(new PVector(0, 20));
    colorDetectionInk.initialize();
  }

  public void drawOnPaper() {
    if (frameCount%30 == 0) {
      clear();
      setLocation(63, 45, 0);

      stroke(100);
      noFill();
      strokeWeight(2);
      rect((int) origin.x, (int) origin.y, 
        (int) captureSize.x, (int)captureSize.y);

      PImage out = boardView.getViewOf(cameraTracking);
      PImage processed = out;

      colorMode(HSB, 360, 100, 100);

      out.loadPixels();
      for (int x=0; x<out.width-1; x++) {
        for (int y=0; y<out.height-1; y++) {
          int loc = x + y * out.width;

          if (isCorrectHSB(out.pixels[loc], inkColor, 80)) 
            processed.pixels[loc] = color(0, 100, 100);
          
        }
      }
      out.updatePixels();
      colorMode(RGB, 255, 255, 255);

      if (out != null) {
        image(processed, 150, 0, 150, 210);
      }

      colorDetectionInk.computeColor();
      inkColor = colorDetectionInk.getColor();

      fill(inkColor);
      ellipse(10, 40, 15, 15);
    }
  }

  public boolean isCorrectHSB(int colorToCompare, int colorFixed, int threshold) {

    float hueC = hue(colorToCompare);
    float hueF = hue(colorFixed);

    if (hueC > hueF + threshold) return false;
    if (hueC < hueF - threshold) return false;

    if (saturation(colorToCompare) < 30) return false;

    if (brightness(colorToCompare) < 15) return false;

    return true;
  }
}