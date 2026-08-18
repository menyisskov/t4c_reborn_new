package com.perso.T4C.harvest;

import lombok.Getter;

@Getter
public final class HerbDefinition {
  private final String id;
  private final String itemKey;
  private final String worldSprite;
  private final int spawnWeight;

  public HerbDefinition(String id, String itemKey, String worldSprite, int spawnWeight) {
    this.id = id == null ? "" : id.trim();
    this.itemKey = itemKey == null ? "" : itemKey.trim();
    this.worldSprite = worldSprite == null ? "" : worldSprite.trim();
    this.spawnWeight = Math.max(0, spawnWeight);
  }
}
