package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class CharacterProgressTest {
  static CharacterProgress sample() {
    var sword=new CharacterProgress.Item(UUID.randomUUID(),"item.recruits_greatsword",1,0);
    return state(List.of(sword),Map.of("WEAPON",sword.id()),List.of(
        new CharacterProgress.Item(UUID.randomUUID(),"item.light_healing_potion",3,3)));
  }

  private static CharacterProgress state(List<CharacterProgress.Item> items,Map<String,UUID> equipment,
      List<CharacterProgress.Item> storage) {
    return new CharacterProgress(1,"warrior","male",
        new CharacterProgress.Attributes(95,85,75,75,75),new CharacterProgress.Vitals(41,60,17,25),
        new CharacterProgress.Advancement(2,3,5,0,100),items,equipment,Map.of("attack",15,"dodge",12),
        Set.of("spell.heal_light","spell.stone_shard"),Map.of("quest.started",1),storage,7,
        new CharacterProgress.Respawn(0,20,30),List.of(new CharacterProgress.Buff("spell.heal_light",1900000000000L,75,75)),
        Map.of("attack",1900000000000L),0,new CharacterProgress.Companion("Wolf",true,2,12,"follow"));
  }

  @Test void preventsDuplicateInstancesAndDanglingEquipmentBeforeSaving() {
    var sword=new CharacterProgress.Item(UUID.randomUUID(),"item.recruits_greatsword",1,0);
    assertThrows(IllegalArgumentException.class,() -> state(List.of(sword),Map.of("WEAPON",UUID.randomUUID()),List.of()));
    assertThrows(IllegalArgumentException.class,() -> state(List.of(sword),Map.of("WEAPON",sword.id()),List.of(sword)));
    var mutable=new ArrayList<>(List.of(sword));
    var saved=state(mutable,Map.of("WEAPON",sword.id()),List.of());
    mutable.clear();
    assertEquals(List.of(sword),saved.inventory());
    assertThrows(UnsupportedOperationException.class,() -> saved.inventory().clear());
  }
}
