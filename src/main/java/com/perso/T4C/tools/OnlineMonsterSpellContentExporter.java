package com.perso.T4C.tools;

import com.perso.T4C.divine.DivineAfflictions;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.core.MonsterSpellRules;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.online.model.MonsterSpellContent;
import com.perso.T4C.online.model.MonsterSpellContent.Behavior;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.TreeSet;

/** Build-time export of the exact monster-only spell IDs referenced by authored monster attacks. */
public final class OnlineMonsterSpellContentExporter {
  private OnlineMonsterSpellContentExporter() {}

  public static MonsterSpellContent snapshot() {
    MonsterRegistry.resetToGeneratedDefinitions();
    MonsterJsonLoader.loadAndRegister();
    var ids = new TreeSet<Integer>();
    for (MonsterDef definition : MonsterRegistry.load()) {
      if (definition.getAttacks() == null) continue;
      definition.getAttacks().stream().filter(java.util.Objects::nonNull)
          .filter(MonsterDef.Attack::isSpell).map(MonsterDef.Attack::getSpellId).forEach(ids::add);
    }

    var spells = new TreeMap<String, MonsterSpellContent.Spell>();
    for (int id : ids) {
      SpellData spell = SpellRegistry.findById(id);
      Behavior behavior = behavior(id, spell);
      spells.put(Integer.toString(id), new MonsterSpellContent.Spell(id,
          spell == null ? null : spell.getKey(), element(id, spell, behavior), behavior));
    }
    return new MonsterSpellContent(MonsterSpellContent.VERSION, spells);
  }

  private static Behavior behavior(int id, SpellData spell) {
    if (MonsterSpellRules.healsCaster(id)) return Behavior.SELF_HEAL;
    if (id == 10596) return Behavior.TELEPORT;
    if (DivineAfflictions.isAffliction(id)) return Behavior.AFFLICTION;
    // DivineAvatar overrides the raw roll for these judgment IDs; do not substitute BaseMonster's roll.
    if (id >= 18100 && id < 18200) return Behavior.UNSUPPORTED;
    // These two legacy monster-only spells intentionally have no SpellRegistry entry.
    // MonsterSpellRules still supplies their roll/balance and visual fallback.
    if (spell == null) return id == 10095 || id == 10120
        ? Behavior.ELEMENTAL_DAMAGE
        : Behavior.UNSUPPORTED;
    if (MonsterSpellRules.usesArmor(id)) return Behavior.ARMOR_DAMAGE;
    if (MonsterSpellRules.isElementalDamageSpell(id) && elementId(spell.getElement()) >= 0)
      return Behavior.ELEMENTAL_DAMAGE;
    return Behavior.UNSUPPORTED;
  }

  private static String element(int id, SpellData spell, Behavior behavior) {
    if (behavior != Behavior.ELEMENTAL_DAMAGE) return null;
    if (spell == null) return MonsterSpellRules.fallbackElement(id);
    return switch (spell.getElement()) {
      case 1 -> "fire";
      case 2 -> "earth";
      case 3 -> "air";
      case 4 -> "water";
      case 5 -> "light";
      case 6 -> "dark";
      default -> "water";
    };
  }

  /** Element IDs follow the legacy single-player spell callback; zero uses its water fallback. */
  private static int elementId(int element) {
    return element >= 0 && element <= 6 ? element : -1;
  }

  public static String render(MonsterSpellContent content) {
    return OnlineContentJson.render(content);
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Expected output JSON path");
    var path = Path.of(args[0]);
    if (path.getParent() != null) Files.createDirectories(path.getParent());
    Files.writeString(path, render(snapshot()));
    System.out.println("Exported authored monster attack spell metadata.");
  }
}
