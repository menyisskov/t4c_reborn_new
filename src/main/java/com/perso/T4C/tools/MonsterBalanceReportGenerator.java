package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dumps every registered monster (Java + JSON) with full combat/resist stats to JSON, for
 * comparing new-content bosses against the existing monster/level curve. Ad hoc analysis tool,
 * not wired into the compendium site.
 */
public final class MonsterBalanceReportGenerator {
  private MonsterBalanceReportGenerator() {}

  public static void main(String[] args) throws IOException {
    MonsterJsonLoader.loadAndRegister();
    List<MonsterDef> defs =
        MonsterRegistry.load().stream()
            .sorted(Comparator.comparing(MonsterDef::getLevel).thenComparing(MonsterDef::getName))
            .toList();

    List<Map<String, Object>> out = new ArrayList<>();
    for (MonsterDef def : defs) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("name", def.getName());
      m.put("displayName", I18n.resolve(def.getDisplayName()));
      m.put("level", def.getLevel());
      m.put("health", def.getHealth());
      m.put("mana", def.getMana());
      m.put("xpPerHit", def.getXpPerHit());
      m.put("xpOnDeath", def.getXpOnDeath());
      m.put("hitDamageMin", def.getHitDamageMin());
      m.put("hitDamageMax", def.getHitDamageMax());
      m.put("goldMin", def.getGoldMin());
      m.put("goldMax", def.getGoldMax());
      m.put("dodge", def.getDodge());
      m.put("acMin", def.getAcMin());
      m.put("acMax", def.getAcMax());
      m.put("aggro", def.getAggro());
      m.put("speed", def.getSpeed());
      m.put("canAttack", def.isCanAttack());
      m.put("respawnTimeMs", def.getRespawnTime());
      Map<String, Integer> stats = new LinkedHashMap<>();
      stats.put("str", def.getStr());
      stats.put("end", def.getEnd());
      stats.put("agi", def.getAgi());
      stats.put("intel", def.getIntel());
      stats.put("will", def.getWill());
      stats.put("wis", def.getWis());
      stats.put("luck", def.getLuck());
      m.put("stats", stats);
      int[] r = def.getResists();
      Map<String, Integer> resists = new LinkedHashMap<>();
      if (r != null) {
        String[] labels = {"air", "earth", "water", "fire", "dark", "light"};
        for (int i = 0; i < labels.length && i < r.length; i++) resists.put(labels[i], r[i]);
      }
      m.put("resists", resists);
      List<Map<String, Object>> attacks = new ArrayList<>();
      if (def.getAttacks() != null) {
        for (MonsterDef.Attack atk : def.getAttacks()) {
          Map<String, Object> a = new LinkedHashMap<>();
          a.put("formula", atk.getName());
          // BaseMonster#rollDamage: value1 is the combat-attack score fed into hit-chance
          // resolution (the real "hit power" stat); value2 is only a selection weight among
          // multiple eligible attacks at this range, not a hit percentage - do not label it as
          // one (see BaseMonster#pickAttack).
          a.put("combatAttack", atk.getValue1());
          a.put("selectionWeight", atk.getValue2());
          a.put("isSpell", atk.isSpell());
          if (atk.isSpell()) {
            a.put("spellId", atk.getSpellId());
            a.put("minRangeTiles", atk.getMinRangeTiles());
            a.put("maxRangeTiles", atk.getMaxRangeTiles());
          }
          attacks.add(a);
        }
      }
      m.put("attacks", attacks);
      out.add(m);
    }

    Path outPath = Path.of(args.length > 0 ? args[0] : "monsters_balance_report.json");
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter w = new FileWriter(outPath.toFile())) {
      gson.toJson(out, w);
    }
    System.out.println("Wrote " + out.size() + " monsters to " + outPath.toAbsolutePath());
  }
}
