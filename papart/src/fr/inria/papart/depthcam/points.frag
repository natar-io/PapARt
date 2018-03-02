#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 vertColor;

void main() {

  //  gl_FragColor = vec4(1, 1, 1, 1);
  gl_FragColor = vec4(vertColor.rgb, 1);
}
