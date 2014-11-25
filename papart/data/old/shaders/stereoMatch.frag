uniform sampler2D tex;
uniform sampler2D ShadowMap;
uniform float usingTex;

varying vec4 ShadowCoord;
varying vec4 diffuse,ambient;
varying vec3 normal,lightDir,halfVector;
varying vec4 pos;

void main()
{	

	vec4 shadowCoordinateWdivide = ShadowCoord / ShadowCoord.w ;
	//	shadowCoordinateWdivide.z += 0.0005;
	vec4 viewFromOther = texture2D(ShadowMap, shadowCoordinateWdivide.st);

	// If depth is the same (approx)
	// we take the other color, 
	// else we compute the right color

	float d = distance(pos, viewFromOther) / 1000.;

	if(d < 0.3){
	  gl_FragColor = vec4(viewFromOther.rg, 0, 1);
	  
	  // TODO: compute the distance, shift to match the depth

	}else {

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
	  
	  gl_FragColor = color;
	  //	  gl_FragColor = vec4(1.0, 0, 0, 1);
	}


	// The main difference is in Z !

	d = 0.005 * abs(pos.z - viewFromOther.z);
	/* gl_FragColor = vec4(0.005 *abs(pos.x - viewFromOther.x), */
	/* 		    0.005 *abs(pos.y - viewFromOther.y), */
	/* 		    0.005 *abs(pos.z - viewFromOther.z), 1); */


	/* float d = 0.01 * sqrt(distance(pos, viewFromOther)); */
	 gl_FragColor = vec4(d, d, d, 1);

	//	gl_FragColor = vec4(0, viewFromOther.x, 0, 1.0);

	/* // display the texture */
	/* vec2 screenCoord = vec2(gl_FragCoord.s / 800., 1.- (gl_FragCoord.t / 600.)); */
	/* gl_FragColor = vec4(texture2D(tex, screenCoord).rgb, 1.); */


}
		
