# The Mirror of Echoes (T4C-0042)

An unprompted "surprise" content pass: the owner asked for something new and meaningful to find
on their return, with no brief. The goal was a feature that is about the player's *own*
character rather than one more boss with a stat block.

## What shipped

- **Ysmera the Mirrorwarden** in the Colosseum (fast travel "Colosseum", 1736,1840 worldZ 0).
- **The Echo** (`monster/EchoOfSelf`): a boss that copies whoever summons it, live - paperdoll,
  name, level, combat skill, dodge, and (for casters) their strongest known attack spell - tinted
  pale blue. It speaks lines built from the player's real level, rebirths and gold.
- **Ten trials** of rising strength, first-clear XP and gold, penalty-free falls, and the
  **bound Echo companion** after trial ten.
- A one-time login whisper pointing every character to the Colosseum.

Numbers and rules: `DESIGN_GUIDELINES.md` section 9.

## Why it scales to the player instead of a level table

Characters in this project span level 1 to 400 with up to 50 rebirths, and test saves carry far
larger numbers (a 15,000-wisdom mage exists). Any fixed Echo stat block would be trivial for one
character and impossible for another. Instead, the Echo's blows are a share of the maker's max
HP, and its health is a multiple of the maker's strongest opening blows, so every character gets
a fight of about the same length and danger.

## Backlog ideas (not built)

- **Echo speech bubbles.** Lines currently go to the top-of-screen message and chat. Showing
  them over the Echo's head would need a speech-bubble renderer that works for monsters.
- **Echo of a past life.** Snapshot the character (look, level, spell) at each rebirth, and let
  the mirror show the *previous* life instead of the current one. That needs a real save-format
  field (a list of snapshots), not quest flags.
- **A mirror leaderboard** of fastest trial-ten wins, once the MMO server groundwork has a
  shared place to keep one.
