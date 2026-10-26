package com.perso.T4C.npc;

import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.ArrayList;
import java.util.List;

@Spawn(
    type = "CelestialChronicler",
    x = 4048,
    y = 1488,
    z = 0,
    stationary = true,
    aggressive = false)
public final class CelestialChronicler extends ScriptedNpc {
  public static final String ID = "CelestialChronicler";
  public static final String DISPLAY_NAME = "${npc.celestial_chronicler}";
  public static final String SPRITE_BASE = null;

  public CelestialChronicler(NpcContext context) throws GameException {
    super(spec(), context);
  }

  public static NpcSpec spec() {
    List<NpcSpec.DialogueTopic> topics = new ArrayList<>();
    for (String topic : List.of("concord", "gods", "tiers", "scrolls", "settlements"))
      topics.add(
          new NpcSpec.DialogueTopic(
              List.of(topic), "${npc.celestial_chronicler." + topic + "}", List.of()));
    for (var god : DivinePantheon.all())
      topics.add(
          new NpcSpec.DialogueTopic(
              List.of(god.id()), "${npc.celestial_chronicler." + god.id() + "}", List.of()));
    for (String topic :
        List.of("hunting", "witness_hunts", "threnody_hunts", "kraanhold_hunts", "mainland_hunts"))
      topics.add(
          new NpcSpec.DialogueTopic(
              List.of("${npc.celestial_chronicler.keyword." + topic + "}"),
              "${npc.celestial_chronicler." + topic + "}",
              List.of()));
    return new NpcSpec(
        ID,
        DISPLAY_NAME,
        null,
        List.of(
            new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
            new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
            new NpcSpec.Part(BodyPart.BACK, "PupSeraphWhiteWings")),
        0,
        List.of(),
        "${npc.celestial_chronicler.welcome}",
        topics,
        "DivineConcord");
  }
}
