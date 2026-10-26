package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.MonsterContent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Loads registry-authored data without a renderer, classpath scanning or desktop saves. */
public final class MonsterCatalog {
  private MonsterCatalog() {}

  public static MonsterContent loadBundled() throws IOException {
    try (var input = MonsterCatalog.class.getResourceAsStream("/content/monsters-v1.json")) {
      if (input == null) throw new IOException("Missing bundled monster content");
      return read(input);
    }
  }

  static MonsterContent read(InputStream input) throws IOException {
    int limit = 16 * 1024 * 1024;
    byte[] bytes = input.readNBytes(limit + 1);
    if (bytes.length > limit) throw new IOException("Monster content exceeds size limit");
    try {
      var content = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), MonsterContent.class);
      if (content == null) throw new IllegalArgumentException("Empty content");
      return content;
    } catch (RuntimeException error) {
      throw new IOException("Invalid or unsupported monster content", error);
    }
  }
}
