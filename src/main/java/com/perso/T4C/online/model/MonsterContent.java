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

    /** Stable across enumeration/balance changes; moving or renaming a spawn requires migration. */
    public String persistenceKey() {
      String identity = world + "\u0000" + x + "\u0000" + y + "\u0000" + type;
      try {
        return "authored-v1:" + java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
            .digest(identity.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
      } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
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

  /** Staged source inventory. Version one remains the only runtime format until hosted NPCs exist. */
  public record Classified(int version, Map<String, MonsterDef> monsters,
      Map<String, Authority> authorities, List<ClanRelation> relations,
      List<ClassifiedSpawn> spawns) {
    public static final int VERSION=2;
    public Classified {
      if (version!=VERSION) throw new IllegalArgumentException("Unsupported classified monster content version");
      monsters=Map.copyOf(monsters);authorities=Map.copyOf(authorities);
      relations=List.copyOf(relations);spawns=List.copyOf(spawns);
      if (monsters.isEmpty() || !authorities.keySet().equals(monsters.keySet()))
        throw new IllegalArgumentException("Missing monster authority metadata");
      monsters.forEach((name,definition) -> {
        if (name.isBlank() || !name.equals(definition.getName()))
          throw new IllegalArgumentException("Monster identity mismatch");
        Objects.requireNonNull(definition.getLoot());Objects.requireNonNull(definition.getAttacks());
      });
      for (var relation:relations)
        if (relation.source()==relation.target() || relation.source()==Clan.NEUTRAL
            || relation.target()==Clan.NEUTRAL)
          throw new IllegalArgumentException("Invalid clan relation");
      var identities=new java.util.HashSet<String>();
      for (var spawn:spawns) {
        if (!identities.add(spawn.persistenceKey()))
          throw new IllegalArgumentException("Duplicate authored spawn identity");
        if (spawn.ownership()==Ownership.MONSTER && spawn.definition()!=null
            && !monsters.containsKey(spawn.definition()))
          throw new IllegalArgumentException("Unresolved monster definition");
      }
    }
  }

  public enum Ownership { MONSTER, NPC }
  public enum Clan { NEUTRAL, ANIMAL, BEAST, CENTAUR, DEMON, DRACONIAN, GOBLIN, HORSE,
    HUMAN, INSECT, KOBOLD, KRAANIAN, ORC, SKAVEN, UNDEAD }
  public enum AttackFrameEvidence { FRAME_NAME_PRESENT, NO_FRAME, UNKNOWN }
  public record Authority(Clan clan, AttackFrameEvidence attackFrameEvidence) {
    public Authority { Objects.requireNonNull(clan);Objects.requireNonNull(attackFrameEvidence); }
  }
  public record ClanRelation(Clan source, Clan target) {
    public ClanRelation { Objects.requireNonNull(source);Objects.requireNonNull(target); }
  }
  /** MONSTER keeps offline definition precedence; canonicalNpcId also records independent NPCManager lookup. */
  public record ClassifiedSpawn(String type, String definition, String canonicalNpcId,
      Ownership ownership, int x, int y, int world, boolean stationary, boolean aggressive) {
    public ClassifiedSpawn {
      if (type==null || type.isBlank() || x<0 || y<0 || world<0)
        throw new IllegalArgumentException("Invalid classified spawn");
      Objects.requireNonNull(ownership);
      if (ownership==Ownership.NPC &&
          (definition!=null || canonicalNpcId==null || canonicalNpcId.isBlank()))
        throw new IllegalArgumentException("Conflicting classified spawn ownership");
    }
    public String persistenceKey() {
      return new Spawn(type,definition,x,y,world,stationary,aggressive).persistenceKey();
    }
  }
}
