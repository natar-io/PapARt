
/* -------------------------------------------------------

This shader implements a directional light per pixel using  the 
diffuse, specular, and ambient terms acoording to "Mathematics of Lighthing" 
as found in the book "OpenGL Programming Guide" (aka the Red Book)

António Ramires Fernandes

--------------------------------------------------------- */

varying vec4 diffuse,ambient;
varying vec3 normal,lightDir,halfVector;


void main()
{
	vec3 n,halfV,viewV,ldir;
	float NdotL,NdotHV;
	vec4 color = ambient;
	vec4 _color;

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


	/* if (intensity > 0.98) */
	/*   _color = vec4(1.0,1.0,1.0,1.0); */
	/* else  */

	if (color.r > 0.5)
	  _color = vec4(0.8,0.8,0.8,1.0);
	else if (color.r > 0.35)
	  _color = vec4(0.4,0.4,0.4,1.0);
	else
	  _color = vec4(0.0,0.0,0.0,1.0);

	gl_FragColor = _color;

	//	gl_FragColor = color;
}







/* varying vec3 normal, lightDir; */
/* varying vec2 texCoord; */
/* //uniform sampler2D texture; */

/* void main() */
/* { */
/*   float intensity; */
/*   vec3 n; */
/*   vec4 _color; */

/*   n = normalize(normal); */
/*   intensity = dot(lightDir, n); */

/*   /\* if (intensity > 0.98) *\/ */
/*   /\*   _color = vec4(1.0,1.0,1.0,1.0); *\/ */
/*   /\* else  *\/ */
/*     if (intensity > 0.5) */
/*     _color = vec4(0.8,0.8,0.8,1.0); */
/*   else if (intensity > 0.35) */
/*     _color = vec4(0.4,0.4,0.4,1.0); */
/*   else */
/*   _color = vec4(0.0,0.0,0.0,1.0); */
/* //  gl_FragColor = _color * texture2D(texture, texCoord); */
/*  gl_FragColor = _color; */
/* } */
