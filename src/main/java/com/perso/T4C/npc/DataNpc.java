package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.gui.screen.ShopScreen;
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
import java.util.Set;

import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_RADIUS;

/** Runtime NPC backed by the simple welcome/topics dialogue definition. */
@Slf4j
public class DataNpc extends BaseNPC {
    private static final Set<String> KNOWN_SKILLS = Set.of(
            "attack", "archery", "dodge", "peek", "stun_blow", "powerful_blow",
            "rapid_healing", "first_aid", "parry", "critical_strike", "hide", "sneak",
            "search", "picklock", "armor_penetration", "two_weapons", "rob",
            "strength", "dexterity", "endurance", "intelligence", "wisdom");
    private final NpcDef def;
    private final QuestService questService;

    public DataNpc(NpcDef def) throws GameException {
        this(def, null);
    }

    public DataNpc(NpcDef def, QuestService questService) throws GameException {
        super(def.getName(), def.getSpriteBase(), buildParts(def));
        this.def = def;
        this.questService = questService;
        if (def.getDisplayName() != null && !def.getDisplayName().isEmpty()) {
            String translatedName = I18n.resolve(def.getName());
            setDisplayName(translatedName.equals(def.getName())
                    ? I18n.resolve(def.getDisplayName()) : translatedName);
        }
    }

    private static Object[] buildParts(NpcDef def) {
        List<Object> parts = new ArrayList<>();
        for (NpcDef.Part part : def.getParts()) {
            if (part != null && part.getBodyPart() != null) {
                parts.add(part.getBodyPart());
                parts.add(part.getSpriteBase());
            }
        }
        return parts.toArray();
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
        if (def.getWelcomeText() != null && !def.getWelcomeText().isBlank()) {
            showDialog(I18n.resolve(def.getWelcomeText()), 0L);
        }
    }

    @Override
    protected List<String> getDialogKeywords() {
        List<String> keywords = new ArrayList<>(super.getDialogKeywords());
        for (NpcDef.DialogTopic topic : def.getTopics()) {
            for (String keyword : topic.getKeywords()) {
                String resolved = I18n.resolve(keyword);
                if (resolved != null && !resolved.isBlank() && !keywords.contains(resolved)) {
                    keywords.add(resolved);
                }
            }
        }
        return keywords;
    }

    @Override
    public boolean talk(String text, Player player) {
        if (isInteracting && respondToTopic(text, player)) return true;
        return super.talk(text, player);
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
        }
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
