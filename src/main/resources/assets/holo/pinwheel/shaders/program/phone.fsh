#version 410

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    fragColor = vec4(color.r,color.g,color.b,1.0);
}
