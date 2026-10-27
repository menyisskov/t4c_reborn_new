package com.perso.T4C.online.model;

import java.util.List;

/**
 * Owner-only dialogue acknowledgement. No-op greetings may retain the request revision. Args are
 * authored numbers for the message (for example quest progress as kills, required, remaining).
 */
public record NpcInteractionReply(long revision, String messageKey, boolean changed, List<Integer> args) {
  public NpcInteractionReply {
    if(revision<0 || messageKey==null || messageKey.length()>256
        || !(messageKey.startsWith("npc.") || messageKey.startsWith("quest.") || messageKey.startsWith("message.")))
      throw new IllegalArgumentException("Invalid NPC interaction acknowledgement");
    args=args==null ? List.of() : List.copyOf(args);
  }
  public NpcInteractionReply(long revision,String messageKey,boolean changed) {
    this(revision,messageKey,changed,List.of());
  }
}
