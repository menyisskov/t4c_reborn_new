package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * A retaliation commit that misses only the database-clock due check is deferred, not a Conflict. The decision must not
 * depend on a second clock reading (soaks showed readings 1 to 2 ms past a deadline the UPDATE saw as not yet due, which
 * disconnected the target), so it is a pure comparison of the locked row against the attack's CAS fields.
 */
class AttackDeadlineMissTest {
  private static final UUID LIFE=UUID.randomUUID(),TARGET=UUID.randomUUID();

  private static OnlineStore.MonsterAttack attack(boolean withMotion) {
    var motion=withMotion ? new OnlineStore.SpawnMotion("authored-v1:key",LIFE,15,1.5,2.5,"RETALIATION",TARGET,0L) : null;
    return new OnlineStore.MonsterAttack("authored-v1:key",LIFE,TARGET,1000,2000,motion);
  }

  @Test
  void aMatchingRowIsDeferredWhateverTheClockSaysNow() {
    assertTrue(OnlineStore.onlyDueCheckMissed(attack(true),LIFE,15,TARGET,1000,2.0,25,false));
    assertTrue(OnlineStore.onlyDueCheckMissed(attack(true),LIFE,15,TARGET,1000,2.0,null,false));
    assertTrue(OnlineStore.onlyDueCheckMissed(attack(false),LIFE,99,TARGET,1000,null,25,false),
        "Without motion the revision is not compared and the position must still be unset");
  }

  @Test
  void anyOtherChangedFieldStaysAConflict() {
    assertFalse(OnlineStore.onlyDueCheckMissed(attack(true),UUID.randomUUID(),15,TARGET,1000,2.0,25,false),"different life");
    assertFalse(OnlineStore.onlyDueCheckMissed(attack(true),LIFE,16,TARGET,1000,2.0,25,false),"state revision moved");
    assertFalse(OnlineStore.onlyDueCheckMissed(attack(true),LIFE,15,UUID.randomUUID(),1000,2.0,25,false),"target changed");
    assertFalse(OnlineStore.onlyDueCheckMissed(attack(true),LIFE,15,null,1000,2.0,25,false),"target cleared");
    assertFalse(OnlineStore.onlyDueCheckMissed(attack(true),LIFE,15,TARGET,1001,2.0,25,false),"deadline moved");
    assertFalse(OnlineStore.onlyDueCheckMissed(attack(true),LIFE,15,TARGET,1000,2.0,0,false),"creature has no health");
    assertFalse(OnlineStore.onlyDueCheckMissed(attack(true),LIFE,15,TARGET,1000,2.0,25,true),"creature died");
    assertFalse(OnlineStore.onlyDueCheckMissed(attack(false),LIFE,15,TARGET,1000,2.0,25,false),"position set without motion");
  }
}
