package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.perso.T4C.combat.ArmorClassRules;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.HudTooltip;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Statistics extends GuiScreenBase {
  private final Player player;
  private final List<StatHoverInfo> statHoverInfos = new ArrayList<>();
  private final HudTooltip tooltip = new HudTooltip();
  private final Map<String, Integer> pendingAllocations = new LinkedHashMap<>();
  private final Map<String, GuiButton> statUpButtons = new LinkedHashMap<>();
  private final Map<String, GuiButton> statDownButtons = new LinkedHashMap<>();
  private final List<GuiClickZone> tabZones = new ArrayList<>();
  private GuiButton applyButton;

  public Statistics(Player player) {
    this.player = player;
    try {
      background = SpriteLoader.getInstance().getRegionFromSpriteName("GUI_BackStat");
    } catch (GameException ignored) {
      background = null;
    }
    centerOnScreen();
    addCloseButton();
    addTabs();
    addStatLabels();
    addStatPointButtons();
  }

  private void addTabs() {
    if (background == null || player == null) return;
    var white = Color.WHITE;
    var gold = Color.valueOf("F2B705");
    var activeTabFont = FontManager.getInstance().getJetBrainsMonoFont(12, gold);
    var tabFont = FontManager.getInstance().getJetBrainsMonoFont(12, white);
    labels.add(
        new GuiText(activeTabFont, x + 10f, y + 15f, () -> "[ " + I18n.key("ui.character_sheet") + " ]"));
    labels.add(
        new GuiText(
            tabFont, x + 150f, y + 15f, () -> "[ " + I18n.key("ui.elemental_stats") + " ]"));
    tabZones.add(
        new GuiClickZone(
            x + 150f,
            y + 5f,
            130f,
            16f,
            () -> GuiManager.open(new com.perso.T4C.gui.screen.ElementalStats(player))));
  }

  @Override
  public void onTouchUp(float screenX, float screenY) {
    for (GuiClickZone zone : new ArrayList<>(tabZones)) {
      if (zone.contains(screenX, screenY)) {
        zone.run();
        return;
      }
    }
    super.onTouchUp(screenX, screenY);
  }

  private void addCloseButton() {
    addCloseButton(550f, 1f);
  }

  public GuiButton addButton(
      String normalSprite,
      String hoverSprite,
      String pressedSprite,
      float positionX,
      float positionY,
      Runnable callback) {
    try {
      var loader = SpriteLoader.getInstance();
      var normal = loader.getRegionFromSpriteName(normalSprite);
      var hover = loader.getRegionFromSpriteName(hoverSprite);
      var pressed = loader.getRegionFromSpriteName(pressedSprite);
      if (normal == null || hover == null || pressed == null) {
        return null;
      }
      GuiButton button = new GuiButton(normal, hover, pressed, positionX, positionY, callback);
      buttons.add(button);
      return button;
    } catch (GameException e) {
      return null;
    }
  }

  private void addStatLabels() {
    if (player == null || background == null) {
      return;
    }
    var font = FontManager.getInstance().getJetBrainsMonoFont(14, Color.WHITE);
    var gold = Color.valueOf("F2B705");
    labels.add(
        new GuiBoxedText(
                FontManager.getInstance().getHaettenschweilerFont(18, gold),
                x + 221f,
                y + 2f,
                134f,
                19f,
                () -> com.perso.T4C.i18n.I18n.key("ui.character_sheet"),
                () -> gold)
            .shrinkToFit());
    labels.add(
        buffedStatLabel(
            font,
            I18n.key("ui.strength"),
            "strength",
            x + 51f,
            y + 50f,
            90f,
            16f,
            () -> player.getStrength() + pending("strength"),
            player::getEffectiveStrength));
    labels.add(
        buffedStatLabel(
            font,
            I18n.key("ui.endurance"),
            "constitution",
            x + 51f,
            y + 93f,
            90f,
            16f,
            () -> player.getEndurance() + pending("endurance"),
            player::getEffectiveEndurance));
    labels.add(
        buffedStatLabel(
            font,
            I18n.key("ui.dexterity"),
            "dexterity",
            x + 51f,
            y + 136f,
            90f,
            16f,
            () -> player.getDexterity() + pending("dexterity"),
            player::getEffectiveDexterity));
    labels.add(
        buffedStatLabel(
            font,
            I18n.key("ui.wisdom"),
            "wisdom",
            x + 51f,
            y + 179f,
            90f,
            16f,
            () -> player.getWisdom() + pending("wisdom"),
            player::getEffectiveWisdom));
    labels.add(
        buffedStatLabel(
            font,
            I18n.key("ui.intelligence"),
            "intelligence",
            x + 51f,
            y + 222f,
            90f,
            16f,
            () -> player.getIntelligence() + pending("intelligence"),
            player::getEffectiveIntelligence));
    labels.add(
        new GuiBoxedText(
            font,
            x + 255f,
            y + 50f,
            28f,
            20f,
            () -> String.valueOf(player.getLevel()),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 202f,
            y + 136f,
            90f,
            17f,
            () -> player.getCurrentHp() + " / " + player.getMaxHp(),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 202f,
            y + 179f,
            90f,
            17f,
            () -> player.getMana() + " / " + player.getMaxMana(),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
                font,
                x + 202f,
                y + 222f,
                90f,
                17f,
                () ->
                    InventoryService.currentWeight(player)
                        + " / "
                        + InventoryService.maximumWeight(player),
                () -> Color.WHITE)
            .shrinkToFit());
    labels.add(
        new GuiBoxedText(
            font,
            x + 330f,
            y + 118f,
            69f,
            14f,
            () -> String.valueOf(player.getStatPoints() - spentPendingPoints()),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 330f,
            y + 169f,
            69f,
            14f,
            () -> String.valueOf(player.getSkillPoints()),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 37f,
            y + 299f,
            176f,
            15f,
            () -> String.valueOf(player.getCurrentXp()),
            () -> Color.WHITE));
    labels.add(
        new GuiBoxedText(
            font,
            x + 231f,
            y + 299f,
            176f,
            15f,
            () -> String.valueOf(player.getXpToNextLevel()),
            () -> Color.WHITE));
    labels.add(
        buffedStatLabel(
            font,
            I18n.key("ui.armor_class"),
            "armorclass",
            x + 217f,
            y + 52f,
            35f,
            15f,
            () -> displayedArmorClass(ArmorClassRules.trueArmorClass(player)),
            () -> displayedArmorClass(ArmorClassRules.effectiveArmorClass(player))));
    var infoFont = FontManager.getInstance().getJetBrainsMonoFont(11, gold);
    addCombatSkillRows(font);
    labels.add(
        new GuiBoxedText(
            infoFont, x + 37f, y + 278f, 176f, 17f, () -> I18n.key("ui.experience"), () -> gold));
    labels.add(
        new GuiBoxedText(
            infoFont,
            x + 231f,
            y + 278f,
            176f,
            17f,
            () -> I18n.key("ui.xp_to_next_level"),
            () -> gold));
    labels.add(
        new GuiBoxedText(
            infoFont, x + 424f, y + 278f, 114f, 17f, () -> I18n.key("ui.karma"), () -> gold));
    labels.add(
        new GuiBoxedText(
            infoFont,
            x + 330f,
            y + 99f,
            69f,
            16f,
            () -> I18n.key("ui.stat_points_short"),
            () -> gold));
    labels.add(
        new GuiBoxedText(
            infoFont,
            x + 330f,
            y + 150f,
            69f,
            16f,
            () -> I18n.key("ui.skill_points_short"),
            () -> gold));
  }

  private static final float STAT_SPIN_X = 147f;
  private static final float STAT_SPIN_UP_DY = 45f - 50f;
  private static final float STAT_SPIN_DN_DY = 61f - 50f;
  private static final String[] STAT_IDS = {
    "strength", "endurance", "dexterity", "wisdom", "intelligence"
  };
  private static final float[] STAT_ROW_Y = {50f, 93f, 136f, 179f, 222f};
  private static final float APPLY_BTN_X = 330f;
  private static final float APPLY_BTN_Y = 190f;

  private void addStatPointButtons() {
    if (player == null || background == null) {
      return;
    }
    var upN = GuiSprites.load("GUI_SpinUp");
    var upH = GuiSprites.load("GUI_SpinHUp");
    var dnN = GuiSprites.load("GUI_SpinDown");
    var dnH = GuiSprites.load("GUI_SpinHDown");
    if (upN == null || dnN == null) {
      return;
    }
    for (int i = 0; i < STAT_IDS.length; i++) {
      String statId = STAT_IDS[i];
      float rowY = STAT_ROW_Y[i];
      GuiButton up =
          new GuiButton(
              upN,
              upH != null ? upH : upN,
              upH != null ? upH : upN,
              x + STAT_SPIN_X,
              y + rowY + STAT_SPIN_UP_DY,
              () -> allocatePoint(statId));
      GuiButton down =
          new GuiButton(
              dnN,
              dnH != null ? dnH : dnN,
              dnH != null ? dnH : dnN,
              x + STAT_SPIN_X,
              y + rowY + STAT_SPIN_DN_DY,
              () -> deallocatePoint(statId));
      buttons.add(up);
      buttons.add(down);
      statUpButtons.put(statId, up);
      statDownButtons.put(statId, down);
    }
    addApplyButton();
    refreshStatPointButtonsVisibility();
  }

  private void addApplyButton() {
    var normal = GuiSprites.load("GUI_ButtonDisabled");
    var hover = GuiSprites.load("GUI_ButtonHUp");
    var pressed = GuiSprites.load("GUI_ButtonDown");
    if (normal == null) {
      return;
    }
    var gold = Color.valueOf("F2B705");
    var chewy = FontManager.getInstance().getHaettenschweilerFont(13, gold);
    applyButton =
        new GuiButton(
                normal,
                hover != null ? hover : normal,
                pressed != null ? pressed : normal,
                x + APPLY_BTN_X,
                y + APPLY_BTN_Y,
                this::applyPendingAllocations)
            .withLabel(chewy, () -> com.perso.T4C.i18n.I18n.key("ui.apply"));
    buttons.add(applyButton);
  }

  private int pending(String statId) {
    return pendingAllocations.getOrDefault(statId, 0);
  }

  private int spentPendingPoints() {
    return pendingAllocations.values().stream().mapToInt(Integer::intValue).sum();
  }

  private void allocatePoint(String statId) {
    if (player == null || spentPendingPoints() >= player.getStatPoints()) {
      return;
    }
    pendingAllocations.merge(statId, 1, Integer::sum);
    refreshStatPointButtonsVisibility();
  }

  private void deallocatePoint(String statId) {
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
    com.perso.T4C.helper.PlayerStateStore.save(player);
    refreshStatPointButtonsVisibility();
  }

  private void refreshStatPointButtonsVisibility() {
    boolean hasStatPoints = player != null && player.getStatPoints() > 0;
    boolean canAllocate = hasStatPoints && spentPendingPoints() < player.getStatPoints();
    for (Map.Entry<String, GuiButton> entry : statUpButtons.entrySet()) {
      entry.getValue().setVisible(canAllocate);
    }
    for (GuiButton down : statDownButtons.values()) {
      down.setVisible(hasStatPoints);
    }
    if (applyButton != null) {
      applyButton.setVisible(!pendingAllocations.isEmpty());
    }
  }

  private static final float[] SKILL_ICON_BOX = {417f, 61f, 32f, 32f};
  private static final float[] SKILL_VALUE_BOX = {462f, 64f, 40f, 24f};
  private static final float SKILL_ROW_PITCH = 43f;
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
  public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
    super.render(batch);
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
