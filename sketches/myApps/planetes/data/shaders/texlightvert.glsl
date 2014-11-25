#define PROCESSING_TEXLIGHT_SHADER

uniform mat4 modelview;
uniform mat4 transform;
uniform mat3 normalMatrix;
uniform mat4 texMatrix;

uniform vec4 lightPosition;

attribute vec4 vertex;
attribute vec4 color;
attribute vec3 normal;
attribute vec2 texCoord;

varying vec4 vertColor;
varying vec3 ecNormal;
varying vec3 lightDir;
varying vec4 vertTexCoord;

void main() {
  gl_Position = transform * vertex;    
  vec3 ecVertex = vec3(modelview * vertex);  
  
  ecNormal = normalize(normalMatrix * normal);    

  // Invert the light position on x and Z, for some reason.
  /*   vec3 lp = vec3(-lightPosition.x , -lightPosition.y, lightPosition.z); */
  /* lightDir = normalize(lp.xyz - ecVertex);   */

  lightDir = normalize(lightPosition.xyz - ecVertex);  
  vertColor = color;  
  
  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);        
}

