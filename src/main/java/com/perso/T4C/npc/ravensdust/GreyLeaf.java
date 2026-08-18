package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spawn.SpawnKind;
import java.util.List;

@Spawn(type = "GreyLeaf", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
@Spawn(
    type = "GREYLEAF",
    x = 849,
    y = 2086,
    z = 0,
    stationary = false,
    aggressive = false,
    kind = SpawnKind.MONSTER)
public final class GreyLeaf extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String ID = "GreyLeaf";

  public static final String DISPLAY_NAME = "${npc.greyleaf}";

  public static final String SPRITE_BASE = "TreeEnt";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.greyleaf}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyleaf.0.0}", "${npc.topic_keyword.greyleaf.0.1}"),
                  "${npc.topic.greyleaf.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.greyleaf.1.0}",
                      "${npc.topic_keyword.greyleaf.1.1}",
                      "${npc.topic_keyword.greyleaf.1.2}"),
                  "${npc.topic.greyleaf.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyleaf.2.0}"),
                  "${npc.topic.greyleaf.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyleaf.3.0}"),
                  "${npc.topic.greyleaf.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyleaf.4.0}"),
                  "${npc.topic.greyleaf.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyleaf.5.0}"),
                  "${npc.topic.greyleaf.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyleaf.6.0}", "${npc.topic_keyword.greyleaf.6.1}"),
                  "${npc.topic.greyleaf.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyleaf.7.0}", "${npc.topic_keyword.greyleaf.7.1}"),
                  "${npc.topic.greyleaf.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyleaf.8.0}"),
                  "${npc.topic.greyleaf.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.greyleaf.9.0}"),
                  "${npc.topic.greyleaf.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.greyleaf.10.0}", "${npc.topic_keyword.greyleaf.10.1}"),
                  "${npc.topic.greyleaf.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.greyleaf.11.0}",
                      "${npc.topic_keyword.greyleaf.11.1}",
                      "${npc.topic_keyword.greyleaf.11.2}",
                      "${npc.topic_keyword.greyleaf.11.3}"),
                  "${npc.topic.greyleaf.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.greyleaf.12.0}",
                      "${npc.topic_keyword.greyleaf.12.1}",
                      "${npc.topic_keyword.greyleaf.12.2}",
                      "${npc.topic_keyword.greyleaf.12.3}"),
                  "${npc.topic.greyleaf.12}",
                  List.of())),
          "GreyLeafNPC",
          new NpcSpec.CombatProfile(38, 1584, 53, 49, 49, 19, 466, 162, "1d50+37"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onDeath(NpcBehaviorContext c) {

        if (c.itemCount("belladona_berries") == 0) c.giveItem("belladona_berries");
      }

      @Override
      public void onAttacked(NpcBehaviorContext c) {

        if (java.util.concurrent.ThreadLocalRandom.current().nextInt(25) == 0)
          c.summon("Demon Tree", c.npcTileX() - 4, c.npcTileY() - 4, 0);
      }

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        if (!c.hasItem("green_gemstone")) c.sayKey("npc.greyleaf.no_gem");
        else c.sayKey("npc.greyleaf.welcome");
      }
    };
  }

  public GreyLeaf(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
