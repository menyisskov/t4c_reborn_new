package com.perso.T4C.online.model;

import com.perso.T4C.combat.WeaponDamageRules;
import com.perso.T4C.player.BodyPart;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable registry-authored equipment inputs; not executable item-use or spell behavior. */
public record ItemContent(int version, Map<String, Definition> items) {
  public static final int VERSION = 1;

  public ItemContent {
    if (version != VERSION) throw new IllegalArgumentException("Unsupported item content version");
    items = Map.copyOf(items);
    if (items.isEmpty()) throw new IllegalArgumentException("Empty item content");
    items.forEach((key, item) -> {
      if (!key.equals(item.key())) throw new IllegalArgumentException("Item key mismatch");
    });
  }

  public record Definition(String key, String name, BodyPart bodyPart, BodyPart secondaryBodyPart,
      String appearanceEquippedPrimary, String appearanceEquippedSecondary, String appearanceInventory,
      long price, long weight, double armorClass, long dodgeLost, long minEnd, long reqAttack,
      long reqStr, long reqAgi, long minInt, long minWis, double attackSpeed,
      boolean unique, boolean isBow, boolean unlimitedUse, int numId, int structure,
      String dmgFormula, String atkDelay, int nbCharges, boolean undroppable,
      List<Boost> boosts, List<Spell> spells, boolean rarityPriced, Splash quiverSplash) {
    public Definition {
      if (key == null || !key.startsWith("item.") || key.length() <= 5 || key.length() > 256
          || name == null || name.isBlank() || !Double.isFinite(armorClass)
          || !Double.isFinite(attackSpeed)) throw new IllegalArgumentException("Invalid item definition");
      boosts = List.copyOf(boosts);
      spells = List.copyOf(spells);
    }

    public WeaponDamageRules.Weapon weapon() {
      return new WeaponDamageRules.Weapon(key, bodyPart, isBow, structure, dmgFormula);
    }
  }

  public record Boost(int boostId, int statId, String expression, int minWis, int minInt) {
    public Boost { Objects.requireNonNull(expression); }
  }
  public record Spell(int spellId, int level, int chance) {}
  public record Splash(double radius, int targets, int damagePercent) {}
}
