/* -------------------------------------------------------

This shader implements a directional light per pixel using  the 
diffuse, specular, and ambient terms acoording to "Mathematics of Lighthing" 
as found in the book "OpenGL Programming Guide" (aka the Red Book)

António Ramires Fernandes

--------------------------------------------------------- */

varying vec4 diffuse,ambient;
varying vec3 normal,lightDir,halfVector;
varying vec4 v_color; 

void main()
{
	vec3 n,halfV,viewV,ldir;
	float NdotL,NdotHV;

       	//vec4 color = ambient;
	//	vec4 color = v_color;
	vec4 color = vec4(0, 0, 0, 0);
		

	/* a fragment shader can't write a verying variable, hence we need
	a new variable to store the normalized interpolated normal */
	n = normalize(normal);
	
	/* compute the dot product between normal and ldir */
	NdotL = max(dot(n,lightDir),0.0);

	if (NdotL > 0.4) {
		halfV = normalize(halfVector);
		NdotHV = max(dot(n,halfV),0.0);

		/* color += gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess); */

		//		color += gl_LightSource[0].ambient * pow(NdotHV, 10.);
		color += gl_LightSource[0].ambient * NdotL * v_color;
		//		color += diffuse * NdotL;
	}

	gl_FragColor = v_color;

}
