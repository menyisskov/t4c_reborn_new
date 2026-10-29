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
 * <p>Usage: {@code LiveRunProvision COLLISION_FILE temple|skeletons OUT_DIR [NAME_SUFFIX]}.
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
