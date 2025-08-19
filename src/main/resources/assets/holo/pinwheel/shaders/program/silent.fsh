
uniform sampler2D DiffuseSampler0;

in vec2 texCoord;

out vec4 fragColor;
uniform int shouldRender;

void main() {
    // Sample from the screen

    vec4 baseColor = texture(DiffuseSampler0, texCoord);
    fragColor = baseColor;
}