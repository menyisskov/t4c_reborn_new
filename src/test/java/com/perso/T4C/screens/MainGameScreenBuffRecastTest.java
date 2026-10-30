package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MainGameScreenBuffRecastTest {
  @Test
  void onlineDoubleClickDispatchesResolvedOwnBuffKey() {
    AtomicReference<String> castKey = new AtomicReference<>();

    assertTrue(
        MainGameScreen.recastRemoteBuff(
            "Protection", List.of("spell.protection"), castKey::set));

    assertEquals("spell.protection", castKey.get());
  }

  @Test
  void onlineRecastRejectsOtherPlayerAndUnknownBuffs() {
    AtomicReference<String> castKey = new AtomicReference<>();

    assertFalse(
        MainGameScreen.recastRemoteBuff(
            "Protection", List.of("spell.bless"), castKey::set));
    assertFalse(
        MainGameScreen.recastRemoteBuff(
            "Unmapped buff", List.of("spell.protection"), castKey::set));

    assertEquals(null, castKey.get());
  }
}
