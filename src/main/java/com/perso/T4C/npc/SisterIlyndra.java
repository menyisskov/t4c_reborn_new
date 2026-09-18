package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// Temple priest at the Avalon Sanctuary Temple door (1340,1479), worldZ 0. Refuge/recall-flavored
// dialogue and a paid heal, following the Kilhiam/BrotherKiran "temple" pattern (npc.kilhiam.temple
// / npc.kiran.temple topics, a stationary priest, HEAL keyword -> yes/no -> gold-for-hp).
@Spawn(type = "SisterIlyndra", x = 1340, y = 1479, z = 0, stationary = false, aggressive = false)
public final class SisterIlyndra extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public static final String ID = "SisterIlyndra";

  public static final String DISPLAY_NAME = "${npc.sisterilyndra}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.sisterilyndra}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.sisterilyndra.0.0}",
                      "${npc.topic_keyword.sisterilyndra.0.1}"),
                  "${npc.topic.sisterilyndra.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sisterilyndra.1.0}"),
                  "${npc.topic.sisterilyndra.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sisterilyndra.2.0}"),
                  "${npc.topic.sisterilyndra.2}",
                  List.of())),
          "SisterIlyndraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {
    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onInitialise(NpcBehaviorContext c) {
          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {
          c.sayKey("npc.ilyndra.temple");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {
          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("HEAL")) {
            if (c.player().getCurrentHp() >= c.player().getMaxHp()) {
              c.sayKey("npc.ilyndra.noheal");
              return true;
            }
            c.sayKey("npc.ilyndra.heal.ask");
            c.askYesNo("heal");
            return true;
          }

          return false;
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {
          if (!"heal".equals(state)) return false;

          if (!yes) {
            c.sayKey("npc.ilyndra.heal.no");
            return true;
          }

          int missing = c.player().getMaxHp() - c.player().getCurrentHp(),
              cost = Math.max(1, missing / 2);

          if (c.player().getGold() < cost) {
            c.sayKey("npc.ilyndra.heal.poor");
            return true;
          }

          c.player().addGold(-cost);
          c.player().applyHeal(c.player().getMaxHp(), c.player().getMaxHp());
          c.sayKey("npc.ilyndra.heal.done");
          return true;
        }
      };

  public SisterIlyndra(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }
}
