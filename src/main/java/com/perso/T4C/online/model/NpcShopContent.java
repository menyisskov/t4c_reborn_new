package com.perso.T4C.online.model;

import com.perso.T4C.item.ItemKeys;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Data-only vendor stock exported from the existing offline NPC shop catalog. */
public record NpcShopContent(int version, List<Vendor> vendors) {
  public static final int VERSION = 2;

  public NpcShopContent {
    if (version != VERSION) throw new IllegalArgumentException("Unsupported NPC shop content version");
    Objects.requireNonNull(vendors, "vendors");
    vendors = vendors.stream().sorted(Comparator.comparing(Vendor::npcId)).toList();
    var ids = new HashSet<String>();
    for (Vendor vendor : vendors) {
      if (!ids.add(vendor.npcId())) throw new IllegalArgumentException("Duplicate vendor NPC id");
    }
    if (vendors.isEmpty()) throw new IllegalArgumentException("Empty NPC shop content");
  }

  public record Vendor(String npcId, List<Offer> offers) {
    public Vendor {
      if (npcId == null || npcId.isBlank()) throw new IllegalArgumentException("Invalid vendor NPC id");
      Objects.requireNonNull(offers, "offers");
      offers = List.copyOf(offers);
      if (offers.isEmpty())
        throw new IllegalArgumentException("Empty or invalid vendor inventory");
    }
  }

  public record Offer(String itemKey, long buyPrice, long sellPrice) {
    public Offer {
      itemKey = ItemKeys.normalize(itemKey);
      if (itemKey == null || itemKey.isBlank() || buyPrice < 0 || sellPrice < 0)
        throw new IllegalArgumentException("Invalid vendor offer");
    }
  }
}
