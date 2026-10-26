package com.perso.T4C.gui.screen;

import com.perso.T4C.gui.core.CharacterPanel;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.widget.GuiPlayerPreview;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.player.RobeWardrobe;
import com.perso.T4C.ui.PlayerHUD;

/** Instant cosmetic selection with a rotating preview of the actual equipped character. */
public final class RobeWardrobeScreen extends CharacterPanel {
  public RobeWardrobeScreen(Player player, PlayerHUD hud) {
    super(player, hud, "wardrobe.title", "inventory");
    text("wardrobe.hint", 30, 111, 660, 35, 12, JournalTheme.DIM);
    for (int i = 0; i <= RobeWardrobe.STYLES.size(); i++) {
      final int choice = i;
      String key = i == 0 ? "wardrobe.default" : "wardrobe." + RobeWardrobe.STYLES.get(i - 1);
      buttons.add(
          theme.button(
              x + 34,
              y + 160 + i * 38,
              245,
              32,
              () -> I18n.key(key),
              () -> {
                RobeWardrobe.select(player, choice);
                PlayerAppearanceDefaults.applyDefaults(player);
                if (player.getAnimations() != null) player.getAnimations().refresh();
                PlayerStateStore.save(player);
                GuiManager.open(new RobeWardrobeScreen(player, hud));
              },
              RobeWardrobe.selection(player) == i));
    }
    previews.add(new GuiPlayerPreview(player, x + 490, y + 305));
    value(
        () ->
            I18n.key(
                RobeWardrobe.selection(player) == 0
                    ? "wardrobe.default"
                    : "wardrobe." + RobeWardrobe.STYLES.get(RobeWardrobe.selection(player) - 1)),
        322,
        367,
        350,
        24,
        16,
        JournalTheme.GOLD);
    text("wardrobe.stats_unchanged", 320, 403, 350, 40, 12, JournalTheme.DIM);
    buttons.add(
        theme.button(
            x + 470,
            y + 463,
            200,
            28,
            () -> I18n.key("wardrobe.back"),
            () -> GuiManager.open(new Inventory(player, hud))));
  }
}
