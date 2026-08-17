package com.perso.T4C.npc;

import com.perso.T4C.monster.DataMonster;
import com.perso.T4C.player.Player;

import java.util.ArrayList;
import java.util.List;

/** Executes lifecycle handlers persisted on data-driven monster definitions. */
public final class MonsterScriptBridge {
    public record Effects(List<String> messages, List<String> selfSpells, List<String> targetSpells) {}

    private MonsterScriptBridge() {}

    public static Effects death(DataMonster monster, Player player) {
        List<String> messages = new ArrayList<>(), selfSpells = new ArrayList<>(), targetSpells = new ArrayList<>();
        execute(monster, player, "OnDeath", messages, selfSpells, targetSpells);
        execute(monster, player, "OnDestroy", messages, selfSpells, targetSpells);
        return new Effects(List.copyOf(messages), List.copyOf(selfSpells), List.copyOf(targetSpells));
    }

    private static void execute(DataMonster monster, Player player, String event, List<String> messages,
                                List<String> selfSpells, List<String> targetSpells) {
        String script = monster.getSourceEvents().get(event);
        if (script == null || script.isBlank()) return;
        NpcScriptEngine.Result result = NpcScriptEngine.event(script, monster.getCanonicalName(), player, 0, 0);
        messages.addAll(result.systemMessages());
        for (String spell : result.selfSpells()) {
            String spellScript = monster.getSourceEvents().get("@spell." + spell);
            if (spellScript != null && !spellScript.isBlank()) {
                NpcScriptEngine.Result spellResult = NpcScriptEngine.event(
                        spellScript, monster.getCanonicalName(), player, 0, 0);
                messages.addAll(spellResult.systemMessages());
                targetSpells.addAll(spellResult.targetSpells());
            }
            selfSpells.add(spell);
        }
        targetSpells.addAll(result.targetSpells());
    }
}
