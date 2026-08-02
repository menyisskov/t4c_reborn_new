package com.perso.T4C.gui.screen;

import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiText;

import com.perso.T4C.i18n.I18n;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.SystemMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spell-learning screen opened when the player clicks an NPC's "teach"
 * dialog keyword ({@code KeywordActionType.TEACH}).
 *
 * Uses the original T4C GUIBackSkill background (576×368, same family as
 * {@link ShopScreen}'s GUI_BackBuy): a skill list with Skill Name / Price /
 * Pts. columns, selection lamps on the left, a scrollbar and a GOLD panel
 * (On Hand / Cost / Total) on the right. Clicking a row selects it (yellow
 * lamp + Cost update); learning is confirmed with the APPRENDRE button.
 */
public class LearnScreen extends GuiListScreen {

    // Bottom pair of plaques: summed skill points of the basket.
    private static final float[] SKILL_LBL_BOX  = {457f, 233f, 94f, 17f};
    private static final float[] SKILL_VAL_BOX  = {457f, 253f, 94f, 15f};
    // APPRENDRE button under the skill-points plaques.
    private static final Color BLOCKED = Color.valueOf("E33300");

    private final Player player;
    private final List<LearnEntry> entries   = new ArrayList<>();
    private LearnEntry selected = null;
    // Basket: each spell or skill point queued with the spin buttons costs 5 skill points.
    private static final int SKILL_POINTS_PER_SPELL = 5;
    // Skill id → icon sprite, from the original client's SkillIcons table
    // (GoN VisualObjectList.cpp, SkillIcons.BindSprite calls).
    private static final Map<String, String> SKILL_ICONS = Map.ofEntries(
            Map.entry("attack",            "64kIconSword"),
            Map.entry("archery",           "64kIconBow"),
            Map.entry("dodge",             "64kIconShield"),
            Map.entry("parry",             "64kIconParry"),
            Map.entry("stun_blow",         "64kIconStunBlow"),
            Map.entry("powerful_blow",     "64kIconPowerBlow"),
            Map.entry("first_aid",         "64kIconFirstAid"),
            Map.entry("rapid_healing",     "64kIconRapidHealing"),
            Map.entry("hide",              "64kIconHide"),
            Map.entry("sneak",             "64kIconSneak"),
            Map.entry("search",            "64kIconSearch"),
            Map.entry("peek",              "64kIconPeek"),
            Map.entry("picklock",          "64kIconPicklock"),
            Map.entry("armor_penetration", "64kIconArmorPierce"),
            Map.entry("rob",               "64kIconRob"));

    public LearnScreen(Player player, List<String> spellIds) {
        this(player, spellIds, null);
    }

    /** Same screen, fed with trainable skills instead of spells (e.g. Ortanalas). */
    public static LearnScreen forTraining(Player player, List<String> skillIds) {
        return new LearnScreen(player, null, skillIds);
    }

    private LearnScreen(Player player, List<String> spellIds, List<String> skillIds) {
        this.player = player;
        try {
            background = SpriteLoader.getInstance().getRegionFromSpriteName("GUIBackSkill");
        } catch (GameException ignored) {
            background = null;
        }
        centerOnScreen();
        addCloseButton();
        addStaticLabels();
        addLearnButton();
        loadEntries(spellIds);
        loadSkillEntries(skillIds);
        if (!entries.isEmpty()) {
            selected = entries.get(0);
        }
        rebuildList();
    }

    // ── Static UI ─────────────────────────────────────────────────────────────

    private void addCloseButton() {
        addCloseButton(CLOSE_X, CLOSE_Y);
    }

    private void addStaticLabels() {
        if (background == null) {
            return;
        }
        BitmapFont chewy = FontManager.getInstance().getHaettenschweilerFont(18, GOLD);
        labels.add(boxed(chewy, TITLE_BOX, 0f, () -> I18n.key("ui.learn"), GOLD).shrinkToFit());
        labels.add(boxed(chewy, GOLD_HDR_BOX, 0f, () -> I18n.key("ui.gold"), GOLD).shrinkToFit());

        BitmapFont sm = FontManager.getInstance().getJetBrainsMonoFont(11, GOLD);
        labels.add(boxed(sm, HDR_NAME_BOX,  0f, () -> I18n.key("ui.skill_name"), GOLD));
        labels.add(boxed(sm, HDR_PRICE_BOX, 0f, () -> I18n.key("ui.price"), GOLD));
        labels.add(boxed(sm, HDR_THIRD_BOX,   0f, () -> I18n.key("ui.pts_short"), GOLD));

        labels.add(boxed(sm, ONHAND_LBL_BOX, 0f, () -> I18n.key("ui.on_hand"), GOLD));
        labels.add(boxed(sm, COST_LBL_BOX,   0f, () -> I18n.key("ui.cost"), GOLD));
        labels.add(boxed(sm, TOTAL_LBL_BOX,  0f, () -> I18n.key("ui.total"), GOLD));
        labels.add(boxed(sm, SKILL_LBL_BOX,  0f, () -> I18n.key("ui.skill_points"), GOLD).shrinkToFit());
    }

    /** APPRENDRE button: learns every basketed spell (gold + skill points). */
    private void addLearnButton() {
        var normal  = GuiSprites.load("GUI_ButtonDisabled");
        var hover   = GuiSprites.load("GUI_ButtonHUp");
        var pressed = GuiSprites.load("GUI_ButtonDown");
        if (normal == null) {
            return;
        }
        BitmapFont chewy = FontManager.getInstance().getHaettenschweilerFont(14, GOLD);
        buttons.add(new GuiButton(normal, hover != null ? hover : normal,
                pressed != null ? pressed : normal,
                x + ACTION_BTN_X, y + ACTION_BTN_Y, this::learnBasket)
                .withLabel(chewy, () -> I18n.key("ui.learn")));
    }

    // ── Entries ───────────────────────────────────────────────────────────────

    private void loadEntries(List<String> spellIds) {
        entries.clear();
        if (spellIds == null) {
            return;
        }
        for (String spellId : spellIds) {
            if (spellId == null || spellId.isEmpty()) {
                continue;
            }
            SpellData data = SpellRegistry.findByName(spellId);
            if (data != null) {
                entries.add(LearnEntry.forSpell(spellId, data));
            }
        }
    }


    private void loadSkillEntries(List<String> skillIds) {
        if (skillIds == null) {
            return;
        }
        for (String skillId : skillIds) {
            if (skillId == null || skillId.isEmpty()) {
                continue;
            }
            entries.add(LearnEntry.forSkill(skillId));
        }
    }

    // ── Dynamic list (rebuilt on page change / selection / learn) ─────────────

    @Override
    protected void rebuildList() {
        labels.removeAll(dynLabels);
        dynLabels.clear();
        buttons.removeAll(dynButtons);
        dynButtons.clear();
        zones.clear();
        animatedSprites.clear();

        int total = entries.size();
        int pages = Math.max(1, (total + ROWS_VISIBLE - 1) / ROWS_VISIBLE);
        page = Math.min(page, pages - 1);
        int start = page * ROWS_VISIBLE;
        int end   = Math.min(start + ROWS_VISIBLE, total);

        BitmapFont fontWh = FontManager.getInstance().getJetBrainsMonoFont(12, WHITE);
        BitmapFont fontBl = FontManager.getInstance().getJetBrainsMonoFont(12, BLOCKED);
        BitmapFont fontDm = FontManager.getInstance().getJetBrainsMonoFont(12, DIM);
        BitmapFont fontGo = FontManager.getInstance().getJetBrainsMonoFont(12, GOLD);

        for (int i = start; i < end; i++) {
            LearnEntry entry = entries.get(i);
            float rowY = ROW_0_Y + (i - start) * ROW_H_PITCH;

            boolean known   = isMaxed(entry);
            boolean blocked = !known && blockReason(entry) != null;
            BitmapFont font = known || blocked ? fontBl : fontWh;
            Color      col  = known || blocked ? BLOCKED : WHITE;

            final String name = entry.isSkill()
                    ? skillName(entry)
                    : I18n.key(entry.spell.getKey(), I18n.resolve(entry.spell.getName()));
            final String price = String.valueOf(priceOf(entry));
            // Skills show the player's current points (plus the queued ones),
            // as the original V3_TrainDlg does; spells show the 5-pt cost.
            final String pts = entry.isSkill()
                    ? String.valueOf(currentSkillLevel(entry) + entry.count)
                    : String.valueOf(entry.count * SKILL_POINTS_PER_SPELL);
            addDyn(font, CELL_NAME,  rowY, () -> name, col);
            addDyn(font, CELL_PRICE, rowY, () -> price, col);
            addDyn(font, CELL_THIRD,   rowY, () -> pts, col);

            // Left socket: the spell's or skill's own icon (empty when it has none).
            TextureRegion socket = entry.isSkill()
                    ? GuiSprites.load(SKILL_ICONS.get(entry.id))
                    : GuiSprites.load(entry.spell.getIconId());
            if (socket != null) {
                animatedSprites.add(new GuiAnimatedSprite(
                        List.of(socket), x + ICON_BOX[0], y + rowY + ICON_BOX[1], 1f)
                        .boxed(ICON_BOX[2], ICON_BOX[3]));
            }

            final LearnEntry e = entry;
            zones.add(new GuiClickZone(x + 10f, y + rowY - 4f, 370f, ROW_H_PITCH - 4f,
                    () -> onRowClick(e)));
            addRowSpinButtons(rowY, e);
        }

        // Scrollbar arrows
        if (page > 0) {
            zones.add(new GuiClickZone(x + SCROLL_X, y + SCROLL_UP_Y,
                    SCROLL_W, SCROLL_BTN_H, () -> scrollPages(-1)));
        }
        if (page < pages - 1) {
            zones.add(new GuiClickZone(x + SCROLL_X, y + SCROLL_DN_Y,
                    SCROLL_W, SCROLL_BTN_H, () -> scrollPages(1)));
        }

        // Right panel dynamic values: the basket's summed gold cost (0 when empty).
        final long onHand = player != null ? player.getGold() : 0L;
        final long cost = basketGoldCost();
        // Total: the gold left after paying the basket (m_stLeft in V3_TrainDlg).
        final long left = onHand - cost;
        addDyn(fontGo, ONHAND_VAL_BOX, 0f, () -> String.valueOf(onHand), GOLD);
        addDyn(cost > 0 ? fontGo : fontDm, COST_VAL_BOX, 0f,
                () -> String.valueOf(cost), cost > 0 ? GOLD : DIM);
        addDyn(left >= 0 ? fontGo : fontBl, TOTAL_VAL_BOX, 0f,
                () -> String.valueOf(left), left >= 0 ? GOLD : BLOCKED);
        // Remaining skill points: the player's total minus what the basket
        // consumes, decreasing live as the original client's m_stSkill does.
        final int skillPts = (player != null ? player.getSkillPoints() : 0) - basketSkillPoints();
        addDyn(skillPts >= 0 ? fontGo : fontBl, SKILL_VAL_BOX, 0f,
                () -> String.valueOf(skillPts), skillPts >= 0 ? GOLD : BLOCKED);
    }

    private int basketCount() {
        return entries.stream().mapToInt(e -> e.count).sum();
    }

    private int currentSkillLevel(LearnEntry entry) {
        return player != null ? player.getSkillLevel(entry.id) : 0;
    }

    /** Skill points consumed by the basket: 1 per skill point, 5 per spell (V3_TrainDlg). */
    private int basketSkillPoints() {
        return entries.stream().mapToInt(e -> e.count * (e.isSkill() ? 1 : SKILL_POINTS_PER_SPELL)).sum();
    }

    private long basketGoldCost() {
        return entries.stream().mapToLong(e -> (long) e.count * priceOf(e)).sum();
    }

    /** Gold actually charged: each queued skill point or spell costs its price. */
    private long basketGoldNeeded() {
        return basketGoldCost();
    }

    /** APPRENDRE: learns every basketed spell / trains every basketed skill point. */
    private void learnBasket() {
        if (player == null || basketCount() == 0) {
            return;
        }
        int cost = (int) basketGoldNeeded();
        int pts  = basketSkillPoints();
        if (player.getGold() < cost) {
            SystemMessage.showShared(I18n.message("message.learn_not_enough_gold"));
            return;
        }
        if (player.getSkillPoints() < pts) {
            SystemMessage.showShared(I18n.message("message.learn_not_enough_points"));
            return;
        }
        for (LearnEntry entry : entries) {
            if (entry.count == 0 || entry.isSkill()) {
                continue;
            }
            String reason = blockReason(entry);
            if (reason != null) {
                SystemMessage.showShared(I18n.message("message.spell_cannot_learn", 
                        I18n.key(entry.spell.getKey(), I18n.resolve(entry.spell.getName())), reason.toLowerCase()));
                return;
            }
        }
        player.setGold(player.getGold() - cost);
        player.setSkillPoints(player.getSkillPoints() - pts);
        List<String> spells = player.getSpells();
        if (spells == null) {
            spells = new ArrayList<>();
            player.setSpells(spells);
        }
        List<String> learnedNames = new ArrayList<>();
        for (LearnEntry entry : entries) {
            if (entry.count == 0) {
                continue;
            }
            if (entry.isSkill()) {
                int current = player.getSkillLevel(entry.id);
                player.setSkillLevel(entry.id, current + entry.count);
                SystemMessage.showShared(I18n.message("message.stat_increased", 
                        skillName(entry), current + entry.count));
            } else {
                spells.add(entry.id);
                learnedNames.add(I18n.key(entry.spell.getKey(), I18n.resolve(entry.spell.getName())));
            }
            entry.count = 0;
        }
        if (learnedNames.size() == 1) {
            SystemMessage.showShared(I18n.message("message.spell_learned", 
                    learnedNames.get(0)));
        } else if (learnedNames.size() > 1) {
            SystemMessage.showShared(I18n.message("message.spells_learned", 
                    learnedNames.size(), String.join(", ", learnedNames)));
        }
        PlayerStateStore.save(player);
        SoundManager.animateSound("Page turning sound.wav");
        rebuildList();
    }

    /** Spin UP: queue the spell (once) or one more skill point in the basket. */
    private void basketAdd(LearnEntry entry) {
        if (entry.isSkill()) {
            int current = currentSkillLevel(entry);
            // Each queued point consumes 1 skill point, as the original client does.
            if (player != null && basketSkillPoints() + 1 > player.getSkillPoints()) {
                return;
            }
            entry.count++;
        } else {
            if (isMaxed(entry) || entry.count > 0) {
                return;
            }
            entry.count = 1;
        }
        selected = entry;
        rebuildList();
    }

    /** Spin DOWN: remove the spell / one skill point from the basket. */
    private void basketRemove(LearnEntry entry) {
        if (entry.count > 0) {
            entry.count--;
            rebuildList();
        }
    }

    // ── Learning ──────────────────────────────────────────────────────────────

    private void onRowClick(LearnEntry entry) {
        selected = entry;
        rebuildList();
    }

    private boolean isKnown(String spellName) {
        return player != null && player.getSpells() != null && player.getSpells().contains(spellName);
    }

    /** Spell already known, or skill at its trainer cap. */
    private boolean isMaxed(LearnEntry entry) {
        if (entry.isSkill()) {
            return false;
        }
        return isKnown(entry.id);
    }

    /** Returns the first unmet learning condition, or null when eligible. */
    private String blockReason(LearnEntry entry) {
        if (player == null) {
            return I18n.message("message.learn_unavailable");
        }
        if (entry.isSkill()) {
            return player.getGold() < priceOf(entry) ? I18n.message("message.learn_not_enough_gold") : null;
        }
        SpellData spell = entry.spell;
        if (player.getLevel() < spell.getMinLevel()) {
            return I18n.message("message.learn_need_level", spell.getMinLevel());
        }
        if (player.getIntelligence() < spell.getMinInt()) {
            return I18n.message("message.learn_need_intelligence", spell.getMinInt());
        }
        if (player.getWisdom() < spell.getMinWis()) {
            return I18n.message("message.learn_need_wisdom", spell.getMinWis());
        }
        if (player.getGold() < priceOf(entry)) {
            return I18n.message("message.learn_not_enough_gold");
        }
        return null;
    }

    @Override
    protected String blockedReason(ListRow row) {
        LearnEntry entry = (LearnEntry) row;
        return isMaxed(entry) ? null : blockReason(entry);
    }

    @Override
    protected String rowTooltipReason(ListRow row) {
        LearnEntry entry = (LearnEntry) row;
        return isMaxed(entry) ? I18n.message("message.learn_already_known") : blockReason(entry);
    }

    /** Localized skill name, falling back to the English display name. */
    private static String skillName(LearnEntry entry) {
        return I18n.key("skill." + entry.id);
    }

    /** A trainer can override a spell's default catalogue price, as GoN does. */
    private static int priceOf(LearnEntry entry) {
        if (entry.isSkill()) {
            return 0;
        }
        return entry.spell.getPrice();
    }

    /** One list row: either a taught spell or a trainable skill. */
    @Override
    protected List<? extends ListRow> rows() {
        return entries;
    }

    @Override
    protected void basketAdd(ListRow row) {
        basketAdd((LearnEntry) row);
    }

    @Override
    protected void basketRemove(ListRow row) {
        basketRemove((LearnEntry) row);
    }

    @Override
    protected Color blockedColor() {
        return BLOCKED;
    }

    @Override
    protected float rowZoneWidth() {
        return 370f;
    }

    @Override
    protected boolean pageTurnSound() {
        return true;
    }

    private static final class LearnEntry implements ListRow {
        private final String id;
        private final SpellData spell;             // null for skills
        private final boolean skill;
        private final String skillDisplayName;
        // Basketed quantity: 0/1 for a spell, any number of points for a skill.
        private int count;

        private LearnEntry(String id, SpellData spell,
                           boolean skill, String skillDisplayName) {
            this.id = id;
            this.spell = spell;
            this.skill = skill;
            this.skillDisplayName = skillDisplayName;
        }

        static LearnEntry forSpell(String spellId, SpellData spell) {
            return new LearnEntry(spellId, spell, false, null);
        }

        static LearnEntry forSkill(String skillId) {
            return new LearnEntry(skillId, null, true, skillDisplayName(skillId));
        }

        boolean isSkill() {
            return skill;
        }

        @Override public String nameText() {
            return isSkill() ? skillDisplayName : I18n.key(spell.getKey(), I18n.resolve(spell.getName()));
        }
        @Override public String priceText() { return String.valueOf(priceOf(this)); }
        @Override public String thirdColumnText() { return String.valueOf(count); }
        @Override public String iconSprite() {
            return isSkill() ? SKILL_ICONS.get(id) : spell.getIconId();
        }
        @Override public int getCount() { return count; }

        private static String skillDisplayName(String statId) {
            if (I18n.has("skill." + statId)) {
                return I18n.key("skill." + statId);
            }
            StringBuilder sb = new StringBuilder();
            for (String word : statId.split("_")) {
                if (!word.isEmpty()) {
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(Character.toUpperCase(word.charAt(0)));
                    sb.append(word.substring(1).toLowerCase());
                }
            }
            return sb.toString();
        }
    }

}

