package com.perso.T4C.server.persistence;

import static org.junit.jupiter.api.Assertions.*;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class PersistenceFailureTest {
  @Test void reportsConnectionCauseAndStateWithoutDriverMessagesOrSqlValues() {
    var failure=new SQLException("private JDBC URL and password", "08001",new SocketTimeoutException("private host and account"));
    var category=PersistenceFailure.describe(failure);
    assertTrue(category.contains("SQLException/SocketTimeoutException"));assertTrue(category.contains("08001"));
    assertFalse(category.contains("private"));assertFalse(category.contains("password"));assertFalse(category.contains("account"));
  }
}
