package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcSpawnOwnership;
import com.perso.T4C.npc.core.NPCAnimations;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.online.model.NpcAppearanceContent;
import com.perso.T4C.spawn.SpawnRegistry;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;

class OnlineMonsterContentExporterTest {
  @TempDir Path temporary;

  private static final String[] NPC_ANGLES={"000","045","090","135","180"};

  @Test void npcFrameMetadataMatchesStaticSingleBaseAndCompositeLoaderRules() {
    assertTrue(NPCAnimations.hasRequiredFrameNames("@static:Horloge Solaire",Map.of(),
        Set.of(SpriteBinIO.key("Horloge Solaire"))));
    assertFalse(NPCAnimations.hasRequiredFrameNames("@static:Missing",Map.of(),Set.of()));
    var single=new HashSet<String>();
    for(String angle:NPC_ANGLES) single.add(SpriteBinIO.key("Npc"+angle+"-a"));
    assertTrue(NPCAnimations.hasRequiredFrameNames("Npc",Map.of(),single));
    single.remove(SpriteBinIO.key("Npc180-a"));
    assertFalse(NPCAnimations.hasRequiredFrameNames("Npc",Map.of(),single));
    var composite=new HashSet<String>();
    var effective=NPCAnimations.effectiveCompositeParts(Map.of(BodyPart.HEAD,"CustomHead"));
    for(String base:effective.values()) for(String angle:NPC_ANGLES)
      composite.add(SpriteBinIO.key(base+angle+"-c"));
    assertTrue(NPCAnimations.hasRequiredFrameNames(null,
        Map.of(BodyPart.HEAD,"CustomHead"),composite));
    composite.remove(SpriteBinIO.key("CustomHead180-c"));
    assertFalse(NPCAnimations.hasRequiredFrameNames(null,
        Map.of(BodyPart.HEAD,"CustomHead"),composite));
    composite.add(SpriteBinIO.key("CustomHead180"));
    assertTrue(NPCAnimations.hasRequiredFrameNames(null,
        Map.of(BodyPart.HEAD,"CustomHead"),composite));
  }

  @ParameterizedTest
  @ValueSource(strings = {"monsters", "items", "portals", "npc-interactions",
      "monsters-v2", "npc-appearances"})
  void packagedContentMatchesActualRuntimeDefinitions(String kind) throws Exception {
    String exporter = switch(kind) {
      case "monsters" -> OnlineMonsterContentExporter.class.getName();
      case "monsters-v2" -> OnlineMonsterContentExporter.class.getName();
      case "npc-appearances" -> OnlineNpcAppearanceExporter.class.getName();
      case "items" -> OnlineItemContentExporter.class.getName();
      case "npc-interactions" -> OnlineNpcContentExporter.class.getName();
      default -> OnlinePortalContentExporter.class.getName();
    };
    String file=kind.equals("monsters-v2") ? kind+".json" : kind+"-v1.json";
    String actual=Files.readString(Path.of("multiplayer/server/src/main/resources/content/" + file))
        .replace("\r\n","\n");
    // Other tests customize static registries. Export in a fresh production-classpath JVM so
    // freshness depends on shipped content, never test ordering or test-only annotated spawns.
    String tests=Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
    String classpath=java.util.Arrays.stream(System.getProperty("surefire.test.class.path",System.getProperty("java.class.path"))
        .split(java.util.regex.Pattern.quote(java.io.File.pathSeparator)))
        .filter(entry -> !Path.of(entry).toAbsolutePath().normalize().equals(Path.of(tests).toAbsolutePath().normalize()))
        .collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator));
    String executable=System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
    Path generated=temporary.resolve(kind + ".json"), log=temporary.resolve("export.log");
    var command=new ArrayList<String>(List.of(Path.of(System.getProperty("java.home"),"bin",executable).toString(),
        "-cp",classpath,exporter));
    if(kind.equals("monsters-v2")) command.add("--classified");
    command.add(generated.toString());
    var process=new ProcessBuilder(command)
        .redirectErrorStream(true).redirectOutput(log.toFile()).start();
    try {
      assertTrue(process.waitFor(30,TimeUnit.SECONDS),"Content exporter timed out");
      String output=Files.readString(log);
      assertEquals(0,process.exitValue(),output.substring(Math.max(0,output.length()-2000)));
    } finally {
      if (process.isAlive()) { process.destroyForcibly(); process.waitFor(5,TimeUnit.SECONDS); }
    }
    String expected=Files.readString(generated);
    if (!digest(expected).equals(digest(actual))) {
      var report=Path.of("target/surefire-reports/online-" + kind + "-runtime.json");
      Files.createDirectories(report.getParent());
      Files.writeString(report,expected);
    }
    assertEquals(digest(expected),digest(actual),
        "Online content is stale: run " + exporter + " for the bundled JSON");
  }

  private static String digest(String value) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
  }

  @Test void classifiedMonsterAndNpcInventoriesReconcileWithEverySourceRecord() throws Exception {
    var gson=new Gson();
    var classified=gson.fromJson(Files.readString(Path.of(
        "multiplayer/server/src/main/resources/content/monsters-v2.json")),MonsterContent.Classified.class);
    var appearances=gson.fromJson(Files.readString(Path.of(
        "multiplayer/server/src/main/resources/content/npc-appearances-v1.json")),NpcAppearanceContent.class);
    var monsterKeys=SpawnRegistry.monsters().stream().map(s->new MonsterContent.Spawn(s.type(),null,
        s.x(),s.y(),s.z(),s.stationary(),s.aggressive()).persistenceKey()).collect(java.util.stream.Collectors.toSet());
    assertEquals(monsterKeys.size(),classified.spawns().size());
    assertEquals(monsterKeys,classified.spawns().stream().map(MonsterContent.ClassifiedSpawn::persistenceKey)
        .collect(java.util.stream.Collectors.toSet()));
    var npcKeys=java.util.stream.Stream.concat(SpawnRegistry.npcs().stream(),SpawnRegistry.monsters().stream()
        .filter(s->NpcSpawnOwnership.owns(s.type()))).map(s->new MonsterContent.Spawn(s.type(),null,
        s.x(),s.y(),s.z(),s.stationary(),s.aggressive()).persistenceKey()).collect(java.util.stream.Collectors.toSet());
    assertEquals(npcKeys,appearances.spawns().stream().map(NpcAppearanceContent.Spawn::spawnKey)
        .collect(java.util.stream.Collectors.toSet()));
    var hosted=appearances.spawns().stream().filter(s->s.world()==0
        && s.x()>=2735 && s.x()<2984 && s.y()>=1029 && s.y()<1146).toList();
    assertFalse(hosted.isEmpty());
    assertTrue(hosted.stream().allMatch(s->s.deployable()
        && ("@invisible".equals(s.spriteBase())
            || s.frameEvidence()==NpcAppearanceContent.FrameEvidence.FRAME_NAME_PRESENT)),
        "Hosted authored NPCs need explicit sprite-name evidence");
    var sundial=classified.spawns().stream().filter(s->s.world()==0 && s.x()==2881 && s.y()==1070).findFirst().orElseThrow();
    assertEquals(MonsterContent.Ownership.NPC,sundial.ownership());
    assertEquals("Sundial",sundial.canonicalNpcId());
    assertNull(sundial.definition());
    var overlap=classified.spawns().stream().filter(s->"SKRAUGGRUBBRINGAH".equals(s.type()))
        .findFirst().orElseThrow();
    assertEquals(MonsterContent.Ownership.MONSTER,overlap.ownership());
    assertEquals(MonsterRegistry.findByName(overlap.type()).getName(),overlap.definition());
    assertEquals(NpcSpawnOwnership.resolveStrict(overlap.type()).id(),overlap.canonicalNpcId());
    assertTrue(appearances.spawns().stream().anyMatch(s->s.spawnKey().equals(overlap.persistenceKey())
        && s.canonicalNpcId().equals(overlap.canonicalNpcId())));
    var appearance=appearances.spawns().stream().filter(s->s.spawnKey().equals(sundial.persistenceKey()))
        .findFirst().orElseThrow();
    assertEquals("@static:Horloge Solaire",appearance.spriteBase());
    assertEquals(NpcAppearanceContent.FrameEvidence.FRAME_NAME_PRESENT,appearance.frameEvidence());
    assertFalse(appearance.interactionSupported());
    assertTrue(appearance.deployable());
    assertEquals(MonsterContent.Clan.GOBLIN,classified.authorities().get("Goblin").clan());
    assertEquals(MonsterContent.AttackFrameEvidence.FRAME_NAME_PRESENT,
        classified.authorities().get("Goblin").attackFrameEvidence());
    assertEquals(MonsterContent.Clan.HORSE,classified.authorities().get("Wild Horse").clan());
    assertEquals(MonsterContent.AttackFrameEvidence.NO_FRAME,
        classified.authorities().get("Wild Horse").attackFrameEvidence());
    assertTrue(classified.relations().contains(new MonsterContent.ClanRelation(
        MonsterContent.Clan.GOBLIN,MonsterContent.Clan.HORSE)));
    assertFalse(classified.relations().contains(new MonsterContent.ClanRelation(
        MonsterContent.Clan.HORSE,MonsterContent.Clan.GOBLIN)));
  }
}
