package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemGloomStaffEffect {
  private ItemGloomStaffEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_gloom_staff_effect}",
        "${spell.description.item_gloom_staff_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "0",
        "64kSpellEnergyBallBlack-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "300000",
        "0",
        233,
        null,
        10417,
        0,
        0,
        1,
        "100",
        "0",
        "0",
        "0",
        30062,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_fire"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.r_fire/4)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.r_water/4)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_earth"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.r_earth/4)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_air"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.r_air/4)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "ac"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.ac/4)")))));
  }
}
