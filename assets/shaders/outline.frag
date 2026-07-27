#version 120
#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform vec2 u_texelSize;
uniform vec4 u_outlineColor;

void main() {
    vec4 baseColor = texture2D(u_texture, v_texCoord);

    if (baseColor.a > 0.1) {
        gl_FragColor = baseColor * v_color;
    } else {
        float alpha = 0.0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                vec2 offset = vec2(x, y) * u_texelSize;
                alpha = max(alpha, texture2D(u_texture, v_texCoord + offset).a);
            }
        }
        if (alpha > 0.1) {
            gl_FragColor = u_outlineColor;
        } else {
            discard;
        }
    }
}
