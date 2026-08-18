package com.perso.T4C.npc.arakas;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LighthavenSamaritan extends BaseNPC {

  public static final String ID = "LighthavenSamaritan";

  public static final String DISPLAY_NAME = "${npc.lighthavensamaritan}";

  public static final String SPRITE_BASE = "PaysanModel1";

  private static final String RAT_QUEST_ID = "lighthaven_samaritan_rats";

  private static final String OFFENSIVE_TALK_FLAG = "__OFFENSIVE_TALK";

  private static final String KNOW_ABOUT_QUEST_FLAG = "__KNOW_ABOUT_QUEST";

  private static final List<DialogueTopic> TOPICS =
      List.of(
          topic("name", response(0), keyword(0, 0), keyword(0, 1)),
          topic("work", response(1), keyword(1, 0), keyword(1, 1), keyword(1, 2)),
          topic("annabelle", response(2), keyword(2, 0)),
          topic("balork", response(3), keyword(3, 0), keyword(3, 1), keyword(3, 2)),
          topic("keyword", response(4), keyword(4, 0)),
          topic("emphasis", response(5), keyword(5, 0)),
          topic("questions", response(6), keyword(6, 0), keyword(6, 1), keyword(6, 2)),
          topic("basic", response(7), keyword(7, 0)),
          topic("functions", response(8), keyword(8, 0)),
          topic("communication", response(9), keyword(9, 0)),
          topic("paging", response(10), keyword(10, 0), keyword(10, 1)),
          topic("shout", response(11), keyword(11, 0)),
          topic("advanced", response(12), keyword(12, 0)),
          topic("environment", response(13), keyword(13, 0)),
          topic("torch", response(14), keyword(14, 0)),
          topic("caves", response(15), keyword(15, 0), keyword(15, 1)),
          topic("world", response(16), keyword(16, 0)),
          topic("lighthaven", response(17), keyword(17, 0)),
          topic("windhowl", response(18), keyword(18, 0)),
          topic("samaritan", response(19), keyword(19, 0)),
          topic("cities", response(20), keyword(20, 0), keyword(20, 1)),
          topic("spells", response(21), keyword(21, 0)),
          topic("skills", response(22), keyword(22, 0)),
          topic("quests", response(23), keyword(23, 0)),
          topic("questKinds", response(24), keyword(24, 0), keyword(24, 1)),
          topic("mana", response(25), keyword(25, 0)),
          topic("training", response(26), keyword(26, 0), keyword(26, 1)),
          allWordsTopic("prerequisites", response(27), keyword(27, 0), keyword(27, 1)),
          topic("teachers", response(28), keyword(28, 0)),
          topic("stats", response(29), keyword(29, 0)),
          topic("level", response(30), keyword(30, 0)),
          topic("weapons", response(31), keyword(31, 0)),
          topic("armor", response(32), keyword(32, 0)),
          topic("combat", response(33), keyword(33, 0)),
          topic("hitPoints", response(34), keyword(34, 0)),
          topic("healing", response(35), keyword(35, 0)),
          topic("items", response(36), keyword(36, 0)),
          topic("errand", response(37), keyword(37, 0)),
          topic(
              "offensive",
              response(38),
              keyword(38, 0),
              keyword(38, 1),
              keyword(38, 2),
              keyword(38, 3)));

  private final QuestService questService;

  private boolean awaitingQuestionAnswer;

  public LighthavenSamaritan(NpcContext context) throws GameException {

    super(ID, SPRITE_BASE);

    setDisplayName(I18n.resolve(DISPLAY_NAME));

    this.questService = context == null ? null : context.questService();

    level = 100;

    maxHp = 1_000_000;

    currentHp = maxHp;

    strength = 20;

    dexterity = 24;

    endurance = 22;

    intelligence = 20;

    wisdom = 21;

    setSkillLevel("attack", 250);

    setSkillLevel("dodge", 65_535);
  }

  @Override
  public int getArmorClass() {

    return 1_000_000;
  }

  @Override
  public boolean isPassiveOnAttack() {

    return true;
  }

  protected void onConversationStart(Player player) {

    awaitingQuestionAnswer = false;

    int offensiveTalk = player == null ? 0 : player.getQuestFlag(OFFENSIVE_TALK_FLAG);

    if (offensiveTalk >= 10) {

      if (player != null) player.setQuestFlag(OFFENSIVE_TALK_FLAG, offensiveTalk - 1);

      closeWith("${npc.lighthavensamaritan.offensive_greeting}");

      return;
    }

    awaitingQuestionAnswer = true;

    showTranslated("${npc.welcome.lighthavensamaritan}");
  }

  @Override
  protected void onInteractStart(Player player) {

    awaitingQuestionAnswer = false;

    onConversationStart(player);
  }

  protected boolean handleConversationState(String text, Player player) {

    if (!awaitingQuestionAnswer) return false;

    String answer = normalize(text);

    if (answer.equals("yes") || answer.equals("oui")) {

      awaitingQuestionAnswer = false;

      showTranslated("${npc.lighthavensamaritan.answer_yes}");

    } else if (answer.equals("no") || answer.equals("non")) {

      awaitingQuestionAnswer = false;

      showTranslated("${npc.lighthavensamaritan.answer_no}");

    } else {

      showTranslated("${npc.lighthavensamaritan.answer_required}");
    }

    return true;
  }

  protected void onTopic(DialogueTopic topic, Player player) {

    switch (topic.id()) {
      case "errand" -> offerRatQuest(topic, player);

      case "quests" -> {
        if (player != null) player.setQuestFlag(KNOW_ABOUT_QUEST_FLAG, 1);

        showTranslated(topic.response());
      }

      case "questKinds" -> {
        if (player != null && player.getQuestFlag(KNOW_ABOUT_QUEST_FLAG) > 0) {

          showTranslated(topic.response());

        } else {

          showTranslated("${npc.lighthavensamaritan.quest_unknown}");
        }
      }

      case "offensive" -> {
        if (player != null) {

          player.setQuestFlag(OFFENSIVE_TALK_FLAG, player.getQuestFlag(OFFENSIVE_TALK_FLAG) + 1);
        }

        closeWith(topic.response());
      }

      default -> showTranslated(topic.response());
    }
  }

  protected void onFarewell(Player player) {

    closeWith(response(39));
  }

  protected void onUnknownTopic(Player player) {

    showTranslated("${npc.lighthavensamaritan.unknown}");
  }

  private void offerRatQuest(DialogueTopic fallback, Player player) {

    if (questService == null) {

      showTranslated(fallback.response());

      return;
    }

    String response = questService.giveOrReport(RAT_QUEST_ID, ID, player);

    if (response != null && !response.isBlank()) showDialog(response, 0L);
  }

  private static String response(int topic) {

    return "${npc.topic.lighthavensamaritan." + topic + "}";
  }

  private static String keyword(int topic, int index) {

    return "${npc.topic_keyword.lighthavensamaritan." + topic + "." + index + "}";
  }

  @Override
  public boolean talk(String text, Player player) {

    if (!isInteracting || text == null) return false;

    String normalized = normalize(text);

    if (normalized.isEmpty()) return true;

    if (handleConversationState(text, player)) return true;

    if (isFarewell(normalized)) {

      onFarewell(player);

      return true;
    }

    for (DialogueTopic topic : TOPICS)
      if (topic.matches(normalized)) {

        onTopic(topic, player);

        return true;
      }

    onUnknownTopic(player);

    return true;
  }

  private void showTranslated(String value) {

    showDialog(I18n.resolve(value), 0L);
  }

  private boolean closeAfterDialog;

  private void closeWith(String value) {

    closeAfterDialog = true;

    showTranslated(value);
  }

  @Override
  public boolean advanceDialog() {

    boolean r = super.advanceDialog();

    if (closeAfterDialog && !dialogActive) {

      closeAfterDialog = false;

      endInteraction();
    }

    return r;
  }

  @Override
  protected List<String> getDialogKeywords() {

    List<String> r = new ArrayList<>();

    for (DialogueTopic t : TOPICS)
      for (String k : t.keywords()) {

        String v = I18n.resolve(k);

        if (v != null && !v.isBlank() && !r.contains(v)) r.add(v);
      }

    r.add(FAREWELL_DIALOG_LINK);

    return r;
  }

  public List<DialogueTopic> getTopics() {

    return TOPICS;
  }

  private static String normalize(String value) {

    return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "")
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^\\p{L}\\p{N}]+", " ")
        .trim()
        .replaceAll("\\s+", " ");
  }

  private static boolean isFarewell(String text) {

    return (" " + text + " ").matches(".* (bye|leave|exit|farewell|quit|adieu|au revoir) .*");
  }

  private static boolean contains(String text, String phrase) {

    return (" " + text + " ").contains(" " + normalize(phrase) + " ");
  }

  public record DialogueTopic(
      String id, String response, boolean requireAll, List<String> keywords) {

    public DialogueTopic {

      keywords = List.copyOf(keywords);
    }

    boolean matches(String text) {

      boolean found = false;

      for (String k : keywords) {

        boolean m = contains(text, I18n.resolve(k));

        if (requireAll && !m) return false;

        found |= m;
      }

      return found;
    }
  }

  private static DialogueTopic topic(String id, String response, String... keywords) {

    return new DialogueTopic(id, response, false, List.of(keywords));
  }

  private static DialogueTopic allWordsTopic(String id, String response, String... keywords) {

    return new DialogueTopic(id, response, true, List.of(keywords));
  }
}
