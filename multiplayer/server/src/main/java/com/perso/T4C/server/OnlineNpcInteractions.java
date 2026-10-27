package com.perso.T4C.server;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import java.util.*;
import java.util.function.Supplier;

/** Creates one immutable server-authored draft; publish dialogue only after its transaction commits. */
public final class OnlineNpcInteractions {
  public record Result(CharacterProgress progress, String messageKey, boolean changed) {}
  private final NpcInteractionCatalog catalog;
  private final ItemContent.Definition letter;
  public OnlineNpcInteractions(NpcInteractionCatalog catalog,ItemContent items) {
    this.catalog=Objects.requireNonNull(catalog);
    letter=items.items().get(CrimsonscaleLetterRules.ITEM);
    // This slice has no inventory-weight or unique-item admission engine. The authored letter
    // is weightless and non-unique; fail closed if content changes that requirement.
    if(letter==null || letter.weight()!=0 || letter.unique() || letter.nbCharges()<0)
      throw new IllegalArgumentException("Unsupported letter reward definition");
  }

  /** Position and sight must come from the simulation, never the request body. */
  public Result prepare(CharacterProgress progress,int world,double x,double y,String npcId,
      boolean hasLineOfSight,Supplier<UUID> instanceId) {
    Objects.requireNonNull(progress); Objects.requireNonNull(instanceId);
    var npc=catalog.require(npcId);
    double dx=x-npc.x(),dy=y-npc.y();
    if(progress.vitals().hp()<=0 || world!=npc.world() || !Double.isFinite(x) || !Double.isFinite(y)
        || dx*dx+dy*dy>=GameConstants.NPC_INTERACTION_RANGE_TILES_SQUARED || !hasLineOfSight)
      throw new IllegalArgumentException("NPC is not reachable");
    int flag=progress.questFlags().getOrDefault(CrimsonscaleLetterRules.LETTER_FLAG,0);
    String message=CrimsonscaleLetterRules.greeting(flag,
        progress.questFlags().getOrDefault(CrimsonscaleLetterRules.STORY_FLAG,0));
    if(!CrimsonscaleLetterRules.grantsLetter(flag)) return new Result(progress,message,false);
    var inventory=new ArrayList<>(progress.inventory());
    inventory.add(new CharacterProgress.Item(instanceId.get(),letter.key(),1,letter.nbCharges()));
    var flags=new HashMap<>(progress.questFlags()); flags.put(CrimsonscaleLetterRules.LETTER_FLAG,1);
    var updated=new CharacterProgress(progress.version(),progress.characterClass(),progress.gender(),
        progress.attributes(),progress.vitals(),progress.advancement(),inventory,progress.equipment(),
        progress.skills(),progress.spells(),flags,progress.storage(),progress.storageGold(),progress.respawn(),
        progress.buffs(),progress.cooldowns(),progress.hiddenUntil(),progress.companion());
    return new Result(updated,message,true);
  }
}
