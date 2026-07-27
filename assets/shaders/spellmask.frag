#version 120
#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

// Sprite couleur de l'effet, dessine comme geometrie du batch.
uniform sampler2D u_texture;

// Masque de transparence : un canal de gris = un poids d'alpha 0-255, tel que stocke par la
// librairie d'origine et applique par pixel (cf. TransAlphaMask2 du client C++).
uniform sampler2D u_mask;

// Transforme les coordonnees du sprite couleur vers celles du masque. Les deux sprites ont des
// dimensions et des origines de dessin differentes : le masque est bien plus large que la
// flamme qu'il eclaire.
uniform vec2 u_maskScale;
uniform vec2 u_maskOffset;

void main() {
    vec4 baseColor = texture2D(u_texture, v_texCoord) * v_color;

    vec2 maskCoord = v_texCoord * u_maskScale + u_maskOffset;
    // Hors du masque, aucun poids n'est defini : le pixel reste tel quel.
    float weight = 1.0;
    if (maskCoord.x >= 0.0 && maskCoord.x <= 1.0 && maskCoord.y >= 0.0 && maskCoord.y <= 1.0) {
        weight = texture2D(u_mask, maskCoord).r;
    }

    gl_FragColor = vec4(baseColor.rgb, baseColor.a * weight);
}
