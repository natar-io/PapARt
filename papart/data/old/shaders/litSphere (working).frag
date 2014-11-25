
uniform sampler2D litTexture;

varying vec3 normal;

vec4 litSphereColor(in vec3 n) {
  //Normal to camera space
 
  vec2 pos = normalize(n).xy; 
  vec2 posInTex = vec2( 0.5 + pos.x * 0.5, 0.5 - pos.y * 0.5 ); 
  vec4 color = texture2D(litTexture, posInTex);
 
//    vec2 pos = normalize(normal).xy; 

///    vec2 posInTex = vec2( 0.5 + pos.x * 0.5, 0.5 - pos.y * 0.5 ); 
///    vec4 color0 = texture2D( tex0, posInTex );
///    vec4 color1 = texture2D( tex1, posInTex );
///    float c = clamp(0.5 + 0.5*tanh(curvature*enhance), 0.0,1.0); 
///    gl_FragData[0] = c*color0 + (1.0-c)*color1;

  return color;
}

void main() {
  gl_FragColor = litSphereColor(normal);
}
