---
name: technical-writer
description: Editorial standard and review pass for every word a player reads — the compendium website, quest walkthroughs, zone summaries, NPC dialogue, item/spell/monster descriptions. Use whenever you add or change player-facing text, whenever you touch compendium/app.js, compendium/data/zones.json, compendium/data/meta.json or a `${...}` string in assets/i18n/lang.json, and whenever the user says the site reads "too technical", "robotic", "confusing", or asks for a copy/editorial/readability pass.
---

# Technical writer — the compendium's editorial standard

The compendium is read by **someone who plays the game and has never opened this repo**, and
often has never played T4C at all. They arrived because they want to know where to go next, what
to kill, and what they'll get. They are not an engineer, they do not know what a registry, a
spawn point, a geofence or a task ID is, and they will not forgive a page that makes them feel
stupid.

Every string on that site is a product surface. Treat it like one.

This skill is the **editorial gate**: run it before shipping any pass that changes player-facing
words or the site's structure. It pairs with `verify-website` (which checks the data is current
and the pages render) — this one checks the *words* are right.

---

## 1. The three rules

### Rule 1 — Nothing internal ever reaches the reader

The reader must never see anything that only exists because of how the game is built.

**Banned from player-facing text, without exception:**

| Banned | Why | Say instead |
|---|---|---|
| `T4C-0004`, "this pass", "pass timeline" | Internal task IDs | Nothing — or the update's name |
| `QuestService`, `GameConstants.SERVER_XP_RATE`, `CompendiumExporter.java` | Class/file names | Describe the behavior |
| `unlock.zone.*`, `spell.fire_dart`, `item.tempered_godcore` | Registry keys | The item/spell's display name |
| `self.maxmana`, `-((1d17+6+self.int/23)*self.fire/target.r_fire)` | Engine expressions | "All your remaining mana"; a damage range |
| "Effect type 9", `paramId`, `OnTimer, 100, 10000` | Raw effect rows | A sentence saying what it does |
| `@Spawn`, "spawn points", "zero spawn points", "placeholder stub" | Engine plumbing | "never actually appeared anywhere in the world" |
| "geofence", "world Z", "tile radius", "worldmap center" | Engine vocabulary | "the area shown on the map" |
| "fully-stat'd", "authored", "activated", "wired up", "hand-authored" | Dev vocabulary | "already in the game but unreachable" → or just describe the place |
| "pre-existing", "since the fork", "on top of the original fork" | Repo history | Nothing — players don't have a "before" |
| "int overflow", "widened to long", "silent bug", "refactor", "test" | Implementation | The symptom and that it's fixed |

If a number or name exists only so a developer can find something, it does not go on the page.
If a reader genuinely needs a coordinate (to find a place on a map), show it **on the map**, not
in a sentence.

### Rule 2 — Everything is a place in a journey, not a row in a table

The site's job is to answer *"where am I, and where do I go next?"* Zones, maps and quests are
the spine of that answer, so they must read as a **progression**, not an alphabetical dump.

- Zones, maps and quests are grouped into **chapters** — a named stretch of the world the player
  moves through in order (the mainland coast, then the crossing, then the new continent, then
  the endgame). Each chapter gets one short paragraph saying what changes about the game there.
- Within a chapter, order by **level**, never by id, name, or when it was built.
- Every zone page says, in prose: what this place is, who's there, why it turned hostile, what
  you need before you come, and **where you go afterwards**.
- Every quest page says what it's *for* in the journey: is it the gate to a new continent, a
  farming step for a crafting chain, or a side bounty you can skip?
- Explicit links forward and back ("next: …", "you'll want to have cleared …") beat any amount
  of tagging.

A player should be able to read the Zones page top to bottom and come away with a mental map of
the whole game. That is the test.

### Rule 3 — Say the thing, in the shortest true sentence

- Second person, present tense. "You'll need…", not "the player must…".
- Lead with what the reader does or gets. Setup and caveats go after.
- One idea per sentence. Cut "notably", "essentially", "it should be noted that".
- Concrete beats vague: "hits hard enough to kill an underleveled character in two swings"
  beats "is challenging".
- Never write a sentence whose subject is a system. Not "the quest grants 1,500 gold" —
  "Bryn pays you 1,500 gold".
- Numbers players see are **computed from the game's own data, never retyped** (see
  `CLAUDE.md`). If a sentence quotes a number, it goes stale; prefer letting the page render it.

---

## 2. Where the words live

Everything below is player-facing. Nothing else is.

| Surface | File | Notes |
|---|---|---|
| Site chrome, page leads, panel titles, captions, legends | `compendium/app.js` | Hand-written prose inside JS string literals. The biggest jargon reservoir. |
| Zone names, summaries, chapters, level ranges | `compendium/data/zones.json` | Hand-authored; the exporter only copies it through. |
| Update history, armor-set blurbs, Colosseum blurb | `compendium/data/meta.json` | Hand-authored. |
| Quest titles, offer/completion/walkthrough text | `assets/i18n/lang.json` (`quest.*`) | Referenced as `${quest.x.walkthrough}` from `quest/definition/*.java`. |
| NPC dialogue and greetings | `assets/i18n/lang.json` (`npc.*`) | |
| Item / spell / monster names and descriptions | `assets/items/*.json`, `assets/monsters/*.json`, `spell/definition/*.java`, `lang.json` | |
| Anything else on the site | generated by `tools/CompendiumExporter` | If it reads badly, fix the **exporter or `app.js`**, not the data by hand. |

`compendium/data.js` and `compendium/data/*.json` for monsters/items/spells/quests/npcs are
**generated**. Never hand-edit them; edit the source and re-run the exporter.

---

## 3. The review pass

Run this whenever player-facing text changed. It is a read-the-page pass — automated checks
catch leaks, but only reading catches robotic writing.

### 3a. Leak scan (mechanical, run every time)

The scan checks **prose the reader sees**, not lookup keys. A `"spell.riptide_surge"` inside a
zone's `spells` array is an id the site resolves to a display name — fine. The same string inside
a `summary` is a leak.

```bash
cd "$(git rev-parse --show-toplevel)"
python - <<'PY'
import json, io, re

BAD = re.compile(
    r'T4C-\d{4}|\bself\.[a-z]|\btarget\.[a-z_]+|@Spawn|Effect type|paramId|\.java\b'
    r'|GameConstants|QuestService|CompendiumExporter|unlock\.zone|geofence|world Z'
    r'|pre-existing|fully-stat|placeholder stub|\b(?:item|spell)\.[a-z_]+', re.I)

# Only the fields a reader actually reads. Ids, keys and coordinates are data, not prose.
PROSE = {'name', 'summary', 'biome', 'levelRange', 'intro', 'levels', 'title',
         'note', 'bullets', 'displayName', 'description', 'response', 'welcomeText'}

def walk(node, path, out):
    if isinstance(node, dict):
        for k, v in node.items():
            walk(v, path + [k], out)
    elif isinstance(node, list):
        for i, v in enumerate(node):
            walk(v, path + [str(i)], out)
    elif isinstance(node, str):
        field = next((p for p in reversed(path) if not p.isdigit()), '')
        if field in PROSE and BAD.search(node):
            out.append(('/'.join(path), node))

hits = []
for f in ('compendium/data/zones.json', 'compendium/data/meta.json'):
    walk(json.load(io.open(f, encoding='utf-8')), [f], hits)

lang = json.load(io.open('assets/i18n/lang.json', encoding='utf-8'))
hits += [(k, v) for k, v in lang.items() if isinstance(v, str) and BAD.search(v)]

print('\n'.join('%s :: %s' % h for h in hits) or 'clean')
PY

# app.js holds prose inside string literals, so scan it as text - but skip its own comments,
# which are allowed (and expected) to name the code they explain.
grep -nE "^\s*[^/ ].*('|\")[^'\"]*(T4C-[0-9]{4}|GameConstants|CompendiumExporter|geofence|world Z|Effect type|pre-existing)" compendium/app.js || echo "app.js clean"
```

Any hit is a defect. Fix it or justify it in the PR.

### 3b. Fact-check against the code (the one that catches real bugs)

Prose drifts from behavior silently. Check every claim a sentence makes against the field that
actually drives it. The traps that have already bitten:

- **Required items are not rewards.** `requiredItemKey` is *consumed on turn-in*
  (`QuestService.consumeRequiredItem`); `rewardItemKey` is what the player *receives*
  (`QuestService.complete`). A walkthrough saying "turn in for 1,500 gold and Mordrenn's
  Drowned Cowl" when the Cowl is the required item is wrong — the player brings it.
- **Rewards the site doesn't know about.** If a field exists on the definition but the exporter
  never emits it, the page can't show it and the prose will try to compensate. Fix the exporter.
- **Quoted numbers.** Every gold/XP/kill figure written into a sentence must match the
  definition today. Prefer not writing it at all and letting the page render it.
- **"Unlocks fast travel to X"** must match a non-null `unlockZoneId`.
- **Prerequisites** ("once you've finished Y") must match a real gate, not a suggestion.

```bash
# Cross-check every quest's prose against its own data
python - <<'PY'
import json, io
q = json.load(io.open('compendium/data/quests.json', encoding='utf-8'))
for x in q:
    w = (x.get('walkthroughText') or '')
    req = x.get('requiredItemKey')
    if req and req.split('.')[-1].replace('_', ' ') in w.lower() and 'bring' not in w.lower():
        print('CHECK required-item framing:', x['id'])
    if x.get('unlockZoneId') is None and 'unlock' in w.lower():
        print('CHECK phantom unlock:', x['id'])
PY
```

### 3c. Read it as a player (the pass that matters)

Open the site and actually read these routes, in this order, start to finish:

`#/` → `#/zones` → a mid-game zone → its map → one of its quests → a spell → an item → `#/systems`

At each page ask:

1. **Would a new player know what to do next?** If the page ends without pointing somewhere, fix it.
2. **Is there a word here I'd have to be a developer to understand?** Cut it.
3. **Does this read like a person wrote it?** Not a stat dump with a sentence glued on top.
4. **Does anything contradict another page?** Rewards, level ranges, prerequisites.
5. **Is the hardest thing on this page explained?** Damage formulas, mana costs, effect rows,
   probabilities — either explain them in plain words or hide them.

Use the browser tools; `verify-website` has the render recipe (desktop 1280px and phone 390px).
Read the phone width too — long prose paragraphs break differently there.

### 3d. Technical detail is not banned, it's *demoted*

Some readers do want the formula. Give it to them **behind a disclosure**, never inline:

```html
<details><summary>For the curious: how this is calculated</summary> … </details>
```

The default view stays plain. A `<details>` block may contain formulas and ranges — it still
may not contain class names, registry keys or task IDs.

---

## 4. Writing new copy

### Zone summary — the template

> *What the place is* (one clause, physical). *Who holds it and why they turned* (one sentence,
> a story). *What it demands of you* (level, damage type, a warning). *Where it leads* (the next
> zone or the quest that opens it).

Bad (what this site used to say):

> Capstone of the Drake ladder, Kraanhold's easternmost and highest peak. Kraanian Dragonguards
> (the one genuinely new monster in T4C-0008) hold the approach to Arch Drake — a fully-stat'd,
> canon-confirmed level-1000 legacy boss that sat completely unplaced (zero @Spawn points) until
> that pass gave it one.

Good:

> The highest peak in Kraanhold, and the end of the road for the drake-hunters who came before
> you. Arch Drake has held this lair since long before anyone thought to map it, and the
> Kraanian Dragonguard on the approach are there to make sure nobody reaches him. Come at the
> very top of your power, or don't come. Nothing lies past here.

### Quest walkthrough — the template

> *Who sends you and why they care.* *What you actually do.* *What you must bring, and that it's
> handed over.* *What you walk away with.* *What it opens up.* *One honest warning.*

Never open with "Talk to X to accept the quest" — the reader can see the giver in the header.

### Update notes (`meta.json`)

Same rule as `CHANGELOG.md`: patch notes for a player. Name the zone, monster, item or behavior
and what's different now. No class names, no fixes described as fixes to code.

Bad: *"Fixed a silent int overflow in the XP curve starting at level 541 by widening every
XP-carrying field to long."*
Good: *"Experience past level 541 no longer goes haywire — the curve now holds all the way to
the cap."*

---

## 5. Definition of done

- [ ] 3a leak scan is clean.
- [ ] 3b fact-check found nothing, or found something and it's fixed.
- [ ] Every new/changed page read start-to-finish at 1280px and 390px.
- [ ] Zones, maps and quests still read in progression order, and every new entry has a chapter.
- [ ] No sentence quotes a number the page could render itself.
- [ ] Regenerated the site data (`verify-website`, step 1) — `git status compendium/` shows only
      the diff this pass intends.
- [ ] Task ID claimed in `TASKS.md`, entry in `CHANGELOG.md` in player language, any new
      editorial rule recorded in `DESIGN_GUIDELINES.md`.
