package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.gui.screen.ShopScreen;
import com.perso.T4C.gui.screen.RepairScreen;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.spell.NpcCastVfxHook;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.SystemMessage;
import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_RADIUS;

/** Runtime NPC backed by the simple welcome/topics dialogue definition. */
@Slf4j
public class DataNpc extends BaseNPC {
    static final String TALK_THROUGH_WALLS_EVENT = "@interaction.talkThroughWalls";
    static final String SCRIPT_WELCOME_EVENT = "@interaction.scriptWelcome";
    private static final Set<String> KNOWN_SKILLS = Set.of(
            "attack", "archery", "dodge", "peek", "stun_blow", "powerful_blow",
            "rapid_healing", "first_aid", "parry", "critical_strike", "hide", "sneak",
            "search", "picklock", "armor_penetration", "two_weapons", "rob",
            "meditate", "strength", "dexterity", "endurance", "intelligence", "wisdom");
    private final NpcDef def;
    private final String sourceScript;
    private final QuestService questService;
    /**
     * Resolved when the action runs, not at construction: NPCs are built while
     * the map loads, before the companion manager exists.
     */
    private final Supplier<CompanionManager> companionManagerSupplier;
    private String pendingYesNoState;
    private int armorClass;
    private String damageFormula = "1d3";

    public DataNpc(NpcDef def) throws GameException {
        this(def, null, null);
    }

    public DataNpc(NpcDef def, QuestService questService) throws GameException {
        this(def, questService, null);
    }

    public DataNpc(NpcDef def, QuestService questService,
                   Supplier<CompanionManager> companionManagerSupplier) throws GameException {
        super(def.getName(), def.getSpriteBase(), NpcPartsBuilder.fromDef(def));
        this.def = def;
        this.sourceScript = def.getSourceScript();
        this.questService = questService;
        this.companionManagerSupplier = companionManagerSupplier;
        this.maxHp = Math.max(1, combatInt("@combat.hp", 1));
        this.currentHp = maxHp;
        this.level = Math.max(1, combatInt("@combat.level", 1));
        this.strength = Math.max(1, combatInt("@combat.str", 10));
        this.endurance = Math.max(1, combatInt("@combat.end", 10));
        this.dexterity = Math.max(1, combatInt("@combat.dex", 10));
        this.armorClass = Math.max(0, combatInt("@combat.ac", 0));
        setSkillLevel("attack", Math.max(1, combatInt("@combat.attackSkill", level)));
        setSkillLevel("dodge", Math.max(1, combatInt("@combat.dodge", level)));
        this.damageFormula = def.getSourceEvents().getOrDefault("@combat.damage", "1d3");
        if (def.getDisplayName() != null && !def.getDisplayName().isEmpty()) {
            String translatedName = I18n.resolve(def.getName());
            setDisplayName(translatedName.equals(def.getName())
                    ? I18n.resolve(def.getDisplayName()) : translatedName);
        }
    }

    @Override
    protected boolean canTalkThroughWalls() {
        return Boolean.parseBoolean(def.getSourceEvents().get(TALK_THROUGH_WALLS_EVENT));
    }

    @Override
    protected void onInteractStart(Player player) {
        if (questService != null) {
            String completion = questService.turnInReadyQuests(def.getName(), player);
            if (completion != null && !completion.isBlank()) {
                showDialog(completion, 0L);
                return;
            }
        }
        boolean scriptHandled = false;
        NpcScriptEngine.Result scriptResult = null;
        if (sourceScript != null) {
            scriptResult = NpcScriptEngine.begin(
                    sourceScript, def.getName(), player);
            // Legacy Begin blocks still carry their original literal greeting.
            // Execute their flags/items/actions, but the displayed welcome always
            // comes from NpcDef.welcomeText -> assets/i18n/lang.json.
            scriptHandled = applyScriptResult(scriptResult, player, false);
        }
        if (Boolean.parseBoolean(def.getSourceEvents().get(SCRIPT_WELCOME_EVENT))
                && scriptResult != null && scriptResult.text() != null && !scriptResult.text().isBlank()) {
            showDialog(scriptResult.text(), 0L);
            return;
        }
        if (def.getWelcomeText() != null && !def.getWelcomeText().isBlank()) {
            showDialog(I18n.resolve(def.getWelcomeText()), 0L);
            return;
        }
        if (scriptHandled) return;
    }

    @Override
    protected List<String> getDialogKeywords() {
        List<String> keywords = new ArrayList<>(super.getDialogKeywords());
        for (NpcDef.DialogTopic topic : def.getTopics()) {
            for (String keyword : topic.getKeywords()) addKeyword(keywords, keyword);
        }
        // Topics stored in npcs.bin only cover Command sections; the script itself also answers to
        // CmdAND words, which would otherwise stay invisible to the player.
        for (String keyword : NpcScriptEngine.keywords(sourceScript).keySet()) {
            addKeyword(keywords, keyword);
        }
        return keywords;
    }

    /**
     * Naming a word of a {@code CmdAND} section must say the whole sentence, since the script only
     * reacts when all of its keywords appear together. Applies to clicked links and typed speech
     * alike: saying "reborn" on its own would otherwise reach an unrelated Command section.
     */
    private String sentenceForKeyword(String keyword) {
        return sentenceForKeyword(sourceScript, keyword);
    }

    static String sentenceForKeyword(String script, String keyword) {
        String spoken = normalizeCommand(keyword);
        if (spoken.isEmpty()) return keyword;
        for (Map.Entry<String, String> entry : NpcScriptEngine.keywords(script).entrySet()) {
            String word = normalizeCommand(I18n.resolve(entry.getKey()));
            if (word.isEmpty() || word.equals(normalizeCommand(entry.getValue()))) continue;
            // Only rewrite when the sentence is not already complete, so typing the full
            // "ready reborn" is left untouched.
            if ((" " + spoken + " ").contains(" " + word + " ")
                    && !containsAllWords(spoken, normalizeCommand(entry.getValue()))) {
                return entry.getValue();
            }
        }
        return keyword;
    }

    private static boolean containsAllWords(String spoken, String sentence) {
        for (String word : sentence.split(" ")) {
            if (!word.isEmpty() && !(" " + spoken + " ").contains(" " + word + " ")) return false;
        }
        return true;
    }

    private static void addKeyword(List<String> keywords, String keyword) {
        String resolved = I18n.resolve(keyword);
        if (resolved != null && !resolved.isBlank() && !keywords.contains(resolved)) {
            keywords.add(resolved);
        }
    }

    @Override
    public boolean talk(String text, Player player) {
        if (isInteracting && pendingYesNoState != null) {
            String normalized = normalizeCommand(text);
            boolean yes = normalized.equals("yes") || normalized.equals("oui");
            boolean no = normalized.equals("no") || normalized.equals("non");
            if (yes || no) {
                String state = pendingYesNoState;
                pendingYesNoState = null;
                NpcScriptEngine.Result result = NpcScriptEngine.respondYesNo(
                        sourceScript, def.getName(), state, yes, player);
                if (applyScriptResult(result, player)) return true;
            }
        }
        if (isInteracting && sourceScript != null) {
            NpcScriptEngine.Result result = NpcScriptEngine.respond(
                    sourceScript, def.getName(), sentenceForKeyword(text), player, pendingYesNoState);
            if (applyScriptResult(result, player)) return true;
        }
        if (isInteracting && respondToTopic(text, player)) return true;
        return super.talk(text, player);
    }

    private int combatInt(String key, int fallback) {
        try { return Integer.parseInt(def.getSourceEvents().getOrDefault(key, Integer.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    public int getArmorClass() { return armorClass; }

    @Override
    protected int rollHostileDamage() {
        return Math.max(0, com.perso.T4C.helper.DiceFormula.of(damageFormula).roll());
    }

    private boolean applyScriptResult(NpcScriptEngine.Result result, Player player) {
        return applyScriptResult(result, player, true);
    }

    private boolean applyScriptResult(NpcScriptEngine.Result result, Player player, boolean displayText) {
        if (result == null || !result.handled()) return false;
        if (displayText && result.text() != null && !result.text().isBlank()) showDialog(result.text(), 0L);
        for (String message : result.systemMessages()) {
            if (message != null && !message.isBlank()) SystemMessage.showShared(message);
        }
        if (!result.shopItems().isEmpty()) GuiManager.open(new ShopScreen(player, result.shopItems()));
        if (!result.sellRules().isEmpty()) {
            List<String> sellable = player.getInventory().stream().distinct().filter(key -> {
                var item = ItemRegistry.findByKey(key);
                return item != null && result.sellRules().stream().anyMatch(rule -> sellRuleMatches(rule, item));
            }).toList();
            GuiManager.open(ShopScreen.forSelling(player, sellable));
        }
        if (!result.taughtSpells().isEmpty()) GuiManager.open(new LearnScreen(player,
                result.taughtSpells().stream().filter(id -> SpellRegistry.findByName(id) != null).toList()));
        if (!result.skillOffers().isEmpty()) GuiManager.open(LearnScreen.forTrainingOffers(player,
                result.skillOffers().stream().filter(offer -> KNOWN_SKILLS.contains(offer.skill()))
                        .map(offer -> new LearnScreen.TrainingOffer(offer.skill(), offer.limitOrInitialPoints(),
                                offer.goldCost(), offer.teaching())).toList()));
        else {
            List<String> skills = new ArrayList<>(result.taughtSkills());
            skills.addAll(result.trainedSkills());
            if (!skills.isEmpty()) GuiManager.open(LearnScreen.forTraining(player,
                    skills.stream().filter(KNOWN_SKILLS::contains).distinct().toList()));
        }
        if (!result.formulaOffers().isEmpty()) GuiManager.open(LearnScreen.forFormulaOffers(player,
                result.formulaOffers().stream().map(offer -> new LearnScreen.FormulaOffer(
                        offer.formulaId(), offer.goldCost())).toList()));
        for (String spellId : result.targetSpells()) {
            SpellData spell = resolveSpell(spellId);
            if (spell != null) NpcCastVfxHook.playOnPlayer(spell, player, position);
        }
        for (String spellId : result.selfSpells()) {
            SpellData spell = resolveSpell(spellId);
            if (spell != null) NpcCastVfxHook.playOnSelf(spell, position);
        }
        for (NpcScriptEngine.SummonRequest summon : result.summons()) {
            int z = summon.zExpression() == null ? player.getCoordinates().getZ()
                    : scriptCoordinate(summon.zExpression(), player, false, true);
            float x = scriptCoordinate(summon.xExpression(), player, true, false) * GRID_W;
            float y = scriptCoordinate(summon.yExpression(), player, false, false) * GRID_H;
            if (!NpcSummonBridge.summon(summon.monster(), x, y, z, def.getSourceEvents())) {
                log.warn("NPC '{}' could not summon '{}' at ({}, {}, {})", def.getName(), summon.monster(), x, y, z);
            }
        }
        if (result.xp() != 0 && questService != null) questService.awardScriptXp(player, result.xp());
        if (result.heal()) healFully(player);
        if (result.endConversation()) endInteraction();
        if (result.pendingYesNo() != null) pendingYesNoState = result.pendingYesNo();
        return true;
    }

    /** Executes one migrated C++ lifecycle handler against the local player. */
    public boolean triggerScriptEvent(String event, Player player) {
        String script = def.getSourceEvents().get(event);
        if (script == null) return false;
        NpcScriptEngine.Result result = NpcScriptEngine.event(
                script, def.getName(), player, getCurrentHp(), getMaxHp());
        boolean handled = applyScriptResult(result, player);
        if (result.npcHpOverride() != Integer.MIN_VALUE)
            setCurrentHp(Math.max(0, Math.min(getMaxHp(), result.npcHpOverride())));
        if (result.selfDestruct()) setCurrentHp(0);
        return handled;
    }

    private static boolean sellRuleMatches(NpcScriptEngine.SellRule rule,
            com.perso.T4C.item.ItemDefinition item) {
        if (item.getPrice() < rule.minimumPrice() || item.getPrice() > rule.maximumPrice()) return false;
        String categories = rule.categories();
        boolean structural = (categories.contains("WEAPON") && (item.getStructure() == 1 || item.getStructure() == 8 || item.getStructure() == 9))
                || (categories.contains("ARMOR") && item.getStructure() == 2)
                || (categories.contains("POTION") && item.getStructure() == 5)
                || (categories.contains("JEWEL") && (item.getBodyPart() == com.perso.T4C.player.BodyPart.RING1
                    || item.getBodyPart() == com.perso.T4C.player.BodyPart.RING2
                    || item.getBodyPart() == com.perso.T4C.player.BodyPart.NECK))
                || (categories.contains("MAGIC") && (!item.getSpells().isEmpty() || !item.getBoosts().isEmpty()))
                || categories.contains("PAWNSHOP") || categories.contains("JUNK");
        return structural;
    }

    private int scriptCoordinate(String expression, Player player, boolean xAxis, boolean world) {
        String value = expression == null ? "0" : expression.trim();
        if (value.equals("target->GetWL().X")) return Math.round(player.getCoordinates().getX() / GRID_W);
        if (value.equals("target->GetWL().Y")) return Math.round(player.getCoordinates().getY() / GRID_H);
        if (value.equals("target->GetWL().world")) return player.getCoordinates().getZ();
        java.util.regex.Matcher relative = java.util.regex.Pattern
                .compile("FROM_(NPC|USER)\\s*\\((.+),\\s*[XY]\\s*\\)").matcher(value);
        if (relative.matches()) {
            int offset = scriptExpression(relative.group(2));
            boolean npc = relative.group(1).equals("NPC");
            float base = npc ? (xAxis ? position.x / GRID_W : position.y / GRID_H)
                    : (xAxis ? player.getCoordinates().getX() / GRID_W : player.getCoordinates().getY() / GRID_H);
            return Math.round(base) + offset;
        }
        if (world) return scriptExpression(value);
        return scriptExpression(value);
    }

    private static int scriptExpression(String expression) {
        String value = expression == null ? "0" : expression.replaceAll("\\s+", "");
        java.util.regex.Matcher dice = java.util.regex.Pattern
                .compile("rnd\\.roll\\(dice\\(1,(\\d+)\\)\\)([+-]\\d+)?").matcher(value);
        if (dice.matches()) {
            int sides = Math.max(1, Integer.parseInt(dice.group(1)));
            int offset = dice.group(2) == null ? 0 : Integer.parseInt(dice.group(2));
            return java.util.concurrent.ThreadLocalRandom.current().nextInt(1, sides + 1) + offset;
        }
        try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return 0; }
    }

    private static SpellData resolveSpell(String id) {
        SpellData exact = SpellRegistry.findByName(id);
        if (exact != null || id == null) return exact;
        String alias;
        if (id.contains("serious_heal")) alias = "spell.heal_serious";
        else if (id.contains("fireball")) alias = "spell.fireball";
        // Teleport scripts carry names such as "..._return_teleport_dispel"; they must not fall back
        // to the dispel visual, which would fire a projectile at the player taking the portal.
        else if (id.contains("teleport")) alias = null;
        else if (id.contains("dispel") || id.contains("blue_wipe")) alias = "spell.dispel";
        else if (id.contains("lighthaven") && id.contains("portal")) alias = "spell.lighthaven_portal";
        else if (id.contains("windhowl") && id.contains("portal")) alias = "spell.windhowl_portal";
        else if (id.contains("silversky") && id.contains("portal")) alias = "spell.silversky_portal";
        else if (id.contains("stonecrest") && id.contains("portal")) alias = "spell.stonecrest_portal";
        else alias = null;
        return alias == null ? null : SpellRegistry.findByName(alias);
    }

    private boolean respondToTopic(String text, Player player) {
        if (text == null || player == null) return false;
        for (NpcDef.DialogTopic topic : def.getTopics()) {
            if (matches(topic, text)) {
                applyTopic(topic, player);
                return true;
            }
        }
        return false;
    }

    static boolean matches(NpcDef.DialogTopic topic, String text) {
        for (String keyword : topic.getKeywords()) {
            String command = normalizeCommand(I18n.resolve(keyword));
            if (!command.isEmpty() && (" " + normalizeCommand(text) + " ")
                    .contains(" " + command + " ")) return true;
        }
        return false;
    }

    private void applyTopic(NpcDef.DialogTopic topic, Player player) {
        if (topic.getResponse() != null && !topic.getResponse().isBlank()) {
            showDialog(I18n.resolve(topic.getResponse()), 0L);
        }
        for (NpcDef.Action action : topic.getActions()) executeAction(action, player);
    }

    private void executeAction(NpcDef.Action action, Player player) {
        if (action == null || action.getType() == null) return;
        switch (action.getType()) {
            case OPEN_SPELL_LEARNING -> {
                List<String> valid = action.getTargets().stream()
                        .filter(id -> {
                            boolean found = SpellRegistry.findByName(id) != null;
                            if (!found) log.warn("NPC '{}' references unknown spell '{}'", def.getName(), id);
                            return found;
                        }).toList();
                GuiManager.open(new LearnScreen(player, valid));
            }
            case OPEN_SKILL_LEARNING -> {
                List<String> valid = action.getTargets().stream()
                        .filter(id -> {
                            boolean found = KNOWN_SKILLS.contains(id);
                            if (!found) log.warn("NPC '{}' references unknown skill '{}'", def.getName(), id);
                            return found;
                        }).toList();
                GuiManager.open(LearnScreen.forTraining(player, valid));
            }
            case OPEN_SHOP -> {
                List<String> valid = action.getTargets().stream()
                        .filter(id -> {
                            boolean found = ItemRegistry.findByKey(id) != null;
                            if (!found) log.warn("NPC '{}' references unknown shop item '{}'", def.getName(), id);
                            return found;
                        }).toList();
                GuiManager.open(new ShopScreen(player, valid));
            }
            case OPEN_REPAIR -> GuiManager.open(new RepairScreen(player));
            case GIVE_ITEM -> {
                if (action.getTargets().isEmpty()) return;
                String itemKey = action.getTargets().get(0);
                if (ItemRegistry.findByKey(itemKey) == null) {
                    log.warn("NPC '{}' references unknown gift item '{}'", def.getName(), itemKey);
                } else {
                    InventoryService.add(player, itemKey);
                }
            }
            case GIVE_QUEST -> {
                if (action.getTargets().isEmpty() || questService == null) {
                    if (questService == null) {
                        log.warn("NPC '{}' cannot execute GIVE_QUEST without a quest service", def.getName());
                    }
                    return;
                }
                String response = questService.giveOrReport(
                        action.getTargets().get(0), def.getName(), player);
                if (response != null && !response.isBlank()) {
                    showDialog(response, 0L);
                }
            }
            case END_CONVERSATION -> endInteraction();
            case HEAL -> healFully(player);
            case SUMMON_COMPANION -> summonCompanion(action, player);
        }
    }

    /**
     * Spawns the ally companion named by the action target next to this NPC.
     * The companion then follows the player and fights alongside them.
     */
    private void summonCompanion(NpcDef.Action action, Player player) {
        CompanionManager companionManager =
                companionManagerSupplier == null ? null : companionManagerSupplier.get();
        if (companionManager == null) {
            log.warn("NPC '{}' cannot execute SUMMON_COMPANION without a companion manager", def.getName());
            return;
        }
        if (action.getTargets().isEmpty()) {
            log.warn("NPC '{}' declares SUMMON_COMPANION without a companion type", def.getName());
            return;
        }
        companionManager.spawnCompanionFor(player, action.getTargets().get(0), position);
    }

    private void healFully(Player player) {
        if (player.getCurrentHp() >= player.getMaxHp() && player.getMana() >= player.getMaxMana()) {
            showDialog(I18n.message("message.no_healing_needed"), 0L);
            return;
        }
        int healthBefore = player.getCurrentHp();
        player.applyHeal(player.getMaxHp(), player.getMaxHp());
        player.setMana(player.getMaxMana());
        if (player.getCurrentHp() > healthBefore) {
            SpellData healingSpell = SpellRegistry.findByName("spell.healing");
            if (healingSpell != null) {
                NpcCastVfxHook.playOnPlayer(healingSpell, player, position);
            }
        }
        showDialog(I18n.message("message.healed_dialog"), 0L);
        SystemMessage.showShared(I18n.message("message.wounds_healed"));
    }

    private static String normalizeCommand(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    @Override
    protected boolean onDialogKeywordClick(String keyword, Player player) {
        return respondToTopic(keyword, player);
    }

    @Override
    protected float getPatrolRadius() {
        return def.getPatrolRadiusTiles() > 0
                ? def.getPatrolRadiusTiles() * GRID_W : NPC_PATROL_RADIUS;
    }
}
