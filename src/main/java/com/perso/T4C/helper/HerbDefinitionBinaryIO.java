package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.harvest.HerbDefinition;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class HerbDefinitionBinaryIO {
  private static final byte[] MAGIC = "T4CHRB".getBytes(StandardCharsets.US_ASCII);
  private static final short VERSION = 1;
  private static final int MAX_STRING_BYTES = 4096;

  private HerbDefinitionBinaryIO() {}

  public static List<HerbDefinition> read(File file) throws IOException, GameException {
    return BinaryCatalogueIO.read(
        file,
        MAGIC,
        "herb definition",
        version -> {
          if (version != VERSION)
            throw new GameException("Unsupported herb definition version: " + version);
        },
        HerbDefinitionBinaryIO::readDefinition);
  }

  public static void write(File file, List<HerbDefinition> definitions) throws IOException {
    BinaryCatalogueIO.write(
        file, MAGIC, VERSION, definitions, HerbDefinitionBinaryIO::writeDefinition);
  }

  private static HerbDefinition readDefinition(DataInputStream in, short version)
      throws IOException {
    return new HerbDefinition(
        BinaryIOUtils.readString(in, MAX_STRING_BYTES),
        BinaryIOUtils.readString(in, MAX_STRING_BYTES),
        BinaryIOUtils.readString(in, MAX_STRING_BYTES),
        BinaryIOUtils.readIntLE(in));
  }

  private static void writeDefinition(DataOutputStream out, HerbDefinition definition)
      throws IOException {
    BinaryIOUtils.writeString(out, definition == null ? "" : definition.getId());
    BinaryIOUtils.writeString(out, definition == null ? "" : definition.getItemKey());
    BinaryIOUtils.writeString(out, definition == null ? "" : definition.getWorldSprite());
    BinaryIOUtils.writeIntLE(out, definition == null ? 0 : definition.getSpawnWeight());
  }
}
