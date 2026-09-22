package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "SkywatchIlvara", x = 1830, y = 1900, z = 0, stationary = false, aggressive = false)
public final class SkywatchIlvara extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "SkywatchIlvara";

  public static final String DISPLAY_NAME = "${npc.skywatchilvara}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.skywatchilvara}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.skywatchilvara.0.0}",
                      "${npc.topic_keyword.skywatchilvara.0.1}"),
                  "${npc.topic.skywatchilvara.0}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.GIVE_QUEST, List.of("aerie_wyrmling_cull")))),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.skywatchilvara.1.0}",
                      "${npc.topic_keyword.skywatchilvara.1.1}"),
                  "${npc.topic.skywatchilvara.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.skywatchilvara.2.0}"),
                  "${npc.topic.skywatchilvara.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.skywatchilvara.3.0}",
                      "${npc.topic_keyword.skywatchilvara.3.1}"),
                  "${npc.topic.skywatchilvara.3}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.OPEN_SPELL_LEARNING, List.of("leyward_bastion"))))),
          "SkywatchIlvaraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public SkywatchIlvara(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
