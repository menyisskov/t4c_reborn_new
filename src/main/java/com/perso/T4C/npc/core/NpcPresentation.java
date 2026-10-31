package com.perso.T4C.npc.core;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import java.util.List;

/** Final presentation effects of an authored NPC interaction. Gameplay remains on the NPC. */
public interface NpcPresentation {
  void dialogue(String text, List<String> keywords);

  void systemMessage(String text);

  void spellLearning(Player player, List<String> spellKeys);

  void skillLearning(Player player, List<String> skillKeys);

  void trainingOffers(Player player, List<LearnScreen.TrainingOffer> offers);

  void shop(Player player, List<String> itemKeys);

  void sellShop(Player player, List<String> itemKeys);

  void castOnPlayer(SpellData spell, Player player, Vector2 casterPosition);

  void castOnSelf(SpellData spell, Vector2 casterPosition);

  void sound(String sound);
}
