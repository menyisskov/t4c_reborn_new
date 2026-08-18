package com.perso.T4C.skill;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class MeditateSkillTest {
  private static Player meditator() {
    Player player = new Player();
    player.setSkillLevel("meditate", 20);
    return player;
  }

  @Test
  void usingTheSkillStartsMeditating() {
    Player player = meditator();
    assertTrue(SkillService.use(player, "meditate", 0L).success());
    assertTrue(player.isMeditating());
  }

  @Test
  void usingTheSkillAgainStopsMeditating() {
    Player player = meditator();
    SkillService.use(player, "meditate", 0L);
    SkillService.use(player, "meditate", 0L);
    assertFalse(player.isMeditating());
  }

  @Test
  void anUnlearnedSkillCannotBeUsed() {
    Player player = new Player();
    assertFalse(SkillService.use(player, "meditate", 0L).success());
    assertFalse(player.isMeditating());
  }

  @Test
  void takingDamageBreaksConcentration() {
    Player player = meditator();
    player.setMaxHp(100);
    player.setCurrentHp(100);
    SkillService.use(player, "meditate", 0L);
    player.takeDamage(5);
    assertFalse(player.isMeditating());
  }

  @Test
  void onlyActivelyUsedSkillsAreQuickSlotCandidates() {
    assertTrue(SkillService.isActivelyUsed("meditate"));
    assertFalse(SkillService.isActivelyUsed("attack"));
  }
}
