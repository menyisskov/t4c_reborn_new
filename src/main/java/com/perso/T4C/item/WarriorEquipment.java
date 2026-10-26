package com.perso.T4C.item;

import com.perso.T4C.item.json.ItemJsonDef;
import com.perso.T4C.player.BodyPart;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Attribute-gated warrior equipment; existing native appearances are reused unchanged. */
public final class WarriorEquipment {
  public record Tier(
      String key,
      String name,
      int strength,
      int endurance,
      int bladeMin,
      int bladeMax,
      int greatswordMin,
      int greatswordMax,
      long price) {}

  public static final List<Tier> TIERS =
      List.of(
          new Tier("ironwarden", "Ironwarden", 1100, 450, 900, 1300, 1200, 1700, 3000000),
          new Tier("dreadguard", "Dreadguard", 1250, 480, 1050, 1500, 1400, 1950, 4500000),
          new Tier("titanforged", "Titanforged", 1450, 510, 1250, 1750, 1650, 2300, 7000000),
          new Tier("siegebreaker", "Siegebreaker", 1650, 540, 1450, 2000, 1950, 2700, 10000000),
          new Tier("oathsteel", "Oathsteel", 1850, 570, 1650, 2300, 2200, 3100, 15000000),
          new Tier("worldguard", "Worldguard", 2000, 600, 1800, 2600, 2400, 3400, 22000000));

  private record Piece(
      String key,
      String name,
      BodyPart slot,
      BodyPart secondary,
      String appearance,
      String secondaryAppearance,
      String icon,
      int weight,
      int appearanceId) {}

  private static final List<Piece> PIECES =
      List.of(
          new Piece(
              "armor",
              "Plate Armor",
              BodyPart.BODY,
              null,
              "PupPlateBody",
              null,
              "64kInvPlateArmorSleeves",
              8,
              264),
          new Piece(
              "boots",
              "Plate Boots",
              BodyPart.FEET,
              null,
              "PupPlateFoot",
              null,
              "64kInvPlateArmorFeet",
              3,
              265),
          new Piece(
              "gauntlets",
              "Plate Gauntlets",
              BodyPart.LEFT_HAND,
              BodyPart.RIGHT_HAND,
              "PupPlateGloveL",
              "PupPlateGloveR",
              "64kInvPlateGlove",
              3,
              263),
          new Piece(
              "helmet",
              "Plate Helm",
              BodyPart.HEAD,
              null,
              "PupPlateHelm",
              null,
              "64kInvPlateArmorHelm",
              3,
              267),
          new Piece(
              "leggings",
              "Plate Leggings",
              BodyPart.LEGS,
              null,
              "PupPlateLegs",
              null,
              "64kInvPlateArmorLegs",
              5,
              266),
          new Piece("protector", "War Belt", BodyPart.BELT, null, null, null, "64kInvBelt", 2, 235),
          new Piece(
              "shield",
              "Bulwark",
              BodyPart.SHIELD,
              null,
              "PupCentaurShield",
              null,
              "64kInvCentaurShield1",
              10,
              460),
          new Piece(
              "warblade",
              "Warblade",
              BodyPart.WEAPON,
              null,
              "PupNormalSword",
              null,
              "64kInvBattleSword",
              6,
              1),
          new Piece(
              "greatsword",
              "Greatsword",
              BodyPart.WEAPON,
              BodyPart.WEAPON2,
              "PupBattleSword",
              null,
              "64kInvBattleSword",
              10,
              1));

  private static final List<ItemDefinition> DEFINITIONS = build();

  private WarriorEquipment() {}

  public static List<ItemDefinition> definitions() {
    return DEFINITIONS;
  }

  public static List<String> itemKeys() {
    return DEFINITIONS.stream().map(ItemDefinition::getKey).toList();
  }

  private static List<ItemDefinition> build() {
    List<ItemDefinition> items = new ArrayList<>();
    int index = 0;
    for (Tier tier : TIERS) {
      for (Piece piece : PIECES) {
        ItemJsonDef item = new ItemJsonDef();
        item.key = "warrior_" + tier.key() + "_" + piece.key();
        item.name = "${item." + item.key + "}";
        item.bodyPart = piece.slot().name();
        item.secondaryBodyPart = piece.secondary() == null ? null : piece.secondary().name();
        item.appearanceEquippedPrimary = piece.appearance();
        item.appearanceEquippedSecondary = piece.secondaryAppearance();
        item.appearanceInventory = piece.icon();
        item.appearanceId = piece.appearanceId();
        item.numId = 60100 + index;
        item.weight = piece.weight();
        item.requirements.strength = tier.strength();
        boolean weapon = piece.slot() == BodyPart.WEAPON;
        item.requirements.endurance = weapon ? 0 : tier.endurance();
        double multiplier = ItemBalance.WARRIOR_PROGRESSION_TIER_MULTIPLIER;
        item.armorClass =
            ItemBalance.expectedArmorClass(
                piece.slot(),
                item.requirements.endurance,
                ItemBalance.Archetype.WARRIOR,
                multiplier);
        item.price =
            weapon
                ? tier.price()
                : Math.max(
                    1, Math.round(tier.price() * (piece.slot() == BodyPart.BODY ? 0.6 : 0.3)));
        item.structure = weapon ? 1 : 2;
        if (weapon) {
          boolean twoHanded = piece.secondary() == BodyPart.WEAPON2;
          item.dmgFormula =
              damageFormula(
                  twoHanded ? tier.greatswordMin() : tier.bladeMin(),
                  twoHanded ? tier.greatswordMax() : tier.bladeMax());
          item.atkDelay = twoHanded ? "1100" : "900";
        }
        boolean armor = !weapon && piece.slot() != BodyPart.SHIELD;
        int firstBoost = (armor ? 24000 : 734000) + index * 16;
        int strengthBonus =
            ItemBalance.mainStatBonus(ItemBalance.Archetype.WARRIOR, tier.strength(), multiplier);
        int attackBonus = ItemBalance.combatSkillBonus(tier.strength(), multiplier);
        int dodgeBonus = (int) Math.round(tier.strength() * multiplier / 10);
        int resistanceBonus = ItemBalance.physicalResistBonus(tier.strength(), multiplier);
        List<ItemJsonDef.BoostJson> boosts = new ArrayList<>();
        boosts.add(
            boost(
                firstBoost,
                3,
                Integer.toString(armor ? armorShare(piece, 3 * strengthBonus) : strengthBonus)));
        boosts.add(
            boost(
                firstBoost + 1,
                8,
                weapon
                    ? "self.true_attack*50/100"
                    : Integer.toString(armor ? armorShare(piece, 3 * attackBonus) : attackBonus)));
        boosts.add(
            boost(
                firstBoost + 2,
                9,
                weapon
                    ? "self.true_dodge*20/100"
                    : Integer.toString(armor ? armorShare(piece, 3 * dodgeBonus) : dodgeBonus)));
        int offset = 3;
        for (int stat : List.of(12, 13, 14, 15, 22)) {
          boosts.add(
              boost(
                  firstBoost + offset++,
                  stat,
                  Integer.toString(
                      armor
                          ? (int) Math.round(tier.endurance() / 20d)
                              + armorShare(piece, 3 * resistanceBonus)
                          : resistanceBonus)));
        }
        if (piece.slot() == BodyPart.SHIELD) {
          boosts.add(boost(firstBoost + offset, 10008, Integer.toString(tier.strength() / 10)));
        }
        item.boosts = List.copyOf(boosts);
        items.add(item.toItemDefinition());
        index++;
      }
    }
    return List.copyOf(items);
  }

  /** Split three item budgets across six armor slots without losing points to rounding. */
  private static int armorShare(Piece piece, int total) {
    var armor =
        PIECES.stream()
            .filter(p -> p.slot() != BodyPart.WEAPON && p.slot() != BodyPart.SHIELD)
            .toList();
    double weight = armor.stream().mapToDouble(p -> ItemBalance.slotAc(p.slot())).sum();
    int assigned =
        armor.stream()
            .mapToInt(p -> (int) Math.floor(total * ItemBalance.slotAc(p.slot()) / weight))
            .sum();
    var remainderOrder = new ArrayList<>(armor);
    remainderOrder.sort(
        Comparator.comparingDouble(
                (Piece p) -> {
                  double quota = total * ItemBalance.slotAc(p.slot()) / weight;
                  return quota - Math.floor(quota);
                })
            .reversed());
    int share = (int) Math.floor(total * ItemBalance.slotAc(piece.slot()) / weight);
    return share + (remainderOrder.indexOf(piece) < total - assigned ? 1 : 0);
  }

  private static String damageFormula(int min, int max) {
    return "1d" + (max - min + 1) + "+" + (min - 1);
  }

  private static ItemJsonDef.BoostJson boost(int id, int stat, String expression) {
    ItemJsonDef.BoostJson boost = new ItemJsonDef.BoostJson();
    boost.boostId = id;
    boost.statId = stat;
    boost.expression = expression;
    return boost;
  }
}
