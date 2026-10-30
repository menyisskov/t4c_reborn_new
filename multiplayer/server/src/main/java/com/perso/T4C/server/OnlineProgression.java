package com.perso.T4C.server;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.player.PlayerProgression;
import com.perso.T4C.player.ProgressionState;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.random.RandomGenerator;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/** Builds an immutable reward draft. Persist it once; never reroll it while retrying a receipt. */
final class OnlineProgression {
  private static final XpCurve CURVE=XpCurve.loadDefault();
  private static final Set<String> STAT_IDS = Set.of("strength", "endurance", "dexterity", "wisdom", "intelligence");
  record Result(long experience, CharacterProgress progress) {}
  record Allocation(int pointsSpent, CharacterProgress progress) {}

  static Allocation allocateStats(OnlineStore.CharacterState character, Map<String, Integer> allocation) {
    CharacterProgress progress = character.progress();
    if (progress == null) throw new PreviewWorld.Rejected(409, "uninitialized_character");
    if (allocation == null || allocation.isEmpty() || allocation.size() > STAT_IDS.size())
      throw new PreviewWorld.Rejected(409, "invalid_allocation");
    long spent = 0;
    for (var entry : allocation.entrySet()) {
      if (entry.getKey() == null || !STAT_IDS.contains(entry.getKey())
          || entry.getValue() == null || entry.getValue() <= 0)
        throw new PreviewWorld.Rejected(409, "invalid_allocation");
      spent += entry.getValue();
      if (spent > Integer.MAX_VALUE) throw new PreviewWorld.Rejected(409, "invalid_allocation");
    }
    int available = progress.advancement().statPoints();
    if (spent > available) throw new PreviewWorld.Rejected(409, "insufficient_stat_points");
    var old = progress.attributes();
    int strength = allocated(old.strength(), allocation.get("strength"));
    int endurance = allocated(old.endurance(), allocation.get("endurance"));
    int dexterity = allocated(old.dexterity(), allocation.get("dexterity"));
    int wisdom = allocated(old.wisdom(), allocation.get("wisdom"));
    int intelligence = allocated(old.intelligence(), allocation.get("intelligence"));
    CharacterProgress.Attributes attributes;
    try {
      attributes = new CharacterProgress.Attributes(strength, endurance, dexterity, wisdom, intelligence);
    } catch (ArithmeticException overflow) {
      throw new PreviewWorld.Rejected(409, "invalid_allocation");
    }
    var advancement = progress.advancement();
    var updatedAdvancement = new CharacterProgress.Advancement(advancement.level(),
        available - (int) spent, advancement.skillPoints(), advancement.rebirths(), advancement.karma());
    var updated = new CharacterProgress(progress.version(), progress.characterClass(), progress.gender(), attributes,
        progress.vitals(), updatedAdvancement, progress.inventory(), progress.equipment(), progress.skills(),
        progress.spells(), progress.questFlags(), progress.storage(), progress.storageGold(), progress.respawn(),
        progress.buffs(), progress.cooldowns(), progress.hiddenUntil(), progress.companion());
    return new Allocation((int) spent, updated);
  }

  private static int allocated(int current, Integer amount) {
    if (amount == null) return current;
    try {
      return Math.addExact(current, amount);
    } catch (ArithmeticException overflow) {
      throw new PreviewWorld.Rejected(409, "invalid_allocation");
    }
  }

  static Result award(OnlineStore.CharacterState character, long baseExperience, long now, RandomGenerator random) {
    if (baseExperience<0 || now<0) throw new IllegalArgumentException("Invalid experience award");
    var p=character.progress();
    if (p==null) throw new PreviewWorld.Rejected(409,"uninitialized_character");
    OnlineBuffs.requireModelledOrReject(p,now);
    int level=p.advancement().level();
    long cap=CURVE.getTotalXp(GameConstants.MAX_PLAYER_LEVEL);
    // Online experience is cumulative within this incarnation, not XP remaining in this level.
    if (level>GameConstants.MAX_PLAYER_LEVEL || character.experience()<CURVE.getTotalXp(level)
        || character.experience()>cap
        || (level<GameConstants.MAX_PLAYER_LEVEL && character.experience()>=CURVE.getTotalXp(level+1)))
      throw new PreviewWorld.Rejected(409,"invalid_progression");
    long award=Math.min(cap-character.experience(),PlayerProgression.scaledAward(baseExperience,0,false));
    if (award<=0) return new Result(0,p);
    var draft=new Draft();
    draft.level=level; draft.currentXp=character.experience()-CURVE.getTotalXp(level);
    draft.xpToNextLevel=CURVE.getXpToNextLevel(level);
    draft.endurance=p.attributes().endurance(); draft.intelligence=p.attributes().intelligence(); draft.wisdom=p.attributes().wisdom();
    draft.maxHp=p.vitals().maxHp(); draft.currentHp=p.vitals().hp();
    draft.maxMana=p.vitals().maxMana(); draft.mana=p.vitals().mana();
    draft.statPoints=p.advancement().statPoints(); draft.skillPoints=p.advancement().skillPoints();
    new PlayerProgression(random).addScaledXp(draft,award,CURVE);
    long resultingTotal=Math.addExact(CURVE.getTotalXp(draft.level),draft.currentXp);
    if (resultingTotal!=character.experience()+award) throw new IllegalStateException("Progression total mismatch");
    var advancement=new CharacterProgress.Advancement(draft.level,draft.statPoints,draft.skillPoints,
        p.advancement().rebirths(),p.advancement().karma());
    var vitals=new CharacterProgress.Vitals(draft.currentHp,draft.maxHp,draft.mana,draft.maxMana);
    var progress=new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),vitals,advancement,
        p.inventory(),p.equipment(),p.skills(),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),
        p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
    return new Result(award,progress);
  }

  @Getter @Setter
  private static final class Draft implements ProgressionState {
    private int level,endurance,intelligence,wisdom,maxHp,currentHp,maxMana,mana,statPoints,skillPoints;
    private long currentXp,xpToNextLevel;
    @Override public float getBuffXpMultiplier() { return 0; }
    // Presentation is emitted only from committed online state, never from an uncommitted draft.
    @Override public void showLevelUpMessage(int level,int hpGain,int manaGain) {}
    @Override public void notifyLevelUp(int level) {}
  }
}
