# Avalon sanctuary and Wilds — T4C-0099

This first pass rebuilds the sanctuary and the original Wilds hunting area. Further island-wide expansion and combat balance remain follow-up work.

## Sanctuary

![Sanctuary scenery](sanctuary.png)

A complete stone temple encloses the arrival point at (1340,1477). The temple has an open entrance, pews and a blessing chest. Two smaller buildings house services; townsfolk remain stationary on protected, reachable tiles. Safe-haven ground blocks incoming and outgoing combat, including delayed hits.

## Wilds

![Wilds scenery](wilds.png)

Paths connect the sanctuary to two Stalker hunting glades, a separate Fey Warden grove, and Caradoc's border clearing. Tree clusters frame the glades while leaving travel and combat space visible. Existing unfinished scenery beyond the rebuilt area is still visible at the edges of this overview.

## Quest walkthrough

Speak to Elder Ophira inside the temple at (1344,1462):

1. Say **wilds** to hear the mission, then **accept**.
2. Say **route** for directions. Follow the paths north to the Stalkers and west to Caradoc.
3. Complete the journal's kill objective and recover Caradoc's Sundered Blade.
4. Return and say **report**. Ophira checks both objectives before advancing.
5. Say **veil** to hear the next chapter, then **accept**.

Existing accepted/completed quest states and kill progress are retained. Players who already accepted the Veil quest can still finish it.

## Verification

- Full Maven suite: 514 tests passed, no failures, errors or skips.
- Real collision-map tests check town safety, service access, hunting routes, hostile spawn exclusion and quest bounds.
- Full-map comparison of ground, decor, offsets, scale, draw order and collision found zero edits outside the documented sanctuary/Wilds mask.
- These images use the game's OpenGL ground/decor/object renderer. They omit characters; they are scenery checks, not an interactive gameplay recording.

See [map editor instructions](../map-editor.md) for the launcher, preview tool and regeneration caveats.
