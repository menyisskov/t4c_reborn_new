package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.NpcShopContent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Validated generic NPC shop stock shared from the original offline vendor catalog. */
public final class NpcShopCatalog {
  private static final int MAX_BYTES = 1 * 1024 * 1024;
  private final NpcShopContent content;

  public NpcShopCatalog(NpcShopContent content, ItemContent items) {
    this.content = Objects.requireNonNull(content);
    Objects.requireNonNull(items);
    for (var vendor : content.vendors()) {
      for (var offer : vendor.offers()) {
        var definition = items.items().get(offer.itemKey());
        if (definition == null) {
          throw new IllegalArgumentException("Unknown shop item: " + offer.itemKey());
        }
        if (offer.buyPrice() != definition.price())
          throw new IllegalArgumentException("Shop buy price mismatch: " + offer.itemKey());
        long expectedSellPrice =
            definition.undroppable()
                ? 0
                : definition.price() > 0 || !definition.rarityPriced()
                    ? Math.max(1L, definition.price() / 2L)
                    : -1;
        if ((expectedSellPrice >= 0 && offer.sellPrice() != expectedSellPrice)
            || (expectedSellPrice < 0 && offer.sellPrice() <= 0))
          throw new IllegalArgumentException("Invalid shop sell price: " + offer.itemKey());
      }
    }
  }

  public static NpcShopCatalog loadBundled() throws IOException {
    try (var input = NpcShopCatalog.class.getResourceAsStream("/content/npc-shops-v2.json")) {
      if (input == null) throw new IOException("Missing bundled NPC shop content");
      NpcShopContent content = read(input);
      return new NpcShopCatalog(content, ItemCatalog.loadBundled());
    }
  }

  static NpcShopContent read(InputStream input) throws IOException {
    byte[] bytes = input.readNBytes(MAX_BYTES + 1);
    if (bytes.length > MAX_BYTES) throw new IOException("NPC shop content exceeds size limit");
    try {
      NpcShopContent content =
          new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), NpcShopContent.class);
      if (content == null) throw new IllegalArgumentException("Empty content");
      return content;
    } catch (RuntimeException invalid) {
      throw new IOException("Invalid or unsupported NPC shop content", invalid);
    }
  }

  public NpcShopContent content() {
    return content;
  }
}
