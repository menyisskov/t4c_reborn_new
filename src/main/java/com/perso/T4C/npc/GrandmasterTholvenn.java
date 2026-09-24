package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0033: the last of the three surviving Forgewrights of the First Pact (see
// npc/EmberSmithCorvain.java, npc/WardenSeressa.java), working within the Avalon Wilds
// (1330,1440), worldZ 0. Tholvenn is the only one who can still finish the working: combine a
// Tempered Godcore (quest/definition/ForgeTheGodcore.java) and a Bound Godsigil
// (quest/definition/BindTheGodsigil.java) into one of five Godsforged items, one per class
// archetype - see DESIGN_GUIDELINES.md "Godsforged: a tier above Legendary".
//
// A single QuestDef can only natively track one required item, so each forge_godsforged_* quest
// (see quest/definition/ForgeGodsforgedWarblade.java and its four siblings) only tracks the
// Bound Godsigil natively. javaBehavior() below checks the Tempered Godcore itself before ever
// touching quest state, consumes it manually once confirmed, then calls
// QuestService.completeCraftingQuest() - which checks/consumes the sigil and grants the item
// reward in the same step. This skips the normal accept-now-return-later flow entirely: as soon
// as a player has both components, naming the item finishes the forge in one conversation.
@Spawn(type = "GrandmasterTholvenn", x = 1330, y = 1440, z = 0, stationary = false, aggressive = false)
public final class GrandmasterTholvenn extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "GrandmasterTholvenn";

  public static final String DISPLAY_NAME = "${npc.grandmastertholvenn}";

  public static final String SPRITE_BASE = null;

  private static final String CORE_KEY = "item.tempered_godcore";
  private static final String SIGIL_KEY = "item.bound_godsigil";

  private record ForgeEntry(NpcSpec.DialogueTopic topic, String questId) {}

  // One entry per Godsforged item. Declarative GIVE_QUEST actions here are documentation/
  // compendium fallbacks only - javaBehavior() below always intercepts these same keywords first
  // (see ScriptedNpc.matches()).
  private static final List<ForgeEntry> FORGE_ENTRIES =
      List.of(
          new ForgeEntry(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grandmastertholvenn.0.0}"),
                  "${npc.topic.grandmastertholvenn.0}",
                  List.of(
                      new NpcSpec.Action(ActionType.GIVE_QUEST, List.of("forge_godsforged_warblade")))),
              "forge_godsforged_warblade"),
          new ForgeEntry(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grandmastertholvenn.1.0}"),
                  "${npc.topic.grandmastertholvenn.1}",
                  List.of(
                      new NpcSpec.Action(ActionType.GIVE_QUEST, List.of("forge_godsforged_stormbow")))),
              "forge_godsforged_stormbow"),
          new ForgeEntry(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grandmastertholvenn.2.0}"),
                  "${npc.topic.grandmastertholvenn.2}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.GIVE_QUEST, List.of("forge_godsforged_voidglass_rod")))),
              "forge_godsforged_voidglass_rod"),
          new ForgeEntry(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grandmastertholvenn.3.0}"),
                  "${npc.topic.grandmastertholvenn.3}",
                  List.of(
                      new NpcSpec.Action(
                          ActionType.GIVE_QUEST, List.of("forge_godsforged_zephyr_wand")))),
              "forge_godsforged_zephyr_wand"),
          new ForgeEntry(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.grandmastertholvenn.4.0}"),
                  "${npc.topic.grandmastertholvenn.4}",
                  List.of(new NpcSpec.Action(ActionType.GIVE_QUEST, List.of("forge_godsforged_torc")))),
              "forge_godsforged_torc"));

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupMageRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.grandmastertholvenn}",
          List.of(
              FORGE_ENTRIES.get(0).topic(),
              FORGE_ENTRIES.get(1).topic(),
              FORGE_ENTRIES.get(2).topic(),
              FORGE_ENTRIES.get(3).topic(),
              FORGE_ENTRIES.get(4).topic(),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.grandmastertholvenn.5.0}",
                      "${npc.topic_keyword.grandmastertholvenn.5.1}"),
                  "${npc.topic.grandmastertholvenn.5}",
                  List.of())),
          "GrandmasterTholvennNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public GrandmasterTholvenn(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        String questId = null;
        for (ForgeEntry entry : FORGE_ENTRIES) {
          if (ScriptedNpc.matches(entry.topic(), keyword)) {
            questId = entry.questId();
            break;
          }
        }
        if (questId == null) {
          return false;
        }
        Player player = context.player();
        QuestService quests = context.npc().questService();
        if (quests == null) {
          return false;
        }
        if (QuestService.statusFor(player, QuestRegistry.findById(questId))
            == QuestService.STATUS_COMPLETED) {
          context.say(quests.giveOrReport(questId, ID, player));
          return true;
        }
        boolean hasCore = InventoryService.count(player, CORE_KEY) >= 1;
        boolean hasSigil = InventoryService.count(player, SIGIL_KEY) >= 1;
        if (!hasCore && !hasSigil) {
          context.say(I18n.resolve("${npc.grandmastertholvenn.missing_both}"));
          return true;
        }
        if (!hasCore) {
          context.say(I18n.resolve("${npc.grandmastertholvenn.missing_core}"));
          return true;
        }
        if (!hasSigil) {
          context.say(I18n.resolve("${npc.grandmastertholvenn.missing_sigil}"));
          return true;
        }
        // Checked before the core is consumed below: completeCraftingQuest() itself refuses to
        // consume the sigil/mark the quest done if the reward item can't be granted, but it has
        // no way to know the core was already spent here - so that same check must happen first,
        // or a doomed forge would cost the player their core for nothing.
        String rewardItemKey = QuestRegistry.findById(questId).getRewardItemKey();
        if (!InventoryService.canAdd(player, rewardItemKey)) {
          context.say(
              I18n.message("message.quest_reward_item_blocked", itemDisplayName(rewardItemKey)));
          return true;
        }
        InventoryService.remove(player, -1, CORE_KEY);
        String response = quests.completeCraftingQuest(questId, ID, player);
        if (response != null && !response.isBlank()) {
          context.say(response);
        }
        return true;
      }
    };
  }

  /** Player-facing name for an item key, mirroring QuestService's own private helper of the same
   * shape - falls back to the raw key if the item isn't registered. */
  private static String itemDisplayName(String itemKey) {
    if (itemKey == null || itemKey.isBlank()) return itemKey;
    ItemDefinition item = ItemRegistry.findByKey(itemKey);
    return item == null ? itemKey : I18n.resolve(item.getName());
  }
}
