package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.render.ObjectMapping;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ObjectMappingsBinaryIO {
  private static final byte[] MAGIC = "T4COBJ".getBytes(StandardCharsets.US_ASCII);
  private static final short VERSION = 3;
  private static final int MAX_STRING_LENGTH = 4096;

  private ObjectMappingsBinaryIO() {}

  public static List<Entry> read(File file) throws IOException, GameException {
    return BinaryCatalogueIO.read(
        file,
        MAGIC,
        "object mappings",
        version -> {
          if (version > VERSION) {
            throw new GameException("Unsupported object mappings version: " + version);
          }
        },
        (in, version) -> {
          Entry entry = new Entry();
          entry.logicalName = readString(in);
          entry.mapping =
              new ObjectMapping(
                  readIntLE(in),
                  readString(in),
                  in.readBoolean(),
                  in.readBoolean(),
                  readString(in),
                  readString(in),
                  in.readBoolean(),
                  version >= 2 ? readString(in) : "",
                  version >= 3 ? readIntLE(in) : 0);
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
          ObjectMapping mapping = entry != null ? entry.mapping : null;
          writeString(out, entry != null ? entry.logicalName : "");
          writeIntLE(out, mapping != null ? mapping.id : 0);
          writeString(out, mapping != null ? mapping.sprite : "");
          out.writeBoolean(mapping != null && mapping.clickAnimate);
          out.writeBoolean(mapping != null && mapping.mirror);
          writeString(out, mapping != null ? mapping.animateSound : "");
          writeString(out, mapping != null ? mapping.reverseAnimateSound : "");
          out.writeBoolean(mapping != null && mapping.alwaysBehindEntities);
          writeString(
              out,
              mapping != null
                  ? I18n.placeholderFor("object", entry.logicalName, mapping.displayName)
                  : "");
          writeIntLE(out, mapping != null ? mapping.depthTileOffsetY : 0);
        });
  }

  private static String readString(DataInputStream in) throws IOException {
    return BinaryIOUtils.readString(in, MAX_STRING_LENGTH);
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
    public String logicalName;
    public ObjectMapping mapping;

    public Entry() {}

    public Entry(String logicalName, ObjectMapping mapping) {
      this.logicalName = logicalName;
      this.mapping = mapping;
    }
  }
}
