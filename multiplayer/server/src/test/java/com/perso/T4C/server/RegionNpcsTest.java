package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.online.model.NpcAppearanceContent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RegionNpcsTest {
  @Test void templeConversationIgnoresSanctuaryButNotWallsOrRegionOwnership() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled();var authored=catalog.npcs().getFirst();
    var spec=new RegionMapLoader.Spec(authored.world(),"temple",authored.x()-5,authored.y()-5,12,12,authored.x(),authored.y());
    var items=ItemCatalog.loadBundled();var npcs=new RegionNpcs(catalog,items,spec);
    var terrain=new byte[12][12]; for(var row:terrain) java.util.Arrays.fill(row,(byte)6);
    var map=new PreviewMap(new boolean[12][12],terrain,spec.originX(),spec.originY(),5,5);
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    assertFalse(map.allowsCombat(3.5,5.5,5,5,false));
    assertEquals("npc.kilhiam.letter",talk(npcs,progress,map,authored.id()).messageKey());
    assertEquals(5,npcs.views().getFirst().x());
    terrain[5][4]=1;
    var wallMap=new PreviewMap(new boolean[12][12],terrain,spec.originX(),spec.originY(),5,5);
    assertThrows(IllegalArgumentException.class,()->talk(npcs,progress,wallMap,authored.id()));
    var other=new RegionNpcs(catalog,items,new RegionMapLoader.Spec(authored.world()+1,"other",0,0,12,12,5,5));
    assertTrue(other.views().isEmpty());
    assertThrows(IllegalArgumentException.class,()->talk(other,progress,map,authored.id()));
  }

  private static OnlineNpcInteractions.Result talk(RegionNpcs npcs,
      com.perso.T4C.online.model.CharacterProgress progress,PreviewMap map,String npcId) {
    var saved=new com.perso.T4C.server.persistence.OnlineStore.CharacterState(java.util.UUID.randomUUID(),
        java.util.UUID.randomUUID(),"Tester",0,3,3,0,0,0,progress);
    return npcs.prepare(saved,map,3.5,5.5,npcId,java.util.OptionalInt.empty(),100,new Random(1));
  }

  @Test void sigfriedGreetsWithHisAuthoredWelcomeAndIsTheOnlyReachableShopkeeperInHisRegion() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled();var sigfried=catalog.require("Sigfried");
    var items=ItemCatalog.loadBundled();
    var spec=new RegionMapLoader.Spec(sigfried.world(),"shop",sigfried.x()-5,sigfried.y()-5,40,12,sigfried.x(),sigfried.y());
    var npcs=new RegionNpcs(catalog,items,spec);
    var map=new PreviewMap(new boolean[12][40],spec.originX(),spec.originY(),5,5);
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var saved=new com.perso.T4C.server.persistence.OnlineStore.CharacterState(java.util.UUID.randomUUID(),
        java.util.UUID.randomUUID(),"Tester",0,3,3,0,0,0,progress);
    var greeting=npcs.prepare(saved,map,3.5,5.5,"Sigfried",java.util.OptionalInt.empty(),100,new Random(1));
    assertEquals("npc.welcome.sigfried",greeting.messageKey());
    assertFalse(greeting.changed());
    assertThrows(IllegalArgumentException.class,()->npcs.prepare(saved,map,3.5,5.5,"Sigfried",
        java.util.OptionalInt.of(0),100,new Random(1)),"shopkeepers have no online topics");
    assertEquals(java.util.Optional.of("Sigfried"),npcs.reachableShopkeeper(saved,map,3.5,5.5,"Sigfried"));
    assertThrows(IllegalArgumentException.class,()->npcs.reachableShopkeeper(saved,map,30.5,5.5,"Sigfried"),
        "out of conversation range");
    assertThrows(IllegalArgumentException.class,()->npcs.reachableShopkeeper(saved,map,3.5,5.5,"Nobody"));
    var kilhiam=catalog.require("Kilhiam");
    var temple=new RegionNpcs(catalog,items,new RegionMapLoader.Spec(kilhiam.world(),"temple",kilhiam.x()-5,kilhiam.y()-5,12,12,kilhiam.x(),kilhiam.y()));
    var templeMap=new PreviewMap(new boolean[12][12],kilhiam.x()-5,kilhiam.y()-5,5,5);
    assertTrue(temple.reachableShopkeeper(saved,templeMap,3.5,5.5,"Kilhiam").isEmpty(),"a reachable NPC without a shop");
    var dead=new com.perso.T4C.server.persistence.OnlineStore.CharacterState(saved.id(),saved.accountId(),"Dead",0,3,3,0,0,0,
        ProgressEdits.withVitals(progress,new com.perso.T4C.online.model.CharacterProgress.Vitals(0,progress.vitals().maxHp(),0,0)));
    assertThrows(IllegalArgumentException.class,()->npcs.reachableShopkeeper(dead,map,3.5,5.5,"Sigfried"));
  }

  @Test void wardenAelricIsListedOnlyInTheRegionThatContainsHisSpawn() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled(); var items=ItemCatalog.loadBundled();
    var temple=new RegionNpcs(catalog,items,new RegionMapLoader.Spec(0,"temple",2904,1029,80,60,2948,1044));
    assertTrue(temple.views().stream().anyMatch(n->n.id().equals("WardenAelric")));
    var elsewhere=new RegionNpcs(catalog,items,new RegionMapLoader.Spec(0,"elsewhere",0,0,10,10,5,5));
    assertTrue(elsewhere.views().isEmpty());
  }

  @Test void realHostedAppearancesPreserveSourceInventoryAndSundialWithoutGrantingItsScript()
      throws Exception {
    var spec=new RegionMapLoader.Spec(0,"start",2735,1029,249,117,2944,1059);
    var appearances=NpcAppearanceCatalog.loadBundled();
    var source=appearances.inRegion(spec);
    var npcs=new RegionNpcs(NpcInteractionCatalog.loadBundled(),ItemCatalog.loadBundled(),
        spec,appearances);
    assertEquals(source.size(),npcs.views().size(),"Every hosted source appearance is published");
    assertEquals(npcs.views().size(),
        npcs.views().stream().map(n->n.id()).collect(java.util.stream.Collectors.toSet()).size());
    for(var spawn:source) {
      String id=spawn.interactionSupported() ? spawn.canonicalNpcId()
          : NpcAppearanceCatalog.viewId(spawn);
      var view=npcs.views().stream().filter(n->n.id().equals(id)).findFirst().orElseThrow();
      assertEquals(spawn.x()-spec.originX(),view.x());
      assertEquals(spawn.y()-spec.originY(),view.y());
      assertEquals(spawn.spriteBase(),view.spriteBase());
      assertEquals(spawn.parts(),view.parts());
    }
    var sundial=source.stream().filter(s->s.canonicalNpcId().equals("Sundial")
        && s.x()==2881 && s.y()==1070).findFirst().orElseThrow();
    assertEquals("@static:Horloge Solaire",sundial.spriteBase());
    assertEquals(NpcAppearanceContent.FrameEvidence.FRAME_NAME_PRESENT,sundial.frameEvidence());
    assertFalse(sundial.interactionSupported());
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,
        new Random(1)).progress();
    var map=new PreviewMap(new boolean[spec.height()][spec.width()],
        spec.originX(),spec.originY(),2944-spec.originX(),1059-spec.originY());
    assertThrows(IllegalArgumentException.class,()->talk(npcs,progress,map,
        NpcAppearanceCatalog.viewId(sundial)));
    assertTrue(npcs.views().stream().anyMatch(n->n.id().equals("Kilhiam")));
    assertTrue(npcs.views().stream().anyMatch(n->n.id().equals("WardenAelric")));
  }

  @Test void duplicateCanonicalNpcKeepsDistinctAppearanceButNoSecondInteraction() throws Exception {
    var catalog=NpcInteractionCatalog.loadBundled();
    var interactive=catalog.npcs().stream().filter(n->n.id().equals("Kilhiam"))
        .findFirst().orElseThrow();
    var spec=new RegionMapLoader.Spec(interactive.world(),"duplicates",
        interactive.x()-3,interactive.y()-3,10,10,interactive.x(),interactive.y());
    var original=NpcAppearanceCatalog.loadBundled().inRegion(spec).stream()
        .filter(s->s.canonicalNpcId().equals(interactive.id()) && s.x()==interactive.x()
            && s.y()==interactive.y()).findFirst().orElseThrow();
    var duplicate=moved(original,interactive.x()+1,false);
    String sharedKey=new MonsterContent.Spawn("OtherNpc",null,original.x(),original.y(),
        original.world(),original.stationary(),original.aggressive()).persistenceKey();
    var sharedAnchor=new NpcAppearanceContent.Spawn(sharedKey,"OtherNpc","OtherNpc",
        original.world(),original.x(),original.y(),original.stationary(),original.aggressive(),
        true,"Other NPC",original.spriteBase(),original.parts(),false,
        original.frameEvidence(),original.appearanceDiagnostic());
    var appearances=new NpcAppearanceCatalog(new NpcAppearanceContent(
        NpcAppearanceContent.VERSION,List.of(original,duplicate,sharedAnchor)));
    var npcs=new RegionNpcs(catalog,ItemCatalog.loadBundled(),spec,appearances);
    assertEquals(3,npcs.views().size(),"Same-anchor appearances must not be deduplicated");
    assertTrue(npcs.views().stream().anyMatch(n->n.id().equals(interactive.id())));
    assertTrue(npcs.views().stream().anyMatch(n->n.id().equals(
        NpcAppearanceCatalog.viewId(duplicate))));
    assertTrue(npcs.views().stream().anyMatch(n->n.id().equals(
        NpcAppearanceCatalog.viewId(sharedAnchor))));
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,
        new Random(1)).progress();
    var map=new PreviewMap(new boolean[10][10],spec.originX(),spec.originY(),3,3);
    assertThrows(IllegalArgumentException.class,()->talk(npcs,progress,map,
        NpcAppearanceCatalog.viewId(duplicate)));
    assertEquals("npc.kilhiam.letter",talk(npcs,progress,map,interactive.id()).messageKey());
  }

  @Test void authoredLighthavenVendorsCanGreetAndTradeWithoutGrantingDialogueTopics() throws Exception {
    var spec=new RegionMapLoader.Spec(0,"start",2735,1029,249,117,2944,1059);
    var appearances=NpcAppearanceCatalog.loadBundled();
    var items=ItemCatalog.loadBundled();
    var npcs=new RegionNpcs(NpcInteractionCatalog.loadBundled(),items,spec,appearances);
    var map=new PreviewMap(new boolean[spec.height()][spec.width()],spec.originX(),spec.originY(),
        2944-spec.originX(),1059-spec.originY());
    var progress=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1)).progress();
    var saved=new com.perso.T4C.server.persistence.OnlineStore.CharacterState(java.util.UUID.randomUUID(),
        java.util.UUID.randomUUID(),"Tester",0,2944,1059,0,0,0,progress);

    for(String id:List.of("Fali","Rolph","Araknor")) {
      var spawn=appearances.inRegion(spec).stream().filter(s->s.canonicalNpcId().equals(id))
          .findFirst().orElseThrow();
      assertFalse(spawn.interactionSupported(),"the vendor has no authored online dialogue script");
      String viewId=NpcAppearanceCatalog.viewId(spawn);
      var view=npcs.views().stream().filter(n->n.id().equals(viewId)).findFirst().orElseThrow();
      assertTrue(npcs.shops().content().vendors().stream().anyMatch(v->v.npcId().equals(id)
          && !v.offers().isEmpty()));
      var greeting=npcs.prepare(saved,map,view.x()+0.5,view.y()+0.5,viewId,
          java.util.OptionalInt.empty(),100,new Random(1));
      assertEquals("npc.welcome."+id.toLowerCase(java.util.Locale.ROOT),greeting.messageKey());
      assertFalse(greeting.changed());
      assertEquals(java.util.Optional.of(id),npcs.reachableShopkeeper(saved,map,
          view.x()+0.5,view.y()+0.5,viewId));
      assertThrows(IllegalArgumentException.class,()->npcs.reachableShopkeeper(saved,map,
          0.5,0.5,viewId));
      assertThrows(IllegalArgumentException.class,()->npcs.prepare(saved,map,view.x()+0.5,view.y()+0.5,
          viewId,java.util.OptionalInt.of(0),100,new Random(1)));
    }
  }

  @Test void hostedMissingFrameAndExcessViewsFailRatherThanDisappear() throws Exception {
    var region=new RegionMapLoader.Spec(0,"sundial",2878,1067,8,8,2881,1070);
    var source=NpcAppearanceCatalog.loadBundled().inRegion(region).stream()
        .filter(s->s.canonicalNpcId().equals("Sundial") && s.x()==2881 && s.y()==1070)
        .findFirst().orElseThrow();
    var missing=new NpcAppearanceContent.Spawn(source.spawnKey(),source.type(),
        source.canonicalNpcId(),source.world(),source.x(),source.y(),source.stationary(),
        source.aggressive(),source.deployable(),source.displayName(),source.spriteBase(),
        source.parts(),source.interactionSupported(),NpcAppearanceContent.FrameEvidence.NO_FRAME,
        "static_frame_name_missing");
    assertThrows(IllegalArgumentException.class,()->new NpcAppearanceCatalog(
        new NpcAppearanceContent(1,List.of(missing))).inRegion(region));
    var unknown=new NpcAppearanceContent.Spawn(source.spawnKey(),source.type(),
        source.canonicalNpcId(),source.world(),source.x(),source.y(),source.stationary(),
        source.aggressive(),source.deployable(),source.displayName(),"Npc",
        java.util.Map.of(),source.interactionSupported(),NpcAppearanceContent.FrameEvidence.UNKNOWN,
        "sprite_metadata_unavailable");
    assertThrows(IllegalArgumentException.class,()->new NpcAppearanceCatalog(
        new NpcAppearanceContent(1,List.of(unknown))).inRegion(region));
    var undeployable=new NpcAppearanceContent.Spawn(source.spawnKey(),source.type(),
        source.canonicalNpcId(),source.world(),source.x(),source.y(),source.stationary(),
        source.aggressive(),false,source.displayName(),source.spriteBase(),source.parts(),
        source.interactionSupported(),source.frameEvidence(),source.appearanceDiagnostic());
    assertThrows(IllegalArgumentException.class,()->new NpcAppearanceCatalog(
        new NpcAppearanceContent(1,List.of(undeployable))).inRegion(region));
    var many=new ArrayList<NpcAppearanceContent.Spawn>();
    for(int x=0;x<65;x++) many.add(moved(source,x,false));
    assertEquals(65,new HashSet<>(many.stream().map(NpcAppearanceContent.Spawn::spawnKey).toList()).size());
    assertThrows(IllegalArgumentException.class,()->new NpcAppearanceCatalog(
        new NpcAppearanceContent(1,many)).inRegion(
            new RegionMapLoader.Spec(0,"excess",0,1067,65,8,1,1070)));
  }

  private static NpcAppearanceContent.Spawn moved(NpcAppearanceContent.Spawn source,int x,
      boolean supported) {
    String key=new MonsterContent.Spawn(source.type(),null,x,source.y(),source.world(),
        source.stationary(),source.aggressive()).persistenceKey();
    return new NpcAppearanceContent.Spawn(key,source.type(),source.canonicalNpcId(),
        source.world(),x,source.y(),source.stationary(),source.aggressive(),source.deployable(),
        source.displayName(),source.spriteBase(),source.parts(),supported,
        source.frameEvidence(),source.appearanceDiagnostic());
  }
}
