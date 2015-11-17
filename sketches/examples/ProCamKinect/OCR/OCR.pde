import fr.inria.papart.procam.camera.*;
import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.*;
import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.IplImage;
import fr.inria.skatolo.Skatolo;

public class MyApp  extends PaperScreen {

    TrackedView boardView;

    // 5cm  ->  50 x 50 pixels
    PVector captureSize = new PVector(200, 150);
    PVector origin = new PVector(40, 40);
    int picSize = 512; // Works better with power  of 2

    PFont myFont;
    BytePointer outText;
    TessBaseAPI api;

    void settings(){
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath() + "/data/A3-small1.cfg", 297, 210);
    }

    void setup() {
	boardView = new TrackedView(this);
	boardView.setCaptureSizeMM(captureSize);

	boardView.setImageWidthPx(picSize);
	boardView.setImageHeightPx(picSize);

	boardView.setBottomLeftCorner(origin);

	boardView.init();

	api = new TessBaseAPI();
	if (api.Init(null, "eng") != 0) {
	    System.err.println("Could not initialize tesseract.");
	    System.exit(1);
	}

         myFont = loadFont("DejaVuSerifCondensed-48.vlw");

    }


    void drawOnPaper() {

	clear();
        background(50);
	boardView.setBottomLeftCorner(new PVector(0, 60));

	fill(200, 100, 20);
	// rect(10, 10, 10, 10);
	PImage out = boardView.getViewOf(cameraTracking);
	IplImage ipl = boardView.getIplViewOf(cameraTracking);


	out.save("/dev/shm/tmp.tiff");
	PIX image = pixRead("/dev/shm/tmp.tiff");


	// int bpp = 16; // save the BytesPerPixel for this Pixelformat
	// int bpl = bpp*512; // BytesPerLines is just the "BPP * width" of your PreviewFormat/Picture

	// api.SetImage(ipl.imageData(), 512, 512, bpp, bpl);

	api.SetImage(image);


	// Get OCR result
	outText = api.GetUTF8Text();
String str = outText.getString();
	System.out.println("OCR output:\n" + str);

fill(255);
stroke(255);
        textFont(myFont, 40);
	text(str, 71, 191);
	if(out != null){
	  //  image(out, 0, 0, 50, 50);
	}
    }

    void close(){
    // Destroy used object and release memory
    api.End();
    outText.deallocate();

    }

}
