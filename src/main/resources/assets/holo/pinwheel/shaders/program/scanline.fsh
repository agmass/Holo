#version 410

uniform sampler2D Sampler0;
in vec2 texCoord0;

uniform float STime;

out vec4 fragColor;
void main() {
    vec4 color = texture(Sampler0, texCoord0);
    color.r *= 0.5;
    color.g *= 0.5;
    if (mod(round((texCoord0.y+STime)*500.0),2) == 0) {
        fragColor = vec4(color.r,color.g,color.b, color.a*0.5);
    } else {
        fragColor = vec4(color.r,color.g,color.b, color.a);
    }
}

