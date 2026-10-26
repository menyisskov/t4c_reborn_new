package com.perso.T4C.divine;

import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellEffectManager;
import java.util.ArrayList;
import java.util.List;

/** Recipient-based, timed status spells used only by the elder divine trials. */
public final class DivineAfflictions {
  public static final int ARMOR_MELT = 18200;
  public static final int CURSE = 18201;
  public static final int ENFEEBLE = 18202;
  public static final int EXPOSE = 18203;
  public static final int DURATION_SECONDS = 20;
  public static final long CAST_INTERVAL_MILLIS = 10000L;

  private DivineAfflictions() {}

  public static boolean isAffliction(int spellId) {
    return spellId >= ARMOR_MELT && spellId <= EXPOSE;
  }

  public static List<Integer> spellIds(DivinePantheon.God god) {
    if (god.tier() < 2) return List.of();
    return List.of(ARMOR_MELT, CURSE, god.school().equals("warrior") ? ENFEEBLE : EXPOSE);
  }

  public static List<SpellData> definitions() {
    List<SpellData.T4cEffect> exposure = new ArrayList<>();
    for (String element : List.of("fire", "earth", "air", "water", "light", "dark")) {
      exposure.add(boost("r_" + element, "-(self.true_r_" + element + "/5)"));
    }
    return List.of(
        definition(
            ARMOR_MELT,
            "armor_melt",
            "64kSpellIconFireAttackSingle",
            "FireWipe-",
            1,
            List.of(boost("ac percent", "-25"))),
        definition(
            CURSE,
            "curse",
            "64kSpellIconDarkAttackSingle",
            "Curse-",
            6,
            List.of(boost("healing received", "-40"))),
        definition(
            ENFEEBLE,
            "enfeeble",
            "64kSpellIconDarkAttackSingle",
            "Curse-",
            6,
            List.of(
                boost("attack", "-(self.true_attack/5)"),
                boost("archery", "-(self.true_archery/5)"),
                boost("dodge", "-(self.true_dodge/5)"))),
        definition(EXPOSE, "expose", "64kSpellIconDarkAttackSingle", "Curse-", 6, exposure));
  }

  /** No damage roll: these spells affect the recipient and use their own true-stat baseline. */
  public static boolean apply(SpellData spell, Player recipient) {
    if (spell == null || !isAffliction(spell.getSpellId())) return false;
    if (recipient == null || recipient.getCurrentHp() <= 0) return true;
    SpellEffectManager effects = new SpellEffectManager();
    recipient.applyBuff(
        spell.getName(),
        spell.getDescription(),
        spell.getIconId(),
        effects.resolveDurationSeconds(spell, recipient),
        false,
        effects.resolvePlayerBuffEffects(spell, recipient));
    return true;
  }

  private static SpellData.T4cEffect boost(String attribute, String amount) {
    return new SpellData.T4cEffect(
        2,
        List.of(
            new SpellData.T4cEffect.EffectParam(2, attribute),
            new SpellData.T4cEffect.EffectParam(3, amount)));
  }

  private static SpellData definition(
      int id,
      String suffix,
      String icon,
      String impact,
      int element,
      List<SpellData.T4cEffect> effects) {
    String key = "mob_divine_" + suffix;
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
        icon,
        null,
        impact,
        0,
        0,
        null,
        null,
        (int) (CAST_INTERVAL_MILLIS / 1000),
        Integer.toString(DURATION_SECONDS * 1000),
        "0",
        0,
        null,
        id,
        element,
        4,
        SpellData.ATTACK_MENTAL,
        "100",
        "0",
        "0",
        "0",
        0,
        0,
        false,
        effects);
  }
}
