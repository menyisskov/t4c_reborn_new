package com.perso.T4C.adventure;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.*;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.ConcordCampaign;
import com.perso.T4C.spawn.HuntingGrounds;
import java.util.*;
import java.util.function.LongSupplier;

/** Persisted event state machine. Real-world deadlines prevent offline defense/reward farming. */
public final class WorldEvents {
  public enum Stage {
    DORMANT,
    RESTORE,
    DEFEND,
    CHAMPION,
    SUCCESS,
    FAILURE
  }

  public record Definition(
      String id, String family, int tier, int level, int ac, String unlock, int gold, int xp) {
    public HuntingGrounds.Ground ground() {
      return HuntingGrounds.all().stream()
          .filter(g -> g.trainingTier() == tier && family.equals(g.family()))
          .findFirst()
          .orElseThrow();
    }

    public List<HuntingGrounds.Point> nodes() {
      var points = ground().points();
      return List.of(points.get(5), points.get(20), points.get(35));
    }

    public String name() {
      return I18n.key("event." + id + ".name");
    }
  }

  public interface Host {
    boolean spawn(Definition event, boolean champion, int wave);

    int threats(Definition event);

    void clear(Definition event);

    void reward(int gold, int xp);

    void save();

    void message(String message);
  }

  public static final List<Definition> ALL =
      List.of(
          new Definition("boneward", "Skeleton", 1, 100, 150, "avalon_sanctuary", 100000, 15000000),
          new Definition(
              "silkfall", "Tarantula", 1, 140, 150, "avalon_sanctuary", 150000, 35000000),
          new Definition("iron_bell", "Skraug", 2, 180, 250, "threnody_reach", 250000, 75000000));
  private final Player player;
  private final Host host;
  private final LongSupplier clock;
  private final Set<String> materialized = new HashSet<>();
  private long lastTick;

  public WorldEvents(Player player, Host host, LongSupplier clock) {
    this.player = player;
    this.host = host;
    this.clock = clock;
  }

  public static List<Definition> definitions() {
    return ALL;
  }

  public static int wardBonus(Player p, long now) {
    if (p == null) return 0;
    return ALL.stream()
        .filter(e -> stage(p, e) == Stage.SUCCESS && deadline(p, e) > now && nearby(p, e, 35))
        .mapToInt(e -> e.tier() * 20)
        .max()
        .orElse(0);
  }

  public void unload() {
    materialized.clear();
  }

  private static String key(Definition e, String field) {
    return "worldevent." + e.id() + "." + field;
  }

  private int get(Definition e, String field) {
    return player.getQuestFlag(key(e, field));
  }

  private void set(Definition e, String field, int value) {
    player.setQuestFlag(key(e, field), value);
  }

  public static Stage stage(Player player, Definition e) {
    int n = player.getQuestFlag(key(e, "stage"));
    return n >= 0 && n < Stage.values().length ? Stage.values()[n] : Stage.DORMANT;
  }

  public static int pending(Player p, Definition e) {
    return Math.max(0, p.getQuestFlag(key(e, "pending")));
  }

  public static int integrity(Player p, Definition e) {
    return p.getQuestFlag(key(e, "integrity"));
  }

  public static int restored(Player p, Definition e) {
    return Integer.bitCount(p.getQuestFlag(key(e, "nodes")) & 7);
  }

  public static long deadline(Player p, Definition e) {
    return ((long) p.getQuestFlag(key(e, "deadline_hi")) << 32)
        | Integer.toUnsignedLong(p.getQuestFlag(key(e, "deadline_lo")));
  }

  private void deadline(Definition e, long value) {
    set(e, "deadline_hi", (int) (value >>> 32));
    set(e, "deadline_lo", (int) value);
  }

  public static boolean nearby(Player p, Definition e, int radius) {
    return p != null
        && p.getCoordinates().getZ() == 0
        && Math.max(
                Math.abs(p.getCoordinates().getX() / 32 - e.ground().x()),
                Math.abs(p.getCoordinates().getY() / 16 - e.ground().y()))
            <= radius;
  }

  public void tick() {
    long now = clock.getAsLong();
    if (lastTick != 0 && now - lastTick < 1000) return;
    long elapsed = lastTick == 0 ? 1 : Math.max(1, Math.min(1000, (now - lastTick) / 1000));
    lastTick = now;
    for (var e : ALL) {
      Stage stage = stage(player, e);
      if (stage == Stage.SUCCESS || stage == Stage.FAILURE) {
        if (now >= deadline(player, e)) {
          set(e, "stage", Stage.DORMANT.ordinal());
          host.save();
        }
        continue;
      }
      if (stage == Stage.DORMANT) {
        if (player.getCurrentHp() > 0
            && nearby(player, e, 24)
            && QuestService.hasUnlockedZone(player, e.unlock())) {
          set(e, "nodes", 0);
          set(e, "integrity", 1000);
          set(e, "wave", 0);
          set(e, "stage", Stage.RESTORE.ordinal());
          deadline(e, now + 180000);
          host.message(I18n.message("event.started", e.name()));
          host.save();
        }
        continue;
      }
      if (!QuestService.hasUnlockedZone(player, e.unlock())) {
        finish(e, false, now);
        continue;
      }
      if (!materialized.contains(e.id()) && (stage == Stage.DEFEND || stage == Stage.CHAMPION)) {
        if (now >= deadline(player, e)) {
          finish(e, false, now);
          continue;
        }
        if (!host.spawn(e, stage == Stage.CHAMPION, 0)) {
          finish(e, false, now);
          continue;
        }
        materialized.add(e.id());
      }
      if (stage == Stage.DEFEND) {
        int loss =
            (int)
                (elapsed
                    * (host.threats(e) * 3
                        + (nearby(player, e, 45) && player.getCurrentHp() > 0 ? 0 : 25)));
        set(e, "integrity", Math.max(0, get(e, "integrity") - loss));
        if (get(e, "integrity") == 0) {
          finish(e, false, now);
          continue;
        }
        long remaining = deadline(player, e) - now;
        int wave = remaining <= 30000 ? 2 : remaining <= 60000 ? 1 : 0;
        if (wave > get(e, "wave")) {
          if (remaining <= 0 && elapsed > 10) {
            finish(e, false, now);
            continue;
          }
          for (int next = get(e, "wave") + 1; next <= wave; next++) {
            if (!host.spawn(e, false, next)) {
              finish(e, false, now);
              break;
            }
            set(e, "wave", next);
          }
          if (stage(player, e) == Stage.FAILURE) continue;
        }
        if (remaining <= 0) {
          host.clear(e);
          materialized.remove(e.id());
          set(e, "stage", Stage.CHAMPION.ordinal());
          deadline(e, now + 180000);
          host.reward(e.gold() / 4, e.xp() / 3);
          host.message(I18n.message("event.champion", e.name()));
        }
        if (now / 1000 % 10 == 0 || remaining <= 0) host.save();
      } else if (now >= deadline(player, e)) finish(e, false, now);
    }
  }

  public boolean interact() {
    if (player.getCurrentHp() <= 0) return false;
    for (var e : ALL)
      if (stage(player, e) == Stage.RESTORE && nearby(player, e, 40)) {
        if (clock.getAsLong() >= deadline(player, e)) {
          finish(e, false, clock.getAsLong());
          return false;
        }
        for (int index = 0; index < e.nodes().size(); index++) {
          int mask = 1 << index;
          var node = e.nodes().get(index);
          if ((get(e, "nodes") & mask) != 0) continue;
          if (Math.max(
                  Math.abs(player.getCoordinates().getX() / 32 - node.x()),
                  Math.abs(player.getCoordinates().getY() / 16 - node.y()))
              > 3) continue;
          set(e, "nodes", get(e, "nodes") | mask);
          host.message(I18n.message("event.restored", restored(player, e), 3));
          if (restored(player, e) == 3) {
            set(e, "stage", Stage.DEFEND.ordinal());
            deadline(e, clock.getAsLong() + 90000);
            set(e, "wave", 0);
            materialized.remove(e.id());
            host.reward(e.gold() / 4, e.xp() / 3);
            host.message(I18n.message("event.defend", e.name()));
          }
          host.save();
          return true;
        }
      }
    return false;
  }

  public boolean nodeRestored(Definition e, int index) {
    return (get(e, "nodes") & (1 << index)) != 0;
  }

  public void championKilled(Definition e) {
    if (stage(player, e) == Stage.CHAMPION && nearby(player, e, 65))
      finish(e, clock.getAsLong() < deadline(player, e), clock.getAsLong());
  }

  private void finish(Definition e, boolean success, long now) {
    host.clear(e);
    materialized.remove(e.id());
    set(e, "stage", (success ? Stage.SUCCESS : Stage.FAILURE).ordinal());
    deadline(e, now + (success ? 600000 : 180000));
    if (success) {
      set(e, "pending", Math.min(1000, pending(player, e) + 1));
      set(e, "wins", Math.min(Integer.MAX_VALUE - 1, get(e, "wins")) + 1);
      host.reward(e.gold(), e.xp());
    }
    host.message(I18n.message(success ? "event.success" : "event.failure", e.name()));
    host.save();
  }

  public static String rewardKey(Definition event, String style) {
    return "event_" + event.id() + "_" + style;
  }

  public static boolean claim(Player p, Definition e, String style) {
    if (!ConcordCampaign.STYLES.contains(style) || pending(p, e) <= 0) return false;
    if (!InventoryService.add(p, rewardKey(e, style)).success()) return false;
    p.setQuestFlag(key(e, "pending"), pending(p, e) - 1);
    return true;
  }
}
