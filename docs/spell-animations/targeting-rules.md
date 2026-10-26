# Spell targeting rules

Player area attacks select up to eight eligible enemies nearest the cast centre. Enemy-targeted area spells reserve one of these slots for the selected enemy. Self-centred spells and Seraph aura area procs use the same cap.

Player single-target attack spells deal full damage to the selected enemy, then 25% damage to up to seven adjacent enemies. Adjacency includes diagonals: at most one tile on either axis. Splash requires line of sight and respects safe zones and attackability. It applies damage only, without copying summons, healing/drains, dispels, control effects or timed hooks. Periodic damage ticks do not create further splash.

The cap includes the primary target. Selection happens before damage so kills and death-triggered spawns cannot expand the same cast. Support spells retain their existing behavior.

Emberqueen's Wrath remains spell ID 99909 and keeps its learned key. It is now an enemy-targeted comet strike using the single-target damage/mana/exhaustion curve, with adjacent residual damage. Worldroot Upheaval, Drowning Deep and Hallowed Nova remain area attacks and render one complete anchored effect per cast.
