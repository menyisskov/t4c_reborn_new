package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class WordOfRecall {
  private WordOfRecall() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.word_of_recall}",
        "${spell.description.word_of_recall}",
        "100",
        0,
        36,
        36,
        21,
        false,
        false,
        "64kSpellIconAirDefense",
        "Flak1-",
        null,
        0,
        0,
        "Explosion.wav",
        null,
        0,
        "0",
        "0",
        18753,
        null,
        10029,
        3,
        5,
        1,
        "100",
        "30000",
        "30000",
        "30000",
        30012,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(11, List.of()),
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10716"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10715"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10714"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10713"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10769"),
                    new SpellData.T4cEffect.EffectParam(2, "100")))));
  }
}
