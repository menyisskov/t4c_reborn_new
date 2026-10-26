package com.perso.T4C.spawn;

import com.perso.T4C.divine.DivineReturnScrolls;
import com.perso.T4C.divine.DivineSpells;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.json.MonsterJsonDef;
import com.perso.T4C.spell.SpellData;
import java.util.List;

/** Patient training champions: endurance, not an armor-check raid or a spell/poison trap. */
public final class TrainingGroundContent {
  private TrainingGroundContent() {}

  public static List<ItemDefinition> scrolls() {
    return HuntingGrounds.all().stream()
        .filter(g -> g.trainingTier() > 0)
        .map(g -> DivineReturnScrolls.definition(g.scrollKey(), g.itemId(), g.spellId()))
        .toList();
  }

  public static List<SpellData> spells() {
    return HuntingGrounds.all().stream()
        .filter(g -> g.trainingTier() > 0)
        .map(
            c ->
                DivineSpells.returnSpell(
                    "item_training_"
                        + c.trainingTier()
                        + "_"
                        + c.family().toLowerCase(java.util.Locale.ROOT)
                        + "_return",
                    c.spellId(),
                    c.x(),
                    c.y()))
        .toList();
  }

  public static MonsterDef boss(int tier, String kind) {
    return creature(tier, kind, true);
  }

  public static MonsterDef normal(int tier, String kind) {
    return creature(tier, kind, false);
  }

  private static MonsterDef creature(int tier, String kind, boolean boss) {
    MonsterDef art =
        switch (kind) {
          case "Tarantula" -> com.perso.T4C.monster.r237Tarantula.definition();
          case "Skraug" -> com.perso.T4C.monster.SKRAUGCLANGBANGAH.definition();
          default -> com.perso.T4C.monster.r215Skeleton.definition();
        };
    MonsterJsonDef d = new MonsterJsonDef();
    d.name = boss ? "Training " + tier + " " + kind : (tier == 1 ? "Witness " : "Threnody ") + kind;
    d.displayName =
        "${monster.training_"
            + tier
            + "_"
            + kind.toLowerCase(java.util.Locale.ROOT)
            + (boss ? "" : "_pack")
            + "}";
    d.health = boss ? (tier == 1 ? 100000 : 250000) : (tier == 1 ? 5000 : 15000);
    d.level = boss ? (tier == 1 ? 150 : 250) : (tier == 1 ? 100 : 180);
    d.xpPerHit = 0;
    d.xpOnDeath = boss ? (tier == 1 ? 5000000 : 15000000) : (tier == 1 ? 25000 : 75000);
    d.hitDamageMin = boss ? (tier == 1 ? 155 : 260) : (tier == 1 ? 100 : 180);
    d.hitDamageMax = boss ? (tier == 1 ? 175 : 290) : (tier == 1 ? 140 : 230);
    d.respawnTime = tier == 1 ? 30000L : 45000L;
    d.walkPattern = art.getWalkPattern();
    d.attackPattern = art.getAttackPattern();
    d.deathPattern = art.getDeathPattern();
    d.soundAttack = art.getSoundAttack();
    d.soundDeath = art.getSoundDeath();
    d.soundHit = art.getSoundHit();
    d.stats.str = 80;
    d.stats.end = 120;
    d.stats.agi = 25;
    d.stats.intel = 30;
    d.stats.wis = 30;
    java.util.Arrays.fill(d.resists, 100);
    d.dodge = tier == 1 ? 25 : 50;
    d.armorClass.min = 0;
    d.armorClass.max = tier == 1 ? 10 : 20;
    d.aggro = boss ? 0 : 100;
    d.clan = 0;
    var attack = new MonsterJsonDef.AttackJson();
    attack.formula =
        boss ? (tier == 1 ? "1d21+154" : "1d31+259") : (tier == 1 ? "1d41+99" : "1d51+179");
    attack.combatAttack = boss ? (tier == 1 ? 300 : 500) : (tier == 1 ? 150 : 250);
    attack.percentage = 100;
    d.attacks = List.of(attack);
    d.gold.min = boss ? (tier == 1 ? 100000 : 200000) : (tier == 1 ? 200 : 500);
    d.gold.max = boss ? (tier == 1 ? 125000 : 250000) : (tier == 1 ? 400 : 1000);
    var loot = new MonsterJsonDef.LootJson();
    loot.item =
        "item.training_ground_"
            + tier
            + (kind.equals("Tarantula") ? "" : "_" + kind.toLowerCase(java.util.Locale.ROOT))
            + "_return_scroll";
    loot.chance = 1f;
    d.loot = boss ? List.of(loot) : List.of();
    return d.toMonsterDef();
  }
}
