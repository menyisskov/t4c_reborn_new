package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import com.perso.T4C.gui.core.CharacterPanel;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestJournal;
import com.perso.T4C.quest.definition.ConcordCampaign;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.HudTooltip;
import com.perso.T4C.ui.PlayerHUD;
import java.util.ArrayList;
import java.util.List;

/** A live, read-only view of quest categories, saved checkpoints and the next action. */
public final class QuestScreen extends CharacterPanel {
  private static final int VISIBLE_ROWS = 7;
  private static final float ROW_Y = 152, ROW_PITCH = 54;
  private static final float DETAIL_Y = 228, DETAIL_HEIGHT = 264;
  private final Player player;
  private final List<QuestJournal.Entry> quests = new ArrayList<>();
  private final List<RewardIcon> rewards = new ArrayList<>();
  private final HudTooltip rewardTooltip = new HudTooltip();
  private final BitmapFont rowFont =
      FontManager.getInstance().getTahomaFont(13, JournalTheme.TEXT, false);
  private final BitmapFont smallFont =
      FontManager.getInstance().getTahomaFont(11, JournalTheme.DIM, false);
  private final BitmapFont bodyFont =
      FontManager.getInstance().getTahomaFont(14, JournalTheme.TEXT, false);
  private final GlyphLayout rowLayout = new GlyphLayout();
  private boolean completedTab;
  private QuestJournal.Kind kindFilter;
  private int selectedIndex, firstVisible;
  private float detailScroll, maxDetailScroll;
  private long nextRefresh;

  public QuestScreen(Player player) {
    this(player, null);
  }

  public QuestScreen(Player player, PlayerHUD hud) {
    super(player, hud, "journal.title", "quests", 840, 600);
    this.player = player;
    buttons.add(
        theme.button(
            x + 28,
            y + 108,
            110,
            28,
            () -> I18n.key("quest.tab.in_progress"),
            () -> changeStatus(false)));
    buttons.add(
        theme.button(
            x + 144,
            y + 108,
            114,
            28,
            () -> I18n.key("quest.tab.completed"),
            () -> changeStatus(true)));
    buttons.add(
        theme.button(
            x + 282, y + 108, 94, 28, () -> I18n.key("journal.all"), () -> changeKind(null)));
    buttons.add(
        theme.button(
            x + 382,
            y + 108,
            130,
            28,
            () -> I18n.key("journal.main_story"),
            () -> changeKind(QuestJournal.Kind.MAIN_STORY)));
    buttons.add(
        theme.button(
            x + 518,
            y + 108,
            124,
            28,
            () -> I18n.key("journal.side_quest"),
            () -> changeKind(QuestJournal.Kind.SIDE_QUEST)));
    buttons.add(
        theme.button(
            x + 706,
            y + 108,
            108,
            28,
            () -> I18n.key("quest.kill_log_link"),
            () -> GuiManager.open(new MonsterKillLogScreen(player))));
    value(
        () -> selected() == null ? "" : selected().title(),
        328,
        158,
        472,
        34,
        22,
        JournalTheme.GOLD);
    value(
        () ->
            selected() == null
                ? ""
                : category(selected())
                    + (selected().chain().isEmpty() ? "" : " / " + selected().chain()),
        328,
        196,
        472,
        24,
        12,
        JournalTheme.DIM);
    value(
        () -> selected() == null ? "" : I18n.message("quest.giver", selected().giver()),
        328,
        489,
        472,
        20,
        12,
        JournalTheme.DIM);
    value(
        () ->
            quests.isEmpty()
                ? I18n.key("journal.none_matching")
                : I18n.message(
                    "journal.list_range",
                    firstVisible + 1,
                    Math.min(quests.size(), firstVisible + VISIBLE_ROWS),
                    quests.size()),
        28,
        565,
        276,
        22,
        11,
        JournalTheme.DIM);
    text("journal.scroll_hint", 328, 565, 472, 22, 11, JournalTheme.DIM);
    addReward("GUI_QuestRewardGold", 340, RewardType.GOLD);
    addReward("GUI_QuestRewardXp", 480, RewardType.XP);
    addReward("GUI_QuestRewardItem", 620, RewardType.ITEM);
    refresh();
  }

  private String category(QuestJournal.Entry e) {
    return I18n.key(
        e.kind() == QuestJournal.Kind.MAIN_STORY ? "journal.main_story" : "journal.side_quest");
  }

  private QuestJournal.Entry selected() {
    return selectedIndex >= 0 && selectedIndex < quests.size() ? quests.get(selectedIndex) : null;
  }

  private void changeStatus(boolean value) {
    completedTab = value;
    resetView();
  }

  private void changeKind(QuestJournal.Kind value) {
    kindFilter = value;
    resetView();
  }

  private void resetView() {
    selectedIndex = 0;
    firstVisible = 0;
    detailScroll = 0;
    quests.clear();
    refresh();
  }

  private void refresh() {
    String selectedId = selected() == null ? null : selected().id();
    quests.clear();
    for (var entry : QuestJournal.entries(player)) {
      if (entry.completed() == completedTab && (kindFilter == null || kindFilter == entry.kind()))
        quests.add(entry);
    }
    selectedIndex = Math.min(selectedIndex, Math.max(0, quests.size() - 1));
    for (int i = 0; i < quests.size(); i++)
      if (quests.get(i).id().equals(selectedId)) selectedIndex = i;
    if (selected() == null || !selected().id().equals(selectedId)) detailScroll = 0;
    firstVisible = Math.max(0, Math.min(firstVisible, quests.size() - VISIBLE_ROWS));
    nextRefresh = TimeUtils.millis() + 250;
  }

  @Override
  public void render(SpriteBatch batch) {
    if (TimeUtils.millis() >= nextRefresh) refresh();
    renderPanelBackground(batch);
    theme.rect(
        batch,
        x + (completedTab ? 144 : 28),
        y + 137,
        completedTab ? 114 : 110,
        1,
        JournalTheme.GOLD);
    theme.rect(
        batch,
        x + (kindFilter == null ? 282 : kindFilter == QuestJournal.Kind.MAIN_STORY ? 382 : 518),
        y + 137,
        kindFilter == null ? 94 : kindFilter == QuestJournal.Kind.MAIN_STORY ? 130 : 124,
        1,
        JournalTheme.GOLD);
    well(batch, 24, 149, 276, 392);
    well(batch, 312, 149, 504, 392);
    drawRows(batch);
    drawDetails(batch);
    for (var element : orderedElements()) element.render(batch);
    if (selected() != null && selected().definition() != null) {
      for (RewardIcon reward : rewards) reward.icon.render(batch);
    }
    updateRewardTooltip();
    rewardTooltip.render(batch);
  }

  private void drawRows(SpriteBatch batch) {
    if (quests.isEmpty()) {
      bodyFont.draw(
          batch, I18n.key("journal.none_matching"), x + 40, y + 235, 244, Align.center, true);
      return;
    }
    for (int row = 0; row < VISIBLE_ROWS && firstVisible + row < quests.size(); row++) {
      int index = firstVisible + row;
      var entry = quests.get(index);
      float ry = y + ROW_Y + row * ROW_PITCH;
      if (index == selectedIndex) theme.accentWell(batch, x + 30, ry, 264, 50);
      else theme.well(batch, x + 30, ry, 264, 50);
      fitted(batch, rowFont, entry.title(), x + 40, ry + 6, 244);
      smallFont.draw(batch, category(entry), x + 40, ry + 31);
    }
    if (quests.size() > VISIBLE_ROWS) {
      float height = 370f * VISIBLE_ROWS / quests.size();
      float offset = (370 - height) * firstVisible / (quests.size() - VISIBLE_ROWS);
      theme.rect(batch, x + 295, y + ROW_Y + offset, 2, height, JournalTheme.DIM);
    }
  }

  private void fitted(
      SpriteBatch batch, BitmapFont font, String text, float px, float py, float width) {
    float sx = font.getData().scaleX, sy = font.getData().scaleY;
    rowLayout.setText(font, text);
    if (rowLayout.width > width) {
      float factor = width / rowLayout.width;
      font.getData().setScale(sx * factor, sy * factor);
      rowLayout.setText(font, text);
    }
    font.draw(batch, rowLayout, px, py);
    font.getData().setScale(sx, sy);
  }

  private record Section(String heading, String text) {}

  private void drawDetails(SpriteBatch batch) {
    var entry = selected();
    if (entry == null) {
      maxDetailScroll = 0;
      return;
    }
    List<Section> sections =
        List.of(
            new Section("journal.current_stage", entry.stage()),
            new Section("journal.next_step", entry.nextStep()),
            new Section("journal.objectives", String.join("\n", entry.objectives())),
            new Section("journal.background", entry.description()));
    List<GlyphLayout> layouts = new ArrayList<>();
    float total = 0;
    for (Section section : sections) {
      GlyphLayout content =
          new GlyphLayout(bodyFont, section.text(), JournalTheme.TEXT, 458, Align.left, true);
      layouts.add(content);
      if (!section.text().isEmpty()) total += 18 + Math.abs(content.height) + 16;
    }
    maxDetailScroll = Math.max(0, total - DETAIL_HEIGHT);
    detailScroll = Math.max(0, Math.min(maxDetailScroll, detailScroll));
    batch.flush();
    Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
    Gdx.gl.glScissor(
        Math.round(x + 326),
        Math.round(Gdx.graphics.getHeight() - y - DETAIL_Y - DETAIL_HEIGHT),
        476,
        Math.round(DETAIL_HEIGHT));
    try {
      float cursor = y + DETAIL_Y - detailScroll;
      for (int i = 0; i < sections.size(); i++) {
        Section section = sections.get(i);
        if (section.text().isEmpty()) continue;
        smallFont.draw(batch, I18n.key(section.heading()), x + 330, cursor);
        cursor += 18;
        bodyFont.draw(batch, layouts.get(i), x + 330, cursor);
        cursor += Math.abs(layouts.get(i).height) + 16;
      }
      batch.flush();
    } finally {
      Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }
    if (maxDetailScroll > 0) {
      float height = Math.max(24, DETAIL_HEIGHT * DETAIL_HEIGHT / total);
      theme.rect(
          batch,
          x + 805,
          y + DETAIL_Y + (DETAIL_HEIGHT - height) * detailScroll / maxDetailScroll,
          3,
          height,
          JournalTheme.DIM);
    }
  }

  private void addReward(String sprite, float dx, RewardType type) {
    var region = GuiSprites.load(sprite);
    if (region != null)
      rewards.add(
          new RewardIcon(
              new GuiAnimatedSprite(List.of(region), x + dx, y + 512, 1).boxed(28, 26), type));
  }

  private void updateRewardTooltip() {
    var entry = selected();
    if (entry == null || entry.definition() == null) {
      rewardTooltip.clear();
      return;
    }
    var q = entry.definition();
    for (RewardIcon reward : rewards) {
      if (!reward.icon.contains(Gdx.input.getX(), Gdx.input.getY())) continue;
      String value =
          switch (reward.type) {
            case GOLD -> I18n.message("quest.reward.gold", q.getRewardGold());
            case XP -> I18n.message("quest.reward.xp", q.getRewardXp());
            case ITEM -> {
              if (ConcordCampaign.all().stream().anyMatch(c -> c.getId().equals(q.getId())))
                yield I18n.key("npc.concord.rewards");
              var item = ItemRegistry.findByKey(q.getRewardItemKey());
              var spell = SpellRegistry.findByName(q.getRewardSpellKey());
              yield item != null
                  ? I18n.resolve(item.getName())
                  : spell != null
                      ? I18n.resolve(spell.getName())
                      : I18n.key("quest.reward.item.none");
            }
          };
      rewardTooltip.show(value, Gdx.input.getX(), Gdx.input.getY());
      return;
    }
    rewardTooltip.clear();
  }

  @Override
  public void onTouchDown(float sx, float sy) {
    if (sx >= x + 30
        && sx <= x + 294
        && sy >= y + ROW_Y
        && sy < y + ROW_Y + VISIBLE_ROWS * ROW_PITCH) {
      int index = firstVisible + (int) ((sy - y - ROW_Y) / ROW_PITCH);
      if (index < quests.size()) {
        selectedIndex = index;
        detailScroll = 0;
      }
      return;
    }
    super.onTouchDown(sx, sy);
  }

  @Override
  public void onScroll(float amount, float sx, float sy) {
    if (amount == 0) return;
    if (sx >= x + 24 && sx <= x + 300 && sy >= y + 149 && sy <= y + 541) {
      firstVisible =
          Math.max(
              0,
              Math.min(
                  Math.max(0, quests.size() - VISIBLE_ROWS), firstVisible + (amount > 0 ? 1 : -1)));
    } else if (sx >= x + 312 && sx <= x + 816 && sy >= y + DETAIL_Y && sy <= y + 492) {
      detailScroll = Math.max(0, Math.min(maxDetailScroll, detailScroll + amount * 36));
    }
  }

  @Override
  public boolean onKeyDown(int key) {
    if (key == Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    if (key == Input.Keys.PAGE_DOWN || key == Input.Keys.PAGE_UP) {
      detailScroll =
          Math.max(
              0,
              Math.min(maxDetailScroll, detailScroll + (key == Input.Keys.PAGE_DOWN ? 180 : -180)));
      return true;
    }
    return false;
  }

  @Override
  public void dispose() {
    rewardTooltip.dispose();
    super.dispose();
  }

  private enum RewardType {
    GOLD,
    XP,
    ITEM
  }

  private record RewardIcon(GuiAnimatedSprite icon, RewardType type) {}
}
