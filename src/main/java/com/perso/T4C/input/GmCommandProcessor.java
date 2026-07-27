package com.perso.T4C.input;

import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.MonsterManager;
import com.perso.T4C.npc.NPCManager;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.SystemMessage;

import java.util.ArrayList;
import java.util.List;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/**
 * Parses and executes GM (game-master) commands typed in-game.
 * Format: {@code <command> <value>}, e.g. {@code level 5} or {@code gold 9999}.
 */
public final class GmCommandProcessor {

    private final XpCurve xpCurve;
    private final NPCManager npcManager;
    private final MonsterManager monsterManager;

    public GmCommandProcessor(XpCurve xpCurve) {
        this(xpCurve, null, null);
    }

    public GmCommandProcessor(XpCurve xpCurve, NPCManager npcManager, MonsterManager monsterManager) {
        this.xpCurve = xpCurve;
        this.npcManager = npcManager;
        this.monsterManager = monsterManager;
    }

    /**
     * Execute a raw command string (without the leading {@code #}).
     * Shows feedback via the shared SystemMessage.
     */
    public void execute(String raw, Player player) {
        if (raw == null || raw.isBlank() || player == null) {
            return;
        }
        String[] parts = raw.trim().split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";

        try {
            switch (cmd) {
                case "level":    setLevel(player, parseInt(arg));    break;
                case "xp":       setXp(player, parseInt(arg));       break;
                case "gold":     setGold(player, parseInt(arg));     break;
                case "hp":       setHp(player, parseInt(arg));       break;
                case "mana":     setMana(player, parseInt(arg));     break;
                case "str":      setStat(player, "str", parseInt(arg)); break;
                case "dex":      setStat(player, "dex", parseInt(arg)); break;
                case "end":      setStat(player, "end", parseInt(arg)); break;
                case "int":      setStat(player, "int", parseInt(arg)); break;
                case "wis":      setStat(player, "wis", parseInt(arg)); break;
                case "statpts":  player.setStatPoints(parseInt(arg));
                                 ok("Stat points set to " + player.getStatPoints(), player); break;
                case "skillpts": player.setSkillPoints(parseInt(arg));
                                 ok("Skill points set to " + player.getSkillPoints(), player); break;
                case "summon":   summon(player, arg); break;
                case "teleport": teleport(player, arg); break;
                case "learn":    learn(player, arg); break;
                case "collision": collision(player, arg); break;
                case "speed":    speed(player, arg); break;
                default:
                    SystemMessage.showShared("Unknown GM command: " + cmd
                            + "  (level/xp/gold/hp/mana/str/dex/end/int/wis/statpts/skillpts/summon/teleport/learn/collision/speed)");
            }
        } catch (NumberFormatException e) {
            SystemMessage.showShared("GM: invalid value \"" + arg + "\" - must be a number.");
        }
    }

    private void setLevel(Player player, int level) {
        if (level < 1) level = 1;
        player.setLevel(level);
        if (xpCurve != null) {
            int next = xpCurve.getXpToNextLevel(level);
            if (next > 0) player.setXpToNextLevel(next);
        }
        ok("Level set to " + player.getLevel(), player);
    }

    private void setXp(Player player, int xp) {
        player.setCurrentXp(Math.max(0, xp));
        ok("XP set to " + player.getCurrentXp(), player);
    }

    private void setGold(Player player, int gold) {
        player.setGold(Math.max(0, gold));
        ok("Gold set to " + player.getGold(), player);
    }

    private void setHp(Player player, int hp) {
        int v = Math.max(1, hp);
        player.setMaxHp(v);
        player.setCurrentHp(v);
        ok("HP set to " + v + " / " + v, player);
    }

    private void setMana(Player player, int mana) {
        int v = Math.max(0, mana);
        player.setMaxMana(v);
        player.setMana(v);
        ok("Mana set to " + v + " / " + v, player);
    }

    private void setStat(Player player, String stat, int value) {
        int v = Math.max(1, value);
        switch (stat) {
            case "str": player.setStrength(v);     ok("Strength set to " + v, player); break;
            case "dex": player.setDexterity(v);    ok("Dexterity set to " + v, player); break;
            case "end": player.setEndurance(v);    ok("Endurance set to " + v, player); break;
            case "int": player.setIntelligence(v); ok("Intelligence set to " + v, player); break;
            case "wis": player.setWisdom(v);       ok("Wisdom set to " + v, player); break;
        }
    }

    private void summon(Player player, String arg) {
        String[] parts = arg == null ? new String[0] : arg.trim().split("\\s+", 2);
        if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
            SystemMessage.showShared("GM: usage #summon item <key>, #summon npc <name>, #summon monster <name>");
            return;
        }

        String type = parts[0].toLowerCase();
        String name = parts[1].trim();
        switch (type) {
            case "item":
                summonItem(player, name);
                break;
            case "npc":
                summonNpc(player, name);
                break;
            case "monster":
                summonMonster(player, name);
                break;
            default:
                SystemMessage.showShared("GM: unknown summon type \"" + type + "\"");
                break;
        }
    }

    private void summonItem(Player player, String key) {
        if (ItemRegistry.findByKey(key) == null) {
            SystemMessage.showShared("GM: unknown item \"" + key + "\"");
            return;
        }
        if (player.getInventory() == null) {
            SystemMessage.showShared("GM: inventory unavailable");
            return;
        }
        player.getInventory().add(key);
        ok("Summoned item " + key, player);
    }

    private void summonNpc(Player player, String name) {
        if (npcManager == null) {
            SystemMessage.showShared("GM: NPC manager unavailable");
            return;
        }
        if (npcManager.spawnNPC(name, player.getPositionVector().x, player.getPositionVector().y)) {
            SystemMessage.showShared("GM: Summoned NPC " + name);
        } else {
            SystemMessage.showShared("GM: unknown NPC \"" + name + "\"");
        }
    }

    private void summonMonster(Player player, String name) {
        if (monsterManager == null) {
            SystemMessage.showShared("GM: monster manager unavailable");
            return;
        }
        if (monsterManager.spawnMonster(name, player.getPositionVector().x, player.getPositionVector().y)) {
            SystemMessage.showShared("GM: Summoned monster " + name);
        } else {
            SystemMessage.showShared("GM: unknown monster \"" + name + "\"");
        }
    }

    private void teleport(Player player, String arg) {
        String[] coords = arg == null ? new String[0] : arg.trim().split("\\s*,\\s*");
        if (coords.length != 3) {
            SystemMessage.showShared("GM: usage #teleport X,Y,Z");
            return;
        }
        int tileX = parseInt(coords[0]);
        int tileY = parseInt(coords[1]);
        int z = parseInt(coords[2]);
        player.setWorldPosition(tileX * GRID_W, tileY * GRID_H, z);
        ok("Teleported to " + tileX + "," + tileY + "," + z, player);
    }

    private void learn(Player player, String arg) {
        String requested = stripTextId(arg).trim();
        if (requested.isEmpty()) {
            SystemMessage.showShared("GM: usage #learn <spell>");
            return;
        }

        SpellData spell = findSpell(requested);
        if (spell == null) {
            SystemMessage.showShared("GM: unknown spell \"" + requested + "\"");
            return;
        }

        List<String> spells = player.getSpells();
        if (spells == null) {
            spells = new ArrayList<>();
            player.setSpells(spells);
        }
        for (String known : spells) {
            if (known != null && known.equalsIgnoreCase(spell.getName())) {
                SystemMessage.showShared("GM: already knows " + spell.getName());
                return;
            }
        }

        spells.add(spell.getName());
        ok("Learned spell " + spell.getName(), player);
    }

    private void collision(Player player, String arg) {
        String mode = arg == null ? "" : arg.trim().toLowerCase();
        if (mode.isEmpty() || "toggle".equals(mode)) {
            player.setPlayerCollisionsEnabled(!player.isPlayerCollisionsEnabled());
        } else if ("on".equals(mode) || "enable".equals(mode) || "enabled".equals(mode) || "1".equals(mode)) {
            player.setPlayerCollisionsEnabled(true);
        } else if ("off".equals(mode) || "disable".equals(mode) || "disabled".equals(mode) || "0".equals(mode)) {
            player.setPlayerCollisionsEnabled(false);
        } else {
            SystemMessage.showShared("GM: usage #collision on|off|toggle");
            return;
        }
        SystemMessage.showShared("GM: player collisions "
                + (player.isPlayerCollisionsEnabled() ? "enabled" : "disabled"));
    }

    private void speed(Player player, String arg) {
        String value = arg == null ? "" : arg.trim().toLowerCase();
        float current = player.getGmSpeedMultiplier();
        float next;
        if (value.isEmpty() || "+".equals(value) || "up".equals(value) || "increase".equals(value)) {
            next = current + 0.25f;
        } else if ("-".equals(value) || "down".equals(value) || "decrease".equals(value)) {
            next = current - 0.25f;
        } else if ("reset".equals(value) || "normal".equals(value)) {
            next = 1.0f;
        } else {
            next = parseFloat(value);
        }
        player.setGmSpeedMultiplier(next);
        SystemMessage.showShared(String.format(java.util.Locale.ROOT,
                "GM: speed multiplier %.2fx", player.getGmSpeedMultiplier()));
    }

    private SpellData findSpell(String requested) {
        SpellData exact = SpellRegistry.findByName(requested);
        if (exact != null) {
            return exact;
        }

        Integer spellId = tryParseInt(requested);
        for (SpellData spell : SpellRegistry.load()) {
            if (spell == null) {
                continue;
            }
            if (spellId != null && spell.getSpellId() == spellId) {
                return spell;
            }
            if (spell.getName() != null && spell.getName().equalsIgnoreCase(requested)) {
                return spell;
            }
        }
        return null;
    }

    private void ok(String msg, Player player) {
        PlayerStateStore.save(player);
        SystemMessage.showShared("GM: " + msg);
    }

    private static int parseInt(String s) {
        if (s == null || s.isEmpty()) throw new NumberFormatException("empty");
        return Integer.parseInt(s);
    }

    private static Integer tryParseInt(String s) {
        try {
            return parseInt(s);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static float parseFloat(String s) {
        if (s == null || s.isEmpty()) throw new NumberFormatException("empty");
        return Float.parseFloat(s);
    }

    private static String stripTextId(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\\s*\\[[0-9]+]\\s*", " ").replaceAll("\\s+", " ");
    }
}
