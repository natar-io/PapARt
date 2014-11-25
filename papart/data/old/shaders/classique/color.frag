
varying float intensity;


void main()
{
  gl_FragColor = vec4(gl_Color.rgb * intensity, 1);
}
