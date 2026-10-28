package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.MyGame;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.ItemSalePricing;
import com.perso.T4C.item.ItemSaleService;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * Stage 0 safety net for the client unification: headless pins of the single-player entry points
 * that the session seam will wrap (the pure-ish parts of the coupling listed in
 * multiplayer/CLIENT_UNIFICATION_DESIGN.md). Values here are copied from today's behaviour on
 * purpose; a refactor that changes one must change it on purpose, in its own commit.
 *
 * <p>Not pinnable without a GL context (pinned instead by {@link MainGameScreenGoldenTest}): the
 * rendered screens, and the 15 s autosave cadence end to end. Not pinned: the shop basket
 * arithmetic ({@code ShopScreen.buyBasket} is a private method of a GL-bound screen) and the melee
 * tick (inline in {@code MainGameScreen.render}); both are listed as gaps in the stage 0 report and
 * in PEER_WORK.
 */
class SinglePlayerSeamPinsTest {
  @Test
  void autosaveIntervalIsFifteenSeconds() throws Exception {
    var field = MainGameScreen.class.getDeclaredField("AUTOSAVE_INTERVAL_SECONDS");
    field.setAccessible(true);
    assertEquals(15f, field.getFloat(null));
  }

  @Test
  void theScreenIsBuiltThroughOneGameArgumentAndLoadsInSevenSteps() throws Exception {
    assertEquals(7, MainGameScreen.loadingStepCount());
    var constructor = MainGameScreen.class.getConstructor(MyGame.class);
    assertTrue(Modifier.isPublic(constructor.getModifiers()));
    assertTrue(
        Arrays.stream(MainGameScreen.class.getDeclaredMethods())
            .anyMatch(
                m ->
                    m.getName().equals("createProgressive")
                        && Modifier.isStatic(m.getModifiers())));
    assertEquals(Player.class, MainGameScreen.class.getMethod("getPlayer").getReturnType());
  }

  /** The seven Player callback hooks the screen wires; a new one is a new coupling point. */
  @Test
  void playerExposesExactlyTheSevenCallbackInterfaces() {
    Set<String> callbacks =
        new TreeSet<>(
            Arrays.stream(Player.class.getDeclaredClasses())
                .filter(Class::isInterface)
                .map(Class::getSimpleName)
                .toList());
    assertEquals(
        new TreeSet<>(
            List.of(
                "DeathCallback",
                "HealingCallback",
                "ItemDropCallback",
                "LevelUpCallback",
                "ManaRestoredCallback",
                "MessageCallback",
                "PositionCallback")),
        callbacks);
  }

  @Test
  void equipAndUnequipMoveOneItemBetweenBackpackAndSlotAndSurviveASaveLoad() {
    Player player = new Player();
    player.setStrength(5000);
    player.setEndurance(5000);
    player.setDexterity(5000);
    player.setIntelligence(5000);
    player.setWisdom(5000);
    player.setSkillLevel("attack", 5000);
    String armor =
        ItemRegistry.load().stream()
            .filter(item -> item.getBodyPart() == BodyPart.BODY)
            .findFirst()
            .orElseThrow()
            .getKey();
    player.getInventory().add(armor);

    assertTrue(InventoryService.equip(player, BodyPart.BODY, armor).success());
    assertFalse(player.getInventory().contains(armor));
    assertEquals(armor, player.getEquippedItems().get(BodyPart.BODY));

    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(player), restored);
    assertEquals(armor, restored.getEquippedItems().get(BodyPart.BODY));

    assertTrue(InventoryService.unequip(player, BodyPart.BODY).success());
    assertEquals(List.of(armor), player.getInventory());
    assertFalse(player.getEquippedItems().containsKey(BodyPart.BODY));
  }

  @Test
  void sellingPaysTheListedPricePerRemovedItemAndNothingForItemsNotOwned() {
    var sellable =
        ItemRegistry.load().stream()
            .filter(item -> ItemSalePricing.sellPrice(item) > 0)
            .findFirst()
            .orElseThrow();
    Player player = new Player();
    player.setGold(100);
    player.getInventory().add(sellable.getKey());
    player.getInventory().add(sellable.getKey());
    long price = ItemSalePricing.sellPrice(sellable);

    assertEquals(price, ItemSaleService.sell(player, sellable.getKey(), 1));
    assertEquals(100 + price, player.getGold());
    assertEquals(1, InventoryService.count(player, sellable.getKey()));
    assertEquals(
        price, ItemSaleService.sell(player, sellable.getKey(), 5), "only one is left to sell");
    assertEquals(
        0, ItemSaleService.sell(player, sellable.getKey(), 5), "nothing left, nothing paid");
    assertEquals(100 + 2 * price, player.getGold());
  }
}
