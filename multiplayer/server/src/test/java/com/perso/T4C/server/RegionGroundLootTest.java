package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.server.persistence.OnlineStore;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class RegionGroundLootTest {
  private static final RegionMapLoader.Spec SPEC=new RegionMapLoader.Spec(0,"start",2904,1029,80,60,2944,1059);
  private final AtomicLong clock=new AtomicLong(1_000_000_000L);

  private static OnlineStore.GroundLoot drop(UUID id,UUID owner,long remaining) {
    return new OnlineStore.GroundLoot(id,0,2930,1050,"item.leather_gloves",1,0,owner,remaining);
  }

  private void advanceMillis(long ms) { clock.addAndGet(ms*1_000_000L); }

  @Test
  void ownerWindowCountsDownAndClearsOnceOver() {
    var loot=new RegionGroundLoot(SPEC,clock::get);
    var id=UUID.randomUUID();var owner=UUID.randomUUID();
    loot.publish(drop(id,owner,60_000));
    assertEquals(60_000,loot.view().getFirst().ownerRemainingMillis());
    advanceMillis(25_000);
    var mid=loot.view().getFirst();
    assertEquals(35_000,mid.ownerRemainingMillis());
    assertEquals(owner,mid.ownerCharacter());
    advanceMillis(40_000);
    var over=loot.view().getFirst();
    assertEquals(0,over.ownerRemainingMillis());
    assertNull(over.ownerCharacter(),"an expired window leaves no owner in the public view");
    assertEquals(60_000,loot.get(id).ownerRemainingMillis(),"the cached record itself is untouched");
  }

  @Test
  void aDatabaseReadCountsDownFromWhenItStarted() {
    var loot=new RegionGroundLoot(SPEC,clock::get);
    var id=UUID.randomUUID();var owner=UUID.randomUUID();
    loot.beginRead();
    advanceMillis(1_000);
    loot.endRead(List.of(drop(id,owner,30_000)));
    assertEquals(29_000,loot.view().getFirst().ownerRemainingMillis());
    advanceMillis(30_000);
    assertNull(loot.view().getFirst().ownerCharacter());
  }

  @Test
  void dropsWithoutAnOwnerAreUnchanged() {
    var loot=new RegionGroundLoot(SPEC,clock::get);
    loot.publish(drop(UUID.randomUUID(),null,0));
    advanceMillis(5_000);
    var view=loot.view().getFirst();
    assertEquals(0,view.ownerRemainingMillis());
    assertNull(view.ownerCharacter());
  }

  @Test
  void aClaimIsRememberedAsTakenByOthersOnly() {
    var loot=new RegionGroundLoot(SPEC,clock::get);
    var id=UUID.randomUUID();var winner=UUID.randomUUID();var loser=UUID.randomUUID();
    loot.publish(drop(id,null,0));
    loot.removeClaimed(id,winner);
    assertFalse(loot.contains(id));
    assertTrue(loot.takenByOther(id,loser));
    assertFalse(loot.takenByOther(id,winner));
    assertFalse(loot.takenByOther(UUID.randomUUID(),loser),"an unknown or expired drop is not reported as taken");
  }

  @Test
  void recentClaimMemoryIsBounded() {
    var loot=new RegionGroundLoot(SPEC,clock::get);
    var first=UUID.randomUUID();var who=UUID.randomUUID();
    loot.removeClaimed(first,who);
    for (int i=0;i<300;i++) loot.removeClaimed(UUID.randomUUID(),who);
    assertFalse(loot.takenByOther(first,UUID.randomUUID()));
  }
}
