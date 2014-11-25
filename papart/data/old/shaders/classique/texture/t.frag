uniform sampler2D tex;

varying vec4 diffuse,ambient;
varying vec3 normal,lightDir;


void main()
{

	vec3 ct,cf;
	vec4 texel;
	float intensity,at,af;
	intensity = max(dot(lightDir,normalize(normal)),0.0);

	/* cf = intensity * (gl_FrontMaterial.diffuse).rgb + */
	/* 			  gl_FrontMaterial.ambient.rgb * 0.6; */

	cf = intensity * diffuse + ambient;
	//				  gl_FrontMaterial.ambient.rgb * 0.6;

	af = gl_FrontMaterial.diffuse.a;
	texel = texture2D(tex,gl_TexCoord[0].st);

	ct = texel.rgb;
	at = texel.a;
	gl_FragColor = vec4(ct * cf, at * af);

	//		gl_FragColor = vec4(cf, 1);
}
