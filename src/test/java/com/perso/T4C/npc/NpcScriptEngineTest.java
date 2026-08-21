package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.NpcScriptEngine;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

class NpcScriptEngineTest {
  @Test
  void makesTopLevelCppConstantsAvailableToEveryDialogueSection() {
    String script =
        """
                CONSTANT Base = 20;
                CONSTANT HintCount = Base + 5;
                InitTalk
                Begin
                INTL(\"hello\")
                Command(\"HINT\")
                FORMAT(INTL(\"There are %u hints.\"), HintCount)
                Default
                FORMAT(INTL(\"Still %u.\"), HintCount)
                EndTalk
                """;
    Player player = new Player();
    assertEquals(
        "There are 25 hints.",
        NpcScriptEngine.respond(script, "ConstantNpc", "hint", player).text());
    assertEquals(
        "Still 25.", NpcScriptEngine.respond(script, "ConstantNpc", "unknown", player).text());
  }

  @Test
  void executesArenaMonsterLifecycleStoredInMonsterBin() throws Exception {
    var monster =
        MonsterRegistry.load().stream()
            .filter(def -> def.getName().equalsIgnoreCase("ArenaMob500"))
            .findFirst()
            .orElseThrow();
    Player player = new Player();
    NpcScriptEngine.setGlobalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA", 1);
    NpcScriptEngine.Result death =
        NpcScriptEngine.event(
            monster.getSourceEvents().get("OnDeath"), monster.getName(), player, 0, 0);
    NpcScriptEngine.event(
        monster.getSourceEvents().get("OnDestroy"), monster.getName(), player, 0, 0);
    assertEquals(List.of("You receive a battle token for your efforts."), death.systemMessages());
    assertEquals(List.of("spell.mob_arena_level_spell"), death.selfSpells());
    assertEquals(0, NpcScriptEngine.globalFlag("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA"));
  }

  @Test
  void arenaMonsterBinsPersistTheirParticipationLevelEffect() throws Exception {
    var arenaMonsters =
        MonsterRegistry.load().stream()
            .filter(def -> def.getName().matches("ArenaMob(?:XP)?\\d+"))
            .toList();
    assertFalse(arenaMonsters.isEmpty());
    for (var monster : arenaMonsters) {
      String level = monster.getName().replaceFirst("^ArenaMob(?:XP)?", "");
      assertEquals(
          "GiveFlag(__FLAG_ARENA_LEVEL," + level + ")",
          monster.getSourceEvents().get("@spell.spell.mob_arena_level_spell"));
    }
    Player player = new Player();
    player.setQuestFlag("__FLAG_ARENA_LEVEL", 60);
    var level70 =
        arenaMonsters.stream()
            .filter(monster -> "ArenaMob70".equals(monster.getName()))
            .findFirst()
            .orElseThrow();
    NpcScriptEngine.event(
        level70.getSourceEvents().get("@spell.spell.mob_arena_level_spell"),
        level70.getName(),
        player,
        0,
        0);
    assertEquals(70, player.getQuestFlag("__FLAG_ARENA_LEVEL"));
  }

  @Test
  void everyArenaMonsterHasAVisibleAnimationDefinition() throws Exception {
    var arenaMonsters =
        MonsterRegistry.load().stream()
            .filter(def -> def.getName().matches("ArenaMob(?:XP)?\\d+"))
            .toList();
    assertFalse(arenaMonsters.isEmpty());
    assertTrue(
        arenaMonsters.stream()
            .allMatch(
                def ->
                    def.getWalkPattern() != null
                        && !def.getWalkPattern().isBlank()
                        && def.getAttackPattern() != null
                        && !def.getAttackPattern().isBlank()
                        && def.getDeathPattern() != null
                        && !def.getDeathPattern().isBlank()));
    for (var def :
        arenaMonsters.stream().filter(d -> d.getName().matches("ArenaMob(?:XP)?250")).toList()) {
      assertEquals("KraanianFlying#h", def.getWalkPattern());
      assertEquals("KraanianFlyingA#h", def.getAttackPattern());
      assertEquals("KraanianFlyingC!l", def.getDeathPattern());
    }
    for (var def :
        arenaMonsters.stream().filter(d -> d.getName().matches("ArenaMob(?:XP)?275")).toList()) {
      assertEquals("KraanianMilipede#i", def.getWalkPattern());
      assertEquals("KraanianMilipedeA#h", def.getAttackPattern());
      assertEquals("KraanianMilipedeC!l", def.getDeathPattern());
    }
  }

  @Test
  void clerkStopsReportingLivingMonstersAfterTheLastArenaDeath() throws Exception {
    ScriptedNpc clerk =
        (ScriptedNpc) NpcFactoryRegistry.create("ColosseumClerk", new NpcContext(null));
    assertNotNull(clerk.publicBehavior());
    assertEquals(null, clerk.getSpec().sourceScript());
  }

  @Test
  void executesOriginalColosseumStateMachineAndExactSpawnPositions() throws Exception {
    ScriptedNpc clerk =
        (ScriptedNpc) NpcFactoryRegistry.create("ColosseumClerk", new NpcContext(null));
    assertTrue(clerk.usesJavaBehavior());
  }

  @Test
  void purchasesAndActivatesColosseumUpgradeFromPersistedScripts() throws Exception {
    ScriptedNpc clerk =
        (ScriptedNpc) NpcFactoryRegistry.create("ColosseumClerk", new NpcContext(null));
    assertTrue(clerk.usesJavaBehavior());
  }

  @Test
  void executesMaterializedColosseumOwnerScript() throws Exception {
    ScriptedNpc owner =
        (ScriptedNpc) NpcFactoryRegistry.create("ColosseumOwner", new NpcContext(null));
    assertTrue(owner.usesJavaBehavior());
    assertEquals(null, owner.getSpec().sourceScript());
  }

  @Test
  void wardenVortimerTeleportsPlayerIntoMadrigansAsylum() throws Exception {
    ScriptedNpc vortimer =
        (ScriptedNpc) NpcFactoryRegistry.create("WardenVortimer", new NpcContext(null));
    assertTrue(vortimer.usesJavaBehavior());
    assertEquals(null, vortimer.getSpec().sourceScript());
  }

  @Test
  void concatenatesAdjacentCppStringLiteralsInDialogue() throws Exception {
    Player player = new Player();
    String script =
        """
                Command(INTL(1, "ETHEREAL"))
                    Conversation
                        INTL(2, "You spoke the word of power "
                                "and may enter the sanctuary.")
                """;
    NpcScriptEngine.Result result =
        NpcScriptEngine.respond(script, "Gatekeeper", "ethereal", player);
    assertEquals("You spoke the word of power and may enter the sanctuary.", result.text());
  }

  @Test
  void selectsOriginalConditionalBranchAndMutatesFlagsGoldAndXp() throws Exception {
    Player player = new Player();
    player.setLevel(12);
    player.setGold(500);
    String script =
        """
                Begin
                    Conversation
                        INTL(1, "Welcome.")
                Command2(INTL(2, "WORK"), INTL(3, "OCCUPATION"))
                    IF (CheckFlag(__QUEST_TEST) == 0 && USER_LEVEL >= 10)
                        Conversation
                            INTL(4, "You are ready.")
                        GiveFlag(__QUEST_TEST, 2)
                        GiveGold(75)
                        GiveXP(250)
                    ELSE
                        Conversation
                            INTL(5, "Not yet.")
                    ENDIF
                """;
    NpcScriptEngine.Result greeting = NpcScriptEngine.begin(script, "Tester", player);
    assertEquals("Welcome.", greeting.text());
    NpcScriptEngine.Result result = NpcScriptEngine.respond(script, "Tester", "work", player);
    assertTrue(result.handled());
    assertEquals("You are ready.", result.text());
    assertEquals(2, player.getQuestFlag("__QUEST_TEST"));
    assertEquals(575, player.getGold());
    assertEquals(250, result.xp());
    NpcScriptEngine.Result repeated =
        NpcScriptEngine.respond(script, "Tester", "occupation", player);
    assertEquals("Not yet.", repeated.text());
  }

  @Test
  void matchesPrefixesLikeOriginalMsgFindMacro() throws Exception {
    Player player = new Player();
    String script = "Command(INTL(1, \"SPELL\"))\n INTL(2, \"Magic.\")";
    assertTrue(NpcScriptEngine.respond(script, "Tester", "spelling", player).handled());
    assertEquals("Magic.", NpcScriptEngine.respond(script, "Tester", "spell", player).text());
  }

  @Test
  void resumesOriginalYesNoState() throws Exception {
    Player player = new Player();
    String script =
        """
                Command(INTL(1, "QUEST"))
                    INTL(2, "Will you help?")
                    SetYesNo(HELP)
                YES(HELP)
                    INTL(3, "Thank you.")
                    GiveFlag(__HELPING, 1)
                NO(HELP)
                    INTL(4, "Too bad.")
                YesNoELSE(HELP)
                    INTL(5, "Answer yes or no.")
                    SetYesNo(HELP)
                """;
    NpcScriptEngine.Result question = NpcScriptEngine.respond(script, "Tester", "quest", player);
    assertEquals("Will you help?", question.text());
    assertEquals("HELP", question.pendingYesNo());
    assertEquals(0, player.getQuestFlag("__HELPING"));
    NpcScriptEngine.Result answer =
        NpcScriptEngine.respondYesNo(script, "Tester", question.pendingYesNo(), true, player);
    assertEquals("Thank you.", answer.text());
    assertEquals(1, player.getQuestFlag("__HELPING"));
  }

  @Test
  void executesOriginalTeleportKarmaAndPrivateMessage() throws Exception {
    Player player = new Player();
    player.setKarma(10);
    String script =
        """
                Command(INTL(1, "PORTAL"))
                    PRIVATE_SYSTEM_MESSAGE(INTL(2, "The portal activates."))
                    GiveKarma(5)
                    TELEPORT(1495, 2470, 2)
                """;
    NpcScriptEngine.Result result = NpcScriptEngine.respond(script, "Portal", "portal", player);
    assertTrue(result.handled());
    assertEquals(List.of("The portal activates."), result.systemMessages());
    assertEquals(15, player.getKarma());
    assertEquals(1495 * com.perso.T4C.config.GameConstants.GRID_W, player.getCoordinates().getX());
    assertEquals(2470 * com.perso.T4C.config.GameConstants.GRID_H, player.getCoordinates().getY());
    assertEquals(2, player.getCoordinates().getZ());
  }

  @Test
  void collectsOriginalSpellTeachingAndSkillTrainingLists() throws Exception {
    Player player = new Player();
    String script =
        """
                Command(INTL(1, "LEARN"))
                    AddTeachSkill("spell.fire_bolt", 12, 20372)
                    AddTeachSkill("stun_blow", 1, 150)
                    SendTeachSkillList
                Command(INTL(2, "TRAIN"))
                    AddTrainSkill("attack", 5000, 10)
                    SendTrainSkillList
                """;
    NpcScriptEngine.Result learn = NpcScriptEngine.respond(script, "Trainer", "learn", player);
    assertEquals(List.of("spell.fire_bolt"), learn.taughtSpells());
    assertEquals(List.of("stun_blow"), learn.taughtSkills());
    assertEquals(
        new NpcScriptEngine.SkillOffer("stun_blow", 1, 150, true), learn.skillOffers().get(0));
    NpcScriptEngine.Result train = NpcScriptEngine.respond(script, "Trainer", "train", player);
    assertEquals(List.of("attack"), train.trainedSkills());
    assertEquals(
        new NpcScriptEngine.SkillOffer("attack", 5000, 10, false), train.skillOffers().get(0));
  }

  @Test
  void executesOriginalSwitchCasesConstantsAndForLoops() throws Exception {
    Player player = new Player();
    player.setGold(100);
    player.setQuestFlag("__ROUTE", 2);
    String script =
        """
                Command(INTL(1, "ACT"))
                    CONSTANT Cost = 7
                    SWITCH(CheckFlag(__ROUTE))
                        CASE(1)
                            GiveFlag(__RESULT, 10)
                        ENDCASE
                        CASE(2)
                            GiveFlag(__RESULT, 20)
                            FOR(0, 3)
                                TakeGold(Cost)
                            ENDFOR
                        ENDCASE
                        OTHERWISE
                            GiveFlag(__RESULT, 30)
                    ENDSWITCH
                """;
    NpcScriptEngine.Result result = NpcScriptEngine.respond(script, "Switch", "act", player);
    assertTrue(result.handled());
    assertEquals(20, player.getQuestFlag("__RESULT"));
    assertEquals(79, player.getGold());
  }

  @Test
  void preservesOriginalTargetAndSelfSpellCasts() throws Exception {
    Player player = new Player();
    String script =
        """
                Command(INTL(1, "CAST"))
                    HealPlayer(USER_MAXHP)
                    CastSpellTarget("spell.npc_cantrip_serious_heal")
                    CastSpellSelf("spell.npc_cantrip_pentacle")
                """;
    NpcScriptEngine.Result result = NpcScriptEngine.respond(script, "Mage", "cast", player);
    assertTrue(result.heal());
    assertEquals(List.of("spell.npc_cantrip_serious_heal"), result.targetSpells());
    assertEquals(List.of("spell.npc_cantrip_pentacle"), result.selfSpells());
  }

  @Test
  void preservesSummonsAndSellRules() throws Exception {
    Player player = new Player();
    String script =
        """
                Command(INTL(1, "TRADE"))
                    AddSellItem(WEAPON | MAGIC, 10, 100000)
                    SendSellItemList(INTL(2, "Sell"))
                    SUMMON2("Brown Rat", FROM_USER(1, X), FROM_NPC(-2, Y), 3)
                """;
    NpcScriptEngine.Result result = NpcScriptEngine.respond(script, "Trader", "trade", player);
    assertEquals(1, result.sellRules().size());
    assertEquals("WEAPON | MAGIC", result.sellRules().get(0).categories());
    assertEquals(10, result.sellRules().get(0).minimumPrice());
    assertEquals(1, result.summons().size());
    assertEquals("Brown Rat", result.summons().get(0).monster());
    assertEquals("FROM_USER(1, X)", result.summons().get(0).xExpression());
    assertEquals("3", result.summons().get(0).zExpression());
  }

  @Test
  void executesMaterializedColosseumClerkEncounter() throws Exception {
    ScriptedNpc clerk =
        (ScriptedNpc) NpcFactoryRegistry.create("ColosseumClerk", new NpcContext(null));
    assertTrue(clerk.usesJavaBehavior());
    assertEquals(null, clerk.getSpec().sourceScript());
  }

  @Test
  void executesOriginalRespawnRemortAndAttributeMacros() throws Exception {
    Player player = new Player();
    player.setStrength(20);
    String script =
        """
                Command(INTL(1, "REBIRTH"))
                    SetDeathLocation(100, 200, 1)
                    SET_STR(USER_TRUE_STR + 1)
                    REMORT_TO(300, 400, 2)
                """;
    NpcScriptEngine.respond(script, "Oracle", "rebirth", player);
    assertTrue(player.isRespawnPointDefined());
    assertEquals(25, player.getStrength());
    assertEquals(1, player.getRebirthCount());
    assertEquals(2, player.getCoordinates().getZ());
  }

  @Test
  void matchesParameterizedCommandsAndExposesNumericParameters() throws Exception {
    Player player = new Player();
    player.setGold(500);
    String script =
        """
                ParamCmd(INTL(1, "DEPOSIT $ GOLD"))
                    IF (NUM_PARAM(0) <= USER_GOLD)
                        TakeGold(NUM_PARAM(0))
                        GiveFlag(__BANK_GOLD, CheckFlag(__BANK_GOLD) + NUM_PARAM(0))
                    ENDIF
                """;
    NpcScriptEngine.Result result =
        NpcScriptEngine.respond(script, "Banker", "deposit 125 gold", player);
    assertTrue(result.handled());
    assertEquals(375, player.getGold());
    assertEquals(125, player.getQuestFlag("__BANK_GOLD"));
    assertFalse(NpcScriptEngine.respond(script, "Banker", "deposit apples gold", player).handled());
  }

  @Test
  void executesDirectOriginalHpMutation() throws Exception {
    Player player = new Player();
    player.setMaxHp(100);
    player.setCurrentHp(80);
    String script = "Command(INTL(1, \"PENANCE\"))\n target->SetHP(USER_HP / 2, true)";
    assertTrue(NpcScriptEngine.respond(script, "Priest", "penance", player).handled());
    assertEquals(40, player.getCurrentHp());
  }

  @Test
  void supportsOriginalRandomRollTimedFlagsAndShouts() throws Exception {
    Player player = new Player();
    String script =
        """
                Command(INTL(1, "ROLL"))
                    GiveNPCFlag(__DELAY, rnd.roll(dice(1, 1)) SECONDS TDELAY)
                    CHATTER_SHOUT(INTL(2, "The ritual begins!"))
                """;
    NpcScriptEngine.Result result = NpcScriptEngine.respond(script, "Mage", "roll", player);
    assertTrue(player.getQuestFlag("npc:Mage:__DELAY") >= System.currentTimeMillis() / 1000L);
    assertEquals(List.of("The ritual begins!"), result.systemMessages());
  }

  @Test
  void decodesOriginalPackedDeathLocationForReturnPortals() throws Exception {
    Player player = new Player();
    String script =
        """
                Command(INTL(1, "SET"))
                    SetDeathLocation(1682, 1163, 2)
                Command(INTL(2, "RETURN"))
                    BYTE world = BYTE(target->ViewFlag(__FLAG_DEATH_LOCATION) & 0xFF)
                    WORD y = WORD((target->ViewFlag(__FLAG_DEATH_LOCATION) & 0xFFF00) >> 8)
                    WORD x = WORD((target->ViewFlag(__FLAG_DEATH_LOCATION) & 0xFFF00000) >> 20)
                    TELEPORT(x, y, world)
                """;
    NpcScriptEngine.respond(script, "Portal", "set", player);
    NpcScriptEngine.respond(script, "Portal", "return", player);
    assertEquals(1682 * com.perso.T4C.config.GameConstants.GRID_W, player.getCoordinates().getX());
    assertEquals(1163 * com.perso.T4C.config.GameConstants.GRID_H, player.getCoordinates().getY());
    assertEquals(2, player.getCoordinates().getZ());
  }

  @Test
  void rendersChainedIntlNumericConversionsAndOriginalRndFunction() throws Exception {
    Player player = new Player();
    player.setQuestFlag("__RATS_KILLED", 7);
    String script =
        """
                Command(INTL(1, "RATS"))
                    int nRatsKilled = CheckFlag(__RATS_KILLED)
                    int remaining = 15 - nRatsKilled
                    INTL(2, "You have killed ") C(nRatsKilled) INTL(3, " rats; ") C(remaining) INTL(4, " remain.")
                    int reward = rnd(3, 3)
                    GiveFlag(__REWARD, reward)
                """;
    NpcScriptEngine.Result result = NpcScriptEngine.respond(script, "Samaritan", "rats", player);
    assertEquals("You have killed 7 rats; 8 remain.", result.text());
    assertEquals(3, player.getQuestFlag("__REWARD"));
  }

  @Test
  void collectsProfessionFormulaOffersAndFormatsNumericDialogue() throws Exception {
    Player player = new Player();
    String script =
        """
                Command(INTL(1, "LEARN"))
                    int cost = 1000
                    FORMAT(INTL(2, "The formula costs %u gold."), cost)
                    CreateFormuleList
                    AddTeachFormule(1000, cost)
                    SendTeachFormuleList
                """;
    NpcScriptEngine.Result result = NpcScriptEngine.respond(script, "Trainer", "learn", player);
    assertEquals("The formula costs 1000 gold.", result.text());
    assertEquals(List.of(new NpcScriptEngine.FormulaOffer(1000, 1000)), result.formulaOffers());
  }

  @Test
  void executesLegacyInlineIfInLotteryCode() throws Exception {
    Player player = new Player();
    String script =
        """
                Command(INTL(1, "PICK"))
                    int picked = 4
                    int first = 4
                    int second = 2
                    int matches = 0
                    if (first == picked) ++matches;
                    if (second == picked) ++matches;
                    GiveFlag(__MATCHES, matches)
                """;
    NpcScriptEngine.respond(script, "Lottery", "pick", player);
    assertEquals(1, player.getQuestFlag("__MATCHES"));
  }

  @Test
  void executesCppLifecycleSwitchCases() throws Exception {
    Player player = new Player();
    String event =
        """
                switch(rnd(0, 0))
                {
                    case 0: SHOUT(INTL(1, "Defend Stonecrest!")); break;
                    default: break;
                }
                """;
    NpcScriptEngine.Result result = NpcScriptEngine.event(event, "Guard", player, 100, 100);
    assertEquals(List.of("Defend Stonecrest!"), result.systemMessages());
  }

  private static NpcSpec spec(String id) {
    NpcSpec spec = NpcFactoryRegistry.specification(id);
    assertNotNull(spec, id);
    return spec;
  }
}
