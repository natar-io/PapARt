String matToString(PMatrix3D matrix){
    // return (matrix.m00 + " " + matrix.m01 + " " + matrix.m02 + " " + matrix.m03 + "\n" +
    //         matrix.m10 + " " + matrix.m11 + " " + matrix.m12 + " " + matrix.m13 + "\n" +
    //         matrix.m20 + " " + matrix.m21 + " " + matrix.m22 + " " + matrix.m23 + "\n" +
    //         matrix.m30 + " " + matrix.matrix.m31 + " " + matrix.matrix.m32 + " " + matrix.matrix.m33 + "\n");
// }
    int big = (int) Math.abs(max(max(max(max(abs(matrix.m00), abs(matrix.m01)),
                                         max(abs(matrix.m02), abs(matrix.m03))),
                                     max(max(abs(matrix.m10), abs(matrix.m11)),
                                         max(abs(matrix.m12), abs(matrix.m13)))),
                                 max(max(max(abs(matrix.m20), abs(matrix.m21)),
                                         max(abs(matrix.m22), abs(matrix.m23))),
                                     max(max(abs(matrix.m30), abs(matrix.m31)),
                                         max(abs(matrix.m32), abs(matrix.m33))))));

    int digits = 1;
    if (Float.isNaN(big) || Float.isInfinite(big)) {  // avoid infinite loop
      digits = 5;
    } else {
      while ((big /= 10) != 0) digits++;  // cheap log()
    }

    StringBuilder output = new StringBuilder();
    output.append(PApplet.nfs(matrix.m00, digits, 4) + " " +
                  PApplet.nfs(matrix.m01, digits, 4) + " " +
                  PApplet.nfs(matrix.m02, digits, 4) + " " +
                  PApplet.nfs(matrix.m03, digits, 4) + "\n");

    output.append(PApplet.nfs(matrix.m10, digits, 4) + " " +
                  PApplet.nfs(matrix.m11, digits, 4) + " " +
                  PApplet.nfs(matrix.m12, digits, 4) + " " +
                  PApplet.nfs(matrix.m13, digits, 4) + "\n");

    output.append(PApplet.nfs(matrix.m20, digits, 4) + " " +
                  PApplet.nfs(matrix.m21, digits, 4) + " " +
                  PApplet.nfs(matrix.m22, digits, 4) + " " +
                  PApplet.nfs(matrix.m23, digits, 4) + "\n");

    output.append(PApplet.nfs(matrix.m30, digits, 4) + " " +
                  PApplet.nfs(matrix.m31, digits, 4) + " " +
                  PApplet.nfs(matrix.m32, digits, 4) + " " +
                  PApplet.nfs(matrix.m33, digits, 4) + "\n");

    return output.toString();
 }


void addMatrices(PMatrix3D sum, PMatrix3D addedElement){
    sum.m00 += addedElement.m00;
    sum.m01 += addedElement.m01;
    sum.m02 += addedElement.m02;
    sum.m03 += addedElement.m03;

    sum.m10 += addedElement.m10;
    sum.m11 += addedElement.m11;
    sum.m12 += addedElement.m12;
    sum.m13 += addedElement.m13;

    sum.m20 += addedElement.m20;
    sum.m21 += addedElement.m21;
    sum.m22 += addedElement.m22;
    sum.m23 += addedElement.m23;

    sum.m30 += addedElement.m30;
    sum.m31 += addedElement.m31;
    sum.m32 += addedElement.m32;
    sum.m33 += addedElement.m33;
}

void multMatrix(PMatrix3D sum, float value){
    sum.m00 *= value;
    sum.m01 *= value;
    sum.m02 *= value;
    sum.m03 *= value;

    sum.m10 *= value;
    sum.m11 *= value;
    sum.m12 *= value;
    sum.m13 *= value;

    sum.m20 *= value;
    sum.m21 *= value;
    sum.m22 *= value;
    sum.m23 *= value;

    sum.m30 *= value;
    sum.m31 *= value;
    sum.m32 *= value;
    sum.m33 *= value;
}

void sumPlane(Plane sum, Plane added){
    sum.addSelf(added);
    sum.normal.addSelf(added.normal);
}


void averagePlane(Plane sum, float v){
    sum.scaleSelf(v);
    sum.normal.normalize();
}
