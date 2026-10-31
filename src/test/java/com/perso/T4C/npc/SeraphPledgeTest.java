package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

/**
 * T4C-0408: the two oaths that open the Seraph endgame. Beltigan's "I pledge to serve Artherk" makes a Lightbringer
 * and Mordenthal's "I swear allegiance to Ogrimar" makes a follower of Ogrimar; roughly fifteen NPCs gate their
 * content on those flags and nothing ever granted them.
 */
class SeraphPledgeTest {
  private static final String LIGHT = "__FLAG_LIGHTBRINGER_OF_ARTHERK";
  private static final String DARK = "__FLAG_FOLLOWER_OF_OGRIMAR";
  private static final String CHAMPION = "__QUEST_FIXED_ALIGNMENT";

  private final BeltiganWhitesword beltigan = newBeltigan();
  private final Mordenthal mordenthal = newMordenthal();

  private static BeltiganWhitesword newBeltigan() {
    try {
      return new BeltiganWhitesword(new NpcContext(null));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static Mordenthal newMordenthal() {
    try {
      return new Mordenthal(new NpcContext(null));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private NpcBehavior good() {
    return beltigan.javaBehavior();
  }

  private NpcBehavior evil() {
    return mordenthal.javaBehavior();
  }

  @Test
  void aChampionOfLightWhoPledgesAndConfirmsBecomesALightbringer() {
    Player player = new Player();
    player.setQuestFlag(CHAMPION, 1);
    NpcBehaviorContext c = new NpcBehaviorContext(beltigan, player);

    assertTrue(good().onKeyword(c, "I pledge to serve Artherk"));
    assertEquals(0, player.getQuestFlag(LIGHT), "asking is not enough, the player must confirm");

    assertTrue(good().onYesNo(c, "beltigan_pledge", true));
    assertEquals(1, player.getQuestFlag(LIGHT));
    assertEquals(0, player.getQuestFlag(DARK));
  }

  @Test
  void decliningLeavesThePlayerAsTheyWere() {
    Player player = new Player();
    player.setQuestFlag(CHAMPION, 1);
    NpcBehaviorContext c = new NpcBehaviorContext(beltigan, player);

    good().onKeyword(c, "pledge");
    good().onYesNo(c, "beltigan_pledge", false);

    assertEquals(0, player.getQuestFlag(LIGHT));
  }

  @Test
  void noChampionNoLightbringer() {
    Player player = new Player();
    NpcBehaviorContext c = new NpcBehaviorContext(beltigan, player);

    good().onKeyword(c, "pledge");
    good().onYesNo(c, "beltigan_pledge", true);

    assertEquals(0, player.getQuestFlag(LIGHT), "the pledge needs the Champion of Light title first");
  }

  @Test
  void aServantOfOgrimarCannotBecomeALightbringer() {
    Player player = new Player();
    player.setQuestFlag(CHAMPION, 1);
    player.setQuestFlag(DARK, 1);
    NpcBehaviorContext c = new NpcBehaviorContext(beltigan, player);

    good().onKeyword(c, "pledge");
    good().onYesNo(c, "beltigan_pledge", true);

    assertEquals(0, player.getQuestFlag(LIGHT));
  }

  @Test
  void swearingAllegianceToOgrimarAndConfirmingMakesAFollower() {
    Player player = new Player();
    NpcBehaviorContext c = new NpcBehaviorContext(mordenthal, player);

    assertTrue(evil().onKeyword(c, "I swear allegiance to Ogrimar"));
    assertEquals(0, player.getQuestFlag(DARK));

    assertTrue(evil().onYesNo(c, "mordenthal_oath", true));
    assertEquals(1, player.getQuestFlag(DARK));
    assertEquals(0, player.getQuestFlag(LIGHT));
  }

  @Test
  void aLightbringerCannotSwearTheOathAndDecliningChangesNothing() {
    Player light = new Player();
    light.setQuestFlag(LIGHT, 1);
    NpcBehaviorContext lc = new NpcBehaviorContext(mordenthal, light);
    evil().onKeyword(lc, "swear");
    evil().onYesNo(lc, "mordenthal_oath", true);
    assertEquals(0, light.getQuestFlag(DARK));

    Player undecided = new Player();
    NpcBehaviorContext uc = new NpcBehaviorContext(mordenthal, undecided);
    evil().onKeyword(uc, "allegiance");
    evil().onYesNo(uc, "mordenthal_oath", false);
    assertEquals(0, undecided.getQuestFlag(DARK));
  }

  @Test
  void theOathOpensWhatWasShutBeforeZhakarGivesTheSpellbooksOnlyToAFollower() throws Exception {
    Zhakar zhakar = new Zhakar(new NpcContext(null));
    Player outsider = new Player();
    NpcBehaviorContext oc = new NpcBehaviorContext(zhakar, outsider);
    zhakar.javaBehavior().onKeyword(oc, "spellbook");
    assertEquals(0, oc.itemCount("arcane_spellbook"), "no oath, no spellbooks");

    Player follower = new Player();
    NpcBehaviorContext mc = new NpcBehaviorContext(mordenthal, follower);
    evil().onKeyword(mc, "I swear allegiance to Ogrimar");
    evil().onYesNo(mc, "mordenthal_oath", true);

    NpcBehaviorContext zc = new NpcBehaviorContext(zhakar, follower);
    zhakar.javaBehavior().onKeyword(zc, "spellbook");
    assertEquals(2, zc.itemCount("arcane_spellbook"), "a sworn follower now gets the two spellbooks");
  }

  @Test
  void theWholeEvilChainReachesZionWhoRevealsWhereTheSkullOfOgrimarIsHidden() throws Exception {
    Player player = new Player();
    NpcBehaviorContext mc = new NpcBehaviorContext(mordenthal, player);
    evil().onKeyword(mc, "I swear allegiance to Ogrimar");
    evil().onYesNo(mc, "mordenthal_oath", true);

    Zhakar zhakar = new Zhakar(new NpcContext(null));
    zhakar.javaBehavior().onKeyword(new NpcBehaviorContext(zhakar, player), "spellbook");

    MeltarWinterstorm meltar = new MeltarWinterstorm(new NpcContext(null));
    meltar.javaBehavior().onKeyword(new NpcBehaviorContext(meltar, player), "crown of corruption");

    // Oogla's trade (15,000 gold, two villain skulls, two scrolls of hate) gives the skull and sets this flag itself.
    NpcBehaviorContext zc = new NpcBehaviorContext(new ZionThunderheart(new NpcContext(null)), player);
    player.setQuestFlag("__FLAG_USER_HAS_SKULL_OF_EVIL", 1);
    com.perso.T4C.item.InventoryService.add(player, "skull_of_evil");

    ZionThunderheart zion = new ZionThunderheart(new NpcContext(null));
    zion.javaBehavior().onConversationStart(new NpcBehaviorContext(zion, player));

    assertEquals(1, player.getQuestFlag("__FLAG_USER_KNOWS_WHERE_SKULL_OF_OGRIMAR_IS_HIDDEN"));
    assertEquals(0, zc.itemCount("arcane_spellbook"), "Zion took both spellbooks");
    assertEquals(0, zc.itemCount("crown_of_corruption"));
    assertEquals(0, zc.itemCount("skull_of_evil"));
  }
}
