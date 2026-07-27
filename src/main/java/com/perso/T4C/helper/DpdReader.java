package com.perso.T4C.helper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Décrypteur du fichier palette .DPD (v2colori.dpd) de The 4th Coming.
 *
 * <p>Format :
 * <ul>
 *   <li>En-tête de 41 octets : hashMd5 (16), tailleDécompressée (int32 LE),
 *       tailleCompressée (int32 LE), hashMd5bis (17).</li>
 *   <li>Flux zlib (à partir de l'offset 41) → données décompressées.</li>
 *   <li>Les données décompressées sont ensuite déchiffrées par XOR octet par octet
 *       avec la clé {@code 0x66}.</li>
 *   <li>Le résultat est une suite de palettes de {@code 64 + 768} octets :
 *       64 octets d'en-tête (ignorés) puis 256 couleurs RGB (3 octets chacune).</li>
 * </ul>
 */
public class DpdReader {

    /** Clé XOR appliquée aux données décompressées du .DPD. */
    private static final byte CLEF = (byte) 0x66;

    private static final int PALETTE_HEADER = 64;
    private static final int COLORS = 256;
    private static final int PALETTE_DATA = COLORS * 3; // 768
    private static final int PALETTE_SIZE = PALETTE_HEADER + PALETTE_DATA; // 832

    /** Une palette : 256 couleurs ARGB (alpha opaque). */
    public static final class Palette {
        /** 256 entrées ARGB (0xFFrrggbb). */
        public final int[] argb;
        public final String name;

        Palette(String name, int[] argb) {
            this.name = name;
            this.argb = argb;
        }

        public int color(int index) { return argb[index & 0xFF]; }
    }

    private final Palette[] palettes;
    private final int fallbackPaletteIndex;

    public DpdReader(Path dpdFile) throws IOException {
        byte[] file = Files.readAllBytes(dpdFile);
        byte[] data = Hdr.decompressXored(file, CLEF);

        int nb = data.length / PALETTE_SIZE;
        palettes = new Palette[nb];
        int bright1 = -1;
        for (int p = 0; p < nb; p++) {
            int paletteBase = p * PALETTE_SIZE;
            String name = normalizePaletteName(cString(data, paletteBase, PALETTE_HEADER));
            if ("Bright1".equals(name)) {
                bright1 = p;
            }

            int base = paletteBase + PALETTE_HEADER;
            int[] argb = new int[COLORS];
            for (int c = 0; c < COLORS; c++) {
                int o = base + c * 3;
                int r = data[o] & 0xFF;
                int g = data[o + 1] & 0xFF;
                int b = data[o + 2] & 0xFF;
                argb[c] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
            palettes[p] = new Palette(name, argb);
        }
        fallbackPaletteIndex = bright1 >= 0 ? bright1 : 0;
    }

    public int paletteCount() { return palettes.length; }

    public Palette getPalette(int index) { return palettes[index]; }

    public Palette getPaletteForSpriteName(String spriteName) {
        return getPaletteForSpriteName(spriteName, 1);
    }

    public Palette getPaletteForSpriteName(String spriteName, int palNumber) {
        String spriteLower = (spriteName == null ? "" : spriteName.trim()).toLowerCase(Locale.ROOT);
        int bestIndex = -1;
        int bestLength = -1;

        for (int i = palettes.length - 1; i >= 0; i--) {
            String paletteName = palettes[i].name;
            if (paletteName == null || paletteName.isEmpty()) {
                continue;
            }
            String matchName = paletteNameForNumber(paletteName, palNumber);
            if (matchName == null || matchName.isEmpty()) {
                continue;
            }
            String paletteLower = matchName.toLowerCase(Locale.ROOT);
            if (spriteLower.startsWith(paletteLower) && paletteLower.length() > bestLength) {
                bestIndex = i;
                bestLength = paletteLower.length();
            }
        }

        if (bestIndex >= 0) {
            return palettes[bestIndex];
        }
        return palettes[fallbackPaletteIndex];
    }

    private static String paletteNameForNumber(String paletteName, int paletteNumber) {
        if (paletteNumber == 1) {
            return paletteName;
        }
        if (paletteNumber < 10) {
            char suffix = (char) ('0' + paletteNumber);
            return paletteName.endsWith(String.valueOf(suffix))
                    ? paletteName.substring(0, paletteName.length() - 1)
                    : null;
        }
        String suffix = String.format(Locale.ROOT, "%02d", paletteNumber);
        return paletteName.endsWith(suffix)
                ? paletteName.substring(0, paletteName.length() - 2)
                : null;
    }

    private static String cString(byte[] data, int offset, int max) {
        int end = offset;
        int limit = Math.min(data.length, offset + max);
        while (end < limit && data[end] != 0) {
            end++;
        }
        return new String(data, offset, end - offset, StandardCharsets.ISO_8859_1);
    }

    private static String normalizePaletteName(String name) {
        String normalized = name.trim();
        if (normalized.endsWith("P")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /** En-tête commun aux fichiers .DPD et .DID. */
    static final class Hdr {
        static final int HEADER_SIZE = 41;

        /** Décompresse (zlib après l'en-tête de 41 octets) puis applique le XOR. */
        static byte[] decompressXored(byte[] file, byte key) throws IOException {
            ByteBuffer h = ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN);
            int tailleUnZip = h.getInt(16);
            int tailleZip = h.getInt(20);

            byte[] data = inflate(file, HEADER_SIZE, tailleZip, tailleUnZip);
            for (int i = 0; i < data.length; i++) {
                data[i] ^= key;
            }
            return data;
        }

        static byte[] inflate(byte[] src, int off, int len, int expected) throws IOException {
            java.util.zip.Inflater inf = new java.util.zip.Inflater();
            inf.setInput(src, off, len);
            byte[] out = new byte[expected];
            try {
                int n = 0;
                while (n < expected && !inf.finished()) {
                    int r = inf.inflate(out, n, expected - n);
                    if (r == 0 && inf.needsInput()) break;
                    n += r;
                }
                if (n != expected) {
                    byte[] trimmed = new byte[n];
                    System.arraycopy(out, 0, trimmed, 0, n);
                    return trimmed;
                }
                return out;
            } catch (java.util.zip.DataFormatException e) {
                throw new IOException("zlib invalide", e);
            } finally {
                inf.end();
            }
        }
    }
}
