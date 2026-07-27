package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.screen.ShopScreen;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.SystemMessage;
import com.perso.T4C.i18n.I18n;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_RADIUS;

/**
 * Concrete NPC built from an {@link NpcDef}. Replaces the former hardcoded
 * {@code com.perso.T4C.npc.named.*} subclasses; data drives appearance, dialog
 * and the keyword action.
 */
public class DataNpc extends BaseNPC {

    private final NpcDef def;

    public DataNpc(NpcDef def) throws GameException {
        super(def.getName(), def.getSpriteBase(), buildParts(def));
        this.def = def;
        if (def.getDisplayName() != null && !def.getDisplayName().isEmpty()) {
            // NPC definitions may serialize their display value as an i18n
            // placeholder (for example ${npc.iraltok}). Resolve from the
            // stable NPC identity instead, so a placeholder never reaches the
            // name renderer used by right-click.
            String translatedName = I18n.npc(def.getName());
            setDisplayName(translatedName.equals(def.getName())
                    ? I18n.npc(def.getDisplayName())
                    : translatedName);
        }
    }

    private static Object[] buildParts(NpcDef def) {
        List<Object> parts = new ArrayList<>();
        if (def.getParts() != null) {
            for (NpcDef.Part part : def.getParts()) {
                if (part != null && part.getBodyPart() != null) {
                    parts.add(part.getBodyPart());
                    parts.add(part.getSpriteBase());
                }
            }
        }
        return parts.toArray();
    }

    @Override
    protected void onInteractStart(Player player) {
        if (def.getDialogText() != null && !def.getDialogText().isEmpty()) {
            showDialog(I18n.npcDialog(def.getName(), def.getDialogText()), 0L);
        }
    }

    @Override
    protected String getDialogKeyword() {
        return I18n.npcKeyword(def.getName(), def.getDialogKeyword());
    }

    @Override
    public boolean talk(String text, Player player) {
        if (isInteracting && text != null) {
            if (isTeachingKeyword(text)) {
                openTeaching(player);
                return true;
            }
            if (matchesDefinitionActionKeyword(text)) {
                return executeDefinitionAction(player);
            }
        }
        return super.talk(text, player);
    }

    private boolean matchesDefinitionActionKeyword(String text) {
        if (text == null || def.getAction() == null || def.getAction() == KeywordActionType.NONE) {
            return false;
        }
        if (def.getAction() == KeywordActionType.HEAL && isHealCommand(text)) {
            return true;
        }
        return containsCommand(text, I18n.english(def.getDialogKeyword()))
                || containsCommand(text, I18n.npcKeyword(def.getName(), def.getDialogKeyword()));
    }

    /** GoN accepts the canonical HEAL keyword and its ordinary French equivalents. */
    static boolean isHealCommand(String text) {
        return containsCommand(text, "heal")
                || containsCommand(text, "soin")
                || containsCommand(text, "soins")
                || containsCommand(text, "soigner")
                || containsCommand(text, "guerir");
    }

    private static boolean containsCommand(String speech, String command) {
        String normalizedSpeech = normalizeCommand(speech);
        String normalizedCommand = normalizeCommand(command);
        return !normalizedCommand.isEmpty()
                && (" " + normalizedSpeech + " ").contains(" " + normalizedCommand + " ");
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
        if (player == null || def.getAction() == null) {
            return false;
        }
        if (isTeachingKeyword(keyword)) {
            openTeaching(player);
            return true;
        }
        return executeDefinitionAction(player);
    }

    private boolean isTeachingKeyword(String keyword) {
        // CAST NPCs reuse taughtSpells as the spell to cast, not a curriculum.
        if (keyword == null || def.getAction() == KeywordActionType.CAST
                || def.getTaughtSpells() == null || def.getTaughtSpells().isEmpty()) return false;
        String normalized = keyword.trim();
        return normalized.equalsIgnoreCase("learn") || normalized.equalsIgnoreCase("spell")
                || normalized.equalsIgnoreCase("spells") || normalized.equalsIgnoreCase("teach")
                || normalized.equalsIgnoreCase("sort") || normalized.equalsIgnoreCase("sorts")
                || normalized.equalsIgnoreCase("apprendre") || normalized.equalsIgnoreCase("enseigner");
    }

    private void openTeaching(Player player) {
        GuiManager.open(new LearnScreen(player, def.getTaughtSpells()));
    }

    private boolean executeDefinitionAction(Player player) {
        switch (def.getAction()) {
            case HEAL:
                int hpBefore = player.getCurrentHp();
                int manaBefore = player.getMana();
                if (hpBefore >= player.getMaxHp() && manaBefore >= player.getMaxMana()) {
                    showDialog(I18n.message("message.no_healing_needed", "message.no_healing_needed"), 0L);
                    return true;
                }
                player.applyHeal(def.getActionParam1(), def.getActionParam2());
                player.setMana(player.getMaxMana());
                if (player.getCurrentHp() > hpBefore || player.getMana() > manaBefore) {
                    showDialog(I18n.message("message.healed_dialog", "message.healed_dialog"), 0L);
                    SystemMessage.showShared(I18n.message("message.wounds_healed", "message.wounds_healed"));
                }
                return true;
            case TEACH:
                GuiManager.open(new LearnScreen(player, def.getTaughtSpells()));
                return true;
            case SHOP:
                GuiManager.open(new ShopScreen(player, def.getShopItems()));
                return true;
            case TRAIN:
                GuiManager.open(LearnScreen.forTraining(player, def.getTrainableStats()));
                return true;
            case CAST:
                return castOnPlayer(player);
            default:
                return false;
        }
    }

    /**
     * CAST action: applies the NPC's first taught spell (heal/mana/buff) to the player.
     * {@code self} in the spell's formulas resolves to the NPC casting it, not the player
     * receiving it — the taught-spell's {@code price} slot (unused for CAST, no gold cost)
     * carries the NPC's caster level/wisdom for that purpose, set via npcs.bin.
     */
    private boolean castOnPlayer(Player player) {
        if (def.getTaughtSpells() == null || def.getTaughtSpells().isEmpty()) {
            return false;
        }
        NpcDef.TaughtSpell taught = def.getTaughtSpells().get(0);
        com.perso.T4C.spell.SpellData spell = com.perso.T4C.spell.SpellRegistry.findByName(taught.getSpellName());
        if (spell == null) {
            return false;
        }
        int casterPower = Math.max(1, taught.getPrice());
        var casterContext = com.perso.T4C.spell.SpellEffectManager.npcCasterContext(casterPower, casterPower);
        var manager = new com.perso.T4C.spell.SpellEffectManager();
        int healthDelta = manager.resolvePlayerHealthDelta(spell, player);
        if (healthDelta > 0) {
            player.applyHeal(healthDelta, healthDelta);
        }
        int manaDelta = manager.resolvePlayerManaDelta(spell, player);
        if (manaDelta != 0) {
            player.setMana(Math.max(0, Math.min(player.getMaxMana(), player.getMana() + manaDelta)));
        }
        var buffs = manager.resolvePlayerBuffEffects(spell, casterContext);
        if (!buffs.isEmpty()) {
            player.applyBuff(spell.getName(), spell.getDescription(), spell.getIconId(),
                    manager.resolveDurationSeconds(spell, casterContext), false, buffs);
        } else if (spell.getBuff() != null) {
            player.applyBuff(spell.getName(), spell.getDescription(), spell.getIconId(),
                    spell.getBuff().getDurationSeconds(),
                    Boolean.TRUE.equals(spell.getBuff().getUnlimited()), spell.getBuff().getEffects());
        }
        com.perso.T4C.spell.NpcCastVfxHook.playOnPlayer(spell, player, position);
        showDialog(I18n.npcCastDialog(def.getName(), "npc.cast_dialog." + def.getName()), 0L);
        SystemMessage.showShared(I18n.message("message.npc_cast", "message.npc_cast",
                getName(), I18n.spellName(spell.getName())));
        return true;
    }

    @Override
    protected float getPatrolRadius() {
        return def.getPatrolRadiusTiles() > 0 ? def.getPatrolRadiusTiles() * GRID_W : NPC_PATROL_RADIUS;
    }
}
