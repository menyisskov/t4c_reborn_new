package com.perso.T4C.server.persistence;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Static, allowlisted labels for the persistence operation that failed. */
public final class PersistenceFailure {
  private PersistenceFailure() {}

  public enum Operation {
    CHECKPOINT("checkpoint"),
    LOOT_CLAIM("loot_claim"),
    SHOP("shop"),
    CORPSE("corpse"),
    TRANSFER("transfer"),
    MONSTER_ATTACK("monster_attack"),
    SPELL_CREATURE_STRIKE("spell_creature_strike"),
    WORLD_REWARD("world_reward"),
    GENERIC_EVENT("generic_event");

    private final String label;

    Operation(String label) { this.label=label; }

    public String label() { return label; }
  }

  /** Bounded exception-type diagnostics retained for existing non-session call sites. */
  public static String describe(Throwable failure) {
    if(failure instanceof java.util.concurrent.RejectedExecutionException) return "queue_rejected";
    var classes=new ArrayList<String>();
    var visited=Collections.newSetFromMap(new IdentityHashMap<Throwable,Boolean>());
    String state="unavailable";
    for(var current=failure;current!=null && classes.size()<4 && visited.add(current);current=current.getCause()) {
      String type=current.getClass().getSimpleName();
      classes.add(type.matches("[A-Za-z0-9_$]{1,80}")?type:"Exception");
      if(state.equals("unavailable") && current instanceof SQLException sql
          && sql.getSQLState()!=null && sql.getSQLState().matches("[A-Z0-9]{5}")) state=sql.getSQLState();
    }
    return "classes="+(classes.isEmpty()?"unknown":String.join("/",classes))+", sqlstate="+state;
  }

  /** Combines a fixed operation label with bounded diagnostics that omit exception messages. */
  public static String format(Operation operation, Throwable failure) {
    return "operation="+operation.label()+", "+describe(failure);
  }
}
