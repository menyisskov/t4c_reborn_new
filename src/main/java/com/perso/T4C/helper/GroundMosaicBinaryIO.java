package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class GroundMosaicBinaryIO {
  private static final byte[] MAGIC = "T4CGMO".getBytes(StandardCharsets.US_ASCII);
  private static final short VERSION = 1;
  private static final int MAX_STRING_BYTES = 4096;

  private GroundMosaicBinaryIO() {}

  public static List<Definition> read(File file) throws IOException, GameException {
    List<Definition> definitions =
        BinaryCatalogueIO.read(
            file,
            MAGIC,
            "ground mosaics binary",
            version -> {
              if (version != VERSION) {
                throw new GameException("Unsupported ground mosaics binary version: " + version);
              }
            },
            (in, version) -> {
              String id = BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim();
              int width = BinaryIOUtils.readIntLE(in);
              int height = BinaryIOUtils.readIntLE(in);
              int frameCount = BinaryIOUtils.readIntLE(in);
              if (frameCount < 0) {
                throw new GameException(
                    "Invalid frame count for ground mosaic " + id + ": " + frameCount);
              }
              List<String> frames = new ArrayList<>(frameCount);
              for (int f = 0; f < frameCount; f++) {
                frames.add(BinaryIOUtils.readString(in, MAX_STRING_BYTES).trim());
              }
              return new Definition(id, width, height, List.copyOf(frames));
            });
    return List.copyOf(definitions);
  }

  public static void write(File file, List<Definition> definitions) throws IOException {
    BinaryCatalogueIO.write(
        file,
        MAGIC,
        VERSION,
        definitions,
        (out, definition) -> {
          BinaryIOUtils.writeString(out, definition.id());
          BinaryIOUtils.writeIntLE(out, definition.width());
          BinaryIOUtils.writeIntLE(out, definition.height());
          List<String> frames = definition.frames();
          BinaryIOUtils.writeIntLE(out, frames.size());
          for (String frame : frames) {
            BinaryIOUtils.writeString(out, frame != null ? frame.trim() : "");
          }
        });
  }

  public record Definition(String id, int width, int height, List<String> frames) {}
}
