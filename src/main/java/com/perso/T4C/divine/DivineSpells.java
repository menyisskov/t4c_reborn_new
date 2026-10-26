package com.perso.T4C.divine;

import com.perso.T4C.spell.SpellData;
import java.util.ArrayList;
import java.util.List;

public final class DivineSpells {
  private DivineSpells() {}

  public static List<SpellData> all() {
    List<SpellData> spells = new ArrayList<>();
    for (var god : DivinePantheon.all()) {
      spells.add(returnSpell(god));
      spells.add(judgment(god));
    }
    spells.addAll(DivineAfflictions.definitions());
    return List.copyOf(spells);
  }

  private static SpellData returnSpell(DivinePantheon.God god) {
    String key = "item_divine_" + god.id() + "_return";
    return returnSpell(key, god.returnSpellId(), god.x() + 3, god.y() + 3);
  }

  public static SpellData returnSpell(String key, int spellId, int x, int y) {
    return new SpellData(
        "${spell." + key + "}",
        "${spell.description." + key + "}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "",
        null,
        null,
        0,
        0,
        null,
        null,
        5,
        "0",
        "0",
        0,
        null,
        spellId,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30012,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                7,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, Integer.toString(x)),
                    new SpellData.T4cEffect.EffectParam(2, Integer.toString(y)),
                    new SpellData.T4cEffect.EffectParam(3, "0")))));
  }

  private static SpellData judgment(DivinePantheon.God god) {
    String key = "mob_divine_" + god.id() + "_judgment";
    String school = god.school();
    String impact =
        switch (school) {
          case "fire" -> "64kSpellFireCircle-";
          case "water" -> "64kSpellGlacier-";
          case "air" -> "ElectricShield-";
          case "dark" -> "Curse-";
          case "light" -> "HealingSpell-";
          case "archer" -> "SmallPoisonCloud-";
          default -> "64kSpellBoulders-";
        };
    String projectile =
        switch (school) {
          case "archer" -> "PoisonArrow";
          case "fire", "air" -> "64kSpellEnergyBallYellow-";
          case "water" -> "64kSpellEnergyBallBlue-";
          case "earth" -> "StoneShard";
          case "light" -> "64kSpellEnergyBallWhite-";
          case "dark" -> "64kSpellEnergyBallBlack-";
          default -> null;
        };
    int element =
        switch (school) {
          case "fire" -> 1;
          case "water" -> 4;
          case "air" -> 3;
          case "earth" -> 2;
          case "light" -> 5;
          case "dark" -> 6;
          default -> 0;
        };
    // The damage formula is authored per tier; each school applies its own resistance.
    int low = god.judgmentDamageMin();
    int high = god.judgmentDamageMax();
    String formula = "-(1d" + (high - low + 1) + "+" + (low - 1) + ")";
    if (!school.equals("warrior") && !school.equals("archer"))
      formula = "(" + formula + "*100/target.r_" + school + ")";
    return new SpellData(
        "${spell." + key + "}",
        "${spell.description." + key + "}",
        "0",
        0,
        0,
        0,
        0,
        true,
        true,
        "",
        projectile,
        impact,
        0,
        0,
        null,
        null,
        0,
        "0",
        "0",
        0,
        null,
        god.judgmentSpellId(),
        element,
        4,
        school.equals("warrior") || school.equals("archer") ? 1 : 2,
        "100",
        "0",
        "0",
        "0",
        0,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, formula),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
