#version 120
#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform vec2 u_texelSize;
uniform vec4 u_outlineColor;

bool isShadowPixel(vec4 color) {
    // Native DDA Shd pixels are exported as 50%-alpha black.
    return color.a > 0.45 && color.a < 0.55
        && color.r < 0.01 && color.g < 0.01 && color.b < 0.01;
}

void main() {
    vec4 baseColor = texture2D(u_texture, v_texCoord);

    if (baseColor.a > 0.1) {
        gl_FragColor = baseColor * v_color;
    } else {
        float alpha = 0.0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                vec2 offset = vec2(x, y) * u_texelSize;
                vec4 neighbor = texture2D(u_texture, v_texCoord + offset);
                if (!isShadowPixel(neighbor)) {
                    alpha = max(alpha, neighbor.a);
                }
            }
        }
        if (alpha > 0.1) {
            gl_FragColor = u_outlineColor;
        } else {
            discard;
        }
    }
}
