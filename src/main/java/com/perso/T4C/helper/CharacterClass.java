package com.perso.T4C.helper;

import com.perso.T4C.player.BodyPart;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The seven starting archetypes offered by character creation (T4C-0059).
 *
 * <p>Replaces the old eight-question personality quiz: instead of rolling attributes from four
 * randomly drawn questions, the player picks an archetype and gets the spread that questionnaire
 * would have produced if every answer had pushed the same way. Every class starts from {@link
 * CharacterCreationRules#BASE_ATTRIBUTE} in all five attributes and spends exactly {@link
 * CharacterCreationRules#CLASS_BONUS_POINTS} extra points, so no archetype is ahead of another on
 * raw points - they only differ in where those points sit.
 *
 * <p>Each class also carries a starting kit it can actually wear and a starting spell it could
 * legitimately have learned: {@code CharacterClassTest} asserts both against {@link
 * com.perso.T4C.item.InventoryService} requirements and the spells' own minInt/minWis, so a kit
 * can never drift above what its own stat spread supports.
 */
public enum CharacterClass {
  /** Heavy melee: the most strength and endurance in the game's opening, no magic at all. */
  WARRIOR(
      "warrior",
      20,
      10,
      0,
      0,
      0,
      equipment(
          BodyPart.WEAPON, "item.recruits_greatsword",
          BodyPart.BODY, "item.leather_armor",
          BodyPart.LEGS, "item.leather_pants",
          BodyPart.FEET, "item.leather_boots",
          BodyPart.HEAD, "item.leather_helmet",
          BodyPart.LEFT_HAND, "item.leather_gloves"),
      List.of(),
      List.of()),

  /** Ranged specialist: agility first, and the only class that opens with a bow and arrows. */
  ARCHER(
      "archer",
      5,
      5,
      20,
      0,
      0,
      equipment(
          BodyPart.WEAPON, "item.ashwood_reflex_bow_1",
          BodyPart.BODY, "item.leather_armor",
          BodyPart.LEGS, "item.leather_pants",
          BodyPart.FEET, "item.leather_boots",
          BodyPart.HEAD, "item.newbi_set_helm",
          BodyPart.LEFT_HAND, "item.leather_gloves"),
      List.of("item.wooden_arrow"),
      List.of()),

  /** Sword-and-board hybrid: warrior-grade strength with enough wisdom to keep itself standing. */
  PALADIN(
      "paladin",
      10,
      10,
      0,
      10,
      0,
      equipment(
          BodyPart.WEAPON, "item.rusted_long_sword_3",
          BodyPart.SHIELD, "item.newbi_set_shield",
          BodyPart.BODY, "item.leather_armor",
          BodyPart.LEGS, "item.leather_pants",
          BodyPart.FEET, "item.leather_boots",
          BodyPart.HEAD, "item.leather_helmet",
          BodyPart.BACK, "item.newbi_set_back"),
      List.of(),
      List.of("spell.heal_light")),

  /** Armoured wisdom caster: hits with a mace, opens the earth school. */
  CLERIC(
      "cleric",
      0,
      10,
      0,
      20,
      0,
      equipment(
          BodyPart.WEAPON, "item.acolytes_earthen_mace",
          BodyPart.BODY, "item.templars_earthen_vestment",
          BodyPart.FEET, "item.leather_boots",
          BodyPart.HEAD, "item.leather_helmet"),
      List.of(),
      List.of("spell.stone_shard")),

  /** Pure support caster: the highest wisdom on offer, and the only class that opens with a heal. */
  HEALER(
      "healer",
      0,
      0,
      0,
      30,
      0,
      equipment(
          BodyPart.WEAPON, "item.acolytes_dawnlit_staff",
          BodyPart.BODY, "item.acolytes_dawnlit_robe"),
      List.of(),
      List.of("spell.stone_shard", "spell.heal_light")),

  /** Pure intelligence caster: the Healer's mirror image - everything in one stat, nothing left
   * over for armour, and the hardest-hitting fire in a new character's hands. */
  MAGE(
      "mage",
      0,
      0,
      0,
      0,
      30,
      equipment(
          BodyPart.WEAPON, "item.staff_of_thorns_1",
          BodyPart.BODY, "item.flowing_black_robe"),
      List.of(),
      List.of("spell.fire_dart")),

  /** Melee-capable caster: as hardy as a Paladin, with intelligence where the Paladin has
   * wisdom. Trades the Mage's raw power for the ability to survive being reached. */
  BATTLE_MAGE(
      "battlemage",
      10,
      10,
      0,
      0,
      10,
      equipment(
          BodyPart.WEAPON, "item.quarterstaff_2",
          BodyPart.BODY, "item.apprentices_emberweave_robe",
          BodyPart.FEET, "item.leather_boots",
          BodyPart.LEFT_HAND, "item.leather_gloves"),
      List.of(),
      List.of("spell.fire_dart"));

  /** How many of each stacking starting item (arrows) a kit grants. */
  public static final int STACKED_KIT_ITEM_COUNT = 30;

  private final String id;
  private final int strengthBonus;
  private final int enduranceBonus;
  private final int dexterityBonus;
  private final int wisdomBonus;
  private final int intelligenceBonus;
  private final Map<BodyPart, String> startingEquipment;
  private final List<String> startingStacks;
  private final List<String> startingSpellKeys;

  CharacterClass(
      String id,
      int strengthBonus,
      int enduranceBonus,
      int dexterityBonus,
      int wisdomBonus,
      int intelligenceBonus,
      Map<BodyPart, String> startingEquipment,
      List<String> startingStacks,
      List<String> startingSpellKeys) {
    this.id = id;
    this.strengthBonus = strengthBonus;
    this.enduranceBonus = enduranceBonus;
    this.dexterityBonus = dexterityBonus;
    this.wisdomBonus = wisdomBonus;
    this.intelligenceBonus = intelligenceBonus;
    this.startingEquipment = startingEquipment;
    this.startingStacks = startingStacks;
    this.startingSpellKeys = startingSpellKeys;
  }

  /** Stable identifier written into save files and used to build i18n keys. */
  public String id() {
    return id;
  }

  /** i18n key for this class's display name, e.g. {@code character.class.warrior.name}. */
  public String nameKey() {
    return "character.class." + id + ".name";
  }

  /** i18n key for the one-line blurb shown next to the class in the picker. */
  public String descriptionKey() {
    return "character.class." + id + ".description";
  }

  public int strengthBonus() {
    return strengthBonus;
  }

  public int enduranceBonus() {
    return enduranceBonus;
  }

  public int dexterityBonus() {
    return dexterityBonus;
  }

  public int wisdomBonus() {
    return wisdomBonus;
  }

  public int intelligenceBonus() {
    return intelligenceBonus;
  }

  /** Total bonus points this class spends - the same for every class, by design. */
  public int totalBonus() {
    return strengthBonus + enduranceBonus + dexterityBonus + wisdomBonus + intelligenceBonus;
  }

  /** Gear the character starts wearing, slot by slot. */
  public Map<BodyPart, String> startingEquipment() {
    return startingEquipment;
  }

  /**
   * Items granted {@link #STACKED_KIT_ITEM_COUNT} at a time rather than worn (arrows). Ordinary
   * starting supplies everyone gets - torches, potions - live in {@link LocalCharacterStore}.
   */
  public List<String> startingStacks() {
    return startingStacks;
  }

  /** Spell keys the character already knows, resolved to spell names through the spell registry. */
  public List<String> startingSpellKeys() {
    return startingSpellKeys;
  }

  /** Looks up a class by its {@link #id()}; falls back to {@link #WARRIOR} for unknown ids. */
  public static CharacterClass byId(String id) {
    if (id != null) {
      for (CharacterClass value : values()) {
        if (value.id.equalsIgnoreCase(id)) return value;
      }
    }
    return WARRIOR;
  }

  private static Map<BodyPart, String> equipment(Object... slotsAndItems) {
    Map<BodyPart, String> map = new LinkedHashMap<>();
    for (int i = 0; i < slotsAndItems.length; i += 2) {
      map.put((BodyPart) slotsAndItems[i], (String) slotsAndItems[i + 1]);
    }
    return Map.copyOf(map);
  }
}
