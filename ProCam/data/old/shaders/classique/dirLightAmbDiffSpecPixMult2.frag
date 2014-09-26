/* -------------------------------------------------------

This shader implements a directional light per pixel using  the 
diffuse, specular, and ambient terms acoording to "Mathematics of Lighthing" 
as found in the book "OpenGL Programming Guide" (aka the Red Book)

António Ramires Fernandes

--------------------------------------------------------- */

varying vec4 diffuse,diffuse2,ambient;
varying vec3 normal,lightDir,halfVector,lightDir2,halfVector2;

void main()
{
	vec3 n,halfV,viewV,ldir;
	float NdotL,NdotL2,NdotHV;
	float intensity, intens1, intens2;
	//	vec4 color = ambient;
	vec4 color = vec4(0, 0, 0, 0);
	
	/* a fragment shader can't write a verying variable, hence we need
	a new variable to store the normalized interpolated normal */
	n = normalize(normal);
	
	/* compute the dot product between normal and ldir */
	NdotL = max(dot(n,lightDir),0.0);
	NdotL2 = max(dot(n,lightDir2),0.0);

	if (NdotL > 0.0) {
	  //		halfV = normalize(halfVector);
		//		NdotHV = max(dot(n,halfV),0.0);
		//		color += gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess);
		color += diffuse * NdotL;
	}

	if (NdotL2 > 0.0) {
	  //		halfV = normalize(halfVector2);
		//		NdotHV = max(dot(n,halfV),0.0);
		//		color += gl_FrontMaterial.specular * gl_LightSource[1].specular * pow(NdotHV,gl_FrontMaterial.shininess);
		color += diffuse2 * NdotL2;
	}

	intensity = (NdotL + NdotL2) * 0.8;

	intens1 = NdotL / intensity;
	intens2 = NdotL2 / intensity;

	//		if(intensity >= 1.)
	color = diffuse * NdotL * intens1 + diffuse2 * NdotL2 * intens2;
	/* else */
	/*   color = diffuse * NdotL + diffuse2 * NdotL2 ; */
	//	color = gl_LightSource[0].diffuse * 0.5 * NdotL + gl_LightSource[1].diffuse * NdotL2  * 0.5;

	gl_FragColor = color;
}
