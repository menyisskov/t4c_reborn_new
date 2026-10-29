package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.ShopOffersReply;
import com.perso.T4C.online.model.ShopOffersReply.Line;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OnlineShopPanelTest {
  private static ShopOffersReply listing(int buyRows, long gold) {
    var buy = new ArrayList<Line>();
    for (int i = 0; i < buyRows; i++) buy.add(new Line("item.stock_" + i, 10L + i, 0));
    return new ShopOffersReply(4, gold, "Sigfried", buy,
        List.of(new Line("item.torch", 2, 3), new Line("item.wooden_club", 4, 1)));
  }

  @Test void closedPanelIgnoresEveryKey() {
    var panel = new OnlineShopPanel();
    assertFalse(panel.isOpen());
    assertTrue(panel.choose(1).isEmpty());
    assertTrue(panel.lines(key -> key).isEmpty());
    panel.toggleMode();
    panel.turnPage(1);
    assertFalse(panel.isOpen());
  }

  @Test void opensOnTheBuyListAndADigitRequestsOneUnitOfThatRow() {
    var panel = new OnlineShopPanel();
    panel.open("Sigfried", "Sigfried", listing(3, 500));
    assertEquals(OnlineShopPanel.Mode.BUY, panel.mode());
    var action = panel.choose(2).orElseThrow();
    assertEquals(new OnlineShopPanel.Action(OnlineShopPanel.Mode.BUY, "Sigfried", "item.stock_1", 1), action);
    assertTrue(panel.choose(4).isEmpty(), "no fourth row");
    assertTrue(panel.choose(0).isEmpty());
    assertTrue(panel.isOpen(), "choosing a row leaves the shop open for another trade");
  }

  @Test void quantityCyclesThroughAFixedSmallSetAndNeverExceedsTheServerLimit() {
    var panel = new OnlineShopPanel();
    panel.open("Sigfried", "Sigfried", listing(1, 500));
    var seen = new ArrayList<Integer>();
    for (int i = 0; i < 4; i++) { seen.add(panel.quantity()); panel.cycleQuantity(); }
    assertEquals(List.of(1, 5, 10, 1), seen);
    for (int q : OnlineShopPanel.QUANTITIES) assertTrue(q >= 1 && q <= com.perso.T4C.item.ShopRules.MAX_TRADE_QUANTITY);
  }

  @Test void sellingNeverAsksForMoreThanIsCarried() {
    var panel = new OnlineShopPanel();
    panel.open("Sigfried", "Sigfried", listing(1, 500));
    panel.toggleMode();
    panel.cycleQuantity();
    panel.cycleQuantity(); // 10 per press
    assertEquals(3, panel.choose(1).orElseThrow().quantity(), "three torches carried");
    assertEquals(1, panel.choose(2).orElseThrow().quantity());
    assertEquals(OnlineShopPanel.Mode.SELL, panel.choose(2).orElseThrow().mode());
  }

  @Test void paginatesNineRowsAtATimeAndClampsAfterARefresh() {
    var panel = new OnlineShopPanel();
    panel.open("Sigfried", "Sigfried", listing(12, 500));
    assertEquals(9, panel.visible().size());
    panel.turnPage(1);
    assertEquals(3, panel.visible().size());
    assertEquals("item.stock_9", panel.choose(1).orElseThrow().itemKey());
    panel.turnPage(5);
    assertEquals(1, panel.page(), "paging stops at the last page");
    panel.refresh(listing(2, 500));
    assertEquals(0, panel.page(), "a shorter refreshed list pulls the page back");
    panel.turnPage(-3);
    assertEquals(0, panel.page());
  }

  @Test void refreshKeepsTheModeAndShowsTheServersNewGold() {
    var panel = new OnlineShopPanel();
    panel.open("Sigfried", "Sigfried", listing(2, 500));
    panel.toggleMode();
    panel.refresh(listing(2, 482));
    assertEquals(OnlineShopPanel.Mode.SELL, panel.mode());
    assertEquals(482, panel.gold());
  }

  @Test void closeForgetsTheListing() {
    var panel = new OnlineShopPanel();
    panel.open("Sigfried", "Sigfried", listing(2, 500));
    panel.close();
    assertFalse(panel.isOpen());
    panel.refresh(listing(2, 1));
    assertFalse(panel.isOpen(), "a late reply cannot reopen a closed shop");
  }

  @Test void linesShowServerPricesAndGoldWithoutComputingAnything() {
    var panel = new OnlineShopPanel();
    panel.open("Sigfried", "Sigfried", listing(2, 500));
    var buyLines = panel.lines(key -> "<" + key + ">");
    assertTrue(buyLines.getFirst().contains("Sigfried"));
    assertTrue(buyLines.stream().anyMatch(line -> line.contains("500")), "gold is shown");
    assertTrue(buyLines.stream().anyMatch(line -> line.contains("<item.stock_0>") && line.contains("10")));
    assertTrue(buyLines.stream().anyMatch(line -> line.contains("<item.stock_1>") && line.contains("11")));
    panel.toggleMode();
    var sellLines = panel.lines(key -> "<" + key + ">");
    assertTrue(sellLines.stream().anyMatch(line -> line.contains("<item.torch>") && line.contains("2") && line.contains("3")));
  }

  @Test void anEmptySideSaysSoInsteadOfShowingNothing() {
    var panel = new OnlineShopPanel();
    panel.open("Sigfried", "Sigfried", new ShopOffersReply(1, 0, "Sigfried", List.of(), List.of()));
    assertEquals(OnlineShopPanel.Mode.BUY, panel.mode());
    assertTrue(panel.choose(1).isEmpty());
    assertEquals(com.perso.T4C.i18n.I18n.key("online.shop.empty_buy"), panel.lines(key -> key).get(2));
  }
}
