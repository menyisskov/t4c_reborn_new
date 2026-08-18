package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobAntimagicShellSpell {
  private MobAntimagicShellSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_antimagic_shell_spell}",
        "${spell.description.mob_antimagic_shell_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconNoneMain",
        "64kSpellEnergyBallPurple-", "ElectricShield-", 0, 0,
        "Healing.wav", "Electric Shield.wav", 0, "60000", "0", 233,
        null, 10385, 0, 4, 1,
        "100", "2000", "0", "0",
        30067, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dark"), new SpellData.T4cEffect.EffectParam(3, "-(target.dark/2)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "light"), new SpellData.T4cEffect.EffectParam(3, "-(target.light/2)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "fire"), new SpellData.T4cEffect.EffectParam(3, "-(target.fire/2)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "earth"), new SpellData.T4cEffect.EffectParam(3, "-(target.earth/2)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "water"), new SpellData.T4cEffect.EffectParam(3, "-(target.water/2)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "air"), new SpellData.T4cEffect.EffectParam(3, "-(target.air/2)")))));
  }
}
