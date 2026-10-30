package com.perso.T4C.ui;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.OnlineCharacterView;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RemoteHudStatusTest {
  private static OnlineConnection.View view(List<CharacterProgress.Buff> buffs, Map<String, Long> cooldowns) {
    CharacterProgress progress = new CharacterProgress(
        1, "warrior", "male", new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(20, 20, 10, 10), new CharacterProgress.Advancement(1, 0, 0, 0, 0),
        List.of(), Map.of(), Map.of(), Set.of(), Map.of(), List.of(), 0,
        new CharacterProgress.Respawn(0, 1, 1), buffs, cooldowns, 0, null);
    OnlineCharacterView character = new OnlineCharacterView(UUID.randomUUID(), "Hero", 0, 0, 1, progress);
    return new OnlineConnection.View(1, null, null, character, List.of());
  }

  @Test
  void projectsOnlyLiveResolvableBuffsAndSpellCooldowns() {
    long now = 1_800_000_000_000L;
    RemoteHudStatus status = RemoteHudStatus.from(view(List.of(
        new CharacterProgress.Buff("spell.sentinel", now + 30_000, 0, 0, Map.of(), now - 10_000),
        new CharacterProgress.Buff("spell.light", Long.MAX_VALUE, 0, 0),
        new CharacterProgress.Buff("spell.light", now, 0, 0),
        new CharacterProgress.Buff("spell.missing", now + 20_000, 0, 0)),
        Map.of("spell.sentinel", now + 5_000, "attack", now + 50_000,
            "spell.light", now, "spell.missing", now + 10_000)), now);

    assertEquals(2, status.buffs().size());
    assertEquals("spell.leyward_bastion", status.buffs().get(0).spellKey());
    assertEquals(now - 10_000, status.buffs().get(0).appliedAt());
    assertEquals(Long.MAX_VALUE, status.buffs().get(1).expiresAt());
    assertEquals(Map.of("spell.leyward_bastion", now + 5_000), status.cooldownUntil());
    assertEquals(5f, status.remainingSeconds("spell.leyward_bastion", now));
    assertEquals(0f, status.remainingSeconds("spell.leyward_bastion", now + 5_000));
  }

  @Test
  void acceptsLegacyBuffWithoutAppliedAtWithoutInventingElapsedTime() {
    long now = 1_800_000_000_000L;
    RemoteHudStatus status = RemoteHudStatus.from(view(
        List.of(new CharacterProgress.Buff("spell.light", now + 60_000, 0, 0)), Map.of()), now);

    assertEquals(1, status.buffs().size());
    assertEquals(0, status.buffs().get(0).appliedAt());
  }
}
