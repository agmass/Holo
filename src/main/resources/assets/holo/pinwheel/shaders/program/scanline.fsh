#version 410

uniform sampler2D Sampler0;
in vec2 texCoord0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
uniform float STime;

out vec4 fragColor;
void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (mod(texCoord0.y+((STime/4)*0.001),0.005) <= 0.0025) {
        fragColor = vec4(color.r,color.g,color.b, color.a*0.5);
    } else {
        fragColor = vec4(color.r,color.g,color.b, color.a);
    }
    color *= ColorModulator;
}

