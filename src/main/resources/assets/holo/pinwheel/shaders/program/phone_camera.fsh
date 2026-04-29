#version 410

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
out vec4 fragColor;
uniform float VeilRenderTime;

float rand(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    float random = rand(vec2((round(texCoord0.x*200)/200)+(VeilRenderTime/4),(round(texCoord0.y*200)/200)+(VeilRenderTime/4)));
    color.rgb *= 1-vec3(random*0.2);
    if (mod(texCoord0.y+((VeilRenderTime/4)*0.001),0.05) <= 0.025) {
        fragColor = vec4(color.r*0.85,color.g*0.85,color.b*0.85, 1.0);
    } else {
        fragColor = vec4(color.r,color.g,color.b, 1.0);
    }
}
