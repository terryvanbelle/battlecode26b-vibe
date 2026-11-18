# Audit 2026-11-18: navigation (Nav, Flow, BitFlow, Trail, ledger, loop) on bots/ratbot
N1 Flow.java:391,:443 (also :252,:296) finishRev reserve 40*(gu+3) uses path COST (dirt=4) not tiles: long dirt paths never finish, 60-turn timeout, failTgt 30 rounds. Fix: track depth. high/medium
N2 Flow.java:389,:442 reverse search ends only on popping a tile next to the rat; if the rat walked into the already-expanded region, never meets -> fail/timeout. Fix: if g[me] or neighbour reached this gen, finishRev from it. medium/medium
N3 Nav.java:318-330, Flow.java:701-709, BitFlow.java:300-310, Baby.java:2178-2181 digs skip DIG_RESERVE (only digToward checks): empty gatherers dig below upkeep floor in austerity. medium/med-low
N4 Flow.java:702, BitFlow.java:301 wait on dirt when raw+global<5: can never dig; path stays ready 200 rounds, rat stands (delivery has no stall limit). high/low
N5 BitFlow.java:68-74,145-148 failed field keeps tgt -> active() 200 rounds, blocks request/DIG_FIRST until BF_GIVEUP. Fix: tgt=null on fail. high/low
N6 BitFlow.java:141, Nav.java:126 finished field keeps building=true -> momentum() long after. medium/low
N7 Nav.java:349-350 flee2 ignores avoid mask (cat flee may step onto trap risk). medium/low
N8 RobotPlayer.java:33/54, Trail.java:34 Flow.work spends to 4000 reserve, Trail.alloc needs 2WH+1500 -> trail never allocates on big maps while searches build; stale searches keep running. == S4 (move alloc earlier). medium/low
minor: G.facing stale after no-move; forward finish truncates at 1023.
Judged correct: ledger can't overstate budget (G comment stale); gen*SPAN; 8-bucket Dial ring (DIG_COST 5 would alias); seedReach; BitFlow dirt.

## Verification (adversarial, 2026-11-18)
O1 CONFIRMED mech: newborn turn 1 pays Sym.init W*H (3600) + static inits/allocs (~3k) + born() before any gate; BC_BAIL 3500 at Baby:127 sized for steady turns. Overrun turns are paused and finish next round, so turn 2 is lost too (17.5k then ~2.8k). Fix: birth-turn bail NB_BAIL~6500 at Baby:127.
N1 CONFIRMED med/low: finishRev 36 bc/tile vs reserve 40*(gu+3) with dirt=4; late paths, 60-turn fails on dirt-heavy 60x60. Fix: depth in par high bits (or lower finish reserve ~1500).
N2 PLAUSIBLE (real scenario): rat walks into closed region; fix: finishRev from me when gg[me] reached.
N3 CONFIRMED low-med: digOk() helper (raw>=5 || global>=reserve+5) at all dig sites; MUST ship with N4.
N4 PLAUSIBLE low: refused dig -> hold counter then drop path.
N6 CONFIRMED narrower: momentum gate Nav:126 add BitFlow.tgt near t.
N8 CONFIRMED low: Trail.alloc before Flow.work (== S4); stop stale Flow work.
N5 CONFIRMED low: tgt=null on BitFlow fail.
N7 CONFIRMED low: flee2 two-pass avoid.
