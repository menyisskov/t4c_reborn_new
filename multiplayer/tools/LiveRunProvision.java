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
    if ("m4".equals(scene)) {
      m4Scene(plan, map, out, args.length > 3 ? args[3] : "");
      return;
    }
    if ("m4-report".equals(scene)) {
      m4Report();
      return;
    }
    if ("accept-report".equals(scene)) {
      acceptReport();
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
   * T4C-0325 scene {@code lobby}: the accounts behind the rendered lobby error cases of milestone M1. Writes
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
      makeWithHeadroom(store, out.resolve("player-b.json"), "Live Bravo" + suffix, b, 30, items,
          Boolean.getBoolean("beta.nofiller") ? -1 : BRAVO_HEADROOM);
    }
    System.out.println("PROVISION_OK");
  }

  /**
   * T4C-0331 scene {@code m4}: two characters of DIFFERENT roles beside a Skeleton anchor, for the combat-and-ability
   * run. {@code -Dm4.pair=warrior-healer} (default) gives Alpha a level-25 Warrior (ten Healing Potions, an unequipped
   * Ashwood Reflex Bow for the unsupported-combat refusal) and Bravo a level-25 Healer (Heal Light, Stone Shard, plus
   * Protection, a timed buff, written into the known spells as an operator edit: the offline game teaches it at a
   * spell merchant). {@code -Dm4.pair=paladin-mage} gives Alpha a Paladin and Bravo a Mage. Levels come through the
   * server's own XP award path, exactly like the other scenes; nothing the server enforces is bypassed.
   */
  private static void m4Scene(HostedRegionPlan plan, PreviewMap map, Path out, String suffix) throws Exception {
    String pair = System.getProperty("m4.pair", "warrior-healer");
    CharacterClass first = pair.startsWith("paladin") ? CharacterClass.PALADIN
        : pair.startsWith("archer") ? CharacterClass.ARCHER : CharacterClass.WARRIOR;
    CharacterClass second = pair.endsWith("mage") ? CharacterClass.MAGE : CharacterClass.HEALER;
    Spot a = skeletonStart(plan, map);
    Spot b = nearby(plan, a, 4, 1);
    System.out.println("scene=m4 pair=" + pair + " alpha=(" + a.x() + "," + a.y() + ") bravo=(" + b.x() + "," + b.y() + ")");
    System.out.println("skeletonGround=" + (int) Math.floor(a.x()) + "," + (int) Math.floor(a.y()));
    int level = Integer.getInteger("m4.level", 25);
    try (var store = OnlineStore.fromEnvironment()) {
      var alpha = levelled(first, "male", "Live " + first.id() + suffix, level);
      var bravo = levelled(second, "female", "Live " + second.id() + suffix, level);
      var alphaProgress = withPotions(alpha.progress(), first == CharacterClass.WARRIOR);
      var bravoProgress = withPotions(bravo.progress(), false);
      if (second == CharacterClass.HEALER) {
        var spells = new java.util.HashSet<>(bravoProgress.spells());
        spells.add("spell.protection");
        bravoProgress = ProgressEdits.withSpells(bravoProgress, spells);
        // The Healer starts at 40 percent health so the self heal visibly restores something.
        var vit = bravoProgress.vitals();
        bravoProgress = ProgressEdits.withVitals(bravoProgress,
            new CharacterProgress.Vitals(Math.max(1, vit.maxHp() * 40 / 100), vit.maxHp(), vit.mana(), vit.maxMana()));
      } else if (second == CharacterClass.MAGE) {
        // Fireball (a stage-3 area attack) is written into the Mage's spells as an operator edit; the offline game teaches it at a merchant.
        var spells = new java.util.HashSet<>(bravoProgress.spells());
        spells.add("spell.fireball");
        bravoProgress = ProgressEdits.withSpells(bravoProgress, spells);
      }
      Integer alphaHp = Integer.getInteger("m4.alphaHp");
      if (alphaHp != null) {
        // Death run only: the first character starts nearly dead and carries a big stack, so the death happens within seconds and
        // the corpse certainly holds something (each stack drops a binomial share on death).
        var vit = alphaProgress.vitals();
        alphaProgress = ProgressEdits.withVitals(alphaProgress,
            new CharacterProgress.Vitals(Math.min(alphaHp, vit.maxHp()), vit.maxHp(), vit.mana(), vit.maxMana()));
        var inventory = new java.util.ArrayList<>(alphaProgress.inventory());
        inventory.add(new CharacterProgress.Item(UUID.randomUUID(), "item.torch", Integer.getInteger("m4.alphaStack", 120), 0));
        alphaProgress = ProgressEdits.withInventory(alphaProgress, inventory);
      }
      if (pair.startsWith("archer") && second == CharacterClass.HEALER) {
        // A flag of a quest the online game does not run yet: combat for this character must be refused, never silently ignored.
        var flags = new java.util.HashMap<>(bravoProgress.questFlags());
        flags.put("unimplemented.quest", 1);
        bravoProgress = ProgressEdits.withFlags(bravoProgress, flags);
      }
      System.out.println("alpha " + first.id() + " attributes " + alphaProgress.attributes() + " vitals " + alphaProgress.vitals()
          + " spells " + alphaProgress.spells());
      System.out.println("bravo " + second.id() + " attributes " + bravoProgress.attributes() + " vitals " + bravoProgress.vitals()
          + " spells " + bravoProgress.spells());
      finish(store, out.resolve("player-a.json"), "Live " + first.id() + suffix, a, alphaProgress, alpha.gold(), alpha.experience(), "role " + first.id());
      finish(store, out.resolve("player-b.json"), "Live " + second.id() + suffix, b, bravoProgress, bravo.gold(), bravo.experience(), "role " + second.id());
    }
    System.out.println("PROVISION_OK");
  }

  private record Levelled(CharacterProgress progress, long experience, long gold) {}

  private static Levelled levelled(CharacterClass role, String gender, String name, int level) {
    var kit = OnlineCharacterCreator.prepare(role, gender, false, new Random(name.hashCode()));
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
    return new Levelled(progress, experience, kit.gold());
  }

  /** Ten Healing Potions with the item's one charge (zero charges are refused by design) and, optionally, a spare bow. */
  private static CharacterProgress withPotions(CharacterProgress progress, boolean bow) {
    var inventory = new java.util.ArrayList<>(progress.inventory());
    inventory.add(new CharacterProgress.Item(UUID.randomUUID(), "item.healing_potion", 10, 1));
    if (bow) inventory.add(new CharacterProgress.Item(UUID.randomUUID(), "item.ashwood_reflex_bow_1", 1, 0));
    return ProgressEdits.withInventory(progress, inventory);
  }

  /** Non-secret rows for the evidence notes: who was rewarded for which life, who owned and claimed which drop, the corpses. */
  private static void m4Report() throws Exception {
    try (var db = DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"))) {
      report(db, "rewards (lethal claimant per creature life)",
          "SELECT c.name, r.experience, r.gold, to_char(r.created_at,'HH24:MI:SS') FROM online_world_rewards w "
              + "JOIN online_rewards r ON r.event_id=w.event_id AND r.character_id=w.character_id "
              + "JOIN online_characters c ON c.id=w.character_id ORDER BY r.created_at");
      report(db, "ground loot (item, state, owner, claimed by)",
          "SELECT l.item_key, l.state, o.name, coalesce(cb.name,'-'), (SELECT count(*) FROM online_loot_claims k WHERE k.loot_id=l.id) "
              + "FROM online_ground_loot l JOIN online_characters o ON o.id=l.owner_character "
              + "LEFT JOIN online_characters cb ON cb.id=l.claimed_by ORDER BY l.created_at");
      report(db, "corpses (character, stacks on it, recovered)",
          "SELECT c.name, jsonb_array_length(k.items), k.recovered, to_char(k.created_at,'HH24:MI:SS') FROM online_corpses k "
              + "JOIN online_characters c ON c.id=k.character_id ORDER BY k.created_at");
      report(db, "creature lives with damage (spawn key tail, health, mode, target)",
          "SELECT right(s.spawn_key,28), s.health, s.mode, coalesce(t.name,'-') FROM online_spawns s "
              + "LEFT JOIN online_characters t ON t.id=s.target_character WHERE s.health IS NOT NULL ORDER BY s.spawn_key");
      report(db, "characters (name, hp-bearing revision)", "SELECT name, revision, experience, gold FROM online_characters ORDER BY name");
    }
  }

  /** T4C-0335: the committed rows behind the accept scene (quest state, once-only rewards, equipment, kill rewards, loot). */
  private static void acceptReport() throws Exception {
    try (var db = DriverManager.getConnection(System.getenv("T4C_DB_URL"), System.getenv("T4C_DB_USER"), System.getenv("T4C_DB_PASSWORD"))) {
      report(db, "characters (name, revision, experience, gold, quest status, kills, knows renew armor, world, location epoch)",
          "SELECT name, revision, experience, gold, progress->'questFlags'->>'quest.renewed_wards.status', progress->'questFlags'->>'quest.renewed_wards.kills', "
              + "(progress->'spells')::text LIKE '%renew_armor%', world, location_epoch FROM online_characters WHERE name LIKE 'Live %' ORDER BY name");
      report(db, "quest turn-in style rewards (rewards that are not creature kills): character, gold, experience, count",
          "SELECT c.name, r.gold, r.experience, count(*) FROM online_rewards r JOIN online_characters c ON c.id=r.character_id "
              + "WHERE r.gold > 0 AND NOT EXISTS (SELECT 1 FROM online_world_rewards w WHERE w.event_id=r.event_id) AND c.name LIKE 'Live %' GROUP BY c.name, r.gold, r.experience ORDER BY c.name");
      report(db, "kill rewards per character (lethal claimant per creature life)",
          "SELECT c.name, count(*) FROM online_world_rewards w JOIN online_characters c ON c.id=w.character_id GROUP BY c.name ORDER BY c.name");
      report(db, "equipment of each character (slot=item key)",
          "SELECT c.name, (SELECT string_agg(e.key || '=' || coalesce((SELECT i->>'key' FROM jsonb_array_elements(c.progress->'inventory') i WHERE i->>'id'=e.value),'?'), ' ') "
              + "FROM jsonb_each_text(c.progress->'equipment') e) FROM online_characters c WHERE c.name LIKE 'Live %' ORDER BY c.name");
      report(db, "ground loot (item, state, owner, claimed by, receipts)",
          "SELECT l.item_key, l.state, o.name, coalesce(cb.name,'-'), (SELECT count(*) FROM online_loot_claims k WHERE k.loot_id=l.id) "
              + "FROM online_ground_loot l JOIN online_characters o ON o.id=l.owner_character LEFT JOIN online_characters cb ON cb.id=l.claimed_by ORDER BY l.created_at");
      report(db, "corpses (character, stacks, recovered)",
          "SELECT c.name, jsonb_array_length(k.items), k.recovered FROM online_corpses k JOIN online_characters c ON c.id=k.character_id WHERE c.name LIKE 'Live %' ORDER BY k.created_at");
    }
  }

  private static void report(java.sql.Connection db, String title, String sql) throws Exception {
    System.out.println("== " + title);
    try (var st = db.createStatement(); var rs = st.executeQuery(sql)) {
      int n = rs.getMetaData().getColumnCount();
      while (rs.next()) {
        var row = new StringBuilder("  ");
        for (int i = 1; i <= n; i++) row.append(rs.getString(i)).append(i < n ? " | " : "");
        System.out.println(row);
      }
    }
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
    // T4C-0335: -Dprovision.extra=item.a,item.b adds one unequipped piece of each (the equip-bonus checks need gear with boosts).
    for (String extra : System.getProperty("provision.extra", "").split(","))
      if (!extra.isBlank()) stocked.add(new CharacterProgress.Item(UUID.randomUUID(), extra.trim(), 1, 1));
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
