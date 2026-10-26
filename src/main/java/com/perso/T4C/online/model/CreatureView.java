package com.perso.T4C.online.model;

/** Public server-owned creature state, in coordinates local to the advertised map region. */
public record CreatureView(long id, String definition, double x, double y, int health, int maxHealth,
    String walkPattern, String attackPattern, String deathPattern) {
  public CreatureView {
    if (id <= 0 || definition == null || definition.isBlank() || definition.length() > 256
        || !Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0
        || maxHealth <= 0 || health < 0 || health > maxHealth
        || walkPattern == null || walkPattern.isBlank())
      throw new IllegalArgumentException("Invalid creature state");
    for (String pattern : new String[]{walkPattern, attackPattern, deathPattern})
      if (pattern != null && pattern.length() > 128) throw new IllegalArgumentException("Invalid creature pattern");
  }
}
