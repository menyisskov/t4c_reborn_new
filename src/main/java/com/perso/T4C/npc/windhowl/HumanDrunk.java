package com.perso.T4C.npc.windhowl;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class HumanDrunk extends ScriptedNpc {

  public static final String ID = "HumanDrunk";

  public static final String DISPLAY_NAME = "${npc.humandrunk}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1")),
          0,
          List.of(),
          "${npc.welcome.humandrunk}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.humandrunk.0.0}", "${npc.topic_keyword.humandrunk.0.1}"),
                  "${npc.topic.humandrunk.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.humandrunk.1.0}"),
                  "${npc.topic.humandrunk.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.humandrunk.2.0}",
                      "${npc.topic_keyword.humandrunk.2.1}",
                      "${npc.topic_keyword.humandrunk.2.2}",
                      "${npc.topic_keyword.humandrunk.2.3}",
                      "${npc.topic_keyword.humandrunk.2.4}"),
                  "${npc.topic.humandrunk.2}",
                  List.of())),
          "HumanDrunkNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  public HumanDrunk(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return StaticDialogueBehavior.INSTANCE;
  }
}
