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
13. **The rush size is a two-sided optimum.** 12 rushers (spawn index 4-15): 8 (u0o) lost its replica block net -7,
    t -2.33, and 16 (u1o) lost net -3, t -1.13, both against the 12-rusher incumbent. Both had looked neutral or
    positive in the local screen (t0t +0.92; t0u 9 of 16).
14. **A cap's bank threshold has an optimum; cheese saved early stays banked.** The rich tier (cap 40 while the bank
    holds RICH_BANK): 500 lost on the replica, 1000 is the incumbent (u2g), 1500 (u3h, rh1) saved +139 by r150 but had
    16.2 babies at r300 to 18.3 and 854 banked at r500 to 591.
15. **Against the in-band field, enemy rat traps are the whole rat-death gap on big maps.** u2g's in-band games
    (2026-10-31), big-map losses r0-500: we lose 39.4 rats a game to their 32.0, and 7.9 of those die to their traps
    (we lay none). Bites, throws and the cat come out even. In small-map losses the gap is in the opening (r0-100: 21.3
    to 17.0) and in the bank: we field 5.6 more babies at r50 and run 532 cheese behind at r100, so by r300 they can
    still replace losses (15.9 babies to our 7.4).
16. **Diagnose losses a candidate can flip.** Opponents who beat us 90%+ dominate any loss tally and drew much of the
    diagnosis time (abcdezyxwv_bestsofar). Losses against opponents in the 25-75% band are the ones a one-switch
    candidate turns (u3c's block 2: +6 -1, every gain against an in-band opponent).
17. **A bank reserve must not gate the rats that are the income or the king's defence.** The starvation line
    (2026-11-12) cut starved kings from 38 of 97 lost dumped replica games to 2 of 20 losses on xj6's starvation
    panel. Its gates (austerity: core 4 while the projection is below 0; KING_RESERVE 600) then made death spirals.
    - Stash B vs YokoOno (26804): 4 babies on a 1300 bank from r140 to r500; the king starved at r782.
    - mincemeat A vs Icarus (26799): 4 babies for 900 rounds under the reserve, with income equal to the 2/round
      upkeep; starved at r1329.
    - The same gates cut combat rat traps on averyfineline vs Finals from xi9's 36/31 (won both) to xj6's 21/7. Our
      king was bitten to death at r110 and r122.
    A baby costs 20 cheese at 4-7 alive and repays itself; a reserve that blocks it starves the king slower but surely.
18. **Delivery over a wall row depends on the king's exact tile.** Transfer reach is dist2 9 to the nearest tile of
    the king's 3x3, across walls.
    - Stash B (xj7, 26816): every transfer came from three tiles, (18-20,8), to the king at (19,4).
    - Carriers holding 40-100 cheese waited one tile out of reach at (16-17,8-9) and (19-20,9).
    - One step up, to (19,5), would reach y=8 from x=16 to 22. xi9's king sat at (21,5) and won the cell.
    - Locally (agrab/araid/atrap3, same seed) the king parked against the wall and delivered 2400-8900. The local
      archetypes do not reproduce the problem.
19. **Identical end rounds mean the change never fired; early divergence re-rolls the whole cell.**
    - The replica plays each map with one seed. xj7 (no austerity core cut) replayed xj6's Meow A/B exactly (L
      r941, L r815).
    - On Stash B, xi9 and xj6 differ from r16 (different first digs), so the two games share nothing after that.
    - A single cell flipping after a change can be chaos rather than its mechanism. Check the mechanism in the dump.
20. **VM /dev/shm is volatile: it empties when our last process on the VM exits.** logind's default RemoveIPC=yes
    deleted all 12 GB of uid 1000's /dev/shm (dump caches, probe logs, scratch scripts, and the unknown /dev/shm/kh)
    at 17:49 UTC on 11-12, when the shutdown killed the last long-running job and stopped user@1000. Keep anything
    worth keeping under ~ (scripts in the repo, replays as ~/diag26b/rp/*.tgz).
