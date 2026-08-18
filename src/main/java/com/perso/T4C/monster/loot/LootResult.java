package com.perso.T4C.monster.loot;

import java.util.List;

public record LootResult(int gold, List<String> itemNames) {
  public boolean isEmpty() {
    return gold <= 0 && (itemNames == null || itemNames.isEmpty());
  }
}
