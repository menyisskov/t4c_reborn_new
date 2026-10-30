package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.perso.T4C.combat.ArmorClassRules;
import com.perso.T4C.gui.core.CharacterPanel;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.session.RemotePlayerCommands;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.HudTooltip;
import com.perso.T4C.ui.PlayerHUD;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Statistics extends CharacterPanel {
  private final Player player;
  private final PlayerHUD hud;
  private final RemotePlayerCommands remotePlayerCommands;
  private final List<StatHoverInfo> statHoverInfos = new ArrayList<>();
  private final HudTooltip tooltip = new HudTooltip();
  private final Map<String, Integer> pendingAllocations = new LinkedHashMap<>();
  private final Map<String, GuiButton> statUpButtons = new LinkedHashMap<>();
  private final Map<String, GuiButton> statDownButtons = new LinkedHashMap<>();
  private GuiButton applyButton;
  private boolean remoteAllocationInFlight;

  public Statistics(Player player) {
    this(player, null);
  }

  public Statistics(Player player, PlayerHUD hud) {
    super(player, hud, "ui.journal.character", "character");
    this.player = player;
    this.hud = hud;
    this.remotePlayerCommands = hud == null ? null : hud.remotePlayerCommands();
    addStatLabels();
    addStatPointButtons();
  }

  private void addStatLabels() {
    value(player::getName, 30, 105, 260, 30, 22, JournalTheme.TEXT);
    value(
        () -> I18n.key("ui.journal.level") + " " + player.getLevel(),
        370,
        109,
        270,
        24,
        15,
        JournalTheme.GOLD);
    value(
        () ->
            I18n.key("ui.stat_points_short")
                + "  "
                + (player.getStatPoints() - spentPendingPoints()),
        30,
        141,
        198,
        22,
        12,
        JournalTheme.DIM);
    value(
        () -> I18n.key("ui.skill_points_short") + "  " + player.getSkillPoints(),
        370,
        141,
        280,
        22,
        12,
        JournalTheme.DIM);
    var font = FontManager.getInstance().getTahomaFont(15, JournalTheme.TEXT, false);
    java.util.function.IntSupplier[] bases = {
      player::getStrength,
      player::getEndurance,
      player::getDexterity,
      player::getWisdom,
      player::getIntelligence
    };
    java.util.function.IntSupplier[] effective = {
      player::getEffectiveStrength,
      player::getEffectiveEndurance,
      player::getEffectiveDexterity,
      player::getEffectiveWisdom,
      player::getEffectiveIntelligence
    };
    String[] names = {
      "ui.strength", "ui.endurance", "ui.dexterity", "ui.wisdom", "ui.intelligence"
    };
    for (int i = 0; i < STAT_IDS.length; i++) {
      int index = i;
      text(names[i], 40, STAT_ROW_Y[i], 140, 28, 13, JournalTheme.DIM);
      labels.add(
          buffedStatLabel(
              font,
              I18n.key(names[i]),
              STAT_IDS[i],
              x + 182,
              y + STAT_ROW_Y[i],
              110,
              28,
              () -> bases[index].getAsInt() + pending(STAT_IDS[index]),
              effective[i]));
    }
    value(
        () ->
            I18n.key("ui.journal.health")
                + "  "
                + player.getCurrentHp()
                + " / "
                + player.getMaxHp(),
        376,
        179,
        306,
        28,
        13,
        JournalTheme.TEXT);
    value(
        () -> I18n.key("ui.journal.mana") + "  " + player.getMana() + " / " + player.getMaxMana(),
        376,
        220,
        306,
        28,
        13,
        JournalTheme.TEXT);
    addCombatSkillRows(font);
    text("ui.armor_class", 44, 405, 170, 26, 14, JournalTheme.GOLD);
    value(
        () ->
            String.format(
                Locale.ROOT,
                "%,d",
                displayedArmorClass(ArmorClassRules.effectiveArmorClass(player))),
        222,
        400,
        108,
        34,
        22,
        JournalTheme.GOLD);
    value(
        () ->
            I18n.key("ui.journal.weight")
                + "  "
                + InventoryService.currentWeight(player)
                + " / "
                + InventoryService.maximumWeight(player),
        38,
        437,
        300,
        24,
        12,
        JournalTheme.DIM);
    value(
        () ->
            I18n.key("ui.experience")
                + "  "
                + String.format(Locale.ROOT, "%,d", player.getCurrentXp()),
        370,
        405,
        318,
        24,
        12,
        JournalTheme.TEXT);
    value(
        () ->
            I18n.key("ui.xp_to_next_level")
                + "  "
                + String.format(Locale.ROOT, "%,d", player.getXpToNextLevel()),
        370,
        437,
        318,
        24,
        12,
        JournalTheme.DIM);
    text("ui.journal.stats_hint", 30, 487, 660, 20, 11, JournalTheme.DIM);
  }

  private static final float STAT_SPIN_X = 309f;
  private static final float STAT_SPIN_UP_DY = 0f;
  private static final float STAT_SPIN_DN_DY = 15f;
  private static final String[] STAT_IDS = {
    "strength", "endurance", "dexterity", "wisdom", "intelligence"
  };
  private static final float[] STAT_ROW_Y = {178f, 220f, 262f, 304f, 346f};
  private static final float APPLY_BTN_X = 240f;
  private static final float APPLY_BTN_Y = 141f;

  private void addStatPointButtons() {
    if (player == null || background == null) {
      return;
    }
    for (int i = 0; i < STAT_IDS.length; i++) {
      String statId = STAT_IDS[i];
      float rowY = STAT_ROW_Y[i];
      GuiButton up =
          theme
              .button(
                  x + STAT_SPIN_X,
                  y + rowY + STAT_SPIN_UP_DY,
                  22,
                  17,
                  () -> "+",
                  () -> allocatePoint(statId))
              .repeatable(true);
      GuiButton down =
          theme
              .button(
                  x + STAT_SPIN_X,
                  y + rowY + STAT_SPIN_DN_DY,
                  22,
                  17,
                  () -> "−",
                  () -> deallocatePoint(statId))
              .repeatable(true);
      buttons.add(up);
      buttons.add(down);
      statUpButtons.put(statId, up);
      statDownButtons.put(statId, down);
    }
    addApplyButton();
    refreshStatPointButtonsVisibility();
  }

  private void addApplyButton() {
    applyButton =
        theme.button(
            x + APPLY_BTN_X,
            y + APPLY_BTN_Y,
            94,
            26,
            () -> I18n.key("ui.apply"),
            this::applyPendingAllocations);
    buttons.add(applyButton);
  }

  private int pending(String statId) {
    return pendingAllocations.getOrDefault(statId, 0);
  }

  private int spentPendingPoints() {
    return pendingAllocations.values().stream().mapToInt(Integer::intValue).sum();
  }

  private void allocatePoint(String statId) {
    if (player == null || remoteAllocationInFlight || spentPendingPoints() >= player.getStatPoints()) {
      return;
    }
    pendingAllocations.merge(statId, 1, Integer::sum);
    refreshStatPointButtonsVisibility();
  }

  private void deallocatePoint(String statId) {
    if (remoteAllocationInFlight) return;
    Integer current = pendingAllocations.get(statId);
    if (current == null || current <= 0) {
      return;
    }
    if (current == 1) {
      pendingAllocations.remove(statId);
    } else {
      pendingAllocations.put(statId, current - 1);
    }
    refreshStatPointButtonsVisibility();
  }

  private void applyPendingAllocations() {
    if (player == null || pendingAllocations.isEmpty()) {
      return;
    }
    if (remotePlayerCommands != null) {
      if (remoteAllocationInFlight) return;
      remoteAllocationInFlight = true;
      refreshStatPointButtonsVisibility();
      boolean queued = remotePlayerCommands.allocateStats(
          Map.copyOf(pendingAllocations), this::completeRemoteAllocation);
      if (!queued) {
        remoteAllocationInFlight = false;
        refreshStatPointButtonsVisibility();
      }
      return;
    }
    for (Map.Entry<String, Integer> entry : pendingAllocations.entrySet()) {
      switch (entry.getKey()) {
        case "strength" -> player.setStrength(player.getStrength() + entry.getValue());
        case "endurance" -> player.setEndurance(player.getEndurance() + entry.getValue());
        case "dexterity" -> player.setDexterity(player.getDexterity() + entry.getValue());
        case "wisdom" -> player.setWisdom(player.getWisdom() + entry.getValue());
        case "intelligence" -> player.setIntelligence(player.getIntelligence() + entry.getValue());
        default -> {}
      }
    }
    player.setStatPoints(player.getStatPoints() - spentPendingPoints());
    pendingAllocations.clear();
    PlayerHUD.persistenceOf(hud).save(player);
    refreshStatPointButtonsVisibility();
  }

  private void completeRemoteAllocation(com.perso.T4C.online.OnlineConnection.CommandResult result) {
    if (result != null && result.accepted() && result.character() != null
        && result.character().progress() != null) {
      var progress = result.character().progress();
      var attributes = progress.attributes();
      player.setStrength(attributes.strength());
      player.setEndurance(attributes.endurance());
      player.setDexterity(attributes.dexterity());
      player.setWisdom(attributes.wisdom());
      player.setIntelligence(attributes.intelligence());
      player.setStatPoints(progress.advancement().statPoints());
      pendingAllocations.clear();
    }
    remoteAllocationInFlight = false;
    refreshStatPointButtonsVisibility();
  }

  private void refreshStatPointButtonsVisibility() {
    boolean hasStatPoints = player != null && player.getStatPoints() > 0;
    boolean canAllocate = !remoteAllocationInFlight && hasStatPoints && spentPendingPoints() < player.getStatPoints();
    for (Map.Entry<String, GuiButton> entry : statUpButtons.entrySet()) {
      entry.getValue().setVisible(canAllocate);
    }
    for (GuiButton down : statDownButtons.values()) {
      down.setVisible(!remoteAllocationInFlight && hasStatPoints);
    }
    if (applyButton != null) {
      applyButton.setVisible(!remoteAllocationInFlight && !pendingAllocations.isEmpty());
    }
  }

  private static final float[] SKILL_ICON_BOX = {374f, 264f, 30f, 30f};
  private static final float[] SKILL_VALUE_BOX = {584f, 267f, 96f, 24f};
  private static final float SKILL_ROW_PITCH = 42f;
  private static final java.util.Map<String, String> SKILL_ICONS =
      java.util.Map.ofEntries(
          java.util.Map.entry("stun_blow", "64kIconStunBlow"),
          java.util.Map.entry("powerful_blow", "64kIconPowerBlow"),
          java.util.Map.entry("first_aid", "64kIconFirstAid"),
          java.util.Map.entry("parry", "64kIconParry"),
          java.util.Map.entry("meditate", "64kIconMeditate"),
          java.util.Map.entry("dodge", "64kIconShield"),
          java.util.Map.entry("attack", "64kIconSword"),
          java.util.Map.entry("hide", "64kIconHide"),
          java.util.Map.entry("rob", "64kIconRob"),
          java.util.Map.entry("sneak", "64kIconSneak"),
          java.util.Map.entry("search", "64kIconSearch"),
          java.util.Map.entry("pick_lock", "64kIconPicklock"),
          java.util.Map.entry("armor_penetration", "64kIconArmorPierce"),
          java.util.Map.entry("peek", "64kIconPeek"),
          java.util.Map.entry("rapid_healing", "64kIconRapidHealing"),
          java.util.Map.entry("archery", "64kIconBow"),
          java.util.Map.entry("two_weapons", "64kIconDualSword"));

  private void addCombatSkillRows(com.badlogic.gdx.graphics.g2d.BitmapFont font) {
    String[] skillIds = {"attack", "dodge", "archery"};
    for (int i = 0; i < skillIds.length; i++) {
      String skillId = skillIds[i];
      float iconX = x + SKILL_ICON_BOX[0];
      float iconY = y + SKILL_ICON_BOX[1] + i * SKILL_ROW_PITCH;
      TextureRegion icon = GuiSprites.load(SKILL_ICONS.get(skillId));
      if (icon != null) {
        animatedSprites.add(
            new GuiAnimatedSprite(List.of(icon), iconX, iconY, 1f)
                .boxed(SKILL_ICON_BOX[2], SKILL_ICON_BOX[3]));
      }
      float valueX = x + SKILL_VALUE_BOX[0];
      float valueY = y + SKILL_VALUE_BOX[1] + i * SKILL_ROW_PITCH;
      String skillLabel = com.perso.T4C.i18n.I18n.key("skill." + skillId);
      value(() -> skillLabel, 416, 267 + i * SKILL_ROW_PITCH, 158, 24, 13, JournalTheme.DIM);
      labels.add(
          buffedStatLabel(
              font,
              skillLabel,
              "skill:" + skillId,
              valueX,
              valueY,
              SKILL_VALUE_BOX[2],
              SKILL_VALUE_BOX[3],
              () -> player.getSkillLevel(skillId),
              () -> player.getEffectiveSkillLevel(skillId)));
    }
  }

  private static int displayedArmorClass(double armorClass) {
    return (int) Math.floor(Math.max(0d, armorClass));
  }

  private GuiText buffedStatLabel(
      com.badlogic.gdx.graphics.g2d.BitmapFont font,
      String displayName,
      String buffAttribute,
      float px,
      float py,
      float width,
      float height,
      java.util.function.IntSupplier base,
      java.util.function.IntSupplier effective) {
    statHoverInfos.add(
        new StatHoverInfo(displayName, buffAttribute, px, py, width, height, base, effective));
    return new GuiBoxedText(
        font,
        px,
        py,
        width,
        height,
        () -> {
          int b = base.getAsInt();
          int e = effective.getAsInt();
          int bonus = e - b;
          return bonus > 0 ? String.valueOf(e) : String.valueOf(b);
        },
        () -> {
          int bonus = effective.getAsInt() - base.getAsInt();
          return bonus > 0 ? Color.GREEN : Color.WHITE;
        });
  }

  @Override
  public void dispose() {
    tooltip.dispose();
    super.dispose();
  }

  @Override
  public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
    renderPanelBackground(batch);
    theme.accentWell(batch, x + 30, y + 397, 308, 40);
    for (float row : STAT_ROW_Y) well(batch, 30, row - 2, 308, 36);
    for (int i = 0; i < 5; i++) well(batch, 366, 176 + i * 42, 326, 36);
    for (GuiElement element : orderedElements()) element.render(batch);
    updateStatTooltip();
    tooltip.render(batch);
  }

  private void updateStatTooltip() {
    if (player == null) {
      tooltip.clear();
      return;
    }
    float mouseX = Gdx.input.getX();
    float mouseY = Gdx.input.getY();
    for (StatHoverInfo info : statHoverInfos) {
      if (!info.contains(mouseX, mouseY) || info.effective.getAsInt() <= info.base.getAsInt()) {
        continue;
      }
      String text = buildStatTooltipText(info);
      if (!text.isEmpty()) {
        tooltip.show(text, mouseX, mouseY);
        return;
      }
    }
    tooltip.clear();
  }

  private String buildStatTooltipText(StatHoverInfo info) {
    StringBuilder text = new StringBuilder();
    text.append(info.displayName).append(": ").append(info.base.getAsInt());
    for (Player.ActiveBuff buff : player.getActiveBuffs()) {
      int amount = buffAmountForAttribute(buff, info.buffAttribute);
      if (amount == 0) {
        continue;
      }
      text.append('\n')
          .append(I18n.key("tooltip.buffed_by"))
          .append(' ')
          .append(I18n.resolve(buff.getSpellName()))
          .append(' ')
          .append('(')
          .append(amount > 0 ? "+" : "")
          .append(amount)
          .append(')');
    }
    return text.toString();
  }

  private int buffAmountForAttribute(Player.ActiveBuff buff, String attribute) {
    if (buff == null || attribute == null) {
      return 0;
    }
    int total = 0;
    for (SpellData.SpellEffect effect : buff.getEffects()) {
      if (effect == null || effect.getType() == null || effect.getAttribute() == null) {
        continue;
      }
      if (!"ATTRIBUTE".equalsIgnoreCase(effect.getType())) {
        continue;
      }
      if (!attributeMatches(attribute, effect.getAttribute())) {
        continue;
      }
      total += parseAmount(effect.getAmount());
    }
    return total;
  }

  private boolean attributeMatches(String expected, String actual) {
    String normalizedExpected = normalizeAttribute(expected);
    String normalizedActual = normalizeAttribute(actual);
    return !normalizedExpected.isEmpty() && normalizedExpected.equals(normalizedActual);
  }

  private String normalizeAttribute(String attribute) {
    if (attribute == null) {
      return "";
    }
    String value = attribute.toLowerCase(Locale.ROOT).replace(" ", "").trim();
    return switch (value) {
      case "str" -> "strength";
      case "dex" -> "dexterity";
      case "end", "endurance", "con" -> "constitution";
      case "int" -> "intelligence";
      case "wis" -> "wisdom";
      case "ac", "armor", "armorclass", "defense" -> "armorclass";
      default -> value;
    };
  }

  private int parseAmount(String value) {
    if (value == null || value.isBlank()) {
      return 0;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private static final class StatHoverInfo {
    private final String displayName;
    private final String buffAttribute;
    private final Rectangle bounds;
    private final java.util.function.IntSupplier base;
    private final java.util.function.IntSupplier effective;

    private StatHoverInfo(
        String displayName,
        String buffAttribute,
        float x,
        float y,
        float width,
        float height,
        java.util.function.IntSupplier base,
        java.util.function.IntSupplier effective) {
      this.displayName = displayName;
      this.buffAttribute = buffAttribute;
      this.bounds = new Rectangle(x, y, width, height);
      this.base = base;
      this.effective = effective;
    }

    private boolean contains(float x, float y) {
      return bounds.contains(x, y);
    }
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    return super.onKeyDown(keycode);
  }
}
