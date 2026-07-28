package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.HudTooltip;

import java.util.ArrayList;
import java.util.List;

/** Read-only quest journal backed by the player's persistent quest flags. */
public final class QuestScreen extends GuiScreenBase {
    private static final int VISIBLE_ROWS = 6;
    private static final float LIST_X = 29f;
    private static final float LIST_Y = 55f;
    private static final float LIST_W = 158f;
    private static final float ROW_H = 48f;
    private static final Color GOLD = Color.valueOf("F2B705");
    private static final Color WHITE = Color.valueOf("E6D8BC");
    private static final Color COMPLETED = Color.valueOf("78A66A");

    private final Player player;
    private final List<QuestDef> quests = new ArrayList<>();
    private final BitmapFont listFont;
    private final BitmapFont detailFont;
    private final List<GuiBoxedText> questRows = new ArrayList<>();
    private final List<RewardIcon> rewardIcons = new ArrayList<>();
    private final HudTooltip rewardTooltip = new HudTooltip();
    private GuiBoxedText detailTitle;
    private GuiBoxedText detailDescription;
    private GuiBoxedText detailObjective;
    private GuiBoxedText detailGiver;
    private int selectedIndex;
    private int firstVisible;

    public QuestScreen(Player player) {
        this.player = player;
        background = GuiSprites.load("GUI_BackQuest");
        listFont = FontManager.getInstance().getHaettenschweilerFont(14, GOLD);
        detailFont = FontManager.getInstance().getJetBrainsMonoFont(11, WHITE);
        centerOnScreen();
        addCloseButton(548f, 1f);
        loadPlayerQuests();
        addStaticLabels();
        addQuestBoxes();
        addRewardIcons();
    }

    private void loadPlayerQuests() {
        if (player == null) return;
        for (QuestDef quest : QuestRegistry.load()) {
            if (quest != null && status(quest) != QuestService.STATUS_NOT_STARTED) {
                quests.add(quest);
            }
        }
    }

    private void addStaticLabels() {
        if (background == null) return;
        var titleFont = FontManager.getInstance().getHaettenschweilerFont(17, GOLD);
        labels.add(new GuiBoxedText(titleFont, x + 238f, y + 2f, 100f, 19f,
                () -> I18n.key("ui.quest_journal"), () -> GOLD).shrinkToFit());
        if (quests.isEmpty()) {
            labels.add(new GuiBoxedText(detailFont, x + 20f, y + 150f, 185f, 30f,
                    () -> I18n.key("quest.none"), () -> WHITE).shrinkToFit());
        }
    }

    /** All journal content is represented by boxed widgets so Ctrl+drag and Shift+resize work uniformly. */
    private void addQuestBoxes() {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            final int rowIndex = row;
            float rowHeight = row == VISIBLE_ROWS - 1 ? 23f : 32f;
            GuiBoxedText box = new GuiBoxedText(listFont, x + LIST_X, y + LIST_Y + row * ROW_H,
                    LIST_W, rowHeight,
                    () -> rowQuestTitle(rowIndex),
                    () -> rowQuestColor(rowIndex))
                    .align(GuiBoxedText.Align.LEFT).shrinkToFit();
            questRows.add(box);
            labels.add(box);
        }
        detailTitle = new GuiBoxedText(listFont, x + 242f, y + 43f, 308f, 29f,
                this::selectedTitle, () -> GOLD)
                .align(GuiBoxedText.Align.LEFT).shrinkToFit();
        labels.add(detailTitle);
        detailDescription = detailBox(242f, 78f, 308f, 100f, this::selectedDescription, () -> WHITE).wrap();
        detailObjective = detailBox(242f, 199f, 308f, 42f, this::selectedObjective,
                () -> selectedQuest() != null && status(selectedQuest()) == QuestService.STATUS_COMPLETED ? COMPLETED : WHITE);
        detailGiver = detailBox(242f, 244f, 308f, 26f, this::selectedGiver, () -> WHITE);
    }

    private void addRewardIcons() {
        addRewardIcon("GUI_QuestRewardGold", 272f, 290f, RewardType.GOLD);
        addRewardIcon("GUI_QuestRewardXp", 373f, 290f, RewardType.XP);
        addRewardIcon("GUI_QuestRewardItem", 482f, 290f, RewardType.ITEM);
    }

    private void addRewardIcon(String spriteName, float dx, float dy, RewardType type) {
        TextureRegion sprite = GuiSprites.load(spriteName);
        if (sprite == null) return;
        GuiAnimatedSprite icon = new GuiAnimatedSprite(List.of(sprite), x + dx, y + dy, 1f).boxed(32f, 34f);
        animatedSprites.add(icon);
        rewardIcons.add(new RewardIcon(icon, type));
    }

    private GuiBoxedText detailBox(float dx, float dy, float width, float height,
                                   java.util.function.Supplier<String> text,
                                   java.util.function.Supplier<Color> color) {
        GuiBoxedText box = new GuiBoxedText(detailFont, x + dx, y + dy, width, height, text, color)
                .align(GuiBoxedText.Align.LEFT).shrinkToFit();
        labels.add(box);
        return box;
    }

    private QuestDef rowQuest(int row) {
        int index = firstVisible + row;
        return index >= 0 && index < quests.size() ? quests.get(index) : null;
    }

    private String rowQuestTitle(int row) {
        QuestDef quest = rowQuest(row);
        return quest == null ? "" : I18n.resolve(quest.getTitle());
    }

    private Color rowQuestColor(int row) {
        QuestDef quest = rowQuest(row);
        if (quest != null && firstVisible + row == selectedIndex) return WHITE;
        return quest != null && status(quest) == QuestService.STATUS_COMPLETED ? COMPLETED : GOLD;
    }

    private QuestDef selectedQuest() {
        return selectedIndex >= 0 && selectedIndex < quests.size() ? quests.get(selectedIndex) : null;
    }

    private String selectedTitle() {
        QuestDef quest = selectedQuest();
        return quest == null ? "" : I18n.resolve(quest.getTitle());
    }

    private String selectedDescription() {
        QuestDef quest = selectedQuest();
        if (quest == null) return "";
        return I18n.resolve(status(quest) == QuestService.STATUS_COMPLETED ? quest.getCompletedText() : quest.getOfferText());
    }

    private String selectedObjective() {
        QuestDef quest = selectedQuest();
        if (quest == null) return "";
        if (status(quest) == QuestService.STATUS_COMPLETED) return I18n.key("quest.status.completed");
        return I18n.message("quest.objective.kills", I18n.resolve(quest.getTargetMonster()),
                kills(quest), quest.getRequiredKills());
    }

    private String selectedGiver() {
        QuestDef quest = selectedQuest();
        return quest == null ? "" : I18n.message("quest.giver", I18n.resolve(quest.getGiverNpc()));
    }

    @Override
    public void render(SpriteBatch batch) {
        if (background != null) GuiDraw.drawRegionFlipped(batch, background, x, y);
        for (GuiElement element : orderedElements()) element.render(batch);
        updateRewardTooltip();
        rewardTooltip.render(batch);
    }

    private void updateRewardTooltip() {
        QuestDef quest = selectedQuest();
        if (quest == null) {
            rewardTooltip.clear();
            return;
        }
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        for (RewardIcon rewardIcon : rewardIcons) {
            if (!rewardIcon.icon.contains(mouseX, mouseY)) continue;
            String text = switch (rewardIcon.type) {
                case GOLD -> I18n.message("quest.reward.gold", quest.getRewardGold());
                case XP -> I18n.message("quest.reward.xp", quest.getRewardXp());
                case ITEM -> I18n.key("quest.reward.item.none");
            };
            rewardTooltip.show(text, mouseX, mouseY);
            return;
        }
        rewardTooltip.clear();
    }

    @Override
    public void onTouchUp(float screenX, float screenY) {
        for (int row = 0; row < questRows.size(); row++) {
            if (questRows.get(row).contains(screenX, screenY)) {
                int index = firstVisible + row;
                if (index >= 0 && index < quests.size()) {
                    selectedIndex = index;
                    return;
                }
            }
        }
        super.onTouchUp(screenX, screenY);
    }

    @Override
    public void onScroll(float amountY, float screenX, float screenY) {
        if (screenX >= x + LIST_X && screenX <= x + LIST_X + 205f) {
            int maxFirst = Math.max(0, quests.size() - VISIBLE_ROWS);
            firstVisible = Math.max(0, Math.min(maxFirst, firstVisible + (amountY > 0 ? 1 : -1)));
            return;
        }
        super.onScroll(amountY, screenX, screenY);
    }

    @Override
    public boolean onKeyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            GuiManager.close();
            return true;
        }
        return false;
    }

    private int status(QuestDef quest) {
        return player.getQuestFlag(QuestService.statusFlag(quest));
    }

    private int kills(QuestDef quest) {
        return Math.max(0, Math.min(quest.getRequiredKills(),
                player.getQuestFlag(QuestService.killsFlag(quest))));
    }

    private enum RewardType { GOLD, XP, ITEM }

    private record RewardIcon(GuiAnimatedSprite icon, RewardType type) {
    }
}
