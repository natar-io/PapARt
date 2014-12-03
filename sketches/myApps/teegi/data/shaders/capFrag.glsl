#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform sampler2D mask;

uniform vec3 color1;
uniform vec3 color2;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {

  vec3 texCol = texture2D(texture, vertTexCoord.st);

  vec3 compo1 = texCol * color1;
  vec3 compo2 = (vec3(1) - texCol) * color2;

  vec3 finalCol = (compo1 + compo2);
  
  vec4 mask = texture2D(mask, vertTexCoord.st);

  gl_FragColor =  vec4(finalCol.rgb, 1.0) * mask;
}
