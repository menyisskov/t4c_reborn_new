package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.perso.T4C.gui.core.*;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.PlayerHUD;
import com.perso.T4C.wiki.WikiCatalog;
import java.util.*;

/** Searchable runtime reference; browsing never grants items, spells, travel or quest progress. */
public final class WikiScreen extends CharacterPanel {
  private static final float TOP = 196, ROW = 27;
  private final WikiCatalog catalog = new WikiCatalog();
  private final BitmapFont font =
      FontManager.getInstance().getTahomaFont(13, JournalTheme.TEXT, false);
  private final BitmapFont small =
      FontManager.getInstance().getTahomaFont(11, JournalTheme.DIM, false);
  private final GlyphLayout fitted = new GlyphLayout();
  private final Deque<String> history = new ArrayDeque<>();
  private final List<GlyphLayout> linkLayouts = new ArrayList<>();
  private final int width, height, rows;
  private final float listWidth, detailX, detailWidth, bodyHeight;
  private WikiCatalog.Category category = WikiCatalog.Category.GUIDES;
  private List<WikiCatalog.Entry> results = List.of();
  private WikiCatalog.Entry selected;
  private WikiCatalog.Page page;
  private GlyphLayout article;
  private String query = "";
  private int first;
  private float detailScroll, totalHeight;
  private boolean searchFocused = true;
  private final Rectangle searchBounds = new Rectangle();

  public WikiScreen(Player player, PlayerHUD hud) {
    super(
        player,
        hud,
        "wiki.title",
        "wiki",
        Math.min(900, Math.max(620, Gdx.graphics.getWidth() - 24)),
        Math.min(640, Math.max(430, Gdx.graphics.getHeight() - 24)));
    width = background.getRegionWidth();
    height = background.getRegionHeight();
    listWidth = width >= 800 ? 252 : 190;
    detailX = 44 + listWidth;
    detailWidth = width - detailX - 42;
    bodyHeight = height - TOP - 50;
    rows = Math.max(1, (int) (bodyHeight / ROW));
    searchBounds.set(x + 28, y + 106, width - 162, 30);
    buttons.add(
        theme.button(
            x + width - 126,
            y + 106,
            96,
            30,
            () -> I18n.key("wiki.clear"),
            () -> {
              query = "";
              refresh();
              searchFocused = true;
            }));
    int tab = (width - 56) / WikiCatalog.Category.values().length;
    for (var c : WikiCatalog.Category.values())
      buttons.add(
          theme.button(
              x + 28 + c.ordinal() * tab,
              y + 149,
              tab - 3,
              28,
              () -> categoryName(c),
              () -> {
                category = c;
                refresh();
              }));
    buttons.add(
        theme.button(
            x + width - 126, y + height - 38, 96, 24, () -> I18n.key("wiki.back"), this::back));
    refresh();
  }

  private static String categoryName(WikiCatalog.Category c) {
    return I18n.key("wiki.category." + c.name().toLowerCase(Locale.ROOT));
  }

  private void refresh() {
    results = catalog.search(category, query);
    first = 0;
    select(results.isEmpty() ? null : results.get(0), false);
  }

  private void select(WikiCatalog.Entry next, boolean remember) {
    if (remember && selected != null && next != null && !selected.id().equals(next.id()))
      history.push(selected.id());
    selected = next;
    detailScroll = 0;
    linkLayouts.clear();
    page = next == null ? null : next.page().get();
    article =
        page == null
            ? null
            : new GlyphLayout(
                font, page.text(), JournalTheme.TEXT, detailWidth - 16, Align.left, true);
    totalHeight = article == null ? 0 : Math.abs(article.height) + 24;
    if (page != null)
      for (var link : page.links()) {
        var layout =
            new GlyphLayout(
                font,
                I18n.message("wiki.link", link.label()),
                JournalTheme.ACCENT,
                detailWidth - 16,
                Align.left,
                true);
        linkLayouts.add(layout);
        totalHeight += Math.max(22, Math.abs(layout.height) + 10);
      }
  }

  private void follow(String target) {
    var next = catalog.find(target);
    if (next == null) return;
    query = "";
    category = next.category();
    results = catalog.search(category, query);
    select(next, true);
    first =
        Math.max(0, Math.min(Math.max(0, results.size() - rows), results.indexOf(next) - rows / 2));
    searchFocused = false;
  }

  public void showEntry(String target) {
    follow(target);
  }

  private void back() {
    if (history.isEmpty()) return;
    var entry = catalog.find(history.pop());
    if (entry == null) return;
    query = "";
    category = entry.category();
    results = catalog.search(category, query);
    select(entry, false);
    first =
        Math.max(
            0, Math.min(Math.max(0, results.size() - rows), results.indexOf(entry) - rows / 2));
  }

  private void fit(SpriteBatch batch, BitmapFont f, String value, float px, float py, float w) {
    fitted.setText(f, value, 0, value.length(), f.getColor(), w, Align.left, false, "...");
    f.draw(batch, fitted, px, py);
  }

  @Override
  public void render(SpriteBatch batch) {
    renderPanelBackground(batch);
    if (searchFocused)
      theme.accentWell(
          batch, searchBounds.x, searchBounds.y, searchBounds.width, searchBounds.height);
    else theme.well(batch, searchBounds.x, searchBounds.y, searchBounds.width, searchBounds.height);
    fit(
        batch,
        query.isEmpty() ? small : font,
        query.isEmpty() ? I18n.key("wiki.search") : query + (searchFocused ? " |" : ""),
        x + 39,
        y + 114,
        searchBounds.width - 22);
    theme.rect(
        batch,
        x + 28 + category.ordinal() * ((width - 56) / WikiCatalog.Category.values().length),
        y + 179,
        (width - 56) / WikiCatalog.Category.values().length - 3,
        1,
        JournalTheme.GOLD);
    theme.well(batch, x + 24, y + TOP - 6, listWidth + 8, bodyHeight + 12);
    theme.well(batch, x + detailX - 10, y + TOP - 6, detailWidth + 20, bodyHeight + 12);
    for (int row = 0; row < rows && first + row < results.size(); row++) {
      var e = results.get(first + row);
      float py = y + TOP + row * ROW;
      if (e == selected) theme.accentWell(batch, x + 29, py - 1, listWidth - 2, ROW - 1);
      fit(batch, font, e.title(), x + 37, py + 5, listWidth - 20);
    }
    if (results.isEmpty())
      fit(batch, small, I18n.key("wiki.no_results"), x + 37, y + TOP + 12, listWidth - 20);
    if (results.size() > rows) {
      float thumb = Math.max(14, bodyHeight * rows / results.size());
      theme.rect(
          batch,
          x + listWidth + 27,
          y + TOP + (bodyHeight - thumb) * first / (results.size() - rows),
          2,
          thumb,
          JournalTheme.DIM);
    }
    drawArticle(batch);
    fit(
        batch,
        small,
        I18n.message("wiki.results", results.size()),
        x + 28,
        y + height - 32,
        listWidth);
    fit(batch, small, I18n.key("wiki.hint"), x + detailX, y + height - 32, detailWidth - 110);
    for (var element : orderedElements()) element.render(batch);
  }

  private void drawArticle(SpriteBatch batch) {
    if (article == null) return;
    detailScroll = Math.max(0, Math.min(Math.max(0, totalHeight - bodyHeight), detailScroll));
    batch.flush();
    var clip = new Rectangle(x + detailX, y + TOP, detailWidth, bodyHeight);
    // Scissor coordinates use the bottom-left framebuffer origin; the journal uses top-left.
    clip.y = Gdx.graphics.getHeight() - clip.y - clip.height;
    if (!ScissorStack.pushScissors(clip)) return;
    try {
      float cursor = y + TOP + 4 - detailScroll;
      font.draw(batch, article, x + detailX, cursor);
      cursor += Math.abs(article.height) + 24;
      for (var link : linkLayouts) {
        font.draw(batch, link, x + detailX, cursor);
        cursor += Math.max(22, Math.abs(link.height) + 10);
      }
      batch.flush();
    } finally {
      ScissorStack.popScissors();
    }
    if (totalHeight > bodyHeight) {
      float thumb = Math.max(18, bodyHeight * bodyHeight / totalHeight);
      theme.rect(
          batch,
          x + width - 30,
          y + TOP + (bodyHeight - thumb) * detailScroll / (totalHeight - bodyHeight),
          3,
          thumb,
          JournalTheme.DIM);
    }
  }

  @Override
  public void onTouchDown(float sx, float sy) {
    if (searchBounds.contains(sx, sy)) {
      searchFocused = true;
      return;
    }
    searchFocused = false;
    if (sy >= y + TOP && sy < y + TOP + bodyHeight) {
      if (sx >= x + 28 && sx < x + listWidth + 30) {
        int index = first + (int) ((sy - y - TOP) / ROW);
        if (index >= 0 && index < results.size()) select(results.get(index), true);
        return;
      }
      if (page != null && sx >= x + detailX && sx <= x + width - 28) {
        float cursor = y + TOP + 4 - detailScroll + Math.abs(article.height) + 24;
        for (int i = 0; i < linkLayouts.size(); i++) {
          float h = Math.max(22, Math.abs(linkLayouts.get(i).height) + 10);
          if (sy >= cursor && sy < cursor + h) {
            follow(page.links().get(i).target());
            return;
          }
          cursor += h;
        }
      }
    }
    super.onTouchDown(sx, sy);
  }

  @Override
  public void onScroll(float amount, float sx, float sy) {
    if (sx >= x + detailX)
      detailScroll =
          Math.max(0, Math.min(Math.max(0, totalHeight - bodyHeight), detailScroll + amount * 45));
    else
      first =
          Math.max(0, Math.min(Math.max(0, results.size() - rows), first + (amount > 0 ? 3 : -3)));
  }

  @Override
  public boolean capturesKeyboard() {
    return true;
  }

  @Override
  public boolean onKeyDown(int key) {
    boolean ctrl =
        Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    boolean shift =
        Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    if (key == Input.Keys.ESCAPE || key == Input.Keys.B && ctrl && shift) {
      GuiManager.close();
      return true;
    }
    if (key == Input.Keys.BACKSPACE) {
      if (searchFocused && !query.isEmpty()) {
        query = query.substring(0, query.offsetByCodePoints(query.length(), -1));
        refresh();
      } else if (!searchFocused) back();
    } else if (key == Input.Keys.F && ctrl) {
      searchFocused = true;
    } else if (key == Input.Keys.TAB) {
      searchFocused = !searchFocused;
    } else if (key == Input.Keys.UP || key == Input.Keys.DOWN) {
      int i = results.indexOf(selected) + (key == Input.Keys.DOWN ? 1 : -1);
      if (!results.isEmpty()) {
        i = Math.max(0, Math.min(results.size() - 1, i));
        select(results.get(i), false);
        first = Math.max(0, Math.min(first, i));
        if (i >= first + rows) first = i - rows + 1;
      }
    } else if (key == Input.Keys.PAGE_DOWN || key == Input.Keys.PAGE_UP) {
      detailScroll =
          Math.max(
              0,
              Math.min(
                  Math.max(0, totalHeight - bodyHeight),
                  detailScroll + (key == Input.Keys.PAGE_DOWN ? bodyHeight : -bodyHeight)));
    } else if (key == Input.Keys.ENTER) {
      searchFocused = false;
    }
    return true;
  }

  @Override
  public boolean onKeyTyped(char c) {
    if (searchFocused
        && c >= 32
        && c != 127
        && query.length() < 96
        && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
      query += c;
      refresh();
    }
    return true;
  }
}
