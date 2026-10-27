package com.perso.T4C.server;

import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.NpcView;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.random.RandomGenerator;

/** Immutable NPC membership; conversation sight deliberately ignores sanctuary combat rules. */
final class RegionNpcs {
  private final int world;
  private final OnlineNpcInteractions interactions;
  private final List<NpcView> views;

  RegionNpcs(NpcInteractionCatalog catalog, ItemContent items, RegionMapLoader.Spec region) {
    world=region.world(); interactions=new OnlineNpcInteractions(catalog,items);
    views=catalog.npcs().stream().filter(n->region.contains(n.world(),n.x(),n.y()))
        .map(n->new NpcView(n.id(),n.displayName(),n.x()-region.originX(),n.y()-region.originY(),n.spriteBase(),n.parts()))
        .toList();
  }

  List<NpcView> views() { return views; }

  OnlineNpcInteractions.Result prepare(OnlineStore.CharacterState saved, PreviewMap map,
      double x, double y, String npcId, OptionalInt topic, long now, RandomGenerator random) {
    var npc=views.stream().filter(n->n.id().equals(npcId)).findFirst()
        .orElseThrow(()->new IllegalArgumentException("NPC is not in this region"));
    return interactions.prepare(saved,world,map.originX+x,map.originY+y,npcId,
        map.hasLineOfSight(x,y,npc.x(),npc.y()),topic,UUID::randomUUID,now,random);
  }
}
