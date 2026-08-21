package com.perso.T4C.npc.behavior;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;

public final class PortalBehavior implements NpcBehavior {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private final int tileX;

  private final int tileY;

  private final int world;

  private final Integer requiredItemId;

  private final String requiredItemKey;

  private final String deniedMessage;

  private final String castSpellKey;

  public PortalBehavior(
      int tileX, int tileY, int world, Integer requiredItemId, String deniedMessage) {

    this.tileX = tileX;

    this.tileY = tileY;

    this.world = world;

    this.requiredItemId = requiredItemId;

    this.requiredItemKey = null;

    this.deniedMessage = deniedMessage;

    this.castSpellKey = null;
  }

  public PortalBehavior(
      int tileX,
      int tileY,
      int world,
      String requiredItemKey,
      String deniedMessage,
      boolean itemKey) {

    this.tileX = tileX;

    this.tileY = tileY;

    this.world = world;

    this.requiredItemId = null;

    this.requiredItemKey = requiredItemKey;

    this.deniedMessage = deniedMessage;

    this.castSpellKey = null;
  }

  public PortalBehavior(
      int tileX,
      int tileY,
      int world,
      String castSpellKey,
      String deniedMessage,
      boolean spellKey,
      boolean ignored) {

    this.tileX = tileX;

    this.tileY = tileY;

    this.world = world;

    this.requiredItemId = null;

    this.requiredItemKey = null;

    this.deniedMessage = deniedMessage;

    this.castSpellKey = castSpellKey;
  }

  @Override
  public void onInitialise(NpcBehaviorContext context) {

    context.npc().setStationary(true);
  }

  @Override
  public void onAttacked(NpcBehaviorContext context) {

    if (context.npcCurrentHp() < 10_000) {

      context.castSelfSpell("spell.npc_cantrip_flak");

      context.selfDestructNpc();
    }
  }

  @Override
  public void onConversationStart(NpcBehaviorContext context) {

    if (!context.isInRange(4)) {

      context.sayKey("npc.portal.too_far");

      context.endConversation();

      return;
    }

    if (requiredItemId != null || requiredItemKey != null) {

      ItemDefinition item =
          requiredItemId == null ? null : ItemRegistry.findByNumId(requiredItemId);

      boolean has =
          requiredItemKey != null
              ? context.hasItem(requiredItemKey)
              : item != null && context.hasItem(item.getKey());

      if (!has) {

        context.systemMessage(deniedMessage);

        context.endConversation();

        return;
      }
    }

    if (castSpellKey != null) context.castTargetSpell(castSpellKey);

    context.teleport(tileX, tileY, world);

    context.endConversation();
  }
}
