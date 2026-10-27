package com.perso.T4C.server;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/** Creates one immutable server-authored draft; publish dialogue only after its transaction commits. */
public final class OnlineNpcInteractions {
  /**
   * A draft to commit once. Gold and experience are the reward the transaction adds (zero for dialogue
   * that changes nothing); args carry numbers for the authored message, such as quest progress.
   */
  public record Result(CharacterProgress progress, String messageKey, boolean changed, long gold, long experience,
      List<Integer> args) {
    public Result {
      Objects.requireNonNull(progress); Objects.requireNonNull(messageKey);
      args=List.copyOf(args);
    }
    public Result(CharacterProgress progress, String messageKey, boolean changed) {
      this(progress,messageKey,changed,0,0,List.of());
    }
  }
  private final NpcInteractionCatalog catalog;
  private final ItemContent.Definition letter;
  private final ItemContent items;
  private final QuestCatalog quests;
  public OnlineNpcInteractions(NpcInteractionCatalog catalog,ItemContent items) {
    this.catalog=Objects.requireNonNull(catalog);
    this.items=Objects.requireNonNull(items);
    this.quests=QuestCatalog.bundled();
    letter=items.items().get(CrimsonscaleLetterRules.ITEM);
    // This slice has no inventory-weight or unique-item admission engine. The authored letter
    // is weightless and non-unique; fail closed if content changes that requirement.
    if(letter==null || letter.weight()!=0 || letter.unique() || letter.nbCharges()<0)
      throw new IllegalArgumentException("Unsupported letter reward definition");
  }

  /**
   * Position and sight must come from the simulation, never the request body. An empty topic is a
   * greeting: a quest giver's greeting turns in a ready quest, while a topic reads the authored line.
   */
  public Result prepare(OnlineStore.CharacterState saved,int world,double x,double y,String npcId,
      boolean hasLineOfSight,OptionalInt topic,Supplier<UUID> instanceId,long now,RandomGenerator random) {
    Objects.requireNonNull(saved); Objects.requireNonNull(topic); Objects.requireNonNull(instanceId);
    Objects.requireNonNull(random);
    var progress=Objects.requireNonNull(saved.progress());
    var npc=catalog.require(npcId);
    double dx=x-npc.x(),dy=y-npc.y();
    if(progress.vitals().hp()<=0 || world!=npc.world() || !Double.isFinite(x) || !Double.isFinite(y)
        || dx*dx+dy*dy>=GameConstants.NPC_INTERACTION_RANGE_TILES_SQUARED || !hasLineOfSight)
      throw new IllegalArgumentException("NPC is not reachable");
    if(!CrimsonscaleLetterRules.NPC.equals(npcId)) return questInteraction(saved,npcId,topic,instanceId,now,random);
    if(topic.isPresent()) throw new IllegalArgumentException("NPC has no topics");
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

  /** A quest giver: a greeting turns in a ready quest atomically; a topic reads the offer or progress line. */
  private Result questInteraction(OnlineStore.CharacterState saved,String npcId,OptionalInt topic,
      Supplier<UUID> instanceId,long now,RandomGenerator random) {
    QuestDef definition=quests.definitions().stream().filter(d->d.getGiverNpc().equals(npcId))
        .findFirst().orElseThrow(()->new IllegalArgumentException("NPC has no online quest"));
    if(topic.isEmpty()) return toResult(OnlineQuests.greet(saved,definition,items,instanceId,now,random));
    return toResult(OnlineQuests.topic(saved.progress(),definition,topic.getAsInt()));
  }

  private static Result toResult(OnlineQuests.Result reply) {
    return new Result(reply.progress(),reply.messageKey(),reply.changed(),reply.gold(),reply.experience(),reply.args());
  }
}
