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
 * <p>Scene {@code wares} (T4C-0312) puts a badly wounded, low-mana level-1 Healer (a heal spell, healing and mana potions, a staff and a
 * robe) and a level-1 Warrior carrying an over-weight stack within talking distance of the hosted shopkeeper Sigfried,
 * for the inventory, spell, potion, shop and walking-animation runs.
 *
 * <p>Usage: {@code LiveRunProvision COLLISION_FILE temple|skeletons|wares|quest|beta|mortal|lobby OUT_DIR [NAME_SUFFIX]}; the operator steps {@code lobby-leases}, {@code lobby-stale-move} and {@code lobby-created} take the same OUT_DIR.
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
    if ("mortal".equals(scene)) {
      // One level-8 warrior with 3 hit points beside a Skeleton anchor: dies within seconds (death and respawn diagnostics).
      Spot start = skeletonStart(plan, map);
      var kit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false, new Random(77));
      var vit = kit.progress().vitals();
      var weak = ProgressEdits.withVitals(kit.progress(), new CharacterProgress.Vitals(3, vit.maxHp(), vit.mana(), vit.maxMana()));
      try (var store = OnlineStore.fromEnvironment()) {
        makeFrom(store, out.resolve("player-a.json"), "Live Mortal" + (args.length > 3 ? args[3] : ""), start, weak, kit.gold(), 0);
        // makeFrom does not create the Bravo profile: the solo scene needs only one.
      }
      System.out.println("PROVISION_OK");
      return;
    }
    if ("beta".equals(scene)) {
      betaScene(plan, out, args.length > 3 ? args[3] : "");
      return;
    }
    if ("lobby".equals(scene)) {
      lobbyScene(map, out, args.length > 3 ? args[3] : "");
      return;
    }
    if ("lobby-leases".equals(scene) || "lobby-stale-move".equals(scene) || "lobby-created".equals(scene)) {
      lobbyDatabaseStep(scene, out);
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
   * T4C-0324 scene {@code lobby}: the accounts behind the rendered lobby error cases of milestone M1. Writes
   * {@code player-a.json} (account A: one ready warrior "Lobby Alpha" at the temple, an unhosted-region character, a
   * character with no respawn point and 22 never-initialised ones, so the roster needs two pages), {@code player-b.json}
   * (account B, one ready warrior), {@code player-c.json} (an account with no characters), {@code player-s.json}
   * (account S, one ready warrior that the operator later moves to account B while its lobby is open) and
   * {@code player-d.json} (a second empty account, for the creation form) and {@code player-badkey.json} (a well-formed key no account has). {@code stale.txt} carries the two non-secret ids the
   * operator needs for that move. Nothing is printed except non-secret labels.
   */
  private static void lobbyScene(PreviewMap map, Path out, String suffix) throws Exception {
    Spot temple = new Spot(map.originX + map.spawnX, map.originY + map.spawnY);
    try (var store = OnlineStore.fromEnvironment()) {
      var keyA = store.provisionAccount();
      var alpha = store.create(keyA.accountId(), "Lobby Alpha" + suffix, WORLD, temple.x(), temple.y(),
          readyKit("female", 1), readyGold());
      // Position (1,1) of world 0 is outside every hosted region: the roster calls it "Region offline".
      var kit = readyKit("male", 2);
      store.create(keyA.accountId(), "Lobby Remote" + suffix, WORLD, 1, 1, kit, readyGold());
      store.create(keyA.accountId(), "Lobby NoRespawn" + suffix, WORLD, temple.x(), temple.y(), withRespawn(kit, null), readyGold());
      for (int i = 0; i < 22; i++)
        store.create(keyA.accountId(), String.format("Lobby Filler %02d%s", i, suffix), WORLD, temple.x(), temple.y());
      writeProfile(out.resolve("player-a.json"), keyA.secret(), alpha.id());

      var keyB = store.provisionAccount();
      var bravo = store.create(keyB.accountId(), "Lobby Bravo" + suffix, WORLD, temple.x() + 1, temple.y(),
          readyKit("female", 3), readyGold());
      writeProfile(out.resolve("player-b.json"), keyB.secret(), bravo.id());

      var keyC = store.provisionAccount();
      writeProfile(out.resolve("player-c.json"), keyC.secret(), null);

      var keyS = store.provisionAccount();
      var stale = store.create(keyS.accountId(), "Lobby Stale" + suffix, WORLD, temple.x() + 2, temple.y(),
          readyKit("male", 4), readyGold());
      writeProfile(out.resolve("player-s.json"), keyS.secret(), stale.id());
      Files.writeString(out.resolve("stale.txt"), stale.id() + " " + keyB.accountId());

      writeProfile(out.resolve("player-d.json"), store.provisionAccount().secret(), null);

      byte[] random = new byte[32];
      new java.security.SecureRandom().nextBytes(random);
      writeProfile(out.resolve("player-badkey.json"), java.util.HexFormat.of().formatHex(random), UUID.randomUUID());
    }
    System.out.println("PROVISION_OK");
  }

  /**
   * Operator steps between the lobby cases. {@code lobby-leases} prints how many characters currently hold a session
   * lease (the lobby must hold none from selection alone). {@code lobby-created} prints what the in-lobby creation form
   * stored for the character it created (row count, gold, inventory stacks). {@code lobby-stale-move} moves the character in {@code stale.txt} to the other
   * account listed there (a plain account_id update while the server is idle for that character), so the open lobby's
   * roster is out of date.
   */
  private static void lobbyDatabaseStep(String scene, Path out) throws Exception {
    try (var db = DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"))) {
      if ("lobby-leases".equals(scene)) {
        try (var ps = db.prepareStatement(
                "SELECT count(*) FILTER (WHERE session_id IS NOT NULL AND session_until>clock_timestamp()), "
                    + "count(*) FILTER (WHERE session_id IS NOT NULL), count(*) FROM online_characters");
            var rows = ps.executeQuery()) {
          rows.next();
          System.out.println("LEASES live=" + rows.getLong(1) + " rowsWithSessionId=" + rows.getLong(2) + " characters=" + rows.getLong(3));
        }
        return;
      }
      if ("lobby-created".equals(scene)) {
        try (var ps = db.prepareStatement(
                "SELECT count(*), max(gold), max(jsonb_array_length(progress->'inventory')), "
                    + "count(*) FILTER (WHERE progress IS NOT NULL) FROM online_characters WHERE name='Lobby Cr'");
            var rows = ps.executeQuery()) {
          rows.next();
          System.out.println("CREATED rows=" + rows.getLong(1) + " gold=" + rows.getLong(2) + " inventoryStacks=" + rows.getLong(3)
              + " withProgress=" + rows.getLong(4));
        }
        return;
      }
      String[] ids = Files.readString(out.resolve("stale.txt")).trim().split(" ");
      try (var ps = db.prepareStatement("UPDATE online_characters SET account_id=?::uuid WHERE id=?::uuid AND session_id IS NULL")) {
        ps.setString(1, ids[1]);
        ps.setString(2, ids[0]);
        if (ps.executeUpdate() != 1) throw new IllegalStateException("the character could not be moved");
      }
      System.out.println("MOVED the stale character to the other account");
    }
  }

  private static CharacterProgress readyKit(String gender, long seed) {
    return OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, gender, false, new Random(seed)).progress();
  }

  private static long readyGold() {
    return OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(1)).gold();
  }

  private static CharacterProgress withRespawn(CharacterProgress q, CharacterProgress.Respawn respawn) {
    return new CharacterProgress(q.version(), q.characterClass(), q.gender(), q.attributes(), q.vitals(),
        q.advancement(), q.inventory(), q.equipment(), q.skills(), q.spells(), q.questFlags(), q.storage(),
        q.storageGold(), respawn, q.buffs(), q.cooldowns(), q.hiddenUntil(), q.companion());
  }

  private static void writeProfile(Path profile, String secret, UUID characterId) throws Exception {
    privateFile(profile);
    var json = new java.util.LinkedHashMap<String, Object>();
    json.put("secret", secret);
    if (characterId != null) json.put("characterId", characterId);
    Files.writeString(profile, new Gson().toJson(json), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  /**
   * T4C-0312 scene {@code wares}: a Healer at half health (so a heal and a healing potion visibly do something)
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

  /**
   * T4C-0316 scene {@code beta}: two level-30 warriors beside WardenAelric for the long mixed session (quest topic
   * panel, equip observed by the other window, loot, portals, soak). The second one (Bravo) carries a filler stack of a
   * one-weight item sized so that exactly {@link #BRAVO_HEADROOM} weight units are left: a Skeleton drop heavier than
   * that (Leather Gloves, Skeleton Bone: weight 3) is refused for Bravo as too heavy while Alpha, who carries only the
   * starting kit, can take it, and the lighter drops can still be raced for. Nothing the server enforces is changed.
   */
  static final long BRAVO_HEADROOM = 2;

  private static void betaScene(HostedRegionPlan plan, Path out, String suffix) throws Exception {
    Spot a = besideNpc(plan, "WardenAelric");
    Spot b = nearby(plan, a, 2, 1);
    var npc = NpcInteractionCatalog.loadBundled().require("WardenAelric");
    System.out.println("scene=beta alpha=(" + a.x() + "," + a.y() + ") bravo=(" + b.x() + "," + b.y() + ") warden=(" + npc.x() + "," + npc.y() + ")");
    var items = ItemCatalog.loadBundled();
    // The tile the driver walks to for the Skeleton fights (the same anchor rule as the loot probe).
    PreviewMap map = plan.regions().stream().filter(r -> r.spec().world() == WORLD).findFirst().orElseThrow().map();
    Spot ground = skeletonStart(plan, map);
    System.out.println("skeletonGround=" + (int) Math.floor(ground.x()) + "," + (int) Math.floor(ground.y()));
    try (var store = OnlineStore.fromEnvironment()) {
      makeWithHeadroom(store, out.resolve("player-a.json"), "Live Alpha" + suffix, a, 30, items, -1);
      makeWithHeadroom(store, out.resolve("player-b.json"), "Live Bravo" + suffix, b, 30, items, BRAVO_HEADROOM);
    }
    System.out.println("PROVISION_OK");
  }

  private static void makeWithHeadroom(OnlineStore store, Path profile, String name, Spot at, int level,
      com.perso.T4C.online.model.ItemContent content, long headroom) throws Exception {
    // Level the character exactly as make() does, then add the filler stack before it is created.
    var kit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false, new Random(name.hashCode()));
    CharacterProgress progress = kit.progress();
    long experience = 0;
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
    // Both beta characters carry twenty Healing Potions so a twenty-minute fight does not end in an incidental death.
    var stocked = new java.util.ArrayList<>(progress.inventory());
    stocked.add(new CharacterProgress.Item(UUID.randomUUID(), "item.healing_potion", 20, 0));
    {
      var q = progress;
      progress = new CharacterProgress(q.version(), q.characterClass(), q.gender(), q.attributes(), q.vitals(),
          q.advancement(), stocked, q.equipment(), q.skills(), q.spells(), q.questFlags(), q.storage(),
          q.storageGold(), q.respawn(), q.buffs(), q.cooldowns(), q.hiddenUntil(), q.companion());
    }
    if (headroom < 0) {
      finish(store, profile, name, at, progress, kit.gold(), experience, "no filler stack");
      return;
    }
    var filler = content.items().values().stream()
        .filter(d -> d.weight() == 1 && !d.unique() && !d.undroppable())
        .min(java.util.Comparator.comparing(com.perso.T4C.online.model.ItemContent.Definition::key))
        .orElseThrow();
    long maximum = com.perso.T4C.item.CarryCapacityRules.maximumWeight(OnlinePickupRules.effectiveStrength(progress, content, 0L));
    long carried = OnlinePickupRules.carriedWeight(progress, content);
    long quantity = maximum - carried - headroom;
    if (quantity < 1 || quantity > Integer.MAX_VALUE) throw new IllegalStateException("cannot size the filler stack: " + quantity);
    var inventory = new java.util.ArrayList<>(progress.inventory());
    inventory.add(new CharacterProgress.Item(UUID.randomUUID(), filler.key(), (int) quantity, 0));
    var p = progress;
    progress = new CharacterProgress(p.version(), p.characterClass(), p.gender(), p.attributes(), p.vitals(),
        p.advancement(), inventory, p.equipment(), p.skills(), p.spells(), p.questFlags(), p.storage(),
        p.storageGold(), p.respawn(), p.buffs(), p.cooldowns(), p.hiddenUntil(), p.companion());
    System.out.println("filler " + filler.key() + " x" + quantity + " maximum " + maximum + " carried before " + carried + " headroom " + headroom);
    finish(store, profile, name, at, progress, kit.gold(), experience, headroom + " weight units of headroom");
  }

  private static void finish(OnlineStore store, Path profile, String name, Spot at, CharacterProgress progress, long gold,
      long experience, String note) throws Exception {
    privateFile(profile);
    var key = store.provisionAccount();
    var created = store.create(key.accountId(), name, WORLD, at.x(), at.y(), progress, gold);
    try (var db = DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"));
        var ps = db.prepareStatement("UPDATE online_characters SET experience=? WHERE id=? AND revision=0")) {
      ps.setLong(1, experience);
      ps.setObject(2, created.id());
      if (ps.executeUpdate() != 1) throw new IllegalStateException("experience seed did not apply");
    }
    String json = new Gson().toJson(Map.of("accountId", key.accountId(), "characterId", created.id(), "secret", key.secret()));
    Files.writeString(profile, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    System.out.println("provisioned " + name + " level " + progress.advancement().level() + " with " + note);
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
