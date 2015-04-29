#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif
 
#define PROCESSING_TEXTURE_SHADER
 
uniform sampler2D texture;
uniform vec2 blurOffset;
 
varying vec4 vertColor;
varying vec4 vertTexCoord;
 
void main() {
    vec4 sum = vec4(0.0);
    sum += texture2D(texture, vec2(vertTexCoord.x - 7.5 * blurOffset.x, vertTexCoord.y - 7.5 * blurOffset.y)) * 0.05;
    sum += texture2D(texture, vec2(vertTexCoord.x - 5.5 * blurOffset.x, vertTexCoord.y - 5.5 * blurOffset.y)) * 0.09;
    sum += texture2D(texture, vec2(vertTexCoord.x - 3.5 * blurOffset.x, vertTexCoord.y - 3.5 * blurOffset.y)) * 0.12;
    sum += texture2D(texture, vec2(vertTexCoord.x - 1.5 * blurOffset.x, vertTexCoord.y - 1.5 * blurOffset.y)) * 0.155;
    sum += texture2D(texture, vec2(vertTexCoord.x, vertTexCoord.y)) * 0.17;
    sum += texture2D(texture, vec2(vertTexCoord.x + 1.5 * blurOffset.x, vertTexCoord.y + 1.5 * blurOffset.y)) * 0.155;
    sum += texture2D(texture, vec2(vertTexCoord.x + 3.5 * blurOffset.x, vertTexCoord.y + 3.5 * blurOffset.y)) * 0.12;
    sum += texture2D(texture, vec2(vertTexCoord.x + 5.5 * blurOffset.x, vertTexCoord.y + 5.5 * blurOffset.y)) * 0.09;
    sum += texture2D(texture, vec2(vertTexCoord.x + 7.5 * blurOffset.x, vertTexCoord.y + 7.5 * blurOffset.y)) * 0.05;
    gl_FragColor = sum;
}
