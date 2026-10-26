package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.npc.behavior.ShopBehavior;
import com.perso.T4C.npc.catalog.ShopCatalog;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.player.RobeWardrobe;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApprovedEquipmentContentTest {
  @Test
  void legacyDamageOutliersDoNotOvershadowTheNewWarriorProgression() {
    var tier = WarriorEquipment.TIERS.getFirst();
    var nextWeapon = ItemRegistry.findByKey("warrior_" + tier.key() + "_warblade");
    int nextMax = DiceFormula.of(nextWeapon.getDmgFormula()).max();
    for (var item : ItemRegistry.allByKey().values()) {
      String key = item.getKey();
      if (!key.startsWith("item.gants_du_boxeur")
          && !key.startsWith("item.lame_du_dragon_")
          && !key.startsWith("item.tranchoir_ecarlate")) continue;
      assertTrue(item.getReqStr() < tier.strength(), key);
      assertTrue(DiceFormula.of(item.getDmgFormula()).max() <= nextMax, key);
    }
    int normal = DiceFormula.of(ItemRegistry.findByKey("gants_du_boxeur_7").getDmgFormula()).max();
    int ultimate =
        DiceFormula.of(ItemRegistry.findByKey("gants_du_boxeur_7_ultimate").getDmgFormula()).max();
    assertTrue(ultimate > normal);
  }

  @Test
  void warriorProgressionIsObtainableAndArmorBonusesStackOncePerPiece() throws Exception {
    var stock =
        ((ShopBehavior) ShopCatalog.get("QuartermasterElenna"))
            .items().stream()
                .map(ItemDefinition::normalizeKey)
                .collect(java.util.stream.Collectors.toSet());
    var ids = new java.util.HashSet<Integer>();
    var boosts = new java.util.HashSet<Integer>();
    for (var item : WarriorEquipment.definitions()) {
      assertTrue(stock.contains(item.getKey()), item.getKey());
      assertEquals(ItemBalance.Archetype.WARRIOR, ItemBalance.archetype(item));
      assertTrue(ids.add(item.getNumId()), item.getKey());
      assertEquals(0, item.getReqAgi());
      assertTrue(item.getBoosts().stream().anyMatch(b -> b.getStatId() == 8));
      assertTrue(item.getBoosts().stream().anyMatch(b -> b.getStatId() == 9));
      for (var boost : item.getBoosts()) assertTrue(boosts.add(boost.getBoostId()), item.getKey());
    }
    var tier = WarriorEquipment.TIERS.getFirst();
    Player player = new Player();
    player.setLevel(1);
    player.setStrength(tier.strength());
    player.setEndurance(tier.endurance());
    player.setSkillLevel("attack", 200);
    player.setSkillLevel("dodge", 200);
    var loadout =
        WarriorEquipment.definitions().stream()
            .filter(i -> i.getReqStr() == tier.strength() && !i.getKey().endsWith("greatsword"))
            .toList();
    for (var item : loadout) {
      player.getInventory().add(item.getKey());
      assertTrue(
          InventoryService.equip(player, item.getBodyPart(), item.getKey()).success(),
          item.getKey());
    }
    var armor =
        loadout.stream()
            .filter(i -> i.getBodyPart() != BodyPart.WEAPON && i.getBodyPart() != BodyPart.SHIELD)
            .toList();
    assertEquals(
        3
            * ItemBalance.combatSkillBonus(
                tier.strength(), ItemBalance.WARRIOR_PROGRESSION_TIER_MULTIPLIER),
        armor.stream().mapToInt(i -> flatOrPercent(i, 8, 200)).sum());
    assertEquals(
        loadout.stream().mapToDouble(ItemDefinition::getArmorClass).sum(),
        InventoryService.equippedArmor(player),
        0.001);
    assertEquals(
        loadout.stream().mapToInt(i -> flatOrPercent(i, 8, 200)).sum(),
        player.getEffectiveSkillLevel("attack") - 200);
    assertEquals(
        loadout.stream().mapToInt(i -> flatOrPercent(i, 9, 200)).sum(),
        player.getEffectiveSkillLevel("dodge") - 200);
    assertTrue(player.getElementResistance("fire") > 0);
  }

  private static int flatOrPercent(ItemDefinition item, int stat, int trained) {
    return item.getBoosts().stream()
        .filter(b -> b.getStatId() == stat)
        .mapToInt(
            b ->
                DiceFormula.of(
                        b.getExpression()
                            .replace("self.true_attack", Integer.toString(trained))
                            .replace("self.true_dodge", Integer.toString(trained)))
                    .evaluate(DiceFormula.Context.ZERO))
        .sum();
  }

  @Test
  void playableBowsApplyTieredArcheryAndReceiveQuiverDamage() throws Exception {
    ItemJsonLoader.loadAndRegister("assets/items");
    try {
      Player player = new Player();
      player.setSkillLevel("archery", 200);
      var withoutArrows = new DiceFormula.Context(20, 20, 20, 20, 0, 20, 0, 1, 0, 100, 100);
      var withArrows = new DiceFormula.Context(20, 20, 20, 20, 0, 20, 0, 1, 7, 100, 100);
      for (var item : ItemRegistry.allByKey().values()) {
        if (!item.isBow() || item.getBodyPart() != BodyPart.WEAPON) continue;
        String key = item.getKey();
        if (key.equals("item.bow")
            || key.startsWith("item.special_bow_")
            || key.startsWith("item.test_")
            || key.startsWith("item.v2_bow")
            || key.startsWith("item.arc_test")) continue;
        var tier = java.util.regex.Pattern.compile("_([1-5])$").matcher(key);
        int percent =
            tier.find()
                ? switch (tier.group(1)) {
                  case "1" -> 15;
                  case "2" -> 30;
                  case "4" -> 75;
                  case "5" -> 100;
                  default -> 50;
                }
                : 50;
        player.getEquippedItems().put(BodyPart.WEAPON, key);
        assertEquals(percent * 2, EquipmentBonusRules.bonus(player, 10035), key);
        var damage = DiceFormula.of(item.getDmgFormula());
        assertTrue(damage.min(withArrows) > damage.min(withoutArrows), key);
        assertTrue(damage.max(withArrows) > damage.max(withoutArrows), key);
      }
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }

  @Test
  void quiverProgressionResolvesInMerchantStockWithValidUnlimitedSplashProfiles() throws Exception {
    ItemJsonLoader.loadAndRegister("assets/items");
    try {
      var stock = ((ShopBehavior) ShopCatalog.get("QuartermasterElenna")).items();
      var quivers =
          ItemRegistry.allByKey().values().stream()
              .filter(i -> i.getQuiverSplash() != null)
              .sorted(java.util.Comparator.comparingLong(ItemDefinition::getReqAgi))
              .toList();
      assertFalse(quivers.isEmpty());
      var ids = new java.util.HashSet<Integer>();
      int previousMin = 0;
      int previousMax = 0;
      for (var item : quivers) {
        assertEquals(com.perso.T4C.player.BodyPart.WEAPON2, item.getBodyPart());
        assertEquals(8, item.getStructure());
        assertTrue(item.isUnlimitedUse());
        assertTrue(ids.add(item.getNumId()), item.getKey());
        assertTrue(
            stock.stream().anyMatch(k -> ItemDefinition.normalizeKey(k).equals(item.getKey())));
        assertTrue(item.getQuiverSplash().targets() <= 8);
        var damage = DiceFormula.of(item.getDmgFormula());
        assertTrue(damage.min() > previousMin, item.getKey() + " minimum damage must progress");
        assertTrue(damage.max() > previousMax, item.getKey() + " maximum damage must progress");
        previousMin = damage.min();
        previousMax = damage.max();
      }
      assertTrue(((ShopBehavior) ShopCatalog.get("Fali")).items().contains("flintspark_quiver"));
      assertTrue(((ShopBehavior) ShopCatalog.get("Rawlin")).items().contains("faultline_quiver"));
      var rebirthGear =
          ItemRegistry.allByKey().values().stream()
              .filter(i -> i.getReqAgi() > ItemBalance.MAX_SINGLE_REQUIREMENT)
              .toList();
      for (BodyPart slot : List.of(BodyPart.WEAPON, BodyPart.WEAPON2)) {
        var progression = rebirthGear.stream().filter(i -> i.getBodyPart() == slot).toList();
        assertFalse(progression.isEmpty(), slot.toString());
        assertTrue(progression.stream().anyMatch(i -> i.getReqAgi() <= 1160));
        assertEquals(
            ItemBalance.MAX_ARCHERY_AGILITY_REQUIREMENT,
            progression.stream().mapToLong(ItemDefinition::getReqAgi).max().orElseThrow());
        for (var item : progression) {
          assertTrue(
              stock.stream().anyMatch(k -> ItemDefinition.normalizeKey(k).equals(item.getKey())),
              item.getKey() + " must be obtainable");
          assertTrue(item.getReqStr() <= ItemBalance.MAX_ARCHERY_STRENGTH_REQUIREMENT);
        }
      }
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }

  @Test
  void approvedEquipmentAndRobeAppearancesResolveToCompleteNativeAnimationRigs() throws Exception {
    ItemJsonLoader.loadAndRegister("assets/items");
    try {
      Map<String, SpriteBinIO.Packed> sprites = new HashMap<>();
      SpriteBinIO.readAll(
          Path.of("assets/sprites"),
          SpriteBinIO.DEFAULT_BASE_NAME,
          p -> {
            if (p.name().startsWith("Pup")
                || p.name().startsWith("Wo")
                || p.name().startsWith("64kInv")) sprites.put(p.name(), p);
          });
      var stock = ((ShopBehavior) ShopCatalog.get("QuartermasterElenna")).items();
      for (String suffix :
          List.of("armor", "boots", "gauntlets", "helm", "leggings", "bow", "halberd", "sword")) {
        String key = "moonsteel_" + suffix;
        ItemDefinition item = ItemRegistry.findByKey(key);
        assertNotNull(item, key);
        assertTrue(stock.contains(key), key + " must be obtainable");
        assertTrue(
            sprites.containsKey(item.getAppearanceInventory()), item.getAppearanceInventory());
        assertEquals(item.getAppearanceInventory(), ItemIconRegistry.iconFor(item));
        assertRig(sprites, item.getAppearanceEquippedPrimary());
      }
      for (String style : RobeWardrobe.STYLES) {
        assertRig(sprites, "PupWhiteRobe__robe_" + style);
        assertRig(sprites, "WoWhiteRobe__robe_" + style);
      }
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }

  private static void assertRig(Map<String, SpriteBinIO.Packed> sprites, String appearance) {
    String original = appearance.substring(0, appearance.indexOf("__"));
    int matched = 0;
    for (var source : sprites.values()) {
      if (!source.name().startsWith(original)) continue;
      String suffix = source.name().substring(original.length());
      if (!suffix.matches("[A-Z]?[0-9]{3}-[a-zA-Z0-9]+")) continue;
      var frame = sprites.get(appearance + suffix);
      assertNotNull(frame, appearance + suffix);
      assertEquals(source.off1X(), frame.off1X());
      assertEquals(source.off1Y(), frame.off1Y());
      assertEquals(source.width(), frame.width());
      assertEquals(source.height(), frame.height());
      matched++;
    }
    assertTrue(matched > 0, appearance + " must have native animation frames");
  }
}
