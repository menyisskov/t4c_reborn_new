package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.player.PlayerProgression;
import com.perso.T4C.server.persistence.OnlineStore;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.DriverManager;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Operator tool for the live RENDERED two-client runs (T4C-0306). It provisions two disposable
 * accounts, one warrior each, into the database named by T4C_DB_URL / T4C_DB_USER / T4C_DB_PASSWORD
 * (the server must have migrated that database first) and writes two private profile files that
 * {@code OnlineLiveRunDriver} loads. Nothing is printed except non-secret labels.
 *
 * <p>Scenes: {@code temple} puts both level-1 warriors at the authored spawn (Kilhiam's temple);
 * {@code skeletons} puts both level-8 warriors beside an authored Skeleton anchor of the
 * {@code renewed_wards} circle, found the same way {@code NativeLootProbe} finds it. Level 8 is the
 * probe's headroom choice (twice the Skeleton level), reached through the server's own XP award path.
 *
 * <p>Scene {@code wares} (T4C-0311) puts a badly wounded, low-mana level-1 Healer (a heal spell, healing and mana potions, a staff and a
 * robe) and a level-1 Warrior carrying an over-weight stack within talking distance of the hosted shopkeeper Sigfried,
 * for the inventory, spell, potion, shop and walking-animation runs.
 *
 * <p>Usage: {@code LiveRunProvision COLLISION_FILE temple|skeletons|wares OUT_DIR [NAME_SUFFIX]}.
 */
public final class LiveRunProvision {
  private static final int WORLD = 0;
  private static final String QUEST_ID = "renewed_wards", MONSTER = "Skeleton";

  private record Spot(double x, double y) {}

  public static void main(String[] args) throws Exception {
    Path collision = Path.of(args[0]);
    String scene = args[1];
    Path out = Path.of(args[2]).toAbsolutePath();
    Files.createDirectories(out);
    var plan = HostedRegionPlan.startingSlice(collision);
    PreviewMap map = plan.regions().stream().filter(r -> r.spec().world() == WORLD).findFirst().orElseThrow().map();
    Spot a, b;
    int level;
    if ("quest".equals(scene)) {
      questScene(plan, out, args.length > 3 ? args[3] : "");
      return;
    }
    if ("move-skeletons".equals(scene) || "move-giver".equals(scene)) {
      move(plan, map, out, scene);
      return;
    }
    if ("wares".equals(scene)) {
      waresScene(plan, out, args.length > 3 ? args[3] : "");
      return;
    }
    if ("temple".equals(scene)) {
      a = new Spot(map.originX + map.spawnX, map.originY + map.spawnY);
      level = 1;
      b = nearby(plan, a, 3, 0);
    } else if ("skeletons".equals(scene)) {
      a = skeletonStart(plan, map);
      level = Math.max(1, MonsterCatalog.loadBundled().monsters().get(MONSTER).getLevel() * 2);
      b = nearby(plan, a, 4, 1);
    } else throw new IllegalArgumentException("scene must be temple or skeletons");
    System.out.println("scene=" + scene + " level=" + level + " A=(" + a.x() + "," + a.y() + ") B=(" + b.x() + "," + b.y() + ")");
    try (var store = OnlineStore.fromEnvironment()) {
      String suffix = args.length > 3 ? args[3] : "";
      make(store, out.resolve("player-a.json"), "Live Alpha" + suffix, a, level);
      make(store, out.resolve("player-b.json"), "Live Bravo" + suffix, b, level);
    }
    System.out.println("PROVISION_OK");
  }

  /**
   * T4C-0311 scene {@code wares}: a Healer at half health (so a heal and a healing potion visibly do something)
   * and a Warrior carrying one over-weight stack, both within talking distance of Sigfried's shop.
   */
  private static void waresScene(HostedRegionPlan plan, Path out, String suffix) throws Exception {
    var npc = NpcInteractionCatalog.loadBundled().require("Sigfried");
    var region = plan.require(npc.world(), npc.x(), npc.y());
    var map = region.map();
    Spot a = null;
    search:
    for (int d = 1; d <= 3; d++)
      for (int dx = -d; dx <= d; dx++)
        for (int dy = -d; dy <= d; dy++) {
          double x = npc.x() + dx + 0.5, y = npc.y() + dy + 0.5;
          if (standable(plan, x, y) && map.canStand(x - map.originX, y - map.originY)) {
            a = new Spot(x, y);
            break search;
          }
        }
    if (a == null) throw new IllegalStateException("no standable tile beside Sigfried");
    Spot b = nearby(plan, a, 2, 1);
    System.out.println("scene=wares healer=(" + a.x() + "," + a.y() + ") warrior=(" + b.x() + "," + b.y() + ") sigfried=(" + npc.x() + "," + npc.y() + ")");
    var items = ItemCatalog.loadBundled();
    try (var store = OnlineStore.fromEnvironment()) {
      var healer = OnlineCharacterCreator.prepare(CharacterClass.HEALER, "female", false, new Random(11));
      var hp = healer.progress().vitals();
      var wounded = ProgressEdits.withVitals(healer.progress(),
          new CharacterProgress.Vitals(Math.min(3, hp.maxHp()), hp.maxHp(), Math.min(5, hp.maxMana()), hp.maxMana()));
      makeFrom(store, out.resolve("player-a.json"), "Live Healer" + suffix, a, wounded, healer.gold(), 0);
      var warrior = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(12));
      var heavy = OnlineOverweightFixture.apply(warrior.progress(), OnlineOverweightFixture.size(warrior.progress(), items));
      makeFrom(store, out.resolve("player-b.json"), "Live Walker" + suffix, b, heavy, warrior.gold(), 0);
    }
    System.out.println("PROVISION_OK");
  }

  /** A standable tile within three tiles of the authored NPC (talking distance). */
  private static Spot besideNpc(HostedRegionPlan plan, String id) throws Exception {
    var npc = NpcInteractionCatalog.loadBundled().require(id);
    var map = plan.require(npc.world(), npc.x(), npc.y()).map();
    for (int d = 1; d <= 3; d++)
      for (int dx = -d; dx <= d; dx++)
        for (int dy = -d; dy <= d; dy++) {
          double x = npc.x() + dx + 0.5, y = npc.y() + dy + 0.5;
          if (standable(plan, x, y) && map.canStand(x - map.originX, y - map.originY)) return new Spot(x, y);
        }
    throw new IllegalStateException("no standable tile beside " + id);
  }

  /**
   * Scene {@code quest}: a level-30 warrior (the quester; a level-12 one lost 100 of its 119 HP killing three Skeletons) and a level-1 warrior (an observer) beside WardenAelric.
   * Scenes {@code move-skeletons} and {@code move-giver} relocate the quester's saved position while the server is
   * stopped, because the Skeleton ground is about 160 tiles from the giver behind cave mouths (walking that is the
   * soak's job). The move is a plain position update on the character row; nothing else is touched.
   */
  private static void questScene(HostedRegionPlan plan, Path out, String suffix) throws Exception {
    Spot a = besideNpc(plan, "WardenAelric");
    Spot b = nearby(plan, a, 2, 1);
    System.out.println("scene=quest quester=(" + a.x() + "," + a.y() + ") observer=(" + b.x() + "," + b.y() + ")");
    try (var store = OnlineStore.fromEnvironment()) {
      make(store, out.resolve("player-a.json"), "Live Quester" + suffix, a, 30);
      make(store, out.resolve("player-b.json"), "Live Observer" + suffix, b, 1);
    }
    System.out.println("PROVISION_OK");
  }

  private static void move(HostedRegionPlan plan, PreviewMap map, Path out, String scene) throws Exception {
    Spot to = "move-skeletons".equals(scene) ? skeletonStart(plan, map) : besideNpc(plan, "WardenAelric");
    var profile = com.google.gson.JsonParser.parseString(Files.readString(out.resolve("player-a.json"))).getAsJsonObject();
    try (var db = DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
        var ps = db.prepareStatement("UPDATE online_characters SET x=?, y=?, revision=revision+1 WHERE id=?::uuid")) {
      ps.setDouble(1, to.x());
      ps.setDouble(2, to.y());
      ps.setString(3, profile.get("characterId").getAsString());
      if (ps.executeUpdate() != 1) throw new IllegalStateException("relocation did not apply");
    }
    System.out.println("relocated the quester to (" + to.x() + "," + to.y() + ") for " + scene);
    System.out.println("PROVISION_OK");
  }

  private static void makeFrom(OnlineStore store, Path profile, String name, Spot at, CharacterProgress progress, long gold, long experience)
      throws Exception {
    privateFile(profile);
    var key = store.provisionAccount();
    var created = store.create(key.accountId(), name, WORLD, at.x(), at.y(), progress, gold);
    String json = new Gson().toJson(Map.of("accountId", key.accountId(), "characterId", created.id(), "secret", key.secret()));
    Files.writeString(profile, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    System.out.println("provisioned " + name + " class " + progress.characterClass() + " gold " + gold);
  }

  private static void make(OnlineStore store, Path profile, String name, Spot at, int level) throws Exception {
    var kit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false, new Random(name.hashCode()));
    CharacterProgress progress = kit.progress();
    long experience = 0;
    if (level > 1) {
      XpCurve curve = XpCurve.loadDefault();
      double rate = PlayerProgression.scaledAward(1_000_000L, 0f, false) / 1_000_000d;
      Random random = new Random(level * 7919L + name.hashCode());
      for (int i = 0; i < 64 && progress.advancement().level() < level; i++) {
        long remaining = curve.getTotalXp(level) - experience;
        long base = Math.max(1L, (long) Math.ceil(remaining / rate));
        var state = new OnlineStore.CharacterState(UUID.randomUUID(), UUID.randomUUID(), name, WORLD, 0, 0, 0, experience, 0, progress);
        var award = OnlineProgression.award(state, base, 0, random);
        if (award.experience() <= 0) break;
        experience += award.experience();
        progress = award.progress();
      }
      if (progress.advancement().level() < level) throw new IllegalStateException("could not reach level " + level);
    }
    privateFile(profile);
    var key = store.provisionAccount();
    var created = store.create(key.accountId(), name, WORLD, at.x(), at.y(), progress, kit.gold());
    if (experience > 0) {
      try (var db = DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
          var ps = db.prepareStatement("UPDATE online_characters SET experience=? WHERE id=? AND revision=0")) {
        ps.setLong(1, experience);
        ps.setObject(2, created.id());
        if (ps.executeUpdate() != 1) throw new IllegalStateException("experience seed did not apply");
      }
    }
    String json = new Gson().toJson(Map.of("accountId", key.accountId(), "characterId", created.id(), "secret", key.secret()));
    Files.writeString(profile, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    System.out.println("provisioned " + name + " level " + progress.advancement().level());
  }

  private static boolean standable(HostedRegionPlan plan, double wx, double wy) {
    if (!plan.canStand(new CharacterProgress.Respawn(WORLD, wx, wy))) return false;
    return !(plan.isRefusedPortalSource(WORLD, wx, wy) || plan.portals.resolve(WORLD, wx, wy, plan::canStand).isPresent());
  }

  private static Spot nearby(HostedRegionPlan plan, Spot a, int dx, int dy) {
    for (int r = 0; r < 6; r++)
      for (int i = -r; i <= r; i++)
        for (int j = -r; j <= r; j++) {
          double x = a.x() + dx + i, y = a.y() + dy + j;
          if ((Math.abs(x - a.x()) >= 1 || Math.abs(y - a.y()) >= 1) && standable(plan, x, y)) return new Spot(x, y);
        }
    throw new IllegalStateException("no standable tile beside the first character");
  }

  private static Spot skeletonStart(HostedRegionPlan plan, PreviewMap map) throws Exception {
    var quest = QuestCatalog.bundled().definition(QUEST_ID);
    var skeletons = MonsterCatalog.loadBundled().spawns().stream()
        .filter(s -> MONSTER.equals(s.definition()) && s.world() == WORLD
            && Math.abs(s.x() - quest.getAreaCenterX()) <= quest.getAreaRadiusTiles()
            && Math.abs(s.y() - quest.getAreaCenterY()) <= quest.getAreaRadiusTiles())
        .sorted(java.util.Comparator.comparingInt((com.perso.T4C.online.model.MonsterContent.Spawn s) -> s.x()).thenComparingInt(s -> s.y()))
        .toList();
    for (var anchor : skeletons) {
      Spot best = null;
      double bestDistance = Double.MAX_VALUE;
      for (int dy = -2; dy <= 2; dy++)
        for (int dx = -2; dx <= 2; dx++) {
          if (dx == 0 && dy == 0) continue;
          double wx = anchor.x() + dx + .5, wy = anchor.y() + dy + .5;
          if (!standable(plan, wx, wy)) continue;
          double distance = Math.hypot(dx * (double) GameConstants.GRID_W, dy * (double) GameConstants.GRID_H);
          if (distance > GameConstants.PLAYER_MELEE_ATTACK_RANGE) continue;
          if (!map.allowsCombat(wx - map.originX, wy - map.originY, anchor.x() - map.originX + .5, anchor.y() - map.originY + .5, false)) continue;
          if (distance < bestDistance) {
            bestDistance = distance;
            best = new Spot(wx, wy);
          }
        }
      if (best != null) return best;
    }
    throw new IllegalStateException("no Skeleton anchor with a melee-reachable standable tile");
  }

  private static void privateFile(Path profile) throws Exception {
    Files.createFile(profile);
    var acl = Files.getFileAttributeView(profile, AclFileAttributeView.class);
    if (acl != null) {
      var entry = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(acl.getOwner())
          .setPermissions(EnumSet.allOf(AclEntryPermission.class)).build();
      acl.setAcl(List.of(entry));
    } else if (Files.getFileAttributeView(profile, PosixFileAttributeView.class) != null) {
      Files.setPosixFilePermissions(profile, PosixFilePermissions.fromString("rw-------"));
    } else throw new IllegalStateException("Private profile permissions are unavailable");
  }
}
