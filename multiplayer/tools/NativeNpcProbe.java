package com.perso.T4C.server;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.OnlineEndpoint;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import com.perso.T4C.server.persistence.OnlineStore;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Consequential QA through two real native network adapters; creates disposable online data only. */
public final class NativeNpcProbe {
  private static OnlineConnection.View connected(OnlineConnection connection) throws Exception {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
    while(connection.view()==null && System.nanoTime()<deadline) Thread.sleep(25);
    var view=connection.view();
    if(view==null) throw new AssertionError("Native connection unavailable");
    connection.confirmRegionReady(view.map().locationEpoch());
    // The network worker sends readiness before the next queued interaction; retries below
    // cover a normally rejected request queued just before that acknowledgement.
    return view;
  }

  private static OnlineConnection.CommandResult talk(OnlineConnection connection) throws Exception {
    for(int attempt=0;attempt<10;attempt++) {
      var result=connection.interact(CrimsonscaleLetterRules.NPC).toCompletableFuture().get(5,TimeUnit.SECONDS);
      if(result.accepted()) return result;
      Thread.sleep(100);
    }
    throw new AssertionError("Nearby authored NPC remained unavailable");
  }

  public static void main(String[] args) throws Exception {
    if(args.length>1) throw new IllegalArgumentException("Expected optional endpoint");
    var endpoint=OnlineEndpoint.validate(URI.create(args.length==0 ? "http://127.0.0.1:18765" : args[0]));
    var store=OnlineStore.fromEnvironment();store.migrate();
    var npc=NpcInteractionCatalog.loadBundled().require(CrimsonscaleLetterRules.NPC);
    var kit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(195));
    var owner=store.provisionAccount();var other=store.provisionAccount();
    var character=store.create(owner.accountId(),"Native letter owner",npc.world(),npc.x()+.5,npc.y()+.5,kit.progress(),kit.gold());
    var observer=store.create(other.accountId(),"Native letter observer",0,2944.5,1059.5,kit.progress(),kit.gold());
    var credentials=new OnlineConnection.Credentials(owner.secret(),character.id());
    UUID letterId;
    try(var player=new OnlineConnection(endpoint,credentials);
        var witness=new OnlineConnection(endpoint,new OnlineConnection.Credentials(other.secret(),observer.id()))) {
      var initial=connected(player);connected(witness);
      if(initial.snapshot().npcs().stream().noneMatch(n->n.id().equals(npc.id())))
        throw new AssertionError("Authored NPC missing from native snapshot");
      var first=talk(player);
      if(!"npc.kilhiam.letter".equals(first.messageKey())) throw new AssertionError("Wrong first dialogue");
      var repeat=talk(player);
      if(!"npc.kilhiam.temple".equals(repeat.messageKey())) throw new AssertionError("Wrong repeat dialogue");
      var saved=store.load(owner.accountId(),character.id());
      var letters=saved.progress().inventory().stream().filter(i->i.key().equals(CrimsonscaleLetterRules.ITEM)).toList();
      if(letters.size()!=1 || letters.getFirst().quantity()!=1
          || saved.progress().questFlags().getOrDefault(CrimsonscaleLetterRules.LETTER_FLAG,0)!=1)
        throw new AssertionError("Letter and story flag did not commit together exactly once");
      letterId=letters.getFirst().id();
      var p=saved.progress();
      var remainingFlags=new HashMap<>(p.questFlags());remainingFlags.remove(CrimsonscaleLetterRules.LETTER_FLAG);
      if(!p.inventory().stream().filter(i->!i.key().equals(CrimsonscaleLetterRules.ITEM)).toList().equals(kit.progress().inventory())
          || !remainingFlags.equals(kit.progress().questFlags()))
        throw new AssertionError("Dialogue changed unrelated inventory or flags");
      var preserved=new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),
          kit.progress().inventory(),p.equipment(),p.skills(),p.spells(),kit.progress().questFlags(),p.storage(),p.storageGold(),
          p.respawn(),p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
      if(!preserved.equals(kit.progress()) || saved.gold()!=kit.gold() || saved.experience()!=character.experience())
        throw new AssertionError("NPC interaction altered unrelated character progress");
      if(!store.load(other.accountId(),observer.id()).progress().equals(kit.progress()))
        throw new AssertionError("Dialogue altered observer progress");
    }
    // close() is asynchronous; wait for the normal logout/checkpoint rather than taking its lease.
    Thread.sleep(2500);
    try(var rejoined=new OnlineConnection(endpoint,credentials)) {
      connected(rejoined);talk(rejoined);
      var letters=store.load(owner.accountId(),character.id()).progress().inventory().stream()
          .filter(i->i.key().equals(CrimsonscaleLetterRules.ITEM)).toList();
      if(letters.size()!=1 || !letters.getFirst().id().equals(letterId))
        throw new AssertionError("Reconnect duplicated or replaced the letter");
    }
    System.out.println("PASS two actual native adapters: NPC replication, first letter/flag, repeat greeting, observer isolation and reconnect without duplicate. No rendered dialogue claim.");
  }
}
