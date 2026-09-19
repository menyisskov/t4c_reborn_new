package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// Scroll & travel merchant at the Avalon Sanctuary Scroll & Travel Merchant door (1318,1512),
// worldZ 0. Sells item.scroll_of_avalon (teleports to the Sanctuary temple via the AvalonGateway
// spell) alongside a Chryseida-style mix of potions and other destination scrolls — see the
// "WayfarerBryndis" entry in npc/catalog/ShopCatalog.java.
@Spawn(type = "WayfarerBryndis", x = 1318, y = 1512, z = 0, stationary = false, aggressive = false)
public final class WayfarerBryndis extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "WayfarerBryndis";

  public static final String DISPLAY_NAME = "${npc.wayfarerbryndis}";

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
          "${npc.welcome.wayfarerbryndis}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.wayfarerbryndis.0.0}",
                      "${npc.topic_keyword.wayfarerbryndis.0.1}"),
                  "${npc.topic.wayfarerbryndis.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.wayfarerbryndis.1.0}"),
                  "${npc.topic.wayfarerbryndis.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.wayfarerbryndis.2.0}"),
                  "${npc.topic.wayfarerbryndis.2}",
                  List.of())),
          "WayfarerBryndisNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public WayfarerBryndis(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
