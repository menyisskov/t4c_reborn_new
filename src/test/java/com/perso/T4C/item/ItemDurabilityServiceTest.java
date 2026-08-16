package com.perso.T4C.item;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemDurabilityServiceTest {
    @Test
    void duplicateItemsKeepIndependentDurabilityAndRoundTripThroughSaveState() throws Exception {
        Player source = new Player();
        source.setInventory(List.of("item.dagger", "item.dagger"));
        source.setInventoryDurability(List.of(23d, 87d));

        PlayerStateDto state = PlayerStateMapper.fromPlayer(source);
        Player restored = new Player();
        PlayerStateMapper.applyToPlayer(state, restored);

        assertEquals(23d, ItemDurabilityService.inventory(restored, 0));
        assertEquals(87d, ItemDurabilityService.inventory(restored, 1));
    }

    @Test
    void brokenEquippedItemNoLongerContributesArmor() throws Exception {
        Player player = new Player();
        player.getEquippedItems().put(BodyPart.BODY, "item.leather_armor");
        assertTrue(InventoryService.equippedArmor(player) > 0);

        player.getEquippedDurability().put(BodyPart.BODY, 0d);

        assertEquals(0d, InventoryService.equippedArmor(player));
        assertTrue(ItemDurabilityService.isBroken(player, BodyPart.BODY));
    }

    @Test
    void legacyStateDefaultsEveryItemToFullDurability() throws Exception {
        PlayerStateDto state = new PlayerStateDto();
        state.inventory = List.of("item.dagger", "item.leather_armor");
        Player restored = new Player();

        PlayerStateMapper.applyToPlayer(state, restored);

        assertEquals(List.of(100d, 100d), restored.getInventoryDurability());
    }

    @Test
    void successfulAttackWearIsPointTwentyPercent() throws Exception {
        Player player = new Player();
        player.getEquippedItems().put(BodyPart.WEAPON, "item.dagger");

        for (int i = 0; i < 5; i++) {
            ItemDurabilityService.damageEquipped(player, BodyPart.WEAPON,
                    ItemDurabilityService.COMBAT_WEAR);
        }

        assertEquals(99d, ItemDurabilityService.equipped(player, BodyPart.WEAPON), 0.000_001d);
    }

    @Test
    void deathRemovesFifteenPercentFromEveryDistinctEquippedItem() throws Exception {
        Player player = new Player();
        player.getEquippedItems().put(BodyPart.WEAPON, "item.dagger");
        player.getEquippedItems().put(BodyPart.BODY, "item.leather_armor");

        ItemDurabilityService.wearAllEquippedOnDeath(player);

        assertEquals(85d, ItemDurabilityService.equipped(player, BodyPart.WEAPON));
        assertEquals(85d, ItemDurabilityService.equipped(player, BodyPart.BODY));
    }
}
