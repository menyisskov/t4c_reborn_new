package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.online.model.ItemContent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

/** Build-time bridge from actual runtime lookup winners to pure server equipment definitions. */
public final class OnlineItemContentExporter {
  private OnlineItemContentExporter() {}

  public static ItemContent snapshot() {
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
    var items = new TreeMap<String, ItemContent.Definition>();
    ItemRegistry.allByKey().forEach((key, d) -> items.put(key, new ItemContent.Definition(
        key, d.getName(), d.getBodyPart(), d.getSecondaryBodyPart(), d.getAppearanceEquippedPrimary(),
        d.getAppearanceEquippedSecondary(), d.getAppearanceInventory(), d.getPrice(), d.getWeight(),
        d.getArmorClass(), d.getDodgeLost(), d.getMinEnd(), d.getReqAttack(), d.getReqStr(), d.getReqAgi(),
        d.getMinInt(), d.getMinWis(), d.getAttackSpeed(), d.isUnique(), d.isBow(), d.isUnlimitedUse(),
        d.getNumId(), d.getStructure(), d.getDmgFormula(), d.getAtkDelay(), d.getNbCharges(), d.isUndroppable(),
        d.getBoosts().stream().map(b -> new ItemContent.Boost(b.getBoostId(), b.getStatId(), b.getExpression(), b.getMinWis(), b.getMinInt())).toList(),
        d.getSpells().stream().map(s -> new ItemContent.Spell(s.getSpellId(), s.getLevel(), s.getChance())).toList(),
        d.isRarityPriced(), d.getQuiverSplash() == null ? null : new ItemContent.Splash(
            d.getQuiverSplash().radius(), d.getQuiverSplash().targets(), d.getQuiverSplash().damagePercent()))));
    return new ItemContent(ItemContent.VERSION, items);
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Expected output JSON path");
    var content = snapshot();
    // Map.copyOf has unspecified iteration order; explicitly sort the serialized registry.
    var gson = new GsonBuilder().serializeNulls().create();
    var json = gson.toJsonTree(content).getAsJsonObject();
    json.add("items", gson.toJsonTree(new TreeMap<>(content.items())));
    var path = Path.of(args[0]);
    if (path.getParent() != null) Files.createDirectories(path.getParent());
    Files.writeString(path, gson.toJson(json) + "\n");
    System.out.println("Exported item equipment data; item-use and spell behavior are not enabled.");
  }
}
