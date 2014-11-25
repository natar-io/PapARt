uniform sampler2D tex;
uniform sampler2D ShadowMap;
uniform float usingTex;

varying vec4 ShadowCoord;
varying vec4 diffuse,ambient;
varying vec3 normal,lightDir,halfVector;


/* float lookup(float x, float y) */
/* { */
/*     /\* float depth = shadow2DProj(ShadowMap, *\/ */
/*     /\*                   ShadowCoord + vec3(x, y, 0) *  0.0005).x; *\/ */

/*   vec4 p = ShadowCoord + vec4(vec3(x * ShadowCoord.w , y * ShadowCoord.w, 0) * 0.0005, 1.); */

/*   float depth = shadow2DProj(ShadowMap, p).w; */

/*   //			       ShadowCoord + vec4(vec3(x, y, 0) *  0.0005,0) ).x; */

/*     return depth != 1.0 ? 0.75 : 1.0; */
/* } */


/* float lookup( vec2 offSet) */
/* { */
/*   // Values are multiplied by ShadowCoord.w because shadow2DProj does a W division for us. */
/*   return shadow2DProj(ShadowMap, ShadowCoord +  */
/* vec4(offSet.x * xPixelOffset * ShadowCoord.w, offSet.y * yPixelOffset * ShadowCoord.w, 0.05, 0.0) ).w; */
/* } */


void main()
{	
	vec3 n,halfV,viewV,ldir;
	float NdotL,NdotHV;
	vec4 color = ambient;
	
	/* a fragment shader can't write a verying variable, hence we need
	a new variable to store the normalized interpolated normal */
	n = normalize(normal);
	
	/* compute the dot product between normal and ldir */
	NdotL = max(dot(n,lightDir),0.0);

	if (NdotL > 0.0) {
		halfV = normalize(halfVector);
		NdotHV = max(dot(n,halfV),0.0);
		color += gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess);
		color += diffuse * NdotL;
	}

	//	vec4 TexColor = texture2D(tex,gl_TexCoord[0].st);


	///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////


	/* float shadeFactor = lookup(0.0, 0.0); */
	/* gl_FragColor = vec4(shadeFactor * color.rgb * TexColor.rgb, color.a * TexColor.a); */


	///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////


	vec4 TexColor = texture2D(tex,gl_TexCoord[0].st);

	vec4 shadowCoordinateWdivide = ShadowCoord / ShadowCoord.w ;
	
	// Used to lower moiré pattern and self-shadowing
	shadowCoordinateWdivide.z += 0.0005;

	//	vec2 ind = vec2(1.-shadowCoordinateWdivide.s, 1.-shadowCoordinateWdivide.t );

	//	float distanceFromLight = texture2D(ShadowMap,shadowCoordinateWdivide.st).z;
	float distanceFromLight = texture2D(ShadowMap, shadowCoordinateWdivide.st).z;
	
 	float shadow = 1.0;
 	if (ShadowCoord.w > 0.0)
	  shadow = distanceFromLight < shadowCoordinateWdivide.z ? 0.5 : 1.0 ;
  	
	/* if(shadowCoordinateWdivide.z > 0.5){ */
	/*   //	  shadow = 1.0; */
	/*   gl_FragColor = vec4(color.xyz * shadow, color[3]); */
	/* }else */
	/*   gl_FragColor = vec4(0., 0., 1., 1.); */

	//	gl_FragColor = vec4(distanceFromLight, shadowCoordinateWdivide.z, 0., 1.);

	//	gl_FragColor =	vec4(color.xyz * shadow, color[3]);


	//	gl_FragColor =	TexColor * color;
	//	gl_FragColor =  shadow * color * TexColor;



	vec3 ct,cf;
	vec4 texel;
	float intensity,at,af;
	intensity = max(dot(lightDir,normalize(normal)),0.0);

	cf = intensity * (gl_FrontMaterial.diffuse).rgb +
				  gl_FrontMaterial.ambient.rgb * 0.6;
	af = gl_FrontMaterial.diffuse.a;
	texel = texture2D(tex,gl_TexCoord[0].st);

	ct = texel.rgb;
	at = texel.a;
	gl_FragColor = vec4(ct * cf * shadow, at * af);

		// NO TEXTURE
	//	gl_FragColor = color * shadow;
	//  gl_FragColor = color;

	/* if(usingTex == 1) */
	/*   gl_FragColor = vec4(ct * cf * shadow, at * af); */
	/* else{ */
	//
	//	gl_FragColor = vec4(cf * shadow, af);
	  //	}


	//	gl_FragColor =	vec4(TexColor.rgb * color.rgb ,  color.a);
		//	gl_FragColor =	vec4(color.rgb * shadow, color.a);


	/* vec2 coord = gl_FragCoord.xy / 512.; */
	/* gl_FragColor =	vec4(texture2D(ShadowMap, coord).rgb, 1.); */
	/* gl_FragColor =	vec4(texture2D(tex, coord).rgba); */



	/* if(shadowCoordinateWdivide.xyz == clamp(shadowCoordinateWdivide.xyz, 0., 1.)){ */
	/*   gl_FragColor = vec4(color.xyz * shadow, color[3]); */
	/* } else{ */
	/*   gl_FragColor = vec4(0., 0., 1., 1.); */
	/* } */
	


	/* gl_FragColor =	vec4(TexColor.xyz * color.xyz * shadow, color[3]); */





	//		gl_FragColor =  vec4((shadowCoordinateWdivide.xyz) ,1);

	/* gl_FragColor =	texture2D(ShadowMap, coord)  + TexColor * 0.00; */


	//	gl_FragColor =	vec4(vec3(texture2D(ShadowMap, coord)) ,1);




}
		
