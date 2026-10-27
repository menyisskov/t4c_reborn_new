package com.perso.T4C.online.model;

/** Owner-only dialogue acknowledgement. No-op greetings may retain the request revision. */
public record NpcInteractionReply(long revision, String messageKey, boolean changed) {
  public NpcInteractionReply {
    if(revision<0 || messageKey==null || messageKey.length()>256
        || !(messageKey.startsWith("npc.") || messageKey.startsWith("quest.") || messageKey.startsWith("message.")))
      throw new IllegalArgumentException("Invalid NPC interaction acknowledgement");
  }
}
