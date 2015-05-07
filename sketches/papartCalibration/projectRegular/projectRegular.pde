import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;

int ellipseSize = 40;

void setup(){
    Papart.projection2D(this);

    background(0);
    float step = 0.5f;
    //    nbPoints = (int) ((1 + 1f / step) * (1 + 1f / step));
    
    int k = 0;
    for (float i = 0; i <= 1.0; i += step) {
	for (float j = 0; j <= 1.0; j += step, k++) {
	    fill(i * 150 + 100, j * 150 +  100, 100);
	    ellipse(i * width, j * height, ellipseSize, ellipseSize);
	}
    }
    noLoop();
}


void draw(){


}



