

varying float intensity;

void main()
{
	vec3 lightDir = normalize(vec3(gl_LightSource[0].position));
	intensity = dot(lightDir,gl_Normal);
  gl_FrontColor = gl_Color;
  gl_Position = ftransform();
}
