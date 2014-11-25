uniform sampler2D leftTex, rightTex;
uniform sampler2D ShadowMap;
uniform float usingTex;

varying vec4 diffuse,ambient;
varying vec3 normal,lightDir,halfVector;
varying vec4 coord, leftCoord, rightCoord;
varying vec4 leftShift, rightShift, pos1, diff;

uniform float shift, scale;

void main()
{	

  vec2 screenCoord = vec2(gl_FragCoord.s / (297. * 6.),
			  gl_FragCoord.t / (210. * 6.));

  vec3 n,halfV,viewV,ldir;
  float NdotL,NdotHV;
  vec4 color = ambient;
  
  n = normalize(normal);
  
  /* compute the dot product between normal and ldir */
  NdotL = max(dot(n,lightDir),0.0);
  
  if (NdotL > 0.0) {
    halfV = normalize(halfVector);
    NdotHV = max(dot(n,halfV),0.0);
    color += gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess);
    color += diffuse * NdotL;
  }
    
  vec4 centerView = color;





  // Todo : linear depth ?
  // Todo : tran
  // TODO: hand calibration ??
  // TODO: calcul de min et max, en pixels 

  /* gl_FragColor = vec4(texture2D(rightTex, screenCoord).r, */
  /* 		      texture2D(leftTex, screenCoord).gb, */
  /* 		      1.); */
  
  // ce pixel, vu par la camera gauche
  /* vec4 leftCoordWdivide = leftCoord / leftCoord.w ; */
  /* vec4 leftView = texture2D(leftTex, leftCoordWdivide.st); */

  float depth2 = (2.0 * gl_FragCoord.z - gl_DepthRange.near - gl_DepthRange.far) / (gl_DepthRange.far - gl_DepthRange.near);

  //  float depth2 = gl_FragCoord.z;

  depth2 = 1. - depth2;

  //  depth2 *= scale;
  depth2 -= shift;  
  //  depth2 = depth2 * scale ;


  float c = depth2 * 297. * 6. / 100.;
  gl_FragColor = vec4(c, c, c, 1);

  //  return ;

  //  return;
  /* float diff2 = 1. - gl_FragCoord.z; */
  /* diff2 -= shift;   */
  /* diff2 *= scale; */
  /* float c = diff2 * 297. * 6. / 10.; */
  /* gl_FragColor = vec4(c, c, c, 1); */

  //  gl_FragColor = vec4(depth2, depth2, depth2, 1);

  gl_FragColor = vec4(leftShift.x, leftShift.y, 0, 1);
  return;
  
  /* vec4 leftView = texture2D(leftTex, screenCoord.rg); */
  /* vec4 rightView = texture2D(rightTex, screenCoord.rg); */
  vec4 leftView = texture2D(leftTex, vec2(screenCoord.r + leftShift.r, screenCoord.g));
  vec4 rightView = texture2D(rightTex, vec2(screenCoord.r - leftShift.g, screenCoord.g));

  /* if(diff2 *297. * 6. < 4 ){ */
  /*   gl_FragColor = vec4(leftView.rgb, 1); */
  /*   return; */
  /* } */

   gl_FragColor =  vec4(rightView.r, leftView.gb, 1);
   return;

  float dist = distance(leftView.rgb, centerView.rgb);
  if(dist < 0.4){

    leftView.r = leftView.r + 4.0 * (leftView.r - centerView.r);
    leftView.g = leftView.g + 4.0 * (leftView.g - centerView.g);
    leftView.b = leftView.b + 4.0 * (leftView.b - centerView.b);

    //    leftView = vec4(0, 0, 0, 1);
  } else 
    if(dist > 0.8){

    leftView.r = leftView.r - 4.0 * (leftView.r - centerView.r);
    leftView.g = leftView.g - 4.0 * (leftView.g - centerView.g);
    leftView.b = leftView.b - 4.0 * (leftView.b - centerView.b);

    //leftView =  centerView;
    }

 dist = distance(rightView.rgb, centerView.rgb);
  if(dist < 0.4){

    rightView.r = rightView.r + 4.0 * (rightView.r - centerView.r);
    rightView.g = rightView.g + 4.0 * (rightView.g - centerView.g);
    rightView.b = rightView.b + 4.0 * (rightView.b - centerView.b);

    //    rightView = vec4(0, 0, 0, 1);
  } else 
    if(dist > 0.8){
      
      rightView.r = rightView.r - 4.0 * (rightView.r - centerView.r);
      rightView.g = rightView.g - 4.0 * (rightView.g - centerView.g);
      rightView.b = rightView.b - 4.0 * (rightView.b - centerView.b);

      //      rightView =  centerView;
    }




  /* float di = distance(vec4(rightView.r, leftView.gb, 1), */
  /* 		      vec4(texture2D(rightTex, screenCoord).r, */
  /* 			   texture2D(leftTex, screenCoord).gb, */
  /* 			   1.)); */
  /*  gl_FragColor =  vec4(di, di, di, 1); */

   gl_FragColor =  vec4(rightView.r, leftView.gb, 1);
  /* gl_FragColor = vec4(texture2D(rightTex, screenCoord).r, */
  /* 		      texture2D(leftTex, screenCoord).gb, */
  /* 		      1.); */
  

}




  ///////////////////////////////////////////////

  
	// The main difference is in Z !

	// d = 0.005 * abs(coord.z - leftView.z);
	/* gl_FragColor = vec4(0.005 *abs(pos.x - viewFromOther.x), */
	/* 		    0.005 *abs(pos.y - viewFromOther.y), */
	/* 		    0.005 *abs(pos.z - viewFromOther.z), 1); */


	/* float d = 0.01 * sqrt(distance(pos, viewFromOther)); */
	//	gl_FragColor = vec4(d , d, d, 1);




	 /********* Stereo *********/
	/* // display the texture */

  /* gl_FragColor = vec4(texture2D(rightTex, screenCoord).r, */
  /* 		      texture2D(leftTex, screenCoord).gb, */
  /* 		      1.); */




	///////////////////// Non fonctionnel ////////

  /* // Idee : fragCoord, reprojeté dans la vue gauche, pour récup sa couleur.  */
  /* vec4 screenCoord = vec4(gl_FragCoord.s / (297 * 6), */
  /* 			  gl_FragCoord.t / (210 * 6), gl_FragDepth, 1); */
  /* // calcul de la position de vertex courant, vu par la camara frontale.  */
  /* vec4 leftPix = gl_TextureMatrix[5] * screenCoord; */
  
  /* // récupération de la couleur de ce vertex. */
  /* vec4 leftPixWd = leftPix / leftPix.w; */
  /* vec4 leftCol = texture2D(leftTex, leftPix.st); */
  

  /* // Test de reprojection... non concluant.  */
  /* vec4 vue = screenCoord * gl_ModelViewProjectionMatrix; */
  /* vue = vue / vue.w; */
  /* vec4 screenCoord2 = vec4(vue.x / (297 * 6), */
  /* 			   vue.y / (210 * 6), vue.z, 1); */


		
