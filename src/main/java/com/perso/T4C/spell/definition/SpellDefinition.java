package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public interface SpellDefinition {
  String getName(); String getDescription(); String getManaCost(); int getRadius(); int getMinInt(); int getMinWis(); int getMinLevel(); boolean isAttack(); boolean isLineOfSight(); String getIconId(); String getProjectileSpell(); String getImpactSpell(); int getMinDamage(); int getMaxDamage(); String getSound(); String getSoundImpact(); int getCooldownSeconds(); String getDuration(); String getFrequency(); int getPrice(); SpellData.SpellBuff getBuff(); int getSpellId(); int getElement(); int getTargetType(); int getAttackType(); String getSuccessRate(); String getMentalExhaustion(); String getPhysicalExhaustion(); String getAttackExhaustion(); int getVisualEffect(); int getVisualEffectTarget(); boolean isPvp(); List<SpellData.T4cEffect> getT4cEffects();
}
