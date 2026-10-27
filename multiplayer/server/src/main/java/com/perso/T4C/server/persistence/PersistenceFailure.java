package com.perso.T4C.server.persistence;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

/** Safe operational categories, never exception messages, SQL values, URLs or identifiers. */
public final class PersistenceFailure {
  private PersistenceFailure() {}
  public static String describe(Throwable failure) {
    if(failure instanceof RejectedExecutionException) return "queue_rejected";
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
}
