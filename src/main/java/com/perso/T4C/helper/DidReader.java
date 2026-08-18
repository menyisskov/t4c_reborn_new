package com.perso.T4C.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DidReader {
  private static final byte CLEF = (byte) 0x99;
  private static final int NAME_LEN = 64;
  private static final int PATH_LEN = 256;
  private static final int ENTRY_SIZE = NAME_LEN + PATH_LEN + 4 + 8;

  public static final class Entry {
    public final String name;
    public final String atlas;
    public final long indexation;
    public final long numDda;

    Entry(String name, String atlas, long indexation, long numDda) {
      this.name = name;
      this.atlas = atlas;
      this.indexation = indexation;
      this.numDda = numDda;
    }

    public int spriteOffset() {
      return (int) (indexation + 4);
    }

    @Override
    public String toString() {
      return name + " (dda=" + numDda + ", off=" + spriteOffset() + ")";
    }
  }

  private final List<Entry> entries;

  public DidReader(Path didFile) throws IOException {
    byte[] data = DpdReader.Hdr.decompressXored(Files.readAllBytes(didFile), CLEF);
    int count = data.length / ENTRY_SIZE;
    entries = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      int o = i * ENTRY_SIZE;
      String name = cString(data, o, NAME_LEN);
      String atlas = cString(data, o + NAME_LEN, PATH_LEN);
      int p = o + NAME_LEN + PATH_LEN;
      long indexation = readU32(data, p);
      long numDda = readI64(data, p + 4);
      entries.add(new Entry(name, atlas, indexation, numDda));
    }
  }

  public List<Entry> getEntries() {
    return entries;
  }

  public int size() {
    return entries.size();
  }

  private static String cString(byte[] b, int off, int max) {
    int end = off;
    int limit = off + max;
    while (end < limit && b[end] != 0) end++;
    return new String(b, off, end - off, StandardCharsets.ISO_8859_1).trim();
  }

  private static long readU32(byte[] b, int o) {
    return (b[o] & 0xFFL)
        | ((b[o + 1] & 0xFFL) << 8)
        | ((b[o + 2] & 0xFFL) << 16)
        | ((b[o + 3] & 0xFFL) << 24);
  }

  private static long readI64(byte[] b, int o) {
    long v = 0;
    for (int i = 0; i < 8; i++) v |= (b[o + i] & 0xFFL) << (i * 8);
    return v;
  }
}
