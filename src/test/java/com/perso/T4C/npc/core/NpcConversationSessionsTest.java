package com.perso.T4C.npc.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.ElderOphira;
import com.perso.T4C.npc.LighthavenSamaritan;
import com.perso.T4C.npc.MoonwakeWitnessIlyra;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.AvalonWildsVigil;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import com.perso.T4C.spell.SpellData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class NpcConversationSessionsTest {
  @Test
  void authoredSamaritanAnswersAndPhysicalPatrolStayIndependent() throws Exception {
    LighthavenSamaritan npc = new LighthavenSamaritan(new NpcContext(null));
    npc.setSpawnPosition(0f, 0f);
    npc.patrolTarget = new Vector2(12f, 14f);
    NpcConversationSessions sessions = new NpcConversationSessions(npc);
    Player a = new Player(), b = new Player();
    Capture aOut = new Capture(), bOut = new Capture();

    assertTrue(sessions.click("a", a, aOut));
    assertTrue(sessions.click("b", b, bOut));
    assertEquals(2, sessions.activeCount());
    assertEquals(null, npc.patrolTarget);
    assertTrue(sessions.talk("a", a, "yes"));
    assertTrue(sessions.talk("b", b, "basic"));
    assertEquals(I18n.resolve("${npc.lighthavensamaritan.answer_required}"), bOut.last());
    assertTrue(sessions.talk("a", a, "basic"));
    assertFalse(aOut.last().equals(bOut.last()));

    sessions.endInteraction("a", a);
    assertEquals(1, sessions.activeCount());
    assertEquals(null, npc.patrolTarget);
    assertTrue(sessions.isInteracting("b"));
    sessions.endInteraction("b", b);
    assertEquals(new Vector2(12f, 14f), npc.patrolTarget);
    assertFalse(npc.isInteracting());
  }

  @Test
  void ophiraOfferAndWitnessTestimonyRemainWithTheirOriginalPlayers() throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    ElderOphira ophira = new ElderOphira(new NpcContext(quests));
    NpcConversationSessions offers = new NpcConversationSessions(ophira);
    Player a = new Player(), b = new Player();
    Capture aOut = new Capture(), bOut = new Capture();
    assertTrue(offers.click("a", a, aOut));
    assertTrue(offers.click("b", b, bOut));
    assertTrue(offers.talk("a", a, "wilds"));
    assertTrue(offers.talk("b", b, "accept"));
    assertEquals(QuestService.STATUS_NOT_STARTED,
        QuestService.statusFor(b, AvalonWildsVigil.definition()));
    assertTrue(offers.talk("a", a, "accept"));
    assertEquals(QuestService.STATUS_ACTIVE,
        QuestService.statusFor(a, AvalonWildsVigil.definition()));

    MoonwakeWitnessIlyra witness = new MoonwakeWitnessIlyra(new NpcContext(null));
    NpcConversationSessions testimonies = new NpcConversationSessions(witness);
    var first = HollowDawnCampaign.avalon().get(0);
    a.setQuestFlag(QuestService.statusFlag(first), QuestService.STATUS_COMPLETED);
    b.setQuestFlag(QuestService.statusFlag(first), QuestService.STATUS_COMPLETED);
    assertTrue(testimonies.click("a", a, aOut));
    assertTrue(testimonies.click("b", b, bOut));
    assertTrue(testimonies.talk("a", a, "testimony"));
    assertTrue(testimonies.talk("b", b, "clue"));
    assertEquals(0, b.getQuestFlag(HollowDawnCampaign.MOONWAKE_CLUE));
    assertTrue(testimonies.talk("a", a, "clue"));
    assertEquals(1, a.getQuestFlag(HollowDawnCampaign.MOONWAKE_CLUE));
  }

  @Test
  void oneParticipantUsesOriginalGreetingAndRejectsChangedPlayerIdentity() throws Exception {
    LighthavenSamaritan direct = new LighthavenSamaritan(new NpcContext(null));
    Capture original = new Capture();
    direct.setPresentation(original);
    LighthavenSamaritan npc = new LighthavenSamaritan(new NpcContext(null));
    NpcConversationSessions sessions = new NpcConversationSessions(npc);
    Player player = new Player();
    Capture out = new Capture();
    assertTrue(direct.onClick(player));
    assertTrue(sessions.click("one", player, out));
    assertEquals(I18n.resolve("${npc.welcome.lighthavensamaritan}"), out.last());
    org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
        () -> sessions.talk("one", new Player(), "yes"));
    assertTrue(sessions.talk("one", player, "yes"));
    assertTrue(direct.talk("yes", player));
    assertEquals(original.dialogue, out.dialogue);
  }

  @Test
  void throwingAuthoredGreetingRestoresAmbientPresentationAndAllowsNextParticipant()
      throws Exception {
    ThrowingNpc npc = new ThrowingNpc();
    NpcPresentation ambient = npc.getPresentation();
    NpcConversationSessions sessions = new NpcConversationSessions(npc);
    Player a = new Player(), b = new Player();
    Capture aOut = new Capture(), bOut = new Capture();

    assertThrows(IllegalStateException.class, () -> sessions.click("a", a, aOut));
    assertSame(ambient, npc.getPresentation());
    assertEquals(1, sessions.activeCount());
    assertTrue(sessions.click("b", b, bOut));
    assertEquals(List.of("hello"), bOut.dialogue);
    assertSame(ambient, npc.getPresentation());
    assertTrue(sessions.isInteracting("a"));
    assertTrue(sessions.isInteracting("b"));
  }

  private static final class ThrowingNpc extends BaseNPC {
    private boolean throwOnce = true;

    ThrowingNpc() throws GameException { super("ThrowingNpc"); }

    @Override protected void onInteractStart(Player player) {
      if (throwOnce) {
        throwOnce = false;
        throw new IllegalStateException("authored callback failed");
      }
      showDialog("hello", 0L);
    }
  }

  private static final class Capture implements NpcPresentation {
    final List<String> dialogue = new ArrayList<>();
    String last() { return dialogue.get(dialogue.size() - 1); }
    public void dialogue(String text, List<String> keywords) { dialogue.add(text); }
    public void systemMessage(String text) {}
    public void spellLearning(Player player, List<String> keys) {}
    public void skillLearning(Player player, List<String> keys) {}
    public void trainingOffers(Player player, List<LearnScreen.TrainingOffer> offers) {}
    public void shop(Player player, List<String> keys) {}
    public void sellShop(Player player, List<String> keys) {}
    public void castOnPlayer(SpellData spell, Player player, Vector2 position) {}
    public void castOnSelf(SpellData spell, Vector2 position) {}
    public void sound(String sound) {}
  }
}
