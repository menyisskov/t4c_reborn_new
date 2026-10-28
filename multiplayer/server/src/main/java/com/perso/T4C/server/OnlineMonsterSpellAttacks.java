package com.perso.T4C.server;

import com.perso.T4C.combat.MonsterCombatRules;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterSpellDamageRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.MonsterSpellContent;
import java.util.List;
import java.util.Random;

/** The bounded subset of authored monster spells which can safely damage an online player. */
final class OnlineMonsterSpellAttacks {
  private OnlineMonsterSpellAttacks() {}

  static MonsterSpellContent.Spell supportedSpell(int spellId) {
    return OnlineMonsterSpellCatalog.bundled().damageSpell(spellId);
  }

  static boolean supported(MonsterDef definition) {
    if (!definition.isCanAttack()) return true;
    List<MonsterDef.Attack> attacks = definition.getAttacks();
    if (attacks == null) return false;
    return attacks.stream().allMatch(attack -> attack != null && (attack.isSpell()
        ? supportedSpell(attack.getSpellId()) != null
        : attack.getName() != null && attack.getName().matches("(?i)[1-9][0-9]*d[1-9][0-9]*([+-][0-9]+)?")));
  }

  static int rawDamage(MonsterDef definition, int spellId, Random random) {
    var stats = MonsterCombatRules.resolve(definition);
    return MonsterSpellDamageRules.rollDamage(spellId, stats.intelligence(), stats.level(), random);
  }

  static int elementalDamage(MonsterSpellContent.Spell spell, MonsterDef definition, int rawDamage,
      CharacterProgress progress, ItemContent items, long now) {
    int balanced = MonsterSpellDamageRules.balanceElementalDamage(
        spell.id(), Math.max(1, definition.getLevel()), rawDamage, definition.getName());
    int resistance = OnlineSpellContexts.effectiveResistance(progress, items, now).of(spell.element());
    return MonsterSpellDamageRules.damageAfterResistance(balanced, resistance);
  }
}
