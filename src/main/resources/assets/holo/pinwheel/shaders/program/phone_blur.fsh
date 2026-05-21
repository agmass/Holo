#version 410

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
out vec4 fragColor;
vec4 blur13(sampler2D image, vec2 uv, vec2 resolution, vec2 direction) {
    vec4 color = vec4(0.0);
    vec2 off1 = vec2(1.411764705882353) * direction;
    vec2 off2 = vec2(3.2941176470588234) * direction;
    vec2 off3 = vec2(5.176470588235294) * direction;
    color += texture2D(image, uv) * 0.1964825501511404;
    color += texture2D(image, uv + (off1 / resolution)) * 0.2969069646728344;
    color += texture2D(image, uv - (off1 / resolution)) * 0.2969069646728344;
    color += texture2D(image, uv + (off2 / resolution)) * 0.09447039785044732;
    color += texture2D(image, uv - (off2 / resolution)) * 0.09447039785044732;
    color += texture2D(image, uv + (off3 / resolution)) * 0.010381362401148057;
    color += texture2D(image, uv - (off3 / resolution)) * 0.010381362401148057;
    return color;
}
void main() {
    vec4 color = blur13(Sampler0, ((texCoord0/2)+vec2(0.25,0.25))*vec2(1,-1), vec2(720,1280),vec2(1,1));
    if (color.a < 0.1) {
        discard;
    }
    fragColor = vec4(color.r,color.g,color.b,1.0);
}
