package com.perso.T4C.npc.core;

public record NpcSoundProfile(String attack, String death, String hit) {
  public static NpcSoundProfile forClass(Class<?> type) {
    return new NpcSoundProfile(
        value(type, "SOUND_ATTACK"), value(type, "SOUND_DEATH"), value(type, "SOUND_HIT"));
  }

  private static String value(Class<?> type, String name) {
    try {
      Object value = type.getField(name).get(null);
      return value == null || value.toString().isBlank() ? null : value.toString();
    } catch (ReflectiveOperationException ignored) {
      return null;
    }
  }
}
