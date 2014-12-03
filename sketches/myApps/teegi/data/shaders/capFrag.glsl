#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform sampler2D mask;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {
  gl_FragColor = texture2D(mask, vertTexCoord.st) 
    * texture2D(texture, vertTexCoord.st) 
    * vertColor;
}
