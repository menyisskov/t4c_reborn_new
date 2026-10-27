package com.perso.T4C.npc.core;

import java.util.List;
import java.util.Locale;

/** NPCManager's lookup for authored monster-kind spawns; MonsterManager keeps definition precedence. */
public final class NpcSpawnOwnership {
  private NpcSpawnOwnership() {}

  public static boolean owns(String type) {
    return type != null && ("SUNDIAL".equalsIgnoreCase(type)
        || NpcFactoryRegistry.find(type) != null);
  }

  /** Export-only resolution: fail if classpath alias ordering could change the NPC identity. */
  public static NpcFactoryRegistry.Registration resolveStrict(String type) {
    return resolveStrict(type, NpcFactoryRegistry.registrations());
  }

  static NpcFactoryRegistry.Registration resolveStrict(
      String type, List<NpcFactoryRegistry.Registration> registrations) {
    if (type == null || type.isBlank()) return null;
    String key=normalize(type);
    NpcFactoryRegistry.Registration found=null;
    for (var registration:registrations) {
      boolean matches=normalize(registration.id()).equals(key)
          || registration.identities().stream().anyMatch(identity -> identity!=null && normalize(identity).equals(key));
      if (!matches) continue;
      if (found!=null && !normalize(found.id()).equals(normalize(registration.id())))
        throw new IllegalStateException("Ambiguous NPC spawn alias: "+type);
      found=registration;
    }
    if (found==null && "sundial".equals(key))
      throw new IllegalStateException("Sundial NPC registration missing");
    return found;
  }

  private static String normalize(String value) { return value.trim().toLowerCase(Locale.ROOT); }
}
