package com.perso.T4C.npc.core;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.gui.screen.ShopScreen;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.NpcCastVfxHook;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.ui.SystemMessage;
import java.util.List;

/** Original local client sinks, kept together so headless NPCs can use another presentation. */
public final class LocalNpcPresentation implements NpcPresentation {
  @Override public void dialogue(String text, List<String> keywords) {
    SystemMessage.showSharedNpcDialogue(text, keywords);
  }

  @Override public void systemMessage(String text) { SystemMessage.showShared(text); }

  @Override public void spellLearning(Player player, List<String> spellKeys) {
    GuiManager.open(new LearnScreen(player, spellKeys));
  }

  @Override public void skillLearning(Player player, List<String> skillKeys) {
    GuiManager.open(LearnScreen.forTraining(player, skillKeys));
  }

  @Override public void trainingOffers(Player player, List<LearnScreen.TrainingOffer> offers) {
    GuiManager.open(LearnScreen.forTrainingOffers(player, offers));
  }

  @Override public void shop(Player player, List<String> itemKeys) {
    GuiManager.open(new ShopScreen(player, itemKeys));
  }

  @Override public void sellShop(Player player, List<String> itemKeys) {
    GuiManager.open(ShopScreen.forSelling(player, itemKeys));
  }

  @Override public void castOnPlayer(SpellData spell, Player player, Vector2 casterPosition) {
    NpcCastVfxHook.playOnPlayer(spell, player, casterPosition);
  }

  @Override public void castOnSelf(SpellData spell, Vector2 casterPosition) {
    NpcCastVfxHook.playOnSelf(spell, casterPosition);
  }

  @Override public void sound(String sound) { SoundManager.animateSound(sound); }
}
