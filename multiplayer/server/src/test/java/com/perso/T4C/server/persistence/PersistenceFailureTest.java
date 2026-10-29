package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PersistenceFailureTest {
  @Test void exposesOnlyTheFixedAllowlistedOperationLabels() {
    var labels=java.util.Arrays.stream(PersistenceFailure.Operation.values())
        .map(PersistenceFailure.Operation::label).collect(Collectors.toSet());
    assertEquals(Set.of("checkpoint","loot_claim","shop","corpse","transfer","monster_attack",
        "spell_creature_strike","world_reward","generic_event"),labels);
    assertEquals("checkpoint",PersistenceFailure.Operation.CHECKPOINT.label());
    assertEquals("generic_event",PersistenceFailure.Operation.GENERIC_EVENT.label());
  }

  @Test void formatsOperationAndSanitizedFailureCategoryWithoutExceptionText() {
    var formatted=PersistenceFailure.format(PersistenceFailure.Operation.TRANSFER,
        new SQLException("private JDBC URL and password", "40001",
            new SocketTimeoutException("private host and account")));
    assertTrue(formatted.contains("operation=transfer"));
    assertTrue(formatted.contains("classes=SQLException/SocketTimeoutException"));
    assertTrue(formatted.contains("sqlstate=40001"));
    assertFalse(formatted.contains("private"));
    assertFalse(formatted.contains("account"));
    assertFalse(formatted.contains("password"));
  }

}
