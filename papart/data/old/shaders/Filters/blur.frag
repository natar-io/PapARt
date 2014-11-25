uniform sampler2D sceneTex;
uniform vec2 wh;


/* vec2 gaussianDeriv(int deriSize){ */

/*   int neighbor = 2*deriSize + 1; */
/*   float sigma = float(deriSize)/3.0; */
/*   float dem = 2.0*sigma*sigma; */
/*   vec4 sumX1 = vec4(0.0); */
/*   vec4 sumY1 = vec4(0.0); */
/*   for(int yi=0; yi<neighbor; yi++) */
/*     { */
/*       float wy1 = exp(-float(yi-deriSize)*float(yi-deriSize)/dem); */
/*       float wy2 = -float(yi-deriSize)*wy1*2.0/dem; */
      
/*       for(int xi=0; xi<neighbor; xi++) */
/*         { */
/* 	  float wx1 = exp(-float(xi-deriSize)*float(xi-deriSize)/dem); */
/* 	  float wx2 = -float(xi-deriSize)*wx1*2.0/dem; */
	  
/* 	  vec2 offsetX = vec2(float(xi-deriSize), float(yi-deriSize)) * wh; */
/* 	  sumX1 += texture2D(sceneTex, gl_TexCoord[0].st + offsetX) * wy1 * wx2 ; */
/* 	  sumY1 += texture2D(sceneTex, gl_TexCoord[0].st + offsetX) * wy2 * wx1 ; */
/*         } */
/*     } */
  
  
/*   float sX1 = (sumX1.r + sumX1.g + sumX1.b) / 3.0; */
/*   float sY1 = (sumY1.r + sumY1.g + sumY1.b) / 3.0; */
  
/*   return vec2(sX1, sY1); */
/* } */




void main()
{
  // RECOPIE
  // gl_FragColor = vec4(0.4, 0.2, 0.8, 0.3);

  /* vec4 maskColor = texture2D(mask, gl_TexCoord[0].st); */
  /* vec4 texC = texture2D(sceneTex, gl_TexCoord[0].st); */



  float dx = wh.s;
  float dy = wh.t;
  vec2 st = gl_TexCoord[0].st;
  
  // Apply 3x3 gaussian filter
  vec4 color	 = 4.0 * texture2D(sceneTex, st);
  color		+= 2.0 * texture2D(sceneTex, st + vec2(+dx, 0.0));
  color		+= 2.0 * texture2D(sceneTex, st + vec2(-dx, 0.0));
  color		+= 2.0 * texture2D(sceneTex, st + vec2(0.0, +dy));
  color		+= 2.0 * texture2D(sceneTex, st + vec2(0.0, -dy));
  color		+= texture2D(sceneTex, st + vec2(+dx, +dy));
  color		+= texture2D(sceneTex, st + vec2(-dx, +dy));
  color		+= texture2D(sceneTex, st + vec2(-dx, -dy));
  color		+= texture2D(sceneTex, st + vec2(+dx, -dy));
  
  gl_FragColor =  color/ 16.0;


  /* vec2 uv = gl_TexCoord[0].xy; */
  /* vec4 c = texture2D(sceneTex, uv); */

  /* c += texture2D(sceneTex, uv+0.001); */
  /* c += texture2D(sceneTex, uv+0.003); */
  /* c += texture2D(sceneTex, uv+0.005); */
  /* c += texture2D(sceneTex, uv+0.007); */
  /* c += texture2D(sceneTex, uv+0.009); */
  /* c += texture2D(sceneTex, uv+0.011); */

  /* c += texture2D(sceneTex, uv-0.001); */
  /* c += texture2D(sceneTex, uv-0.003); */
  /* c += texture2D(sceneTex, uv-0.005); */
  /* c += texture2D(sceneTex, uv-0.007); */
  /* c += texture2D(sceneTex, uv-0.009); */
  /* c += texture2D(sceneTex, uv-0.011); */

  /* c.rgb = vec3((c.r+c.g+c.b)/3.0); */
  /* //  c = c / 9.5; */

  /* c = c / 11.5; */

  /* gl_FragColor = c; */
   
  //  gl_FragColor = vec4(0, 0, 0, 0.5);


  
  /* if(maskColor.g > 0.5){ */
  /*   vec2 intens = gaussianDeriv(derivSize); */

  /*   /\* gl_FragColor = vec4(intens.r, intens.g, 0, 1); *\/ */
  /*   /\* return; *\/ */

  /*   float threshold = intensT; */

  /*   if(intens.r > threshold ||  intens.g > threshold){ */
  /*     //    if(intens.r > 0.2 ||  intens.g > 0.2){ */

  /*     gl_FragColor = texture2D(sceneTex, gl_TexCoord[0].st); */
  /*     return; */

  /*   } else { */

  /*     gl_FragColor = vec4(texC.r * 0.3, texC.g * 0.3, texC.b * 0.3, 1.0); */
  /*     return; */
  /*   } */
  /* } */


  /* /// Blue codes for high intensity */
  /* if(maskColor.b > 0.5){ */

  /*   gl_FragColor = vec4(2.0 * texC.rgb, 1.0); */
  /*   return; */

  /* } */
  /* // green codes for Edge enhance */
  /*   // No code ? Image  */

  /*   gl_FragColor = texture2D(sceneTex, gl_TexCoord[0].st); */
}


