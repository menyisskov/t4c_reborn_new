package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class MusicZoneBinaryIO {
  private static final byte[] MAGIC = "T4CMUZ".getBytes(StandardCharsets.US_ASCII);
  private static final short VERSION = 1;

  private MusicZoneBinaryIO() {}

  public static List<Entry> read(File file) throws IOException, GameException {
    return BinaryCatalogueIO.read(
        file,
        MAGIC,
        "music zone binary",
        version -> {
          if (version != VERSION) {
            throw new GameException("Unsupported music zone binary version: " + version);
          }
        },
        (in, version) -> {
          Entry entry = new Entry();
          entry.name = readString(in);
          entry.x1 = readIntLE(in);
          entry.y1 = readIntLE(in);
          entry.x2 = readIntLE(in);
          entry.y2 = readIntLE(in);
          entry.music = readString(in);
          return entry;
        });
  }

  public static void write(File file, List<Entry> entries) throws IOException {
    BinaryCatalogueIO.write(
        file,
        MAGIC,
        VERSION,
        entries,
        (out, entry) -> {
          writeString(out, entry.name);
          writeIntLE(out, entry.x1);
          writeIntLE(out, entry.y1);
          writeIntLE(out, entry.x2);
          writeIntLE(out, entry.y2);
          writeString(out, entry.music);
        });
  }

  private static String readString(DataInputStream in) throws IOException {
    return BinaryIOUtils.readString(in, 1_000_000);
  }

  private static void writeString(DataOutputStream out, String value) throws IOException {
    BinaryIOUtils.writeString(out, value);
  }

  private static int readIntLE(DataInputStream in) throws IOException {
    return BinaryIOUtils.readIntLE(in);
  }

  private static void writeIntLE(DataOutputStream out, int value) throws IOException {
    BinaryIOUtils.writeIntLE(out, value);
  }

  public static final class Entry {
    public String name;
    public int x1;
    public int y1;
    public int x2;
    public int y2;
    public String music;
  }
}
