package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class SpawnBinaryIO {
  private static final byte[] MAGIC = "T4CSPN".getBytes(StandardCharsets.US_ASCII);
  private static final short VERSION = 1;
  private static final int FLAG_STATIONARY = 1;
  private static final int FLAG_AGGRESSIVE = 1 << 1;

  private SpawnBinaryIO() {}

  public static List<Entry> read(File file) throws IOException, GameException {
    return BinaryCatalogueIO.read(
        file,
        MAGIC,
        "spawn binary",
        version -> {
          if (version != VERSION) {
            throw new GameException("Unsupported spawn binary version: " + version);
          }
        },
        (in, version) -> {
          Entry entry = new Entry();
          entry.type = BinaryIOUtils.readString(in, 4096);
          entry.x = readIntLE(in);
          entry.y = readIntLE(in);
          entry.z = readIntLE(in);
          int flags = in.readUnsignedByte();
          entry.stationary = (flags & FLAG_STATIONARY) != 0;
          entry.aggressive = (flags & FLAG_AGGRESSIVE) != 0;
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
          BinaryIOUtils.writeString(out, entry.type);
          writeIntLE(out, entry.x);
          writeIntLE(out, entry.y);
          writeIntLE(out, entry.z);
          int flags = 0;
          if (entry.stationary) {
            flags |= FLAG_STATIONARY;
          }
          if (entry.aggressive) {
            flags |= FLAG_AGGRESSIVE;
          }
          out.writeByte(flags);
        });
  }

  private static int readIntLE(DataInputStream in) throws IOException {
    return BinaryIOUtils.readIntLE(in);
  }

  private static void writeIntLE(DataOutputStream out, int value) throws IOException {
    BinaryIOUtils.writeIntLE(out, value);
  }

  public static final class Entry {
    public String type;
    public int x;
    public int y;
    public int z;
    public boolean stationary;
    public boolean aggressive;
  }
}
