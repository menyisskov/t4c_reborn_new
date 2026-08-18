package com.perso.T4C.npc.behavior;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.NpcCastVfxHook;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class NpcBehaviorContext {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private final ScriptedNpc npc;

  private final Player player;

  public NpcBehaviorContext(ScriptedNpc npc, Player player) {

    this.npc = Objects.requireNonNull(npc, "npc");

    this.player = Objects.requireNonNull(player, "player");
  }

  public ScriptedNpc npc() {

    return npc;
  }

  public Player player() {

    return player;
  }

  public int flag(String name) {

    return player.getQuestFlag(name);
  }

  public int karma() {

    return player.getKarma();
  }

  public void karma(int value) {

    player.setKarma(value);
  }

  public int globalFlag(String name) {

    return NpcScriptEngine.globalFlagValue(name);
  }

  public int globalFlag(int id) {

    return NpcScriptEngine.globalFlagValue(id);
  }

  public void globalFlag(String name, int value) {

    NpcScriptEngine.setGlobalFlag(name, value);
  }

  public void flag(String name, int value) {

    player.setQuestFlag(name, value);
  }

  public boolean hasFlag(String name, int value) {

    return flag(name) == value;
  }

  public boolean hasFlagAtLeast(String name, int value) {

    return flag(name) >= value;
  }

  public void viewFlag(String name, int value) {

    flag("__VIEW_" + name, value);
  }

  public int viewFlag(String name) {

    return flag("__VIEW_" + name);
  }

  public void say(String text) {

    if (text != null && !text.isBlank()) npc.say(text, player);
  }

  public void sayKey(String key) {

    say(I18n.resolve(key));
  }

  public void sayKey(String key, Object... args) {

    say(String.format(java.util.Locale.ROOT, I18n.resolve(key), args));
  }

  public void shout(String text) {

    if (text != null && !text.isBlank())
      npc.shout(ScriptedNpc.resolvePlayerName(text, player), 3000L);
  }

  public void shoutKey(String key) {

    shout(I18n.resolve(key));
  }

  public void systemMessage(String text) {

    npc.systemMessage(text);
  }

  public void systemMessageKey(String key) {

    systemMessage(I18n.resolve(key));
  }

  public boolean isWounded() {

    return player.getCurrentHp() < player.getMaxHp() / 2;
  }

  public void healToHalf() {

    player.setCurrentHp(Math.max(player.getCurrentHp(), player.getMaxHp() / 2));
  }

  public boolean hasItem(String itemKey) {

    return InventoryService.count(player, itemKey) > 0;
  }

  public int itemCount(String itemKey) {

    return InventoryService.count(player, itemKey);
  }

  public void giveItem(String itemKey) {

    InventoryService.add(player, itemKey);
  }

  public void giveGold(int amount) {

    player.addGold(amount);
  }

  public void giveXp(int amount) {

    player.addXpExact(amount, XpCurve.loadDefault());
  }

  public void takeItem(String itemKey) {

    InventoryService.destroyOne(player, itemKey);
  }

  public void teleport(int tileX, int tileY, int world) {

    player.setWorldPosition(tileX * GRID_W, tileY * GRID_H, world);
  }

  public void setRespawnPoint(int tileX, int tileY, int world) {

    player.setRespawnPoint(tileX * GRID_W, tileY * GRID_H, world);
  }

  public void askYesNo(String state) {

    npc.askYesNo(state);
  }

  public boolean summon(String monster, int tileX, int tileY, int world) {

    return NpcSummonBridge.summon(monster, tileX * GRID_W, tileY * GRID_H, world);
  }

  public int npcTileX() {

    return npc.getTileX();
  }

  public int npcTileY() {

    return npc.getTileY();
  }

  public boolean isInRange(int radiusTiles) {

    int px = (int) (player.getPositionVector().x / GRID_W);

    int py = (int) (player.getPositionVector().y / GRID_H);

    return Math.max(Math.abs(px - npc.getTileX()), Math.abs(py - npc.getTileY())) <= radiusTiles;
  }

  public int npcCurrentHp() {

    return npc.getCurrentHp();
  }

  public int npcMaxHp() {

    return npc.getMaxHp();
  }

  public int npcFlag(String name) {

    return npc.scriptFlag(name);
  }

  public void npcFlag(String name, int value) {

    npc.scriptFlag(name, value);
  }

  public void damageNpc(int amount) {

    npc.applyNativeDamage(amount);
  }

  public void fleeFromPlayer() {

    npc.fleeFrom(player);
  }

  public void selfDestructNpc() {

    npc.requestNativeSelfDestruct();
  }

  public boolean castSelfSpell(String spellId) {

    SpellData spell = SpellRegistry.findByName(spellId);

    if (spell == null) {

      log.warn("NPC {} requested missing self spell {}", npc.getTypeId(), spellId);

      return false;
    }

    NpcCastVfxHook.playOnSelf(spell, new Vector2(npc.getPosition().x, npc.getPosition().y));

    return true;
  }

  public boolean castSelfSpell(int spellId) {

    SpellData spell = SpellRegistry.findById(spellId);

    if (spell == null) {

      log.warn("NPC {} requested missing self spell id {}", npc.getTypeId(), spellId);

      return false;
    }

    NpcCastVfxHook.playOnSelf(spell, new Vector2(npc.getPosition().x, npc.getPosition().y));

    int min = Math.max(0, spell.getMinDamage());

    int max = Math.max(min, spell.getMaxDamage());

    if (max > 0) damageNpc(min + (max == min ? 0 : (int) (Math.random() * (max - min + 1))));

    return true;
  }

  public boolean castTargetSpell(String spellId) {

    SpellData spell = SpellRegistry.findByName(spellId);

    if (spell == null) {

      log.warn("NPC {} requested missing target spell {}", npc.getTypeId(), spellId);

      return false;
    }

    NpcCastVfxHook.playOnPlayer(
        spell, player, new Vector2(npc.getPosition().x, npc.getPosition().y));

    int min = Math.max(0, spell.getMinDamage());

    int max = Math.max(min, spell.getMaxDamage());

    if (max > 0)
      player.takeDamage(min + (max == min ? 0 : (int) (Math.random() * (max - min + 1))));

    return true;
  }

  public boolean castTargetSpell(int spellId) {

    SpellData spell = SpellRegistry.findById(spellId);

    if (spell == null) {

      log.warn("NPC {} requested missing target spell id {}", npc.getTypeId(), spellId);

      return false;
    }

    NpcCastVfxHook.playOnPlayer(
        spell, player, new Vector2(npc.getPosition().x, npc.getPosition().y));

    return true;
  }

  public void openShop(List<String> itemKeys) {

    npc.openShop(player, itemKeys);
  }

  public void openSellShop() {

    npc.openSellShop(player);
  }

  public void openSpellLearning(List<String> spellKeys) {

    npc.openSpellLearning(player, spellKeys);
  }

  public void openSkillLearning(List<LearnScreen.TrainingOffer> offers) {

    npc.openSkillLearning(player, offers);
  }

  public String pendingYesNo() {

    return npc.pendingYesNoState();
  }

  public void endConversation() {

    npc.endInteraction();
  }
}
