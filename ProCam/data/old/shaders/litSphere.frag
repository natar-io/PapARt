uniform sampler2D tex0;
uniform sampler2D tex1;
uniform float enhance;
varying vec3 normal, lightDir;
varying float curvature;

void main () {
    vec2 pos = normalize(normal + lightDir).xy; 
    vec2 posInTex = vec2( 0.5 + pos.x * 0.5, 0.5 - pos.y * 0.5 ); 

    vec4 color0 = texture2D( tex0, posInTex );
    vec4 color1 = texture2D( tex1, posInTex );
    gl_FragData[0] = color0;
}
