package com.perso.T4C.item.json;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

/**
 * Friendlier, Gson-deserializable shape for authoring equipment items in JSON. Fields default to
 * sensible values so an author only needs to specify what makes the item distinct, then {@link
 * #toItemDefinition()} expands it into the full {@link ItemDefinition} the game engine expects.
 */
public class ItemJsonDef {
  public String key;
  public String name;
  public String bodyPart;
  public String secondaryBodyPart;
  public String appearanceEquippedPrimary = "";
  public String appearanceEquippedSecondary;
  public String appearanceInventory = "";
  public long price = 0L;
  public long weight = 1L;
  public double armorClass = 0.0d;
  public long dodgeLost = 0L;
  public Requirements requirements = new Requirements();
  public double attackSpeed = 0.0d;
  public boolean unique = false;
  public boolean isBow = false;
  public boolean unlimitedUse = true;
  public int numId = 0;
  public int structure = 2;
  public int appearanceId = 0;
  public boolean undroppable = false;
  public String dmgFormula;
  public String atkDelay;
  public List<BoostJson> boosts = List.of();

  public ItemDefinition toItemDefinition() {
    if (key == null || key.isBlank()) {
      throw new IllegalStateException("Item JSON definition is missing required field 'key'");
    }
    if (bodyPart == null || bodyPart.isBlank()) {
      throw new IllegalStateException("Item JSON definition '" + key + "' is missing 'bodyPart'");
    }
    List<ItemDefinition.ItemBoost> boostDefs =
        boosts.stream()
            .map(
                b ->
                    new ItemDefinition.ItemBoost(
                        b.boostId, b.statId, b.expression == null ? "0" : b.expression, 0, 0))
            .toList();
    return new ItemDefinition(
        key,
        name == null ? key : name,
        BodyPart.valueOf(bodyPart.toUpperCase(java.util.Locale.ROOT)),
        appearanceEquippedPrimary,
        secondaryBodyPart == null || secondaryBodyPart.isBlank()
            ? null
            : BodyPart.valueOf(secondaryBodyPart.toUpperCase(java.util.Locale.ROOT)),
        appearanceEquippedSecondary,
        appearanceInventory,
        price,
        weight,
        armorClass,
        dodgeLost,
        requirements.endurance,
        requirements.attack,
        requirements.strength,
        requirements.agility,
        requirements.intelligence,
        requirements.wisdom,
        attackSpeed,
        unique,
        isBow,
        unlimitedUse,
        numId,
        structure,
        appearanceId,
        dmgFormula,
        atkDelay == null || atkDelay.isBlank() ? "0" : atkDelay,
        0,
        0,
        false,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        boostDefs,
        List.of(),
        undroppable);
  }

  public static final class Requirements {
    public long endurance = 0L;
    public long strength = 0L;
    public long agility = 0L;
    public long intelligence = 0L;
    public long wisdom = 0L;
    public long attack = 0L;
  }

  public static final class BoostJson {
    public int boostId;
    public int statId;
    public String expression = "0";
  }
}
