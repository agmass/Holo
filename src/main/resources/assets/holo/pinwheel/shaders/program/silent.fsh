
uniform sampler2D DiffuseSampler0;

in vec2 texCoord;

out vec4 fragColor;
uniform int shouldRender;
uniform float noiseTime;


float random (in vec2 st) {
    return fract(sin(dot(st.xy,
    vec2(12.9898,78.233)))
    * 43758.5453123);
}

float exponentialIn(float t) {
    return t == 0.0 ? t : pow(2.0, 10.0 * (t - 1.0));
}


void main() {
    // Sample from the screen

    vec4 baseColor = texture(DiffuseSampler0, texCoord);
    if (shouldRender == 2) {
        float f = (baseColor.r+baseColor.g)/2.0f;
        if (baseColor.b > f) {
            fragColor = vec4(f, f, baseColor.b, baseColor.a);
        } else {
            fragColor = vec4(f, f, f, baseColor.a);
        }
    }
    if (shouldRender == 1) {
        float f = (baseColor.r+baseColor.g+baseColor.b)/2.0f;
        fragColor = vec4(f, f, f, baseColor.a);
    }
    if (shouldRender == 0) {
        fragColor = baseColor;
    }
    if (noiseTime > 0) {
        float color = random(vec2(round(texCoord.x*320.0)+noiseTime,(round(texCoord.y*180.0)+noiseTime)));
        fragColor = mix(fragColor,vec4(color,color,color,1), exponentialIn(noiseTime));
    }
}