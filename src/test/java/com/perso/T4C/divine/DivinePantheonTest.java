package com.perso.T4C.divine;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.death.DeathPenaltyService;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.CollisionMapIO;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.item.*;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.npc.behavior.RebirthBehavior;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.SpawnRegistry;
import com.perso.T4C.spell.SpellRegistry;
import java.io.File;
import java.util.BitSet;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DivinePantheonTest {
  @BeforeEach
  void load() {
    ItemJsonLoader.loadAndRegister("assets/items");
  }

  @AfterEach
  void reset() {
    ItemRegistry.resetAdditionalDefinitions();
    CollisionManager.getInstance().clear();
  }

  @Test
  void everyShrineResolvesItsEquipmentSkillsLootAndNonAggressiveSpawn() throws Exception {
    var manager = new MonsterManager(null);
    manager.initializeMonstersFromMap("assets/maps/worldmap/worldmap.mapbin");
    for (var god : DivinePantheon.all()) {
      MonsterDef monster = MonsterRegistry.findByName("Divine " + god.name());
      assertNotNull(monster, god.id());
      assertFalse(monster.isDefaultAggressive(), god.id());
      assertTrue(monster.getRespawnTime() >= 300000 && monster.getRespawnTime() <= 600000);
      assertTrue(
          SpawnRegistry.monsters().stream()
              .anyMatch(
                  s ->
                      s.type().equals(monster.getName())
                          && s.x() == god.x()
                          && s.y() == god.y()
                          && !s.stationary()
                          && !s.aggressive()),
          god.id());
      manager.updateVisible(0f, null, god.x() - 1, god.x() + 1, god.y() - 1, god.y() + 1, 0);
      assertEquals(
          1L,
          manager.getMonsters().stream()
              .filter(loaded -> loaded.getCanonicalName().equals(monster.getName()))
              .count(),
          god.id() + " must load exactly one embodiment, independent of the spawn multiplier");
      for (String key : DivinePantheon.equipmentKeys(god)) {
        ItemDefinition item = ItemRegistry.findByKey(key);
        assertNotNull(item, key);
        assertSame(item, ItemRegistry.findByNumId(item.getNumId()), key + " numeric ID collision");
        assertTrue(ItemSalePricing.sellPrice(item) > 1, key);
        assertTrue(monster.getLoot().stream().anyMatch(drop -> drop.getItem().equals(key)), key);
      }
      assertNotNull(MonsterPuppetDress.find(monster.getItemBack()), god.id());
      assertEquals(
          .01f,
          monster.getLoot().stream()
              .filter(drop -> drop.getItem().equals(god.scrollKey()))
              .findFirst()
              .orElseThrow()
              .getChance());
      for (var drop : monster.getLoot())
        assertNotNull(ItemRegistry.findByKey(drop.getItem()), drop.getItem());
      for (var attack : monster.getAttacks())
        if (attack.isSpell()) assertNotNull(SpellRegistry.findById(attack.getSpellId()), god.id());
      assertEquals(
          god.tier() >= 2,
          monster.getAttacks().stream()
              .anyMatch(a -> DivineAfflictions.isAffliction(a.getSpellId())),
          god.id());
    }
  }

  @Test
  void seraphTrialsGuaranteeOneQuestItemAndRollWearableAndScrollIndependently() throws Exception {
    for (String id : java.util.List.of("artherk", "ogrimar")) {
      var god = DivinePantheon.find(id);
      var monster = new DivineAvatar(DivinePantheon.definition(id), 0, 0);
      assertEquals(god.health(), monster.getMaxHealth());
      assertTrue(god.level() < DivinePantheon.find("demeter").level());
      assertTrue(god.health() < DivinePantheon.find("demeter").health());
      assertTrue(god.damage() < DivinePantheon.find("demeter").damage());
      // Exercise both probability boundaries and both independent success combinations.
      for (float[] rolls :
          new float[][] {{.009f, .499f}, {.01f, .5f}, {.009f, .5f}, {.01f, .499f}}) {
        Random rng =
            new Random() {
              private int at;

              @Override
              public float nextFloat() {
                return rolls[at++];
              }

              @Override
              public int nextInt(int bound) {
                return bound - 1;
              }
            };
        var items = monster.getLootTable().roll(rng).itemNames();
        assertEquals(
            1, items.stream().filter(DivinePantheon.seraphQuestKeys(god)::contains).count());
        assertEquals(
            rolls[1] < .5f ? 1 : 0,
            items.stream().filter(DivinePantheon.equipmentKeys(god)::contains).count());
        assertEquals(rolls[0] < .01f, items.contains(god.scrollKey()));
        assertEquals(1 + (rolls[1] < .5f ? 1 : 0) + (rolls[0] < .01f ? 1 : 0), items.size());
      }
      assertEquals(
          1f,
          monster.getDefinition().getLoot().stream()
              .filter(d -> DivinePantheon.seraphQuestKeys(god).contains(d.getItem()))
              .map(MonsterDef.LootDrop::getChance)
              .reduce(0f, Float::sum));
      assertEquals(
          .5f,
          monster.getDefinition().getLoot().stream()
              .filter(d -> DivinePantheon.equipmentKeys(god).contains(d.getItem()))
              .map(MonsterDef.LootDrop::getChance)
              .reduce(0f, Float::sum));
    }
  }

  @Test
  void seniorGodsAreStrongerThanTheElderTrialsWithoutACharacterLevelGate() throws Exception {
    var elders = DivinePantheon.all().stream().filter(god -> god.tier() == 2).toList();
    int elderHealth = elders.stream().mapToInt(DivinePantheon.God::health).max().orElseThrow();
    int elderDamage = elders.stream().mapToInt(DivinePantheon.God::damage).max().orElseThrow();
    int elderArmor =
        elders.stream()
            .mapToInt(god -> DivinePantheon.definition(god.id()).getAcMax())
            .max()
            .orElseThrow();
    var seniors = DivinePantheon.all().stream().filter(god -> god.tier() == 3).toList();
    assertFalse(seniors.isEmpty());
    for (var senior : seniors) {
      MonsterDef definition = DivinePantheon.definition(senior.id());
      assertTrue(senior.level() >= 350);
      assertTrue(senior.health() > elderHealth, senior.id());
      assertTrue(senior.damage() > elderDamage, senior.id());
      assertTrue(definition.getAcMin() > elderArmor, senior.id());
      assertTrue(definition.getDodge() > senior.level() * 4, senior.id());
      assertTrue(new DivineAvatar(definition, 0, 0).canBeAttackedByPlayer(), senior.id());
    }
  }

  @Test
  void boundScrollSurvivesDeathSaveAndRebirthAndStillReturnsToItsGod() throws Exception {
    var god = DivinePantheon.find("artherk");
    assertBoundReturn(god.scrollKey(), god.x() + 3, god.y() + 3);
    var camp = com.perso.T4C.spawn.HuntingGrounds.camps().getFirst();
    assertBoundReturn(camp.scrollKey(), camp.x(), camp.y());
  }

  private void assertBoundReturn(String scrollKey, int x, int y) throws Exception {
    CollisionManager.getInstance().clear();
    Player player = new Player();
    player.setStrength(400);
    assertTrue(InventoryService.add(player, scrollKey).success());
    String key = ItemDefinition.normalizeKey(scrollKey);
    player.setItemDropCallback(
        (i, k) -> {
          fail("Bound scroll must never reach the drop callback");
          return true;
        });
    assertFalse(player.dropInventoryItem(0, key));
    assertEquals(
        InventoryService.Failure.BOUND_ITEM, StorageService.deposit(player, 0, key).failure());
    assertEquals(
        InventoryService.Failure.BOUND_ITEM,
        AccountStorageService.deposit(player, 0, key).failure());
    assertEquals(0, ItemSalePricing.sellPrice(ItemRegistry.findByKey(key)));
    var allDrop = new DeathPenaltyService.Rates(100, 100, 0, 0, 0);
    var penalty = new DeathPenaltyService(new DeathPenaltyService.Config(allDrop, allDrop));
    assertTrue(
        penalty
            .apply(player, false, XpCurve.loadDefault(), new Random(1))
            .droppedItems()
            .isEmpty());
    assertTrue(RebirthBehavior.perform(player));
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(player), restored);
    assertEquals(1, InventoryService.count(restored, key));
    assertTrue(ItemUseService.useOnSelf(restored, key, new Random(1)).success());
    assertEquals(x * GRID_W, restored.getPositionVector().x);
    assertEquals(y * GRID_H, restored.getPositionVector().y);
    assertEquals(1, InventoryService.count(restored, key), "Returning must not consume the scroll");
  }

  @Test
  void humanoidGodCanBeChallengedAndRangedDamageUsesItsTier() throws Exception {
    var god = DivinePantheon.find("ogrimar");
    var monster = new DivineAvatar(DivinePantheon.definition(god.id()), 0, 0);
    assertTrue(monster.usesHumanoidAnimations());
    assertTrue(monster.canBeAttackedByPlayer());
    assertFalse(monster.isAggressive());
    int damage = monster.rollMonsterSpellDamage(god.judgmentSpellId());
    assertTrue(damage >= god.damage() / 2 && damage <= god.damage());
    Player challenger = new Player();
    challenger.setWorldPosition(GRID_W, 0, 0);
    java.util.concurrent.atomic.AtomicInteger retaliation =
        new java.util.concurrent.atomic.AtomicInteger();
    monster.setDamageCallback((attacker, dealt) -> retaliation.addAndGet(dealt));
    monster.update(0f, challenger.getPositionVector(), java.util.List.of(monster));
    assertEquals(0, retaliation.get(), "The trial must wait until challenged");
    monster.applyPlayerDamage(1, challenger, null);
    assertEquals(god.health() - 1, monster.getHealth());
    monster.update(0f, challenger.getPositionVector(), java.util.List.of(monster));
    assertTrue(retaliation.get() > 0, "A peaceful god must retaliate after taking player damage");
  }

  @Test
  void elderJudgmentCreatesModeratePressureBeyondACursedFiftyRebirthAura() throws Exception {
    var thor = DivinePantheon.find("thor");
    var monster = new JudgmentProbe(thor);
    int roll = monster.rollJudgment();
    assertTrue(
        roll >= thor.judgmentDamageMin() && roll <= thor.judgmentDamageMax(),
        "The actual attack path must retain the divine range instead of the ordinary caster cap");
    assertEquals(
        thor.judgmentDamageMax(),
        MonsterSpellRules.elementalDamageLimit(thor.judgmentSpellId(), thor.level()));
    // Representative equipped archer readings from the reported high-rebirth build.
    var stats = new com.perso.T4C.combat.SeraphAuraService.EffectiveStats(270, 1716, 632, 270, 270);
    double averageCursedAuraHeal =
        (stats.sum() / 5 * 2 + 6)
            * .6
            * com.perso.T4C.combat.SeraphAuraService.effectivePercent(
                com.perso.T4C.combat.SeraphAuraService.healingChance(50))
            / 100;
    int low = MonsterSpellRules.damageAfterResistance(thor.judgmentDamageMin(), 652);
    int high = MonsterSpellRules.damageAfterResistance(thor.judgmentDamageMax(), 652);
    assertTrue(
        (low + high) / 2d > averageCursedAuraHeal,
        "Curse should make occasional active healing useful even with the strong aura");
    assertTrue(
        high < 9000 / 10,
        "A normal judgment should leave substantial reaction room at the reported HP baseline");
    assertEquals(
        MonsterSpellRules.maximumElementalDamage(250),
        MonsterSpellRules.balanceRawDamage(10095, 250, 499),
        "The Lesser Drake pack reduction must remain in place");
  }

  private static final class JudgmentProbe extends DivineAvatar {
    JudgmentProbe(DivinePantheon.God god) throws com.perso.T4C.exception.GameException {
      super(DivinePantheon.definition(god.id()), 0, 0);
      attacks =
          getDefinition().getAttacks().stream()
              .filter(attack -> attack.getSpellId() == god.judgmentSpellId())
              .toList();
    }

    int rollJudgment() {
      return rollDamage();
    }
  }

  @Test
  void mountainsFistCanKillAnubisAndAwardLoot() throws Exception {
    Player caster = new Player();
    caster.setLevel(400);
    caster.setWisdom(300000);
    caster.setIntelligence(1000);
    caster.setMana(1000);
    caster.setSpells(new java.util.ArrayList<>(java.util.List.of("${spell.mountains_fist}")));
    var spell = SpellRegistry.findById(99928);
    var god = MonsterRegistry.create(MonsterRegistry.findByName("Divine Anubis"), GRID_W, 0);
    var manager = new MonsterManager(null);
    var lootAwards = new java.util.concurrent.atomic.AtomicInteger();
    manager.setLootCallback(m -> lootAwards.incrementAndGet());
    manager.addMonster(god);
    var cast =
        com.perso.T4C.spell.SpellCastingService.begin(
            new com.perso.T4C.spell.SpellCastingService.Request(
                spell,
                caster,
                com.perso.T4C.spell.SpellCastingService.TargetKind.HOSTILE_UNIT,
                1f,
                true,
                false,
                true));
    assertTrue(cast.success(), cast.failure().name());
    assertTrue(cast.manaSpent() > 0);
    var impact = new com.perso.T4C.spell.SpellEffectManager().resolve(spell, caster, god, 0);
    assertTrue(impact.healthDelta() < 0);
    god.applyPlayerDamage(-impact.healthDelta(), caster, null);
    assertTrue(god.isDead(), "A sufficiently powerful offensive spell must be able to kill a god");
    manager.notifyKilledByPlayer(god);
    manager.notifyKilledByPlayer(god);
    assertEquals(1, lootAwards.get(), "Spell kills award the god's loot exactly once");
  }

  @Test
  void clickingTheRenderedHeadSelectsTheGodForCombat() throws Exception {
    // Real packed angel outfits extend 99 pixels above the anchor. The old generic
    // monster rectangle ended at 60 pixels, so head/upper-wing clicks missed entirely.
    var monster =
        new DivineAvatar(DivinePantheon.definition("ogrimar"), 100, 200) {
          @Override
          public com.badlogic.gdx.math.Rectangle getRenderBounds(
              com.badlogic.gdx.math.Rectangle out) {
            return out.set(getPosition().x - 36, getPosition().y - 99, 100, 115);
          }
        };
    var manager = new MonsterManager(null);
    manager.addMonster(monster);
    var camera =
        new com.badlogic.gdx.graphics.OrthographicCamera() {
          @Override
          public com.badlogic.gdx.math.Vector3 unproject(com.badlogic.gdx.math.Vector3 coords) {
            return coords;
          }
        };
    var selected = new java.util.concurrent.atomic.AtomicReference<BaseMonster>();
    var input =
        new com.perso.T4C.input.MonsterInputHandler(
            camera, manager, new Player(), null, null, null);
    input.setOnAttackTargetSelected(selected::set);
    assertTrue(input.touchDown(100, 115, 0, 0));
    assertSame(monster, selected.get());
    assertFalse(monster.isMouseOver(100, 95));
  }

  @Test
  void shrinesAndScrollArrivalsAreReachableCombatGroundOnTheNewMaps() throws Exception {
    var map = CollisionMapIO.read(new File("assets/maps/worldmap/worldmap.colbin"));
    byte[] tiles = map.getData();
    int width = map.getWidth();
    BitSet reached = new BitSet(tiles.length);
    int[] queue = new int[tiles.length];
    int head = 0, tail = 0;
    for (int start : new int[] {1477 * width + 4040, 1150 * width + 5505}) {
      reached.set(start);
      queue[tail++] = start;
    }
    while (head < tail) {
      int at = queue[head++], x = at % width, y = at / width;
      for (int next :
          new int[] {
            x > 0 ? at - 1 : -1,
            x + 1 < width ? at + 1 : -1,
            y > 0 ? at - width : -1,
            y + 1 < map.getHeight() ? at + width : -1
          }) {
        if (next >= 0
            && !reached.get(next)
            && !CollisionType.fromValue(tiles[next] & 255).isBlocksMovement()) {
          reached.set(next);
          queue[tail++] = next;
        }
      }
    }
    for (var god : DivinePantheon.all()) {
      for (int tile : new int[] {god.y() * width + god.x(), (god.y() + 3) * width + god.x() + 3}) {
        assertTrue(reached.get(tile), god.id() + " shrine/return is unreachable");
        assertFalse(
            DeathPenaltyService.isSafeHaven(tiles[tile] & 255),
            god.id() + " cannot be fought in a sanctuary");
      }
    }
  }

  @Test
  void managerKeepsTheFiveMinuteIntervalAfterTheFirstRespawn() throws Exception {
    var avatar = new AgedAvatar();
    var manager = new MonsterManager(null);
    manager.addMonster(avatar);
    avatar.takeDamage(avatar.getMaxHealth());
    avatar.ageDeath(301000L);
    manager.update(1f, null);
    assertFalse(avatar.isDead());
    avatar.takeDamage(avatar.getMaxHealth());
    avatar.ageDeath(31000L);
    manager.update(1f, null);
    assertTrue(avatar.isDead(), "A second death must not inherit the ordinary 18-30 second refill");
  }

  private static final class AgedAvatar extends DivineAvatar {
    AgedAvatar() throws Exception {
      super(DivinePantheon.definition("artherk"), 0, 0);
    }

    void ageDeath(long milliseconds) {
      deathTime = System.currentTimeMillis() - milliseconds;
    }
  }

  @Test
  void changingMapsOrReloadingCannotRestartADefeatedShrineEarly() throws Exception {
    Player player = new Player();
    var defeated = new DivineAvatar(DivinePantheon.definition("artherk"), 0, 0);
    defeated.takeDamage(defeated.getMaxHealth());
    defeated.onDeath(player);
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(player), restored);
    var revisited = new DivineAvatar(DivinePantheon.definition("artherk"), 0, 0);
    revisited.onSpawn(restored);
    assertTrue(revisited.isDead());
    assertFalse(revisited.shouldRespawn());
  }
}
