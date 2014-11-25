uniform mat4 transform;

attribute vec4 vertex;
attribute vec4 color;
attribute vec4 normal;

varying vec4 vertColor;

void main() {
  gl_Position = transform * vertex;    
  vertColor = color / 255;
}
