package com.perso.T4C.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VsfReader {
  private static final int MAGIC_SIGNATURE = 0x1A;
  private static final int TREE_START = 0x1D;
  public static final int SPRITE_HEADER_SIZE = 32;
  public static final int KIND_INDEXED = 2;
  public static final int KIND_ALPHA_MASK = 4;
  private static final int RESOURCE_TYPE_SPRITE = 1;
  private static final int RESOURCE_TABLE_GAP = 9;
  private static final int LAST_NODE_END_FIELD = 5;

  public record TreeNode(String name, int selfId, int parentId) {}

  public record Resource(String name, int type, int offset) {
    public boolean isSprite() {
      return type == RESOURCE_TYPE_SPRITE;
    }
  }

  public record SpriteHeader(
      int treeId,
      int ombre,
      int typeSprite,
      int width,
      int height,
      int originX,
      int originY,
      int kind,
      int dataSize) {
    public boolean isAlphaMask() {
      return kind == KIND_ALPHA_MASK;
    }
  }

  public record Sprite(SpriteHeader header, byte[] pixels) {}

  private final byte[] data;
  private final List<TreeNode> tree = new ArrayList<>();
  private final List<Resource> resources = new ArrayList<>();

  public VsfReader(Path decryptedFile) throws IOException {
    this(Files.readAllBytes(decryptedFile));
  }

  public VsfReader(byte[] decryptedContent) throws IOException {
    this.data = decryptedContent;
    int magic = readLE32(0);
    if ((magic & 0xFF) != MAGIC_SIGNATURE) {
      throw new IOException(String.format("Not a decrypted VSF file (magic=0x%X)", magic));
    }
    int treeEnd = readLE32(0x0C);
    int treeCount = readLE32(0x10);
    int resourceEnd = readTree(treeCount);
    readResources(treeEnd + RESOURCE_TABLE_GAP, resourceEnd);
  }

  public List<TreeNode> getTree() {
    return tree;
  }

  public List<Resource> getResources() {
    return resources;
  }

  private int readTree(int treeCount) throws IOException {
    int p = TREE_START;
    int resourceEnd = data.length;
    for (int i = 0; i < treeCount; i++) {
      int nameLen = readLE16(p);
      p += 2;
      String name = readString(p, nameLen);
      p += nameLen;
      if (i == treeCount - 1) {
        resourceEnd = readLE32(p + LAST_NODE_END_FIELD);
        tree.add(new TreeNode(name, 0, 0));
        break;
      }
      p++;
      int selfId = readLE32(p);
      p += 4;
      int parentId = readLE32(p);
      p += 4;
      tree.add(new TreeNode(name, selfId, parentId));
    }
    return resourceEnd;
  }

  private void readResources(int start, int end) throws IOException {
    int p = start;
    int limit = Math.min(end, data.length);
    while (p + 7 <= limit) {
      int nameLen = readLE16(p);
      if (nameLen <= 0 || p + 2 + nameLen + 5 > limit) {
        break;
      }
      p += 2;
      String name = readString(p, nameLen);
      p += nameLen;
      int type = data[p] & 0xFF;
      p++;
      int offset = readLE32(p);
      p += 4;
      resources.add(new Resource(name, type, offset));
    }
  }

  public SpriteHeader readSpriteHeader(int offset) throws IOException {
    if (offset < 0 || offset + SPRITE_HEADER_SIZE > data.length) {
      throw new IOException("Sprite header out of bounds: " + offset);
    }
    int treeId = readLE32(offset);
    int ombre = data[offset + 4] & 0xFF;
    int typeSprite = data[offset + 5] & 0xFF;
    int width = readLE16(offset + 6);
    int height = readLE16(offset + 8);
    int originX = readLE16Signed(offset + 10);
    int originY = readLE16Signed(offset + 12);
    int kind = readLE32(offset + 20);
    int dataSize = readLE32(offset + 28);
    return new SpriteHeader(
        treeId, ombre, typeSprite, width, height, originX, originY, kind, dataSize);
  }

  public Sprite readSprite(int offset) throws IOException {
    SpriteHeader header = readSpriteHeader(offset);
    int payloadStart = offset + SPRITE_HEADER_SIZE;
    int size = header.dataSize();
    if (size < 0 || payloadStart + size > data.length) {
      throw new IOException("Sprite payload out of bounds at " + offset);
    }
    byte[] payload = new byte[size];
    System.arraycopy(data, payloadStart, payload, 0, size);
    byte[] pixels = decompressRLE(payload, header.width(), header.height());
    return new Sprite(header, pixels);
  }

  private static byte[] decompressRLE(byte[] src, int width, int height) {
    byte[] out = new byte[Math.max(0, width * height)];
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
      if (control != 1) {
        for (int i = 0; i < nbPix; i++) {
          p++;
          if (p >= len) break;
          int dest = i + x + (y * width);
          if (dest >= 0 && dest < out.length) {
            out[dest] = src[p];
          }
          if ((i + x) == width - 1) break;
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
      if (y >= height) break;
    }
    return out;
  }

  private String readString(int offset, int length) throws IOException {
    if (offset < 0 || offset + length > data.length) {
      throw new IOException("String out of bounds at " + offset);
    }
    return new String(data, offset, length, StandardCharsets.US_ASCII);
  }

  private int readLE16(int offset) throws IOException {
    if (offset < 0 || offset + 2 > data.length) {
      throw new IOException("16-bit value out of bounds at " + offset);
    }
    return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
  }

  private int readLE16Signed(int offset) throws IOException {
    return (short) readLE16(offset);
  }

  private int readLE32(int offset) throws IOException {
    if (offset < 0 || offset + 4 > data.length) {
      throw new IOException("32-bit value out of bounds at " + offset);
    }
    return (data[offset] & 0xFF)
        | ((data[offset + 1] & 0xFF) << 8)
        | ((data[offset + 2] & 0xFF) << 16)
        | ((data[offset + 3] & 0xFF) << 24);
  }
}
