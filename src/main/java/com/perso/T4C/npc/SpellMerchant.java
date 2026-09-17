package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.List;
import java.util.Locale;

@Spawn(type = "SpellMerchant", x = 2950, y = 1065, z = 0, stationary = true, aggressive = false)
public final class SpellMerchant extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SpellMerchant";

  public static final String DISPLAY_NAME = "${npc.spellmerchant}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.spellmerchant}",
          List.of(),
          "SpellMerchant",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public SpellMerchant(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(Locale.ROOT);

          if (k.equals("SPELLS") || k.equals("LEARN") || k.equals("BUY")) {

            List<String> spellNames =
                SpellRegistry.playerCastableSpells().stream()
                    .sorted(
                        java.util.Comparator.comparingInt(SpellData::getElement)
                            .thenComparingInt(SpellData::getPrice))
                    .map(SpellData::getName)
                    .toList();

            GuiManager.open(new LearnScreen(c.player(), spellNames));

            return true;
          }

          return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
        }
      };
}
