package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// Weapons merchant at the Avalon Sanctuary Weapons Merchant door (1364,1494), worldZ 0. Stock is
// wired via ShopCatalog under this NPC's id (see npc/catalog/ShopCatalog.java) — no boss-drop
// uniques listed here, those stay drop-only per their price: 0.
@Spawn(type = "QuartermasterElenna", x = 1364, y = 1494, z = 0, stationary = false, aggressive = false)
public final class QuartermasterElenna extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "QuartermasterElenna";

  public static final String DISPLAY_NAME = "${npc.quartermasterelenna}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword")),
          0,
          List.of(),
          "${npc.welcome.quartermasterelenna}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.quartermasterelenna.0.0}",
                      "${npc.topic_keyword.quartermasterelenna.0.1}"),
                  "${npc.topic.quartermasterelenna.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.quartermasterelenna.1.0}"),
                  "${npc.topic.quartermasterelenna.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.quartermasterelenna.2.0}"),
                  "${npc.topic.quartermasterelenna.2}",
                  List.of())),
          "QuartermasterElennaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public QuartermasterElenna(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
