package com.perso.T4C.helper;

import com.perso.T4C.exception.GameException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BinaryCatalogueIO {
  private BinaryCatalogueIO() {}

  @FunctionalInterface
  public interface VersionCheck {
    void check(short version) throws GameException;
  }

  @FunctionalInterface
  public interface DefinitionReader<T> {
    T read(DataInputStream in, short version) throws IOException;
  }

  @FunctionalInterface
  public interface DefinitionWriter<T> {
    void write(DataOutputStream out, T definition) throws IOException;
  }

  public static <T> List<T> read(
      File file,
      byte[] magic,
      String typeLabel,
      VersionCheck versionCheck,
      DefinitionReader<T> reader)
      throws IOException, GameException {
    try (DataInputStream in = new DataInputStream(BinaryIOUtils.openInputStream(file, 1 << 16))) {
      byte[] actualMagic = new byte[magic.length];
      in.readFully(actualMagic);
      if (!Arrays.equals(actualMagic, magic)) {
        throw new GameException("Invalid " + typeLabel + " file: wrong magic header");
      }
      short version = BinaryIOUtils.readShortLE(in);
      versionCheck.check(version);
      int count = BinaryIOUtils.readIntLE(in);
      if (count < 0) {
        throw new GameException("Invalid " + typeLabel + " count: " + count);
      }
      List<T> definitions = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
        definitions.add(reader.read(in, version));
      }
      return definitions;
    }
  }

  public static <T> void write(
      File file, byte[] magic, short version, List<T> definitions, DefinitionWriter<T> writer)
      throws IOException {
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }
    try (DataOutputStream out =
        new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1 << 16))) {
      out.write(magic);
      BinaryIOUtils.writeShortLE(out, version);
      List<T> safeDefinitions = definitions == null ? List.of() : definitions;
      BinaryIOUtils.writeIntLE(out, safeDefinitions.size());
      for (T definition : safeDefinitions) {
        writer.write(out, definition);
      }
    }
  }
}
