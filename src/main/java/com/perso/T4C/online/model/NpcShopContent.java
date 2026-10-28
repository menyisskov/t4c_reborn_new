package com.perso.T4C.online.model;

import com.perso.T4C.item.ItemKeys;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Data-only vendor stock exported from the existing offline NPC shop catalog. */
public record NpcShopContent(int version, List<Vendor> vendors) {
  public static final int VERSION = 1;

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

  public record Vendor(String npcId, List<String> itemKeys) {
    public Vendor {
      if (npcId == null || npcId.isBlank()) throw new IllegalArgumentException("Invalid vendor NPC id");
      Objects.requireNonNull(itemKeys, "itemKeys");
      itemKeys = itemKeys.stream().map(ItemKeys::normalize).toList();
      if (itemKeys.isEmpty() || itemKeys.stream().anyMatch(k -> k == null || k.isBlank()))
        throw new IllegalArgumentException("Empty or invalid vendor inventory");
    }
  }
}
