package com.perso.T4C.server;

import com.perso.T4C.death.DeathRules;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import java.util.random.RandomGenerator;

/** Roll once on the simulation owner; persist this same draft on an uncertain retry. */
final class OnlineDeath {
  private static final XpCurve CURVE=XpCurve.loadDefault();
  record Result(CharacterProgress progress,OnlineStore.DeathTransition transition) {}

  static Result prepare(OnlineStore.CharacterState saved,ItemContent items,
      CharacterProgress.Respawn deathPosition,CharacterProgress.Respawn destination,RandomGenerator random) {
    var p=Objects.requireNonNull(saved.progress());
    int level=p.advancement().level();
    long floor=CURVE.getTotalXp(level);
    if(level>com.perso.T4C.config.GameConstants.MAX_PLAYER_LEVEL || saved.experience()<floor
        || saved.experience()>CURVE.getTotalXp(com.perso.T4C.config.GameConstants.MAX_PLAYER_LEVEL)
        || (level<com.perso.T4C.config.GameConstants.MAX_PLAYER_LEVEL && saved.experience()>=CURVE.getTotalXp(level+1))) throw new IllegalArgumentException("Invalid cumulative experience");
    var inventory=new ArrayList<>(p.inventory());var drops=new ArrayList<CharacterProgress.Item>();
    var equipped=new HashSet<>(p.equipment().values());
    for(int i=inventory.size()-1;i>=0;i--) {
      var item=inventory.get(i);var definition=items.items().get(item.key());
      if(definition==null) throw new IllegalArgumentException("Unknown owned item");
      if(equipped.contains(item.id()) || definition.undroppable()) continue;
      int dropped=binomial(item.quantity(),DeathRules.backpackPercent(false)/100.0,random);
      if(dropped==item.quantity()) { inventory.remove(i);drops.add(item); }
      else if(dropped>0) {
        inventory.set(i,new CharacterProgress.Item(item.id(),item.key(),item.quantity()-dropped,item.charges()));
        drops.add(new CharacterProgress.Item(UUID.randomUUID(),item.key(),dropped,item.charges()));
      }
    }
    var vitals=new CharacterProgress.Vitals(DeathRules.respawnHealth(p.vitals().maxHp()),p.vitals().maxHp(),p.vitals().mana(),p.vitals().maxMana());
    var cooldowns=new HashMap<>(p.cooldowns());cooldowns.remove("stun");
    var progress=new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),vitals,p.advancement(),inventory,p.equipment(),p.skills(),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),p.buffs(),cooldowns,p.hiddenUntil(),p.companion());
    long xpLost=DeathRules.percentage(saved.experience()-floor,DeathRules.experiencePercent(false));
    return new Result(progress,new OnlineStore.DeathTransition(xpLost,destination,deathPosition,drops,0,0));
  }
  /** Exact binomial decomposition through beta order statistics; work scales logarithmically
   * with stack size, rather than iterating potentially billions of owned units. */
  static int binomial(int n,double probability,RandomGenerator random) {
    int successes=0;
    while(n>64 && probability>0 && probability<1) {
      int a=1+n/2,b=n-a+1;
      double left=gamma(a,random),right=gamma(b,random),split=left/(left+right);
      if(split>=probability) { n=a-1;probability/=split; }
      else { successes+=a;n=b-1;probability=(probability-split)/(1-split); }
    }
    if(probability>=1) return successes+n;
    if(probability<=0) return successes;
    for(int i=0;i<n;i++) if(random.nextDouble()<probability) successes++;
    return successes;
  }

  /** Marsaglia-Tsang gamma sampler, used only with integer shape >= 1. */
  private static double gamma(int shape,RandomGenerator random) {
    double d=shape-1.0/3,c=1/Math.sqrt(9*d);
    while(true) {
      double x=random.nextGaussian(),v=1+c*x;
      if(v<=0) continue;
      v=v*v*v;double u=random.nextDouble();
      if(u<1-0.0331*x*x*x*x || Math.log(u)<0.5*x*x+d*(1-v+Math.log(v))) return d*v;
    }
  }
}
