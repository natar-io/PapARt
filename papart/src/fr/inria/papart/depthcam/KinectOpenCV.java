/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.depthcam;

import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.nio.ByteBuffer;
import java.util.Arrays;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import processing.core.PApplet;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class KinectOpenCV extends KinectDepthAnalysis {

    public IplImage validPointsIpl;
    public byte[] validPointsRaw;

    public KinectOpenCV(PApplet parent, CameraOpenKinect camera) {
       super(parent, camera);
       init();
    }

    private void init() {
        validPointsIpl = IplImage.create(cvSize(width,
                height),
                IPL_DEPTH_8U, 3);
        validPointsRaw = new byte[size * 3];
    }

    public IplImage update(IplImage depth, IplImage color) {
        return update(depth, color, 1);
    }

    public IplImage update(IplImage depth, IplImage color, int skip) {

        updateRawDepth(depth);
        updateRawColor(color);
        clearImageBuffer();
        computeDepthAndDo(1, new setImageData());
        updateImageBuffer();
        return validPointsIpl;
    }

    private void clearImageBuffer() {
        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();
        outputBuff.get(validPointsRaw);
        Arrays.fill(validPointsRaw, (byte) 0);
    }

    private void updateImageBuffer() {
        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();
        outputBuff = (ByteBuffer) outputBuff.rewind();
        outputBuff.put(validPointsRaw);
    }

    class setImageData implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {
            depthData.validPointsMask[offset] = true;
            int outputOffset = offset * 3;
            int colorOffset = findColorOffset(p) * 3;
            validPointsRaw[outputOffset + 2] = colorRaw[colorOffset + 2];
            validPointsRaw[outputOffset + 1] = colorRaw[colorOffset + 1];
            validPointsRaw[outputOffset + 0] = colorRaw[colorOffset + 0];
        }
    }

    @Deprecated
    public IplImage getDepthColorIpl() {
        return validPointsIpl;
    }

    public IplImage getColouredDepthImage() {
        return validPointsIpl;
    }

}
