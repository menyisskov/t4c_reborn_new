package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.server.persistence.OnlineStore;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/** Local operator command. Creates a private profile without exposing keys in arguments or logs. */
final class OnlineProvisioner {
  static void provisionAccount(OnlineStore store,Path profile) throws Exception {
    privateFile(profile);
    boolean complete=false;
    try {
      var key=store.provisionAccount();
      String json=new Gson().toJson(Map.of("accountId",key.accountId(),"secret",key.secret()));
      Files.writeString(profile,json,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING);
      complete=true;
    } finally { if(!complete) Files.deleteIfExists(profile); }
  }

  static void provision(OnlineStore store, PreviewMap map, Path profile, String name,
      com.perso.T4C.helper.CharacterClass type, String gender, boolean evil) throws Exception {
    var prepared=OnlineCharacterCreator.prepare(type,gender,evil,new java.security.SecureRandom());
    privateFile(profile);
    boolean complete=false;
    try {
      var key=store.provisionAccount();
      var character=store.create(key.accountId(),name,0,map.originX+map.spawnX,map.originY+map.spawnY,
          prepared.progress(),prepared.gold());
      String json=new Gson().toJson(Map.of("accountId",key.accountId(),"characterId",character.id(),"secret",key.secret()));
      Files.writeString(profile,json,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING);
      complete=true;
    } finally { if (!complete) Files.deleteIfExists(profile); }
  }

  static String controlToken(Path file) throws Exception {
    privateFile(file);
    byte[] bytes=new byte[32]; new java.security.SecureRandom().nextBytes(bytes);
    String token=java.util.HexFormat.of().formatHex(bytes);
    Files.writeString(file,token,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING);
    return token;
  }

  private static void privateFile(Path profile) throws Exception {
    // CREATE_NEW prevents accidental replacement of an existing character's credentials.
    Files.createFile(profile);
    boolean complete=false;
    try {
      var acl=Files.getFileAttributeView(profile,AclFileAttributeView.class);
      if (acl!=null) {
        var entry=AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(acl.getOwner())
            .setPermissions(EnumSet.allOf(AclEntryPermission.class)).build();
        acl.setAcl(List.of(entry));
      } else if (Files.getFileAttributeView(profile,PosixFileAttributeView.class)!=null) {
        Files.setPosixFilePermissions(profile,PosixFilePermissions.fromString("rw-------"));
      } else throw new IllegalStateException("Private profile permissions are unavailable");
      complete=true;
    } finally { if (!complete) Files.deleteIfExists(profile); }
  }
}
