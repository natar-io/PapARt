#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif


uniform sampler2D texture;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {
  gl_FragColor = texture2D(texture, vertTexCoord.st) * vertColor;
}




/* uniform sampler2D texture; */
/* uniform sampler2D mapTex; */

/* void main() */
/* { */

/*   vec4 mapCoord = texture2D(mapTex, gl_TexCoord[0].st); */
/*    gl_FragColor = texture2D(srcTex, mapCoord.rg); */

/*    gl_FragColor = vec4(1, 0, 0, 1); */

/*      /\* gl_FragColor = texture2D(srcTex, gl_TexCoord[0].st); *\/ */
/* //   gl_FragColor = texture2D(mapTex, gl_TexCoord[0].st); */
/* } */

