package com.perso.T4C.npc;

import com.perso.T4C.player.BodyPart;
import lombok.Getter;

import java.util.List;

/**
 * Data-driven NPC definition. Dialogue is intentionally linear: one welcome
 * line followed by zero or more keyword-addressed topics.
 */
@Getter
public final class NpcDef {
    private final String name;
    private final String displayName;
    private final List<Part> parts;
    private final String spriteBase;
    private final int patrolRadiusTiles;
    private final List<String> fleeShouts;
    private final String welcomeText;
    private final List<DialogTopic> topics;
    /** Original C++ NPC template name, retained by the lossless legacy migration. */
    private final String sourceTemplate;
    /** Complete original OnTalk body. Null for native data-driven NPCs. */
    private final String sourceScript;
    /** Original non-dialogue handlers, keyed by OnPopup/OnAttack/OnAttacked/OnDeath/OnInitialise/OnDestroy. */
    private final java.util.Map<String, String> sourceEvents;

    public NpcDef(String name, String displayName, List<Part> parts, String spriteBase,
                  int patrolRadiusTiles, List<String> fleeShouts,
                  String welcomeText, List<DialogTopic> topics) {
        this.name = name;
        this.displayName = displayName;
        this.parts = List.copyOf(parts == null ? List.of() : parts);
        this.spriteBase = spriteBase;
        this.patrolRadiusTiles = patrolRadiusTiles;
        this.fleeShouts = List.copyOf(fleeShouts == null ? List.of() : fleeShouts);
        this.welcomeText = welcomeText;
        this.topics = List.copyOf(topics == null ? List.of() : topics);
        this.sourceTemplate = null;
        this.sourceScript = null;
        this.sourceEvents = java.util.Map.of();
    }

    public NpcDef(String name, String displayName, List<Part> parts, String spriteBase,
                  int patrolRadiusTiles, List<String> fleeShouts, String welcomeText,
                  List<DialogTopic> topics, String sourceTemplate, String sourceScript) {
        this.name = name;
        this.displayName = displayName;
        this.parts = List.copyOf(parts == null ? List.of() : parts);
        this.spriteBase = spriteBase;
        this.patrolRadiusTiles = patrolRadiusTiles;
        this.fleeShouts = List.copyOf(fleeShouts == null ? List.of() : fleeShouts);
        this.welcomeText = welcomeText;
        this.topics = List.copyOf(topics == null ? List.of() : topics);
        this.sourceTemplate = sourceTemplate;
        this.sourceScript = sourceScript;
        this.sourceEvents = java.util.Map.of();
    }

    public NpcDef(String name, String displayName, List<Part> parts, String spriteBase,
                  int patrolRadiusTiles, List<String> fleeShouts, String welcomeText,
                  List<DialogTopic> topics, String sourceTemplate, String sourceScript,
                  java.util.Map<String, String> sourceEvents) {
        this.name = name;
        this.displayName = displayName;
        this.parts = List.copyOf(parts == null ? List.of() : parts);
        this.spriteBase = spriteBase;
        this.patrolRadiusTiles = patrolRadiusTiles;
        this.fleeShouts = List.copyOf(fleeShouts == null ? List.of() : fleeShouts);
        this.welcomeText = welcomeText;
        this.topics = List.copyOf(topics == null ? List.of() : topics);
        this.sourceTemplate = sourceTemplate;
        this.sourceScript = sourceScript;
        this.sourceEvents = java.util.Map.copyOf(sourceEvents == null ? java.util.Map.of() : sourceEvents);
    }

    /*
     * Compatibility views for the old in-game map editor and the unused legacy
     * TrainScreen. They are deliberately not persisted and are not part of the
     * new dialogue model; Content Studio uses Action.targets.
     */
    @Deprecated public List<TaughtSpell> getTaughtSpells() { return List.of(); }
    @Deprecated public List<ShopItem> getShopItems() { return List.of(); }
    @Deprecated public List<TrainableStat> getTrainableStats() { return List.of(); }
    @Deprecated public List<DialogNode> getDialogNodes() { return List.of(); }

    @Deprecated
    public NpcDef(String name, String displayName, List<Part> parts, String spriteBase,
                  int patrolRadiusTiles, List<TaughtSpell> ignoredSpells,
                  List<ShopItem> ignoredItems, List<TrainableStat> ignoredStats,
                  List<String> fleeShouts, List<DialogNode> legacyNodes) {
        this(name, displayName, parts, spriteBase, patrolRadiusTiles, fleeShouts,
                legacyNodes == null ? "" : legacyNodes.stream().filter(DialogNode::isGreeting)
                        .map(DialogNode::getResponse).findFirst().orElse(""),
                legacyNodes == null ? List.of() : legacyNodes.stream()
                        .filter(node -> !node.isGreeting())
                        .map(node -> new DialogTopic(node.getKeywords(), node.getResponse(), node.getActions()))
                        .toList());
    }

    @Getter
    public static final class DialogTopic {
        private final List<String> keywords;
        private final String response;
        private final List<Action> actions;

        public DialogTopic(List<String> keywords, String response, List<Action> actions) {
            this.keywords = List.copyOf(keywords == null ? List.of() : keywords);
            this.response = response;
            this.actions = List.copyOf(actions == null ? List.of() : actions);
        }
    }

    /**
     * Targets contain spell ids, skill ids, item keys, or quest ids depending on
     * the type. GIVE_ITEM and GIVE_QUEST use exactly one target; HEAL and
     * END_CONVERSATION use none.
     */
    @Getter
    public static final class Action {
        private final ActionType type;
        private final List<String> targets;

        public Action(ActionType type, List<String> targets) {
            this.type = type;
            this.targets = List.copyOf(targets == null ? List.of() : targets);
        }

        public Action(ActionType type, String target) {
            this(type, target == null || target.isBlank() ? List.of() : List.of(target));
        }

        public Action(ActionType type) {
            this(type, List.of());
        }

        @Deprecated
        public Action(ActionType type, String stringParam1, String ignoredStringParam2,
                      int ignoredIntParam1, int ignoredIntParam2) {
            this(type, stringParam1);
        }

    }

    @Getter
    public static final class Part {
        private final BodyPart bodyPart;
        private final String spriteBase;

        public Part(BodyPart bodyPart, String spriteBase) {
            this.bodyPart = bodyPart;
            this.spriteBase = spriteBase;
        }
    }

    @Deprecated
    @Getter
    public static final class DialogNode {
        private final String id;
        private final List<String> keywords;
        private final String response;
        private final String requiredFlag;
        private final int requiredFlagValue;
        private final String requiredItem;
        private final boolean greeting;
        private final String fallbackNode;
        private final List<Action> actions;

        public DialogNode(String id, List<String> keywords, String response,
                          String requiredFlag, int requiredFlagValue, String requiredItem,
                          boolean greeting, String fallbackNode, List<Action> actions) {
            this.id = id;
            this.keywords = keywords == null ? List.of() : keywords;
            this.response = response;
            this.requiredFlag = requiredFlag;
            this.requiredFlagValue = requiredFlagValue;
            this.requiredItem = requiredItem;
            this.greeting = greeting;
            this.fallbackNode = fallbackNode;
            this.actions = actions == null ? List.of() : actions;
        }
    }

    @Deprecated
    @Getter
    public static final class TaughtSpell {
        private final String spellName;
        private final int price;
        public TaughtSpell(String spellName, int price) {
            this.spellName = spellName;
            this.price = price;
        }
    }

    @Deprecated
    @Getter
    public static final class ShopItem {
        private final String itemKey;
        private final long price;
        public ShopItem(String itemKey, long price) {
            this.itemKey = itemKey;
            this.price = price;
        }
    }

    @Deprecated
    @Getter
    public static final class TrainableStat {
        private final String statId;
        private final int costPerPoint;
        private final int maxPoints;
        public TrainableStat(String statId, int costPerPoint, int maxPoints) {
            this.statId = statId;
            this.costPerPoint = costPerPoint;
            this.maxPoints = maxPoints;
        }
    }
}
