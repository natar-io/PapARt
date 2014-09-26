uniform sampler2D tex;
uniform sampler2D ShadowMap;

varying vec4 ShadowCoord;
varying vec4 diffuse,ambient;
varying vec3 normal,lightDir,halfVector;

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
  
  
  vec4 shadowCoordinateWdivide = ShadowCoord / ShadowCoord.w ;
  
  // Used to lower moiré pattern and self-shadowing
  shadowCoordinateWdivide.z += 0.0005;
  
  float distanceFromLight = texture2D(ShadowMap, shadowCoordinateWdivide.st).z;
  
  float shadow = 1.0;
  if (ShadowCoord.w > 0.0)
    shadow = distanceFromLight < shadowCoordinateWdivide.z ? 0.5 : 1.0 ;
  
  
  /* vec3 ct,cf; */
  /* vec4 texel; */
  /* float intensity,at,af; */
  /* intensity = max(dot(lightDir,normalize(normal)),0.0); */
  /* cf = intensity * (gl_FrontMaterial.diffuse).rgb + gl_FrontMaterial.ambient.rgb * 0.6; */
  /* af = gl_FrontMaterial.diffuse.a; */

  gl_FragColor = vec4(color.rgb * shadow, color.a);


  //  gl_FragColor = color * 0.5 + 0.5 * vec4(texture2D(ShadowMap, shadowCoordinateWdivide.st).rgb, 2);


  gl_FragColor = vec4(texture2D(ShadowMap, shadowCoordinateWdivide.st).rgb, 1);


  //  gl_FragColor = texture2D(ShadowMap, gl_TexCoord[0].st);

  //   gl_FragColor = vec4(texture2D(ShadowMap, shadowCoordinateWdivide.st).rgb, 1);
  //  gl_FragColor = vec4(1. - shadowCoordinateWdivide.z);

}
		
