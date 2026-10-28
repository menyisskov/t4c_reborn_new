package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.perso.T4C.item.ItemKeys;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.ItemSalePricing;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.npc.catalog.ShopCatalog;
import com.perso.T4C.online.model.NpcShopContent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** Exports generic NPC vendor stock from the same catalog used by the offline game. */
public final class OnlineNpcShopExporter {
  private OnlineNpcShopExporter() {}

  public static NpcShopContent content() {
    // Same registry the running game builds at start-up (generated + JSON items and monsters);
    // without this the output depends on which tests or tools happened to load JSON content first.
    MonsterRegistry.resetToGeneratedDefinitions();
    MonsterJsonLoader.loadAndRegister();
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
    List<NpcShopContent.Vendor> vendors =
        ShopCatalog.allShopInventories().entrySet().stream()
            .filter(
                entry ->
                    entry.getValue().stream()
                        .anyMatch(key -> ItemRegistry.findByKey(key) != null))
            .map(
                entry ->
                    new NpcShopContent.Vendor(
                        entry.getKey(),
                        entry.getValue().stream()
                            .filter(key -> ItemRegistry.findByKey(key) != null)
                            .map(ItemKeys::normalize)
                            .map(
                                key -> {
                                  var item = ItemRegistry.findByKey(key);
                                  return new NpcShopContent.Offer(
                                      key, item.getPrice(), ItemSalePricing.sellPrice(item));
                                })
                            .toList()))
            .toList();
    return new NpcShopContent(NpcShopContent.VERSION, vendors);
  }

  public static String json() {
    return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(content()) + "\n";
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Expected output path");
    Path target = Path.of(args[0]).toAbsolutePath().normalize();
    Files.createDirectories(target.getParent());
    Path temporary = Files.createTempFile(target.getParent(), "npc-shops-", ".tmp");
    try {
      Files.writeString(temporary, json(), java.nio.charset.StandardCharsets.UTF_8);
      try {
        Files.move(
            temporary,
            target,
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
      } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(temporary);
    }
  }
}
