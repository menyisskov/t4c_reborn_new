package com.perso.T4C.ui;

import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only HUD projection of committed online buffs and spell cooldowns. */
public record RemoteHudStatus(List<Buff> buffs, Map<String, Long> cooldownUntil) {
  public record Buff(String spellKey, String name, String description, String iconId,
      long expiresAt, long appliedAt) {}

  public RemoteHudStatus {
    buffs = List.copyOf(buffs);
    cooldownUntil = Map.copyOf(cooldownUntil);
  }

  public static RemoteHudStatus from(OnlineConnection.View view, long nowMillis) {
    if (view == null || view.character() == null || view.character().progress() == null) {
      return new RemoteHudStatus(List.of(), Map.of());
    }
    CharacterProgress progress = view.character().progress();
    List<Buff> buffs = new ArrayList<>();
    for (CharacterProgress.Buff buff : progress.buffs()) {
      if (buff == null || (buff.expiresAt() != Long.MAX_VALUE && buff.expiresAt() <= nowMillis)) continue;
      SpellData spell = SpellRegistry.findByName(buff.spell());
      if (spell == null) continue;
      buffs.add(new Buff(spell.getKey(), spell.getName(), spell.getDescription(), spell.getIconId(),
          buff.expiresAt(), buff.appliedAt()));
    }
    Map<String, Long> cooldowns = new LinkedHashMap<>();
    progress.cooldowns().forEach((key, until) -> {
      SpellData spell = SpellRegistry.findByName(key);
      if (spell != null && until != null && until > nowMillis) {
        cooldowns.merge(spell.getKey(), until, Math::max);
      }
    });
    return new RemoteHudStatus(buffs, cooldowns);
  }

  public float remainingSeconds(String spellKey, long nowMillis) {
    Long until = cooldownUntil.get(spellKey);
    return until == null ? 0f : Math.max(0L, until - nowMillis) / 1000f;
  }
}
