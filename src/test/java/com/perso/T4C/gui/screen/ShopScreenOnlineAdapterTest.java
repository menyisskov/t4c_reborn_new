package com.perso.T4C.gui.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.item.ShopRules;
import com.perso.T4C.online.model.ShopOffersReply;
import org.junit.jupiter.api.Test;

class ShopScreenOnlineAdapterTest {
  @Test
  void salesUseServerQuantityAndBothTradeKindsStayWithinTheServerRequestLimit() {
    var buy = new ShopOffersReply.Line("item.rusted_short_sword", 17, 0);
    var sell = new ShopOffersReply.Line("item.torch", 2, 3);

    assertEquals(ShopRules.MAX_TRADE_QUANTITY, ShopScreen.onlineQuantityLimit(false, buy));
    assertEquals(3, ShopScreen.onlineQuantityLimit(true, sell));
    assertEquals(
        ShopRules.MAX_TRADE_QUANTITY,
        ShopScreen.onlineQuantityLimit(
            true, new ShopOffersReply.Line("item.torch", 2, ShopRules.MAX_TRADE_QUANTITY + 1)));
  }
}
