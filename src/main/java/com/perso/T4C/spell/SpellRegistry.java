package com.perso.T4C.spell;

import com.perso.T4C.i18n.I18n;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpellRegistry {
  private static List<SpellData> cache;
  private static Map<String, SpellData> byName;

  private SpellRegistry() {}

  public static synchronized List<SpellData> load() {
    if (cache != null) {
      return cache;
    }
    rebuild(com.perso.T4C.spell.definition.SpellDefinitions.all());
    return cache;
  }

  public static synchronized SpellData findByName(String name) {
    load();
    if (name == null || name.isBlank()) return null;
    SpellData direct = byName.get(name);
    if (direct != null) return direct;
    SpellData identified = byName.get(identityOf(name));
    if (identified != null) return identified;
    String requested = canonicalKey(name);
    for (SpellData spell : cache) {
      if (spell != null && requested.equals(canonicalKey(spell.getKey()))) return spell;
    }
    String canonicalAlias =
        switch (requested) {
          case "spell.mob_fast_regen", "spell.mob_max_regen" -> "spell.mob_ai_regeneration_spell";
          case "spell.npc_cantrip_serious_heal" -> "spell.heal_serious";
          // T4C-0018 spell renames (same spellId, new display name/key) - aliased so characters
          // who already learned these under the old key keep them: their persisted
          // player.spells entries still hold the old "${spell.<old>}" string, and without this
          // alias findByName would return null for that string now that the old key/definition
          // is gone, silently unlearning the spell (spellbook omits it, casting rejects it).
          case "spell.sentinel" -> "spell.leyward_bastion";
          case "spell.divine_veil" -> "spell.veilstone_aegis";
          case "spell.clemancy" -> "spell.wellspring_mercy";
          case "spell.undead_annihilation" -> "spell.sunscour";
          case "spell.omega_planetoids" -> "spell.gravebreaker";
          default -> null;
        };
    if (canonicalAlias != null) {
      for (SpellData spell : cache) {
        if (spell != null && canonicalAlias.equals(canonicalKey(spell.getKey()))) return spell;
      }
    }
    Integer legacyId =
        switch (requested) {
          case "spell.olin_haad_teleport_2" -> 10751;
          case "spell.olin_haad_teleport_3" -> 10758;
          case "spell.olin_haad_teleport_4" -> 10760;
          default -> null;
        };
    if (legacyId != null) return findById(legacyId);
    return null;
  }

  static String identityOf(String value) {
    String key = I18n.keyOf(value);
    if (key != null) return key;
    if (value.startsWith("spell.")) return value;
    String candidate = "spell." + I18n.normalizedKey(value);
    return I18n.has(candidate) ? candidate : value;
  }

  private static String canonicalKey(String value) {
    if (value == null) return "";
    String key = I18n.keyOf(value);
    if (key != null) value = key;
    while (value.startsWith("spell.spell_")) {
      value = "spell." + value.substring("spell.spell_".length());
    }
    return value;
  }

  public static synchronized SpellData findById(int spellId) {
    if (spellId <= 0) return null;
    for (SpellData spell : load()) {
      if (spell != null && spell.getSpellId() == spellId) return spell;
    }
    return null;
  }

  public static synchronized List<SpellData> playerCastableSpells() {
    Map<String, SpellData> result = new LinkedHashMap<>();
    for (SpellData spell : load()) {
      if (!isPlayerCastable(spell)) continue;
      result.putIfAbsent(canonicalKey(spell.getKey()), spell);
    }
    return List.copyOf(result.values());
  }

  private static boolean isPlayerCastable(SpellData spell) {
    if (spell == null || spell.getName() == null || spell.getName().isBlank()) return false;
    if (spell.getIconId() == null || spell.getIconId().isBlank() || "0".equals(spell.getIconId()))
      return false;
    String identity = spell.getName().toLowerCase();
    if (!identity.startsWith("${spell.")) return false;
    if (identity.startsWith("${spell.item_")
        || identity.startsWith("${spell.mob_")
        || identity.startsWith("${spell.test_")
        || identity.startsWith("${spell.npc_")) return false;
    if (identity.endsWith("_effect}")) return false;
    return !spell.getT4cEffects().isEmpty() || identity.equals("${spell.tame_beast}");
  }

  public static synchronized void invalidate() {
    cache = null;
    byName = null;
  }

  private static void rebuild(List<SpellData> spells) {
    cache = List.copyOf(spells == null ? List.of() : spells);
    Map<String, SpellData> map = new LinkedHashMap<>();
    for (SpellData spell : cache) {
      if (spell != null && spell.getName() != null) {
        map.put(spell.getName(), spell);
        String key = spell.getKey();
        if (key != null) {
          map.putIfAbsent(key, spell);
        }
      }
    }
    byName = Map.copyOf(map);
  }
}
