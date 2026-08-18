package com.perso.T4C.npc.addon;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class OlinHaad3 extends ScriptedNpc {

  public static final String ID = "OlinHaad3";

  public static final String DISPLAY_NAME = "${npc.olinhaad3}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupSimpleStaff")),
          0,
          List.of(),
          "${npc.welcome.olinhaad3}",
          List.of(),
          "OlinHaad3NPC",
          new NpcSpec.CombatProfile(100, 1000000, 200, 220, 240, 10000, 730, 1, "1d90+69"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onPopup(NpcBehaviorContext c) {

          if (c.globalFlag("ADDON_GLURIURL_PRESENT") == 1) c.selfDestructNpc();
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          c.sayKey(
              p < 41
                  ? "npc.olin3.silence"
                  : p == 41 ? "npc.olin3.progress.41" : "npc.olin3.progress.done");

          if (p >= 41) c.npc().provoke();
        }

        @Override
        public void onAttacked(NpcBehaviorContext c) {

          int roll = (int) (Math.random() * 21);

          if (roll == 0) c.castSelfSpell(10763);
          else if (roll == 1) c.shoutKey("npc.olin3.attacked");
        }

        @Override
        public void onDeath(NpcBehaviorContext c) {

          if (c.globalFlag("ADDON_GLURIURL_PRESENT") == 0) {

            c.shoutKey("npc.olin3.death.first");

            c.shoutKey("npc.olin3.death.second");

            c.summon("GLURIURL", 1772, 2379, 0);
          }
        }
      };

  public OlinHaad3(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
