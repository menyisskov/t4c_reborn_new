package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.perso.T4C.server.persistence.OnlineStore;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

class OnlineAccountProvisioningTest {
  @TempDir Path directory;

  @Test void existingProfileIsNeverReplaced() throws Exception {
    var path=directory.resolve("account.json");
    Files.writeString(path,"existing private state");
    var unavailable=new OnlineStore(()-> { throw new AssertionError("Existing profile must be checked before DB access"); });
    assertThrows(java.nio.file.FileAlreadyExistsException.class,
        ()->OnlineProvisioner.provisionAccount(unavailable,path));
    assertEquals("existing private state",Files.readString(path));
  }

  @Test @EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
  void newPrivateProfileContainsAccountKeyOnlyAndListsEmptyRoster() throws Exception {
    var store=OnlineStore.fromEnvironment();store.migrate();
    var path=directory.resolve("new-account.json");
    OnlineProvisioner.provisionAccount(store,path);
    var profile=JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    assertEquals(2,profile.size());assertTrue(profile.has("secret"));assertTrue(profile.has("accountId"));
    assertFalse(profile.has("characterId"));
    assertTrue(store.roster(profile.get("secret").getAsString(),null).isEmpty());
    var posix=Files.getFileAttributeView(path,java.nio.file.attribute.PosixFileAttributeView.class);
    if(posix!=null) assertEquals(java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"),
        Files.getPosixFilePermissions(path));
  }
}
