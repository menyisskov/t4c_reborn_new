package com.perso.T4C.gui.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.perso.T4C.adventure.*;
import com.perso.T4C.gui.core.*;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.definition.ConcordCampaign;
import com.perso.T4C.ui.*;
import com.perso.T4C.wiki.WikiCatalog;
import java.util.*;

/** Collections, practical next goals and the world's repeating event chains in one journal. */
public final class AdventureScreen extends CharacterPanel {
  private enum View {
    COLLECTIONS,
    PROGRESSION,
    EVENTS
  }

  private record Line(String text, String link) {}

  private final Player player;
  private final PlayerHUD hud;
  private final BitmapFont font =
      FontManager.getInstance().getTahomaFont(13, JournalTheme.TEXT, false);
  private final BitmapFont small =
      FontManager.getInstance().getTahomaFont(11, JournalTheme.DIM, false);
  private final GlyphLayout fit = new GlyphLayout();
  private final List<Line> lines = new ArrayList<>();
  private final List<GlyphLayout> layouts = new ArrayList<>();
  private final List<com.perso.T4C.gui.widget.GuiButton> styleButtons = new ArrayList<>();
  private List<CollectionBook.SetEntry> sets;
  private List<ProgressionTracker.Goal> goals;
  private View view = View.COLLECTIONS;
  private int selected, first, style;
  private float scroll, total;
  private long refreshAt;
  private String feedback = "";

  public AdventureScreen(Player player, PlayerHUD hud) {
    super(player, hud, "adventure.title", "adventure", 880, 640);
    this.player = player;
    this.hud = hud;
    style = ConcordCampaign.STYLES.indexOf(ProgressionTracker.style(player));
    int saved = player.getQuestFlag("adventure.reward_style");
    if (saved > 0 && saved <= ConcordCampaign.STYLES.size()) style = saved - 1;
    for (var v : View.values())
      buttons.add(
          theme.button(
              x + 28 + v.ordinal() * 270,
              y + 108,
              264,
              28,
              () -> I18n.key("adventure.view." + v.name().toLowerCase(Locale.ROOT)),
              () -> {
                view = v;
                selected = first = 0;
                scroll = 0;
                feedback = "";
                refresh();
              }));
    for (int i = 0; i < ConcordCampaign.STYLES.size(); i++) {
      final int choice = i;
      var styleButton =
          theme.button(
              x + 28 + i * 103,
              y + 531,
              100,
              24,
              () -> I18n.key("adventure.style." + ConcordCampaign.STYLES.get(choice)),
              () -> {
                style = choice;
                player.setQuestFlag("adventure.reward_style", choice + 1);
                refresh();
              });
      buttons.add(styleButton);
      styleButtons.add(styleButton);
    }
    buttons.add(
        theme.button(
            x + 28,
            y + 569,
            240,
            28,
            () ->
                I18n.key(
                    view == View.COLLECTIONS
                        ? "collection.use_title"
                        : view == View.PROGRESSION ? "progress.pin" : "event.claim"),
            this::action));
    buttons.add(
        theme.button(
            x + 280, y + 569, 220, 28, () -> I18n.key("adventure.inspect"), this::inspect));
    refresh();
  }

  private void refresh() {
    styleButtons.forEach(button -> button.setVisible(view == View.EVENTS));
    refreshAt = System.currentTimeMillis() + 2000;
    CollectionBook.synchronize(player);
    sets = CollectionBook.sets();
    goals = ProgressionTracker.goals(player);
    selected = Math.max(0, Math.min(count() - 1, selected));
    lines.clear();
    layouts.clear();
    total = 0;
    if (view == View.COLLECTIONS && !sets.isEmpty()) {
      var set = sets.get(selected);
      line(set.name(), null);
      line(I18n.message("collection.progress", set.discovered(player), set.items().size()), null);
      line(I18n.key("collection.explanation"), null);
      for (String key : set.items()) {
        var item = ItemRegistry.findByKey(key);
        line(
            I18n.message(
                CollectionBook.has(player, key) ? "collection.found" : "collection.missing",
                I18n.resolve(item.getName())),
            WikiCatalog.id(WikiCatalog.Category.ITEMS, item.getKey()));
      }
      line(
          I18n.key(set.complete(player) ? "collection.title_ready" : "collection.title_locked"),
          null);
    } else if (view == View.PROGRESSION && !goals.isEmpty()) {
      var goal = goals.get(selected);
      line(goal.title(), null);
      line(goal.detail(), null);
      line(I18n.key("progress.explanation"), null);
      line(I18n.key("adventure.inspect"), goal.wikiTarget());
    } else if (view == View.EVENTS) {
      var e = WorldEvents.definitions().get(selected);
      var g = e.ground();
      var stage = WorldEvents.stage(player, e);
      line(e.name(), null);
      line(I18n.key("event." + e.id() + ".story"), null);
      line(
          I18n.message("event.details", g.x(), g.y(), e.level(), e.ac()),
          WikiCatalog.id(WikiCatalog.Category.AREAS, g.locationKey()));
      line(
          I18n.message(
              "event.status",
              I18n.key("event.stage." + stage.name().toLowerCase(Locale.ROOT)),
              Math.max(0, (WorldEvents.deadline(player, e) - System.currentTimeMillis()) / 1000)),
          null);
      line(
          I18n.message(
              "event.progress",
              WorldEvents.restored(player, e),
              WorldEvents.integrity(player, e) / 10),
          null);
      line(I18n.key("event.instructions"), null);
      for (var n : e.nodes()) line(I18n.message("event.node_coordinates", n.x(), n.y()), null);
      line(I18n.message("event.rewards", e.gold(), e.xp(), WorldEvents.pending(player, e)), null);
      var reward =
          ItemRegistry.findByKey(WorldEvents.rewardKey(e, ConcordCampaign.STYLES.get(style)));
      if (reward != null)
        line(
            I18n.resolve(reward.getName()),
            WikiCatalog.id(WikiCatalog.Category.ITEMS, reward.getKey()));
    }
    for (var line : lines) {
      var layout =
          new GlyphLayout(
              font,
              line.text(),
              line.link() == null ? JournalTheme.TEXT : JournalTheme.ACCENT,
              514,
              Align.left,
              true);
      layouts.add(layout);
      total += Math.abs(layout.height) + 12;
    }
    scroll = Math.max(0, Math.min(Math.max(0, total - 330), scroll));
  }

  private void line(String text, String target) {
    if (text != null && !text.isBlank()) lines.add(new Line(text, target));
  }

  private int count() {
    return view == View.COLLECTIONS
        ? sets.size()
        : view == View.PROGRESSION ? goals.size() : WorldEvents.definitions().size();
  }

  private void action() {
    if (view == View.COLLECTIONS && !sets.isEmpty()) {
      var set = sets.get(selected);
      CollectionBook.useTitle(player, set);
      feedback =
          I18n.key(set.complete(player) ? "collection.title_equipped" : "collection.title_locked");
    } else if (view == View.PROGRESSION && !goals.isEmpty()) {
      ProgressionTracker.pin(player, goals.get(selected));
      feedback = I18n.key("progress.pin_saved");
    } else if (view == View.EVENTS)
      feedback =
          I18n.key(
              WorldEvents.claim(
                      player,
                      WorldEvents.definitions().get(selected),
                      ConcordCampaign.STYLES.get(style))
                  ? "event.claimed"
                  : "event.claim_failed");
    PlayerStateStore.save(player);
    refresh();
  }

  private void inspect() {
    String link =
        lines.stream().filter(l -> l.link() != null).map(Line::link).findFirst().orElse(null);
    open(link);
  }

  private void open(String target) {
    if (target == null) return;
    var wiki = new WikiScreen(player, hud);
    wiki.showEntry(target);
    GuiManager.open(wiki);
  }

  private void text(SpriteBatch batch, String s, float px, float py, float width, BitmapFont f) {
    fit.setText(f, s, 0, s.length(), f.getColor(), width, Align.left, false, "...");
    f.draw(batch, fit, px, py);
  }

  @Override
  public void render(SpriteBatch batch) {
    if (System.currentTimeMillis() >= refreshAt) refresh();
    renderPanelBackground(batch);
    theme.rect(batch, x + 28 + view.ordinal() * 270, y + 139, 264, 1, JournalTheme.GOLD);
    well(batch, 24, 174, 272, 338);
    well(batch, 308, 174, 548, 338);
    text(
        batch,
        I18n.key("adventure.view." + view.name().toLowerCase(Locale.ROOT)),
        x + 32,
        y + 149,
        780,
        small);
    for (int row = 0; row < 12 && first + row < count(); row++) {
      int index = first + row;
      String name =
          view == View.COLLECTIONS
              ? sets.get(index).name()
                  + " ("
                  + sets.get(index).discovered(player)
                  + "/"
                  + sets.get(index).items().size()
                  + ")"
              : view == View.PROGRESSION
                  ? goals.get(index).title()
                  : WorldEvents.definitions().get(index).name()
                      + " ("
                      + WorldEvents.pending(player, WorldEvents.definitions().get(index))
                      + ")";
      if (index == selected) theme.accentWell(batch, x + 28, y + 178 + row * 27, 264, 26);
      text(batch, name, x + 36, y + 184 + row * 27, 250, font);
    }
    batch.flush();
    if (count() > 12) {
      float h = 324f * 12 / count();
      theme.rect(
          batch, x + 292, y + 178 + (324 - h) * first / (count() - 12), 2, h, JournalTheme.DIM);
    }
    if (ScissorStack.pushScissors(
        new Rectangle(x + 318, Gdx.graphics.getHeight() - y - 510, 528, 330))) {
      try {
        float py = y + 180 - scroll;
        for (int i = 0; i < layouts.size(); i++) {
          font.draw(batch, layouts.get(i), x + 322, py);
          py += Math.abs(layouts.get(i).height) + 12;
        }
        batch.flush();
      } finally {
        ScissorStack.popScissors();
      }
    }
    if (total > 330) {
      float h = Math.max(18, 330 * 330 / total);
      theme.rect(
          batch, x + 850, y + 180 + (330 - h) * scroll / (total - 330), 2, h, JournalTheme.DIM);
    }
    text(
        batch,
        view == View.EVENTS
            ? I18n.message(
                "adventure.selected_style",
                I18n.key("adventure.style." + ConcordCampaign.STYLES.get(style)))
            : I18n.key("adventure.permanent"),
        x + 28,
        y + 515,
        810,
        small);
    text(
        batch,
        feedback.isEmpty() ? I18n.key("adventure.hint") : feedback,
        x + 28,
        y + 610,
        810,
        small);
    for (var element : orderedElements()) element.render(batch);
  }

  @Override
  public void onScroll(float amount, float sx, float sy) {
    if (sx > x + 308)
      scroll = Math.max(0, Math.min(Math.max(0, total - 330), scroll + amount * 40));
    else first = Math.max(0, Math.min(Math.max(0, count() - 12), first + (amount > 0 ? 3 : -3)));
  }

  @Override
  public void onTouchDown(float sx, float sy) {
    if (sy >= y + 178 && sy < y + 502) {
      if (sx >= x + 28 && sx < x + 296) {
        int n = first + (int) ((sy - y - 178) / 27);
        if (n < count()) {
          selected = n;
          scroll = 0;
          refresh();
        }
        return;
      }
      if (sx > x + 318 && sx < x + 852) {
        float py = y + 180 - scroll;
        for (int i = 0; i < layouts.size(); i++) {
          float h = Math.abs(layouts.get(i).height) + 12;
          if (sy >= py && sy < py + h) {
            open(lines.get(i).link());
            return;
          }
          py += h;
        }
      }
    }
    super.onTouchDown(sx, sy);
  }

  @Override
  public boolean capturesKeyboard() {
    return true;
  }

  @Override
  public boolean onKeyDown(int key) {
    if (key == Input.Keys.ESCAPE
        || key == Input.Keys.J
            && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) GuiManager.close();
    else if (key == Input.Keys.PAGE_DOWN || key == Input.Keys.PAGE_UP)
      onScroll(key == Input.Keys.PAGE_DOWN ? 7 : -7, x + 400, y + 200);
    else if (key == Input.Keys.DOWN || key == Input.Keys.UP) {
      selected = Math.max(0, Math.min(count() - 1, selected + (key == Input.Keys.DOWN ? 1 : -1)));
      first = Math.max(0, Math.min(first, selected));
      if (selected >= first + 12) first = selected - 11;
      scroll = 0;
      refresh();
    }
    return true;
  }
}
