uniform sampler2D tex;

varying vec4 diffuse,ambient;
varying vec3 normal,lightDir;


void main()
{
	/* vec3 n,halfV,viewV,ldir; */
	/* float NdotL,NdotHV; */
	/* vec4 color = ambient; */
	
	/* /\* a fragment shader can't write a verying variable, hence we need */
	/* a new variable to store the normalized interpolated normal *\/ */
	/* n = normalize(normal); */
	
	/* /\* compute the dot product between normal and ldir *\/ */
	/* NdotL = max(dot(n,lightDir),0.0); */

	/* if (NdotL > 0.0) { */
	/* 	halfV = normalize(halfVector); */
	/* 	NdotHV = max(dot(n,halfV),0.0); */
	/* 	color += gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess); */
	/* 	color += diffuse * NdotL; */
	/* } */


	vec3 ct,cf;
	vec4 texel;
	float intensity,at,af;
	intensity = max(dot(lightDir,normalize(normal)),0.0);

	cf = intensity * (gl_FrontMaterial.diffuse).rgb +
				  gl_FrontMaterial.ambient.rgb * 0.6;
	af = gl_FrontMaterial.diffuse.a;
	
	//	texel = texture2D(gl_TextureMatrix[0], gl_TexCoord[0].st);
	texel = texture2D(tex, gl_TexCoord[0].st);
	//	texel = texture2D(tex,gl_TexCoord[0].st);

	ct = texel.rgb;
	at = texel.a;
	gl_FragColor = vec4(ct * cf, at * af);

}
