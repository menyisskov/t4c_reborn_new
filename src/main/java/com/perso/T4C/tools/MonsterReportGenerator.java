package com.perso.T4C.tools;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

/** Dumps every registered monster (Java + JSON defined) with stats/XP/loot to a CSV report. */
public final class MonsterReportGenerator {
  private MonsterReportGenerator() {}

  public static void main(String[] args) throws IOException {
    MonsterJsonLoader.loadAndRegister();
    List<MonsterDef> defs =
        MonsterRegistry.load().stream()
            .sorted(Comparator.comparing(MonsterDef::getLevel).thenComparing(MonsterDef::getName))
            .toList();

    String outputPath = args.length > 0 ? args[0] : "monsters_report.csv";
    try (PrintWriter out =
        new PrintWriter(
            new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8))) {
      out.write('﻿');
      out.println(
          "name,displayName,level,health,mana,xpPerHit,xpOnDeath,hitDamageMin,hitDamageMax,"
              + "goldMin,goldMax,str,end,agi,intel,will,wis,luck,dodge,acMin,acMax,aggro,speed,"
              + "canAttack,tameable,tameMaxLevel,respawnTimeMs,lootItems");
      for (MonsterDef def : defs) {
        String loot =
            def.getLoot() == null
                ? ""
                : String.join(
                    "|",
                    def.getLoot().stream()
                        .map(l -> l.getItem() + ":" + l.getChance())
                        .toList());
        out.printf(
            "%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s,%s,%d,%d,%s%n",
            csv(def.getName()),
            csv(I18n.resolve(def.getDisplayName())),
            def.getLevel(),
            def.getHealth(),
            def.getMana(),
            def.getXpPerHit(),
            def.getXpOnDeath(),
            def.getHitDamageMin(),
            def.getHitDamageMax(),
            def.getGoldMin(),
            def.getGoldMax(),
            def.getStr(),
            def.getEnd(),
            def.getAgi(),
            def.getIntel(),
            def.getWill(),
            def.getWis(),
            def.getLuck(),
            def.getDodge(),
            def.getAcMin(),
            def.getAcMax(),
            def.getAggro(),
            def.getSpeed(),
            def.isCanAttack(),
            def.isTameable(),
            def.getTameMaxLevel(),
            def.getRespawnTime(),
            csv(loot));
      }
    }
    System.out.println("Wrote " + defs.size() + " monsters to " + outputPath);
  }

  private static String csv(String value) {
    if (value == null) return "";
    String escaped = value.replace("\"", "\"\"");
    return escaped.contains(",") || escaped.contains("\"") ? "\"" + escaped + "\"" : escaped;
  }
}
