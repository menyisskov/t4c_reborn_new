package com.perso.T4C.npc.core;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.player.BodyPart;
import java.util.Map;

/**
 * An NPC that exists only as an online puppet: the server published its public appearance (a
 * sprite base or composite body parts) but no registered NPC class stands behind its id. It is a
 * plain {@link BaseNPC} in puppet mode, so the screen renders, hovers and names it like any other
 * NPC; it has no dialogue, shop or script.
 */
public final class PuppetNpc extends BaseNPC {
  public PuppetNpc(String id, String displayName, String spriteBase, Map<String, String> parts)
      throws GameException {
    super(id, spriteBase == null || spriteBase.isBlank() ? null : spriteBase, partArguments(parts));
    setDisplayName(com.perso.T4C.i18n.I18n.resolve(displayName));
    setPuppet(true);
  }

  private static Object[] partArguments(Map<String, String> parts) {
    var arguments = new java.util.ArrayList<>();
    var ordered = new java.util.TreeMap<>(parts);
    ordered.forEach(
        (part, base) -> {
          arguments.add(BodyPart.valueOf(part));
          arguments.add(base);
        });
    return arguments.toArray();
  }
}
