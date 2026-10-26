package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "MeltarWinterstorm", x = 190, y = 1545, z = 1, stationary = false, aggressive = false)
public final class MeltarWinterstorm extends ScriptedNpc {
  private record GemReward(String name, int roughXp, int cutXp, String potion) {}

  private static final List<GemReward> GEMS =
      List.of(
          new GemReward("agate", 5_000, 20_000, "potion_of_lesser_air_resistance"),
          new GemReward("amethyst", 5_000, 20_000, "potion_of_lesser_water_resistance"),
          new GemReward("aquamarine", 15_000, 30_000, "potion_of_greater_water_resistance"),
          new GemReward("carnelian", 5_000, 20_000, "potion_of_lesser_fire_resistance"),
          new GemReward("diamond", 10_000, 25_000, "potion_of_partial_air_resistance"),
          new GemReward("emerald", 10_000, 25_000, "potion_of_partial_earth_resistance"),
          new GemReward("garnet", 15_000, 30_000, "potion_of_greater_fire_resistance"),
          new GemReward("limestone", 15_000, 30_000, "potion_of_greater_earth_resistance"),
          new GemReward("malachite", 5_000, 20_000, "potion_of_lesser_earth_resistance"),
          new GemReward("moonstone", 15_000, 30_000, "potion_of_greater_air_resistance"),
          new GemReward("ruby", 10_000, 25_000, "potion_of_partial_fire_resistance"),
          new GemReward("sapphire", 10_000, 25_000, "potion_of_partial_water_resistance"));

  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MeltarWinterstorm";

  public static final String DISPLAY_NAME = "${npc.meltarwinterstorm}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.meltarwinterstorm}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.0.0}",
                      "${npc.topic_keyword.meltarwinterstorm.0.1}"),
                  "${npc.topic.meltarwinterstorm.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.1.0}",
                      "${npc.topic_keyword.meltarwinterstorm.1.1}"),
                  "${npc.topic.meltarwinterstorm.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.2.0}",
                      "${npc.topic_keyword.meltarwinterstorm.2.1}",
                      "${npc.topic_keyword.meltarwinterstorm.2.2}"),
                  "${npc.topic.meltarwinterstorm.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.3.0}"),
                  "${npc.topic.meltarwinterstorm.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.4.0}",
                      "${npc.topic_keyword.meltarwinterstorm.4.1}",
                      "${npc.topic_keyword.meltarwinterstorm.4.2}",
                      "${npc.topic_keyword.meltarwinterstorm.4.3}"),
                  "${npc.topic.meltarwinterstorm.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.5.0}"),
                  "${npc.topic.meltarwinterstorm.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.6.0}"),
                  "${npc.topic.meltarwinterstorm.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.7.0}"),
                  "${npc.topic.meltarwinterstorm.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.8.0}"),
                  "${npc.topic.meltarwinterstorm.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.9.0}"),
                  "${npc.topic.meltarwinterstorm.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.10.0}",
                      "${npc.topic_keyword.meltarwinterstorm.10.1}"),
                  "${npc.topic.meltarwinterstorm.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.11.0}"),
                  "${npc.topic.meltarwinterstorm.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.12.0}",
                      "${npc.topic_keyword.meltarwinterstorm.12.1}",
                      "${npc.topic_keyword.meltarwinterstorm.12.2}",
                      "${npc.topic_keyword.meltarwinterstorm.12.3}",
                      "${npc.topic_keyword.meltarwinterstorm.12.4}"),
                  "${npc.topic.meltarwinterstorm.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.13.0}"),
                  "${npc.topic.meltarwinterstorm.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.meltarwinterstorm.14.0}",
                      "${npc.topic_keyword.meltarwinterstorm.14.1}"),
                  "${npc.topic.meltarwinterstorm.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.meltarwinterstorm.15.0}"),
                  "${npc.topic.meltarwinterstorm.15}",
                  List.of())),
          "MeltarWinterstormNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int f = c.flag("__FLAG_FACTION_WITH_THIEVES");

        c.sayKey(
            f <= 0
                ? "npc.meltar.f0"
                : f <= 200
                    ? "npc.meltar.f200"
                    : f <= 400
                        ? "npc.meltar.f400"
                        : f <= 600
                            ? "npc.meltar.f600"
                            : f <= 800 ? "npc.meltar.f800" : "npc.meltar.fhigh");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("CROWN") && k.contains("CORRUPTION")) {

          if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") != 1) c.sayKey("npc.meltar.crown.no");
          else if (c.flag("__FLAG_MELTAR_GAVE_CROWN") == 1) c.sayKey("npc.meltar.crown.already");
          else {

            c.giveItem("crown_of_corruption");

            c.flag("__FLAG_MELTAR_GAVE_CROWN", 1);

            c.sayKey("npc.meltar.crown.give");
          }

          return true;
        }

        if (k.contains("GEMS") || k.contains("PRECIOUS STONES")) {
          boolean hasGem =
              GEMS.stream()
                  .anyMatch(
                      gem ->
                          c.hasItem("rough_" + gem.name())
                              || c.hasItem("finely_cut_" + gem.name()));
          if (hasGem) {
            c.sayKey("npc.meltar.gems.ask");
            c.askYesNo("meltar_gems");
          } else c.sayKey("npc.meltar.gems.none");
          return true;
        }

        return com.perso.T4C.npc.behavior.StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String subject, boolean yes) {
        if (!"meltar_gems".equals(subject)) return false;
        if (!yes) return true;

        int turnedIn = 0;
        for (GemReward gem : GEMS) {
          for (boolean cut : new boolean[] {false, true}) {
            String item = (cut ? "finely_cut_" : "rough_") + gem.name();
            while (c.hasItem(item)) {
              c.takeItem(item);
              c.giveXp(cut ? gem.cutXp() : gem.roughXp());
              if (java.util.concurrent.ThreadLocalRandom.current().nextBoolean())
                c.giveItem(gem.potion());
              turnedIn++;
            }
          }
        }
        c.sayKey(turnedIn > 0 ? "npc.meltar.gems.done" : "npc.meltar.gems.none");
        return true;
      }
    };
  }

  public MeltarWinterstorm(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
