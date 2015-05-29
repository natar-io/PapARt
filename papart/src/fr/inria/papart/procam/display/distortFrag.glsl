#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform sampler2D mapTex;

uniform int resX, resY;
uniform float mag;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {


  vec4 mapCoord = texture2D(mapTex, vertTexCoord.st);

  // Red is x shift
  // Green is y shift

  // - 0.5 to get the positive and negative values 
  // difference is in Pixels after that. 

  /* vec2 coord = vec2( (mapCoord.x - 0.5) * 20 / resX + vertTexCoord.s, */
  /* 		     (mapCoord.y - 0.5) * 20 / resY + vertTexCoord.t); */

  vec2 coord2 = vec2( (mapCoord.x - 0.5) * mag / float(resX),
		      (mapCoord.y - 0.5) * mag / float(resY));

  gl_FragColor = vec4(texture2D(texture, coord2 + vertTexCoord.st ).rgba);

  // gl_FragColor = vec4(mapCoord.rgb , 1);
  //  gl_FragColor = vec4(coord2.rg, 1 , 1);
  //  gl_FragColor = vec4(vertTexCoord.rg, 1 , 1);


  // Just the texture
  //   gl_FragColor = vec4(texture2D(mapTex, vertTexCoord.st).rgb, 1);

}




/* void main() */
/* { */

/*   vec4 mapCoord = texture2D(mapTex, gl_TexCoord[0].st); */
/*    gl_FragColor = texture2D(srcTex, mapCoord.rg); */

/*    gl_FragColor = vec4(1, 0, 0, 1); */

/*      /\* gl_FragColor = texture2D(srcTex, gl_TexCoord[0].st); *\/ */
/* //   gl_FragColor = texture2D(mapTex, gl_TexCoord[0].st); */
/* } */


