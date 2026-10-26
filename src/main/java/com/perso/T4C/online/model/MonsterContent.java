package com.perso.T4C.online.model;

import com.perso.T4C.monster.core.MonsterDef;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Versioned runtime content, exported from the game registries without desktop classes. */
public record MonsterContent(int version, Map<String, MonsterDef> monsters, List<Spawn> spawns) {
  public static final int VERSION = 1;

  public record Spawn(String type, String definition, int x, int y, int world,
      boolean stationary, boolean aggressive) {
    public Spawn {
      if (type == null || type.isBlank() || x < 0 || y < 0 || world < 0)
        throw new IllegalArgumentException("Invalid monster spawn");
      // Null records an authored spawn with custom behavior but no registry definition.
    }
  }

  public MonsterContent {
    if (version != VERSION) throw new IllegalArgumentException("Unsupported monster content version");
    monsters = Map.copyOf(monsters);
    spawns = List.copyOf(spawns);
    if (monsters.isEmpty()) throw new IllegalArgumentException("Empty monster content");
    monsters.forEach((key, definition) -> {
      if (key.isBlank() || !key.equals(definition.getName()))
        throw new IllegalArgumentException("Monster identity mismatch");
      Objects.requireNonNull(definition.getLoot());
      Objects.requireNonNull(definition.getAttacks());
    });
    for (var spawn : spawns) {
      if (spawn.definition() != null && !monsters.containsKey(spawn.definition()))
        throw new IllegalArgumentException("Unresolved monster definition");
    }
  }

  /** Authored tile positions. Unsupported custom spawns remain visible to the caller. */
  public List<Spawn> region(int world, int x, int y, int width, int height) {
    if (world < 0 || x < 0 || y < 0 || width <= 0 || height <= 0)
      throw new IllegalArgumentException("Invalid content region");
    return spawns.stream().filter(s -> s.world() == world && s.x() >= x && s.y() >= y
        && s.x() < (long) x + width && s.y() < (long) y + height).toList();
  }
}
