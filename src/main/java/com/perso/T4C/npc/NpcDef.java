package com.perso.T4C.npc;

import com.perso.T4C.player.BodyPart;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Data-driven definition of an NPC type. Replaces the former hardcoded
 * {@code com.perso.T4C.npc.named.*} subclasses; persisted in
 * {@code assets/npcs/npcs.bin} via {@link com.perso.T4C.helper.NpcDefBinaryIO}
 * and instantiated at runtime by {@link DataNpc}.
 */
@Getter
public class NpcDef {
    /** Unique key; doubles as the spawn "type" key (was the class simple name). */
    private final String name;
    /** Name shown in-game; falls back to {@link #name}. */
    private final String displayName;
    /** Ordered body parts that compose the NPC's appearance; may be empty when {@link #spriteBase} is set. */
    private final List<Part> parts;
    /** Single-piece NPC sprite base; when set, this takes precedence over parts. */
    private final String spriteBase;
    /** Full dialog text shown on interaction; nullable/empty = no dialog. */
    private final String dialogText;
    /** Clickable keyword inside the dialog; nullable = no clickable keyword. */
    private final String dialogKeyword;
    /** Action fired when the keyword is clicked; {@code NONE} for a dialog-only NPC. */
    private final KeywordActionType action;
    /** HEAL only: minimum heal amount (applyHeal arg1); unused by other actions. */
    private final int actionParam1;
    /** HEAL only: maximum heal amount (applyHeal arg2); unused by other actions. */
    private final int actionParam2;
    /** Patrol radius in tiles; runtime multiplies by GRID_W. 0 = engine default. */
    private final int patrolRadiusTiles;
    /**
     * TEACH: the curriculum this NPC offers; gold costs live on SpellData, not here.
     * CAST reuses the list differently — see {@link TaughtSpell}.
     */
    private final List<TaughtSpell> taughtSpells;
    /** SHOP: items this NPC sells. */
    private final List<ShopItem> shopItems;
    /** TRAIN: stats this NPC can train. */
    private final List<TrainableStat> trainableStats;
    /**
     * Head-bubble lines shouted when the NPC is attacked, one picked at random.
     * Non-empty marks the NPC as fleeing rather than fighting back; empty means
     * it retaliates (see {@code NPCManager.onNpcAttacked}).
     */
    private final List<String> fleeShouts;

    /** Convenience overload for NPCs that neither sell nor train. */
    public NpcDef(String name, String displayName, List<Part> parts, String spriteBase,
                  String dialogText, String dialogKeyword, KeywordActionType action,
                  int actionParam1, int actionParam2, int patrolRadiusTiles,
                  List<TaughtSpell> taughtSpells) {
        this(name, displayName, parts, spriteBase, dialogText, dialogKeyword, action,
                actionParam1, actionParam2, patrolRadiusTiles, taughtSpells,
                Collections.emptyList(), Collections.emptyList());
    }

    /** Convenience overload for NPCs that fight back instead of fleeing. */
    public NpcDef(String name, String displayName, List<Part> parts, String spriteBase,
                  String dialogText, String dialogKeyword, KeywordActionType action,
                  int actionParam1, int actionParam2, int patrolRadiusTiles,
                  List<TaughtSpell> taughtSpells,
                  List<ShopItem> shopItems,
                  List<TrainableStat> trainableStats) {
        this(name, displayName, parts, spriteBase, dialogText, dialogKeyword, action,
                actionParam1, actionParam2, patrolRadiusTiles, taughtSpells,
                shopItems, trainableStats, Collections.emptyList());
    }

    public NpcDef(String name, String displayName, List<Part> parts, String spriteBase,
                  String dialogText, String dialogKeyword, KeywordActionType action,
                  int actionParam1, int actionParam2, int patrolRadiusTiles,
                  List<TaughtSpell> taughtSpells,
                  List<ShopItem> shopItems,
                  List<TrainableStat> trainableStats,
                  List<String> fleeShouts) {
        this.name = name;
        this.displayName = displayName;
        this.parts = parts;
        this.spriteBase = spriteBase;
        this.dialogText = dialogText;
        this.dialogKeyword = dialogKeyword;
        this.action = action;
        this.actionParam1 = actionParam1;
        this.actionParam2 = actionParam2;
        this.patrolRadiusTiles = patrolRadiusTiles;
        this.taughtSpells = taughtSpells != null ? taughtSpells : Collections.emptyList();
        this.shopItems = shopItems != null ? shopItems : Collections.emptyList();
        this.trainableStats = trainableStats != null ? trainableStats : Collections.emptyList();
        this.fleeShouts = fleeShouts != null ? fleeShouts : Collections.emptyList();
    }

    /**
     * A single body-part to sprite-base mapping.
     */
    @Getter
    public static final class Part {
        private final BodyPart bodyPart;
        private final String spriteBase;

        public Part(BodyPart bodyPart, String spriteBase) {
            this.bodyPart = bodyPart;
            this.spriteBase = spriteBase;
        }
    }

    /**
     * A spell attached to an NPC, read differently per {@link KeywordActionType}:
     * under TEACH it is one entry of the curriculum, under CAST only the first
     * entry is used and it is the spell cast on the player.
     */
    @Getter
    public static final class TaughtSpell {
        private final String spellName;
        /**
         * TEACH: unused — the gold cost comes from SpellData. CAST: the caster
         * level/wisdom the spell's {@code self.*} formulas resolve against, since
         * a cast NPC charges nothing. Not a price in either case, despite the name.
         */
        private final int price;

        public TaughtSpell(String spellName, int price) {
            this.spellName = spellName;
            this.price = price;
        }
    }

    /**
     * An item this NPC sells.
     */
    @Getter
    public static final class ShopItem {
        /**
         * Key into {@code ItemRegistry} ({@code ItemDefinition.key} from items.bin).
         * An entry whose key resolves to nothing is skipped, so the shop silently
         * shrinks rather than showing a broken row.
         */
        private final String itemKey;
        /** Asking price; {@code <= 0} falls back to {@code ItemDefinition.price}. */
        private final long price;

        public ShopItem(String itemKey, long price) {
            this.itemKey = itemKey;
            this.price = price;
        }
    }

    /**
     * A stat this NPC can train for gold.
     */
    @Getter
    public static final class TrainableStat {
        /** Skill id as used by {@code Stats.getSkillLevel} (e.g. {@code attack}, {@code rapid_healing}). */
        private final String statId;
        /** Gold charged per point, and also the affordability check for a single point. */
        private final int costPerPoint;
        /** Cap this trainer will raise the stat to; {@code <= 0} means no cap. */
        private final int maxPoints;

        public TrainableStat(String statId, int costPerPoint, int maxPoints) {
            this.statId = statId;
            this.costPerPoint = costPerPoint;
            this.maxPoints = maxPoints;
        }
    }
}
