# LEARNINGS (each lesson with its measurement, added the day it is confirmed)

1. **Kings act before the babies they spawn** (spawn-order exec). A king reading only `readSqueaks(round)` never hears
   its own rats: read round-1 too. (v0b: zero symmetry merges.)
2. **Baby rats act only on their 3 front tiles**: transfer, bite, dig and pickup need the target inside the 90° cone.
   A rat that stands next to the king without turning delivers nothing (v0b EscapeTheNight: 0 cheese delivered, king starved r295).
3. **Several maps box the king in dirt** (EscapeTheNight, closeup): digging is a basic, not an option. The king can
   dig its own spawn ring (range 8).
4. **Cat traps are the cheapest cat damage**: v0 DefaultSmall, 43 of 50 traps fired = 4300 of 6217 cat damage.
5. Engine property is `bc.game.map-path` (not bc.server.*); only the jar's built-in maps load otherwise.
6. **A cat kills whichever king it reaches first**, and against a passive opponent that race decides the game.
   Gating cat traps on long-run surplus dropped the example battery from 134-14 to 98-50 (v0d -> v0e).
7. **Strong 2026 bots rush.** They backstab by round ~15-35 and kill the king with bites, rat traps and thrown rats
   within 20-300 rounds (g_s0e: 0-80 vs the top 5). Combat and king defense come before economy refinements.
8. **Rat traps fire on the 3x3 around them and stack.** An invisible 20-cheese trap denies 9 tiles; two overlapping
   traps deal 100. Field bots lay them on their front tiles as they advance, so advancing into them is lethal: 17 of 35
   of our deaths in one Icarus game involved enemy traps. TrapRisk (marking the three front tiles of every enemy seen)
   cut enemy trap hits on us from 41-46 to 19 in the same matchup.
9. **Map income is small next to the starting 2500.** Each mine yields about 2.3 cheese/round (DefaultSmall: 2 mines a
   side, about 4.5/round). Early on we did collect most of our half's cheese. The game is decided by the
   army battle around rounds 80-150; the winner then takes the whole map's income (Icarus transferred 3900+ after
   winning). Starving the king (spending everything on rats) and a hard reserve (no rats left to fight) both lose.
10. **Idle rats in contact.** Fortress fighters spent 359 of 420 engaged rat-rounds with every cooldown at 0
    (mv=h tag). Holding a ring does not fight; the enemy waits outside it.
11. **Check the replay tooling against the engine before trusting a count.** The engine emits RatNap both for a grab
    and for a thrown rat's own landing; counting both hid that the enemy grabs us almost three times as often as we
    grab them. The grabs come from behind: any movement code that always turns to face its step exposes the back.
12. **A peaceful self-play screen cannot see war-time behaviour.** Against the incumbent, neither side ever breaks
    cooperation: 0 backstabs and 0 rat traps in every 80-game `sp` screen since benchmark bots left the local arena. A
    switch that acts only at war (trap budget, bite priorities, grab defence, glancing back) plays identical games
    there (s4f vs s4e: 80 of 80 identical), so "neutral on screen" meant untested for s3m, s3r, s3s, KING_PRESS and
    s4f. War changes need an instrument that goes to war: the rush archetypes (`rx` runs against arush/abrk: 67 of 80
    games had a backstab) and the replica.
