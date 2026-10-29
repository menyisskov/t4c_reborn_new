package com.perso.T4C.server;

import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.NpcAppearanceContent;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.random.RandomGenerator;

/** Immutable NPC membership; conversation sight deliberately ignores sanctuary combat rules. */
final class RegionNpcs {
  private final int world;
  private final OnlineNpcInteractions interactions;

  private final List<NpcView> views;
  private final Map<String,String> supportedByView;
  private final Map<String,String> shopkeeperByView;

  RegionNpcs(NpcInteractionCatalog catalog, ItemContent items, RegionMapLoader.Spec region) {
    this(catalog,items,region,null);
  }

  /** Publish the independent authored appearance inventory without granting its scripts. */
  RegionNpcs(NpcInteractionCatalog catalog, ItemContent items, RegionMapLoader.Spec region,
      NpcAppearanceCatalog appearances) {
    world=region.world();
    interactions=new OnlineNpcInteractions(catalog,items,id->OnlineShops.vendor(id).isPresent());
    if(appearances==null) {
      views=catalog.npcs().stream().filter(n->region.contains(n.world(),n.x(),n.y()))
          .map(n->new NpcView(n.id(),n.displayName(),n.x()-region.originX(),n.y()-region.originY(),
              n.spriteBase(),n.parts())).toList();
      var supported=new HashMap<String,String>();
      for(var view:views) supported.put(view.id(),view.id());
      supportedByView=Map.copyOf(supported);
      var shops=new HashMap<String,String>();
      for(var view:views) if(OnlineShops.vendor(view.id()).isPresent()) shops.put(view.id(),view.id());
      shopkeeperByView=Map.copyOf(shops);
      return;
    }
    var published=new ArrayList<NpcView>();
    var supported=new HashMap<String,String>();
    var shopkeepers=new HashMap<String,String>();
    var ids=new HashSet<String>();
    List<NpcAppearanceContent.Spawn> inRegion=appearances.inRegion(region);
    for(var spawn:inRegion) {
      var registered=catalog.npcs().stream().filter(n->n.id().equals(spawn.canonicalNpcId())
          && n.world()==spawn.world() && n.x()==spawn.x() && n.y()==spawn.y()).findFirst();
      if(spawn.interactionSupported()!=registered.isPresent())
        throw new IllegalArgumentException("NPC interaction registration mismatch");
      String id=registered.isPresent() ? spawn.canonicalNpcId() : NpcAppearanceCatalog.viewId(spawn);
      if(!ids.add(id)) throw new IllegalArgumentException("Duplicate public NPC identity");
      published.add(new NpcView(id,spawn.displayName(),spawn.x()-region.originX(),
          spawn.y()-region.originY(),spawn.spriteBase(),spawn.parts()));
      registered.ifPresent(n->supported.put(id,n.id()));
      if(OnlineShops.vendor(spawn.canonicalNpcId()).isPresent())
        shopkeepers.put(id,spawn.canonicalNpcId());
    }
    for(var npc:catalog.npcs())
      if(region.contains(npc.world(),npc.x(),npc.y()) && !supported.containsKey(npc.id()))
        throw new IllegalArgumentException("Registered NPC missing from appearance inventory");
    views=List.copyOf(published);
    supportedByView=Map.copyOf(supported);
    shopkeeperByView=Map.copyOf(shopkeepers);
  }

  List<NpcView> views() { return views; }

  /** Vendor stock shared by every region; read once from the bundled content. */
  NpcShopCatalog shops() { return OnlineShops.catalog(); }

  /**
   * The canonical id of the shopkeeper this character can trade with right now, using the same
   * range and sight rule as dialogue. Throws if the NPC is unknown or unreachable; an empty result
   * means a reachable NPC that has no shop.
   */
  java.util.Optional<String> reachableShopkeeper(OnlineStore.CharacterState saved, PreviewMap map,
      double x, double y, String npcId) {
    var npc=views.stream().filter(n->n.id().equals(npcId)).findFirst()
        .orElseThrow(()->new IllegalArgumentException("NPC is not in this region"));
    String canonical=supportedByView.get(npcId);
    boolean lineOfSight=map.hasLineOfSight(x,y,npc.x(),npc.y());
    if(canonical!=null) interactions.requireReachable(saved,world,map.originX+x,map.originY+y,canonical,lineOfSight);
    else {
      canonical=shopkeeperByView.get(npcId);
      if(canonical==null) throw new IllegalArgumentException("NPC interaction unavailable");
      interactions.requireShopkeeperReachable(saved,world,map.originX+x,map.originY+y,canonical,
          world,map.originX+npc.x(),map.originY+npc.y(),lineOfSight);
    }
    return OnlineShops.vendor(canonical).isPresent() ? java.util.Optional.of(canonical) : java.util.Optional.empty();
  }

  OnlineNpcInteractions.Result prepare(OnlineStore.CharacterState saved, PreviewMap map,
      double x, double y, String npcId, OptionalInt topic, long now, RandomGenerator random) {
    var npc=views.stream().filter(n->n.id().equals(npcId)).findFirst()
        .orElseThrow(()->new IllegalArgumentException("NPC is not in this region"));
    String canonical=supportedByView.get(npcId);
    boolean lineOfSight=map.hasLineOfSight(x,y,npc.x(),npc.y());
    if(canonical==null) {
      canonical=shopkeeperByView.get(npcId);
      if(canonical==null) throw new IllegalArgumentException("NPC interaction unavailable");
      return interactions.prepareShopkeeper(saved,world,map.originX+x,map.originY+y,canonical,
          world,map.originX+npc.x(),map.originY+npc.y(),lineOfSight,topic);
    }
    return interactions.prepare(saved,world,map.originX+x,map.originY+y,canonical,
        lineOfSight,topic,UUID::randomUUID,now,random);
  }
}
