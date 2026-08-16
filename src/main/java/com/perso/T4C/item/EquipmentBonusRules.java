package com.perso.T4C.item;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;

/** Runtime counterpart of ObjectStructure::OnEquip/OnUnequip and Unit::QueryBoost. */
public final class EquipmentBonusRules {
    private EquipmentBonusRules() {}

    public static int bonus(Player player, int statId) {
        if (player == null) return 0;
        Map<Integer, ActiveBoost> active = new LinkedHashMap<>();
        for (Map.Entry<BodyPart, String> equipped : player.getEquippedItems().entrySet()) {
            ItemDefinition item = ItemRegistry.findByKey(equipped.getValue());
            if (item == null || ItemDurabilityService.isBroken(player, equipped.getKey())
                    || isMirrored(player, equipped.getKey(), equipped.getValue(), item)) continue;
            for (ItemDefinition.ItemBoost boost : item.getBoosts()) {
                if (player.getIntelligence() < boost.getMinInt() || player.getWisdom() < boost.getMinWis()) continue;
                // SetBoost is keyed by boost ID in the original server.
                active.put(boost.getBoostId(), new ActiveBoost(boost, evaluate(player, boost.getExpression())));
            }
        }
        long total = 0;
        for (ActiveBoost value : active.values()) if (value.definition.getStatId() == statId) total += value.amount;
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, total));
    }

    private static int evaluate(Player player, String expression) {
        String formula = expression == null ? "0" : expression
                .replace("self.true_attack", Integer.toString(player.getSkillLevel("attack")))
                .replace("self.true_dodge", Integer.toString(player.getSkillLevel("dodge")))
                .replace("self.true_light", Integer.toString(basePower(player, "light")))
                .replaceAll("true_skill\\(35\\)", Integer.toString(player.getSkillLevel("archery")));
        DiceFormula.Context context = new DiceFormula.Context(player.getStrength(), player.getEndurance(),
                player.getDexterity(), player.getIntelligence(), 0, player.getWisdom(), 0, player.getLevel());
        return DiceFormula.of(formula).evaluate(context);
    }

    private static int basePower(Player player, String id) {
        int value = player.getSkillLevel(id);
        return value <= 0 ? 100 : value;
    }

    private static boolean isMirrored(Player player, BodyPart slot, String key, ItemDefinition item) {
        return item.getSecondaryBodyPart() == slot && key.equals(player.getEquippedItems().get(item.getBodyPart()));
    }

    private record ActiveBoost(ItemDefinition.ItemBoost definition, int amount) {}
}
