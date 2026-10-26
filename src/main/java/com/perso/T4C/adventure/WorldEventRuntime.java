package com.perso.T4C.adventure;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.entity.NameRenderer;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/** Connects event logic to bounded transient encounters; authored spawns are never removed. */
public final class WorldEventRuntime implements WorldEvents.Host {
  private final Player player;
  private final QuestService quests;
  private final Runnable save;
  private final Consumer<String> messages;
  private final WorldEvents events;
  private final Map<String, List<BaseMonster>> actors = new HashMap<>();
  private final Map<BaseMonster, WorldEvents.Definition> champions = new IdentityHashMap<>();
  private final List<BaseMonster> discarded = new ArrayList<>();
  private CollisionReader collision;
  private MonsterManager manager;
  private long nextGuide;
  private String pinned = "", title = "";

  public WorldEventRuntime(Player p, QuestService q, Runnable save, Consumer<String> messages) {
    this.player = p;
    this.quests = q;
    this.save = save;
    this.messages = messages;
    events = new WorldEvents(p, this, System::currentTimeMillis);
  }

  public void tick(MonsterManager current) {
    if (current != manager) {
      if (manager != null)
        manager.discardMonsters(actors.values().stream().flatMap(List::stream).toList());
      events.unload();
      actors.clear();
      champions.clear();
      discarded.clear();
      manager = current;
    }
    if (manager == null) return;
    if (!discarded.isEmpty()) {
      manager.discardMonsters(discarded);
      discarded.clear();
    }
    events.tick();
    if (System.currentTimeMillis() >= nextGuide) {
      nextGuide = System.currentTimeMillis() + 2000;
      CollectionBook.synchronize(player);
      var goal = ProgressionTracker.pinned(player, ProgressionTracker.goals(player));
      pinned =
          goal == null
              ? ""
              : goal.title()
                  + (goal.x() == null
                      ? ""
                      : I18n.message("progress.pin_location", goal.x(), goal.y()));
      title = CollectionBook.activeTitle(player);
    }
  }

  @Override
  public boolean spawn(WorldEvents.Definition e, boolean champion, int wave) {
    if (manager == null || player.getCoordinates().getZ() != 0) return false;
    List<BaseMonster> spawned = new ArrayList<>();
    try {
      if (collision == null)
        collision = new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
      int count = champion ? 1 : 4;
      for (int i = 0; i < count; i++) {
        var point = e.ground().points().get((wave * 11 + i * 7) % e.ground().points().size());
        if (collision.hasCollision(point.x(), point.y())) {
          discarded.addAll(spawned);
          return false;
        }
        String type = champion ? e.ground().bossType() : e.ground().monster();
        var monster =
            MonsterRegistry.create(
                MonsterRegistry.findByName(type), point.x() * 32f, point.y() * 16f);
        monster.disableRespawn();
        monster.aggroOn(player.getPositionVector());
        manager.addMonster(monster);
        spawned.add(monster);
        if (champion) champions.put(monster, e);
      }
      actors.computeIfAbsent(e.id(), k -> new ArrayList<>()).addAll(spawned);
      return true;
    } catch (Exception ex) {
      discarded.addAll(spawned);
      spawned.forEach(champions::remove);
      messages.accept(I18n.key("event.unavailable"));
      return false;
    }
  }

  @Override
  public int threats(WorldEvents.Definition e) {
    return (int) actors.getOrDefault(e.id(), List.of()).stream().filter(m -> !m.isDead()).count();
  }

  @Override
  public void clear(WorldEvents.Definition e) {
    var owned = actors.remove(e.id());
    if (owned != null) {
      discarded.addAll(owned);
      owned.forEach(champions::remove);
    }
  }

  public void killed(BaseMonster monster) {
    var e = champions.remove(monster);
    if (e != null) events.championKilled(e);
  }

  @Override
  public void reward(int gold, int xp) {
    player.addGold(gold);
    quests.awardScriptXp(player, xp);
    messages.accept(I18n.message("event.checkpoint_reward", gold, xp));
  }

  @Override
  public void save() {
    save.run();
  }

  @Override
  public void message(String message) {
    messages.accept(message);
  }

  public boolean interact() {
    return events.interact();
  }

  public void renderWorld(SpriteBatch batch) {
    for (var entry : champions.entrySet())
      if (!entry.getKey().isDead() && WorldEvents.nearby(player, entry.getValue(), 70))
        NameRenderer.renderName(
            batch,
            I18n.key("event.champion_marker"),
            entry.getKey().getPosition().x,
            entry.getKey().getPosition().y - 28,
            0,
            0);
    for (var e : WorldEvents.definitions())
      if (WorldEvents.nearby(player, e, 70)
          && WorldEvents.stage(player, e) == WorldEvents.Stage.RESTORE) {
        for (int i = 0; i < e.nodes().size(); i++)
          if (!events.nodeRestored(e, i)) {
            var p = e.nodes().get(i);
            NameRenderer.renderName(
                batch, I18n.key("event.node_marker"), p.x() * 32f, p.y() * 16f, 0, 0);
          }
      }
    if (!title.isEmpty())
      NameRenderer.renderName(
          batch, title, player.getPositionVector().x, player.getPositionVector().y - 50, 0, 0);
  }

  public String hudText() {
    for (var e : WorldEvents.definitions())
      if (WorldEvents.nearby(player, e, 70)) {
        var stage = WorldEvents.stage(player, e);
        if (stage == WorldEvents.Stage.RESTORE
            || stage == WorldEvents.Stage.DEFEND
            || stage == WorldEvents.Stage.CHAMPION)
          return I18n.message(
              "event.hud",
              e.name(),
              I18n.key("event.stage." + stage.name().toLowerCase(Locale.ROOT)),
              Math.max(0, (WorldEvents.deadline(player, e) - System.currentTimeMillis()) / 1000),
              WorldEvents.restored(player, e),
              WorldEvents.integrity(player, e) / 10);
        if (stage == WorldEvents.Stage.SUCCESS)
          return I18n.message(
              "event.reclaimed", WorldEvents.wardBonus(player, System.currentTimeMillis()));
      }
    return pinned.isEmpty() ? "" : I18n.message("progress.pinned", pinned);
  }
}
