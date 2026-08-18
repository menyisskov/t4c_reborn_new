package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemTorchRadiance {
  private ItemTorchRadiance() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_torch_radiance}",
        "${spell.description.item_torch_radiance}",
        "0", 0, 0, 0, 5,
        false, false, "64kIconTorch",
        null, null, 0, 0,
        null, null, 0, "600000", "0", 1393,
        null, 10015, 1, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, "TRUE"), new SpellData.T4cEffect.EffectParam(2, "Radiance"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
