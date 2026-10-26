package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.ItemContent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Equipment data only: loading this bundle cannot initialize desktop registries or saves. */
public final class ItemCatalog {
  private ItemCatalog() {}

  public static ItemContent loadBundled() throws IOException {
    try (var input = ItemCatalog.class.getResourceAsStream("/content/items-v1.json")) {
      if (input == null) throw new IOException("Missing bundled item content");
      return read(input);
    }
  }

  static ItemContent read(InputStream input) throws IOException {
    int limit = 16 * 1024 * 1024;
    byte[] bytes = input.readNBytes(limit + 1);
    if (bytes.length > limit) throw new IOException("Item content exceeds size limit");
    try {
      var content = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), ItemContent.class);
      if (content == null) throw new IllegalArgumentException("Empty content");
      return content;
    } catch (RuntimeException error) {
      throw new IOException("Invalid or unsupported item content", error);
    }
  }
}
