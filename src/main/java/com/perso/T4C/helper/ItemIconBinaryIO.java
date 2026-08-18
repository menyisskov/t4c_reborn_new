package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ItemIconBinaryIO {
  private static final byte[] MAGIC = "T4CICO".getBytes(StandardCharsets.US_ASCII);
  private static final short VERSION = 1;
  private static final int MAX_SPRITE_BYTES = 4096;

  private ItemIconBinaryIO() {}

  public static Map<Integer, String> read(File file) throws IOException, GameException {
    try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
      byte[] magic = new byte[MAGIC.length];
      in.readFully(magic);
      if (!Arrays.equals(magic, MAGIC)) {
        throw new GameException("Invalid item icons binary file: wrong magic header");
      }
      short version = BinaryIOUtils.readShortLE(in);
      if (version != VERSION) {
        throw new GameException("Unsupported item icons binary version: " + version);
      }
      int count = BinaryIOUtils.readIntLE(in);
      if (count < 0) {
        throw new GameException("Invalid item icon count: " + count);
      }
      Map<Integer, String> icons = new LinkedHashMap<>(count);
      for (int i = 0; i < count; i++) {
        int appearanceId = BinaryIOUtils.readIntLE(in);
        String sprite = BinaryIOUtils.readString(in, MAX_SPRITE_BYTES).trim();
        if (appearanceId > 0 && !sprite.isEmpty()) {
          icons.put(appearanceId, sprite);
        }
      }
      return icons;
    }
  }

  public static void write(File file, Map<Integer, String> icons) throws IOException {
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }
    try (DataOutputStream out =
        new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
      out.write(MAGIC);
      BinaryIOUtils.writeShortLE(out, VERSION);
      BinaryIOUtils.writeIntLE(out, icons != null ? icons.size() : 0);
      if (icons == null) {
        return;
      }
      for (Map.Entry<Integer, String> entry : icons.entrySet()) {
        BinaryIOUtils.writeIntLE(out, entry.getKey());
        BinaryIOUtils.writeString(out, entry.getValue() != null ? entry.getValue().trim() : "");
      }
    }
  }
}
