# The Names That Remain

This is the new connective investigation for the existing world and the Hollow Dawn campaign.
It starts on Arakas, uses the established histories of Raven's Dust and Stoneheim as evidence,
and ends at the Witness Isles and Threnody Reach. The new characters, threat and interpretation
are original to this game. Existing classic quests retain their own outcomes.

## Premise

A sailor in Lighthaven remembers his daughter's laughter and the coat he mended for her, but
cannot recall her name. Kilhiam's recovery record has the same blank space. Lantalir finds a
similar omission in an oral verse, where a gardener's work remains but the gardener's name is
gone. The librarians distinguish these missing witnesses from the recorded histories of
Oberon/Makrsh P'Tangh, Gluriurl and the Harbinger. Rangor's incomplete ship manifests show
the loss is spreading in the present.

The Witness Isles held a mortal oath against Rhunor, a god who gains power when people forget
one another. The Dusk Regent, a former warder joined to a shard of Rhunor, commands three
lieutenants and their armies. Defeating Rhunor preserves the living witnesses' ability to
remember and testify. It does not undo earlier deaths, the Oracle's trials, Makrsh's defeat,
or the Harbinger's judgments.

## Conversation and objective route

| Chapter | Player action | Gate and handoff |
| --- | --- | --- |
| I. A Place Left Blank | Kilhiam in Lighthaven: `missing names` → `record`. Lantalir on Arakas: `remembrance` → `testimony`. | The grove testimony requires Kilhiam's record. `story` at either speaker repeats the next NPC, coordinates and keyword. |
| II. Silence and Judgment | Thomas at the Great Library: `missing names` → `record`. Jeremiah: `judgment` → `difference`. Return to Thomas: `compare`. | Both accounts are required for Thomas to compare the evidence. Their lines preserve established Makrsh, Gluriurl and Harbinger history. |
| III. The Stonecrest Passage | Rangor: `manifest` → `compare`; then `isles`, `scouts`, `report`, `chart`, `report` for the existing voyage. | The mainland comparison requires the Library's conclusion. The investigation does **not** gate existing Witness Isles passage. The Oracle's optional `tremor` → `witness` works before or after a Makrsh victory. |
| IV. The Failing Pact | Ophira: `wilds` / `accept` / `report`, then `veil` / `accept` / `report`. Maelin: `story`, then choose `moonwake` or `emberglass`. | The existing Wilds and Veil objectives remain required for new branch entries. Older accepted/completed saves remain authoritative. |
| V. Two Accounts | Moonwake: revenants → Ilyra `testimony` → `clue` → Pale Cantor → Maelin `report`. **Or** Emberglass: Ashguard → Soren `testimony` → `clue` → Cinder Marshal → Maelin `report`. | A witness clue is required for a new lieutenant offer; an already accepted lieutenant quest can continue. Completing either branch opens Threnody. |
| VI. The Silenced | Vael's `story`, `accept`, `route`, and `report` lead through Ashbound Exiles and the Hush Cantor. | Each accepted deed waits for its kill count, any required item and level floor before turn-in. |
| VII. The Regent's Bargain | Clear the Nullguard, defeat the Dusk Regent, and break the rift wraiths' hold. | The Regent's guaranteed Last Witness Seal is required at his turn-in. The rift stage opens the inner court. |
| VIII. The Name That Remains | Confront Rhunor, report to Vael, then ask `epilogue`. | The epilogue is available after the final quest. Travel and hunting unlocks remain after rebirth. |

The new mainland evidence is saved in `witness_story.*` flags. Each flag is granted once after
its prerequisite conversation. `story` (or `investigation`) is read-only at every mainland
speaker and always points to the next missing account. Maelin and Vael also have repeatable
`story`/`route` answers. No external walkthrough or quest item is required to recover a missed
conversation lead.

## Cast and antagonists

Kilhiam safeguards the first written account; Lantalir keeps the grove's spoken remembrance.
Thomas and Jeremiah test it against the old histories. Rangor compares a current manifest and
opens the voyage. The Oracle offers optional perspective without demanding rebirth. Ophira
identifies damage to the Isles' pact, and Maelin records one of two complementary witnesses:
Ilyra at Moonwake or Soren at Emberglass. Vael guides the shared Threnody chapters.

Moonwake Revenants and Emberglass Ashguard are the outer armies. The Pale Cantor and Cinder
Marshal command them. The Hush Cantor silences the Ashbound Exiles; Nullguard protect the Dusk
Regent. The Regent is the half-divine heir and Rhunor is the final god behind the erasures.
Both player branches converge before the Threnody chapters and lead to the same final encounter.

## Save compatibility

All established `avalon_*` quest IDs, branch status flags, boss drops, coordinates, travel
unlock flags, rewards and objective counts remain stable. The mainland investigation adds no
mandatory restriction to an existing island unlock. Returning veterans can hear the new
accounts without repeating completed deeds. Oracle and alignment flags are only read for
optional dialogue; the new story does not write them. Rebirth keeps the investigation and
earned Witness Isles/Threnody access.

The storyline currently uses existing maps and encounters. Further dungeon spaces, boss
mechanics and late-game side quests can extend these chapters without changing this route.
