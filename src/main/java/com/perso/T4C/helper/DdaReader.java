package com.perso.T4C.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.InflaterInputStream;

public class DdaReader {
  private static final long[] CLEF_DDA = {
    0x1458AAAAL, 0x62421234L, 0xF6C32355L, 0xAAAAAAF3L, 0x12344321L, 0xDDCCBBAAL, 0xAABBCCDDL
  };
  public static final int HEADER_SIZE = 28;
  private static final int ZLIB_THRESHOLD = 180;
  private static final int SHADOW_CHUNK = 1;

  public static final class SpriteHeader {
    public final int typeSprite;
    public final int ombre;
    public final int largeur;
    public final int hauteur;
    public final short offsetX;
    public final short offsetY;
    public final short offsetX2;
    public final short offsetY2;
    public final int inconnu9;
    public final int couleurTrans;
    public final long nbBytes;
    public final long nbBytesC;

    SpriteHeader(byte[] h) {
      ByteBuffer b = ByteBuffer.wrap(h);
      ombre = b.getShort() & 0xFFFF;
      typeSprite = b.getShort() & 0xFFFF;
      hauteur = b.getShort() & 0xFFFF;
      largeur = b.getShort() & 0xFFFF;
      offsetY = b.getShort();
      offsetX = b.getShort();
      offsetY2 = b.getShort();
      offsetX2 = b.getShort();
      couleurTrans = b.getShort() & 0xFFFF;
      inconnu9 = b.getShort() & 0xFFFF;
      nbBytes = b.getInt() & 0xFFFFFFFFL;
      nbBytesC = b.getInt() & 0xFFFFFFFFL;
    }
  }

  public static final class Sprite {
    public final SpriteHeader header;
    public final byte[] pixels;
    public final byte[] shadow;

    Sprite(SpriteHeader header, byte[] pixels) {
      this(header, pixels, null);
    }

    Sprite(SpriteHeader header, byte[] pixels, byte[] shadow) {
      this.header = header;
      this.pixels = pixels;
      this.shadow = shadow;
    }

    public int getLargeur() {
      return header.largeur;
    }

    public int getHauteur() {
      return header.hauteur;
    }

    public boolean hasShadow() {
      return shadow != null;
    }
  }

  private final byte[] data;

  public DdaReader(Path ddaFile) throws IOException {
    this.data = Files.readAllBytes(ddaFile);
  }

  public DdaReader(byte[] ddaContent) {
    this.data = ddaContent;
  }

  public SpriteHeader readHeader(int offset) {
    if (offset < 0 || offset + HEADER_SIZE > data.length) {
      throw new IndexOutOfBoundsException("Header offset out of bounds: " + offset);
    }
    byte[] h = new byte[HEADER_SIZE];
    for (int j = 0; j < 7; j++) {
      int p = offset + j * 4;
      long v =
          (data[p] & 0xFFL)
              | ((data[p + 1] & 0xFFL) << 8)
              | ((data[p + 2] & 0xFFL) << 16)
              | ((data[p + 3] & 0xFFL) << 24);
      long dec = v ^ CLEF_DDA[j];
      h[j * 4] = (byte) (dec >>> 24);
      h[j * 4 + 1] = (byte) (dec >>> 16);
      h[j * 4 + 2] = (byte) (dec >>> 8);
      h[j * 4 + 3] = (byte) dec;
    }
    return new SpriteHeader(h);
  }

  public Sprite readSprite(int offset) throws IOException {
    SpriteHeader header = readHeader(offset);
    int dataStart = offset + HEADER_SIZE;
    byte[] pixels;
    int expected = header.largeur * header.hauteur;
    switch (header.typeSprite) {
      case 1:
        pixels = readBytes(dataStart, expected, offset);
        break;
      case 2:
        byte[] rleSource = readBytes(dataStart, (int) header.nbBytesC, offset);
        if (header.largeur > ZLIB_THRESHOLD || header.hauteur > ZLIB_THRESHOLD) {
          rleSource = inflate(rleSource);
        }
        return decompressRLE(rleSource, header);
      case 3:
        pixels = new byte[Math.max(0, expected)];
        fill(pixels, (byte) header.couleurTrans);
        break;
      case 9:
        pixels = decompressType9(dataStart, header, offset);
        break;
      case 10:
        int rawLength = readLE32(data, dataStart);
        byte[] raw = readBytes(dataStart + 4, rawLength, offset);
        pixels = new byte[Math.max(0, expected)];
        fill(pixels, (byte) header.couleurTrans);
        System.arraycopy(raw, 0, pixels, 0, Math.min(raw.length, pixels.length));
        break;
      default:
        throw new IOException("Unknown sprite type: " + header.typeSprite + " at offset " + offset);
    }
    return new Sprite(header, pixels);
  }

  private byte[] decompressType9(int dataStart, SpriteHeader header, int spriteOffset)
      throws IOException {
    int compressedLength = (int) header.nbBytesC;
    if (isZlibHeader(data, dataStart + 4)) {
      int unzipLen = readLE32(data, dataStart);
      return inflate(readBytes(dataStart + 4, compressedLength, spriteOffset), unzipLen);
    }
    byte[] decoded =
        inflate(readBytes(dataStart, compressedLength, spriteOffset), (int) header.nbBytes);
    if (decoded.length >= 6 && isZlibHeader(decoded, 4)) {
      int unzipLen = readLE32(decoded, 0);
      return inflate(slice(decoded, 4, decoded.length - 4), unzipLen);
    }
    return decoded;
  }

  private static boolean isZlibHeader(byte[] bytes, int offset) {
    if (offset < 0 || offset + 2 > bytes.length) return false;
    int cmf = bytes[offset] & 0xFF;
    int flg = bytes[offset + 1] & 0xFF;
    return (cmf & 0x0F) == 8 && ((cmf << 8) + flg) % 31 == 0;
  }

  private byte[] readBytes(int start, int len, int spriteOffset) throws IOException {
    if (len < 0 || start < 0 || start + len > data.length) {
      throw new IOException(
          "Sprite data out of bounds at offset " + spriteOffset + " (length=" + len + ")");
    }
    byte[] raw = new byte[len];
    System.arraycopy(data, start, raw, 0, len);
    return raw;
  }

  private static Sprite decompressRLE(byte[] src, SpriteHeader h) {
    int largeur = h.largeur;
    int hauteur = h.hauteur;
    byte[] out = new byte[largeur * hauteur];
    fill(out, (byte) h.couleurTrans);
    byte[] shadow = null;
    int p = 0;
    int y = 0;
    int len = src.length;
    while (p + 4 <= len) {
      int x = (src[p] & 0xFF) | ((src[p + 1] & 0xFF) << 8);
      p += 2;
      int nbPix = (src[p] & 0xFF) * 4 + (src[p + 1] & 0xFF);
      p += 2;
      if (p >= len) break;
      int control = src[p] & 0xFF;
      if (control == SHADOW_CHUNK) {
        if (shadow == null) {
          shadow = new byte[largeur * hauteur];
        }
        markShadowRun(shadow, largeur, x, y, nbPix);
      } else {
        for (int i = 0; i < nbPix; i++) {
          p++;
          if (p >= len) break;
          int dest = i + x + (y * largeur);
          if (dest >= 0 && dest < out.length) {
            out[dest] = src[p];
          }
          if ((i + x) == largeur - 1) break;
        }
      }
      p++;
      if (p >= len) break;
      int code = src[p] & 0xFF;
      if (code == 0) {
        break;
      } else if (code == 2) {
        y++;
      }
      p++;
      if (y >= hauteur) break;
    }
    return new Sprite(h, out, shadow);
  }

  private static void markShadowRun(byte[] shadow, int largeur, int x, int y, int nbPix) {
    int rowStart = y * largeur;
    if (rowStart < 0 || rowStart >= shadow.length) {
      return;
    }
    for (int i = 0; i < nbPix; i++) {
      int px = x + i;
      if (px < 0 || px >= largeur) {
        continue;
      }
      int dest = rowStart + px;
      if (dest >= 0 && dest < shadow.length) {
        shadow[dest] = 1;
      }
    }
  }

  private static byte[] inflate(byte[] src) throws IOException {
    return inflate(src, Math.max(64, src.length * 2));
  }

  private static byte[] inflate(byte[] src, int expectedSize) throws IOException {
    try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(src))) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(64, expectedSize));
      byte[] buf = new byte[8192];
      int n;
      while ((n = in.read(buf)) != -1) {
        out.write(buf, 0, n);
      }
      return out.toByteArray();
    }
  }

  private static int readLE32(byte[] src, int offset) throws IOException {
    if (offset < 0 || offset + 4 > src.length) {
      throw new IOException("32-bit integer out of bounds");
    }
    return (src[offset] & 0xFF)
        | ((src[offset + 1] & 0xFF) << 8)
        | ((src[offset + 2] & 0xFF) << 16)
        | ((src[offset + 3] & 0xFF) << 24);
  }

  private static byte[] slice(byte[] src, int offset, int len) throws IOException {
    if (len < 0 || offset < 0 || offset + len > src.length) {
      throw new IOException("Data slice out of bounds");
    }
    byte[] out = new byte[len];
    System.arraycopy(src, offset, out, 0, len);
    return out;
  }

  private static void fill(byte[] arr, byte value) {
    java.util.Arrays.fill(arr, value);
  }
}
