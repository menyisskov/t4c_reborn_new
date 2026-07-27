package com.perso.T4C.tools;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;

import java.util.ArrayList;
import java.util.List;

/** Imports GoN's basic healing/mana potions and their use spells. */
public final class GoNHealingPotionMigration {
    private static final String LIGHT_HEALING_POTION_KEY = "Light healing potion";
    private static final int LIGHT_HEALING_POTION_SPELL_ID = 10176;
    private static final String POTION_KEY = "Potion of healing";
    private static final int POTION_SPELL_ID = 10191;

    private GoNHealingPotionMigration() {
    }

    public static void main(String[] args) throws Exception {
        List<SpellData> spells = new ArrayList<>(SpellRegistry.load());
        spells.removeIf(spell -> spell != null && spell.getSpellId() == LIGHT_HEALING_POTION_SPELL_ID);
        spells.add(new SpellData("ITEM: Potion of Light Healing", "", "0", 0, 0, 0, 0,
                false, false, null, null, null, 0, 0, null, null, 0, "0", "0", 0, null,
                LIGHT_HEALING_POTION_SPELL_ID, 2, 1, 0, "100", "0", "0", "0", 0, 0, false,
                List.of(new SpellData.T4cEffect(1, List.of(
                        new SpellData.T4cEffect.EffectParam(1, "25"),
                        new SpellData.T4cEffect.EffectParam(3, "100"))))));
        spells.removeIf(spell -> spell != null && spell.getSpellId() == POTION_SPELL_ID);
        spells.add(new SpellData("ITEM: Potion of Healing", "", "0", 0, 0, 0, 0,
                false, false, null, null, null, 0, 0, null, null, 0, "0", "0", 0, null,
                POTION_SPELL_ID, 2, 1, 0, "100", "0", "0", "0", 0, 0, false,
                List.of(new SpellData.T4cEffect(1, List.of(
                        new SpellData.T4cEffect.EffectParam(1, "50"),
                        new SpellData.T4cEffect.EffectParam(3, "100"))))));
        spells.removeIf(spell -> spell != null && spell.getSpellId() == 10062);
        spells.add(new SpellData("ITEM: Potion of mana", "", "0", 0, 0, 0, 0,
                false, false, null, null, null, 0, 0, null, null, 0, "0", "0", 0, null,
                10062, 0, 1, 0, "100", "0", "0", "0", 0, 0, false,
                List.of(new SpellData.T4cEffect(2, List.of(
                        new SpellData.T4cEffect.EffectParam(1, "TRUE"),
                        new SpellData.T4cEffect.EffectParam(2, "MANA"),
                        new SpellData.T4cEffect.EffectParam(3, "25"))))));
        SpellRegistry.save(spells);

        List<ItemDefinition> items = new ArrayList<>(ItemRegistry.load());
        boolean foundLightPotion = false;
        boolean foundHealingPotion = false;
        for (int i = 0; i < items.size(); i++) {
            ItemDefinition item = items.get(i);
            if (item != null && LIGHT_HEALING_POTION_KEY.equals(item.getKey())) {
                items.set(i, copyPotion(item, LIGHT_HEALING_POTION_SPELL_ID));
                foundLightPotion = true;
            }
            if (item != null && POTION_KEY.equals(item.getKey())) {
                items.set(i, copyPotion(item, POTION_SPELL_ID));
                foundHealingPotion = true;
            }
        }
        if (!foundLightPotion) throw new IllegalStateException("Missing item definition: " + LIGHT_HEALING_POTION_KEY);
        if (!foundHealingPotion) throw new IllegalStateException("Missing item definition: " + POTION_KEY);
        ItemRegistry.save(items);
    }

    private static ItemDefinition copyPotion(ItemDefinition item, int spellId) {
        return new ItemDefinition(item.getKey(), item.getName(), item.getBodyPart(),
                item.getAppearanceEquippedPrimary(), item.getSecondaryBodyPart(), item.getAppearanceEquippedSecondary(),
                item.getAppearanceInventory(), item.getPrice(), item.getWeight(), item.getArmorClass(),
                item.getDodgeLost(), item.getMinEnd(), item.getReqAttack(), item.getReqStr(), item.getReqAgi(),
                item.getMinInt(), item.getMinWis(), item.getAttackSpeed(), item.isUnique(), item.isBow(),
                item.isUnlimitedUse(), item.getNumId(), item.getStructure(), item.getAppearanceId(),
                item.getDmgFormula(), item.getAtkDelay(), item.getRadiance(), 1, item.isCanSummon(),
                item.getLockName(), item.getLockDiff(), item.getSignText(), item.getContainerGold(),
                item.getGlobalRespawn(), item.getLocalRespawn(),
                List.of(new ItemDefinition.ItemSpell(spellId, 0, 100)), item.getBoosts());
    }
}
