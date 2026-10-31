# Original-content restoration follow-up (T4C-0409)

Base: `origin/main` 52990d8e. Scope: offline original-game content. This pass checked the open leads in `2026-10-content-audit-recheck.md` against shipped Java and the original NPC DLL text in `assets/origin`. It did not alter multiplayer, shared engine classes, maps or spawn coordinates.

## Repaired: Elysana to Samuel wax-apple referral

`npc/ElysanaBlackrose.java` already handles the poison-apple trade and has static `WAX`/`APPLE` dialogue. The original `NPC RavensDust.dll` contains Elysana's line pointing the player to Samuel for a wax apple. `npc/Samuel.java` already implements the ten-wax, 1,000-gold crafting attempt after the Death Jester, but first checks `__QUEST_WAX_APPLE != 0`. No code set that flag, so the exchange was permanently locked. Elysana's `WAX` or `APPLE` conversation now sets the referral flag and retains the original dialogue. Asking about `POISON APPLE` follows its existing branch and does not set the referral.

Reproduce the source check from the repository root:

```powershell
rg -n 'QUEST_WAX_APPLE|k.equals\("WAX"\)|k.equals\("APPLE"\)' src/main/java/com/perso/T4C/npc
python -c "from pathlib import Path; a=[s.decode('latin1','ignore') for s in Path('assets/origin/NPC RavensDust.dll').read_bytes().split(bytes([0]))]; print(*[s for s in a if 'king\'s advisor, Samuel' in s or 'wax apple is a beautiful decoration' in s],sep=chr(10))"
```

The DLL text confirms the dialogue referral; the precise original bytecode assignment of the quest flag has not been recovered. Setting that existing gate when the referral is given is the narrowest behavior supported by the original dialogue and current Samuel implementation. Samuel's cost, chance of success and Death Jester requirement remain as they were.

## Remaining leads

- **Zhakar's Arcane Spellbook route.** `NPC RavensDust.dll` says that Sweepers in Zhakar's tower provide Arcane Scrolls, and crafting requires at least ten scrolls and 35,000 gold. It also mentions a later completion and possible failure. `npc/Sweeper.java` gives a scroll on death but is only spawned at `(0,0,0)`; Zhakar currently grants two books to Ogrimar followers and has no scroll-crafting route. The DLL text does not give Sweeper coordinates, crafting odds or delay. Those details need original script/map reconstruction before restoration.
- **Shadow Guild missions.** Kalastor's mission and faction flags have consumers but no complete mission system. The original mission steps and rewards must be extracted before wiring them.
- **Unplaced NPCs and monster definitions.** The prior audit lists them; name matches alone do not prove a spawn is missing. No coordinates were inferred or added here.
- **Areas.** This pass did not establish a missing original area or a safe map placement from the available binary assets. Area parity remains unverified.

These open items are not counted as restored or verified gameplay.
