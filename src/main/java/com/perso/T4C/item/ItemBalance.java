package com.perso.T4C.item;

import com.perso.T4C.player.BodyPart;
import java.util.Map;

/**
 * The item balance rules (T4C-0027, see DESIGN_GUIDELINES.md "Items"): every equippable item's
 * class comes from its requirements, and its Armor Class and bonuses follow from that class, its
 * main requirement and its endurance requirement. {@code tools/ArmorSetGenerator} builds the armor
 * sets from these numbers and {@code ItemBalanceGuidelinesTest} checks every JSON item against
 * them, so a new item that ignores them fails the build.
 *
 * <ul>
 *   <li><b>Warrior</b> (strength is the main requirement): AC, resistance to all six elements,
 *       strength and attack.
 *   <li><b>Archer</b> (agility is the main requirement, or any bow): AC, resistance to all six
 *       elements, agility and archery.
 *   <li><b>Intelligence mage</b>: fire/water/dark power and resistance, extra intelligence, and
 *       less AC than a wisdom item.
 *   <li><b>Wisdom mage</b>: earth/light power and resistance, wisdom, and more AC.
 *   <li><b>Hybrid</b> (intelligence and wisdom within 20% of each other): air power and
 *       resistance, intelligence and wisdom.
 * </ul>
 *
 * <p>AC follows the endurance requirement: {@code slotAc(slot) * endurance / 100 * classAc}.
 */
public final class ItemBalance {
  /** No item may ask for more endurance than this. */
  public static final int MAX_ENDURANCE_REQUIREMENT = 600;

  /** No item may ask for more than this in any single attribute (the level-cap main stat). */
  public static final int MAX_SINGLE_REQUIREMENT = 1000;

  public enum Archetype {
    WARRIOR(1.10),
    ARCHER(0.85),
    INT_MAGE(0.80),
    WIS_MAGE(1.00),
    HYBRID_MAGE(0.90);

    private final double acMultiplier;

    Archetype(double acMultiplier) {
      this.acMultiplier = acMultiplier;
    }

    public double acMultiplier() {
      return acMultiplier;
    }

    public boolean isMage() {
      return this == INT_MAGE || this == WIS_MAGE || this == HYBRID_MAGE;
    }
  }

  /** AC per 100 points of endurance requirement, by slot (armor-set slots keep the legacy
   * platemail proportions: body 31, legs 10.32, feet/hands 9.28, head 8.94, belt 6.88). */
  private static final Map<BodyPart, Double> SLOT_AC =
      Map.ofEntries(
          Map.entry(BodyPart.BODY, 31.0),
          Map.entry(BodyPart.LEGS, 10.32),
          Map.entry(BodyPart.FEET, 9.28),
          Map.entry(BodyPart.LEFT_HAND, 9.28),
          Map.entry(BodyPart.RIGHT_HAND, 9.28),
          Map.entry(BodyPart.HEAD, 8.94),
          Map.entry(BodyPart.BELT, 6.88),
          Map.entry(BodyPart.BACK, 9.0),
          Map.entry(BodyPart.SHIELD, 16.0),
          Map.entry(BodyPart.NECK, 5.0),
          Map.entry(BodyPart.RING1, 5.0),
          Map.entry(BodyPart.RING2, 5.0),
          Map.entry(BodyPart.BRACER, 5.0));

  private ItemBalance() {}

  public static double slotAc(BodyPart slot) {
    return SLOT_AC.getOrDefault(slot, 0d);
  }

  /** The Armor Class an item in {@code slot} with this endurance requirement should carry. */
  public static double expectedArmorClass(BodyPart slot, long endurance, Archetype archetype) {
    return Math.round(slotAc(slot) * endurance / 100d * archetype.acMultiplier() * 10d) / 10d;
  }

  /** The item's class, from its requirements (bows are always archer gear). */
  public static Archetype archetype(long str, long agi, long intel, long wis, boolean bow) {
    if (bow) return Archetype.ARCHER;
    long physical = Math.max(str, agi);
    long mental = Math.max(intel, wis);
    if (physical == 0 && mental == 0) return null;
    if (physical >= mental) return str >= agi ? Archetype.WARRIOR : Archetype.ARCHER;
    if (intel > 0 && wis > 0 && Math.min(intel, wis) >= 0.8 * Math.max(intel, wis))
      return Archetype.HYBRID_MAGE;
    return intel > wis ? Archetype.INT_MAGE : Archetype.WIS_MAGE;
  }

  public static Archetype archetype(ItemDefinition def) {
    return archetype(def.getReqStr(), def.getReqAgi(), def.getMinInt(), def.getMinWis(), def.isBow());
  }

  /** The requirement bonuses scale from: the class's main stat (a hybrid counts 80% of its
   * intelligence + wisdom, so 375/375 matches a 600 single-stat item). */
  public static double primaryRequirement(
      Archetype a, long str, long agi, long intel, long wis) {
    return switch (a) {
      case WARRIOR -> str;
      case ARCHER -> Math.max(agi, str);
      case INT_MAGE -> intel;
      case WIS_MAGE -> wis;
      case HYBRID_MAGE -> (intel + wis) * 0.8;
    };
  }

  // Single-item (non-set) bonus budget, as a fraction of the primary requirement P.
  /** Main stat: strength/agility P/12, intelligence P/10 (extra), wisdom P/12, hybrid P/24 each. */
  public static int mainStatBonus(Archetype a, double p) {
    return (int)
        Math.round(
            switch (a) {
              case INT_MAGE -> p / 10;
              case HYBRID_MAGE -> p / 24;
              default -> p / 12;
            });
  }

  /** Attack (warrior) or archery (archer): P/5. */
  public static int combatSkillBonus(double p) {
    return (int) Math.round(p / 5);
  }

  /** Mage elemental power: P/10. */
  public static int magicPowerBonus(double p) {
    return (int) Math.round(p / 10);
  }

  /** Mage resistance to its own element: P/15. */
  public static int magicResistBonus(double p) {
    return (int) Math.round(p / 15);
  }

  /** Warrior/archer resistance to each of the six elements: P/40. */
  public static int physicalResistBonus(double p) {
    return (int) Math.round(p / 40);
  }
}
