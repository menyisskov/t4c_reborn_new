package com.perso.T4C.npc.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.Uranos;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class NpcPresentationTest {
  @Test
  void authoredGreetingAndTrainingKeepTheirOrderWithoutGui() throws Exception {
    Uranos npc = new Uranos(new NpcContext(null));
    assertInstanceOf(LocalNpcPresentation.class, npc.getPresentation());
    Capture effects = new Capture();
    npc.setPresentation(effects);
    Player player = new Player();
    player.setName("Alyssa");

    npc.onInteractStart(player);
    npc.isInteracting = true;
    assertTrue(npc.talk("teach", player));

    assertEquals(List.of("dialogue", "training"), effects.events);
    assertEquals(I18n.resolve("${npc.welcome.uranos}"), effects.dialogueText);
    assertTrue(effects.keywords.contains(I18n.resolve("${npc.topic_keyword.uranos.0.0}")));
    assertEquals(2, effects.offers.size());
    assertTrue(effects.offers.stream().allMatch(LearnScreen.TrainingOffer::teaching));
    assertTrue(npc.dialogActive);
    assertEquals(0, npc.dialogPageIndex);
  }

  private static final class Capture implements NpcPresentation {
    private final List<String> events = new ArrayList<>();
    private String dialogueText;
    private List<String> keywords;
    private List<LearnScreen.TrainingOffer> offers;

    public void dialogue(String text, List<String> keywords) {
      events.add("dialogue");
      dialogueText = text;
      this.keywords = List.copyOf(keywords);
    }
    public void systemMessage(String text) { events.add("system"); }
    public void spellLearning(Player player, List<String> keys) { events.add("spell"); }
    public void skillLearning(Player player, List<String> keys) { events.add("skill"); }
    public void trainingOffers(Player player, List<LearnScreen.TrainingOffer> offers) {
      events.add("training");
      this.offers = List.copyOf(offers);
    }
    public void shop(Player player, List<String> keys) { events.add("shop"); }
    public void sellShop(Player player, List<String> keys) { events.add("sell"); }
    public void castOnPlayer(SpellData spell, Player player, Vector2 casterPosition) {
      events.add("target cast");
    }
    public void castOnSelf(SpellData spell, Vector2 casterPosition) { events.add("self cast"); }
    public void sound(String sound) { events.add("sound"); }
  }
}
