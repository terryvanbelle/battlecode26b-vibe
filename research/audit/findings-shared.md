# Audit 2026-11-18: shared state (Comms, Sym, TrapRisk, Debug, G, RobotPlayer) on bots/ratbot
S1 Comms.java:46-52 stamp round&1023 aliases after 1024 rounds: dead KING/CAT/ALERT slots read fresh again (phantom king -> numKings, Econ, formation closes; FIX_ALERT_MAX matches a 1024-old alert). Fix: zero stale slots (>~100 rounds). high mech / low-med freq. == K3 (independent).
S2 TrapRisk.java:36 + Baby.java:83 arrived=G.round-1 wrong when the previous turn skipped observe (budget gate): marks cleared that should stay -> walk into trap. Fix: arrived = lastObs. medium
S3 RobotPlayer.java:43-54 overruns inside indicator/tag/Trail.alloc not counted (OVR check before them). Fix: re-check before yield. high (telemetry only)
S4 RobotPlayer.java:49-54, Trail.java:34 tag pad/indicator cost before Trail.alloc gate -> debug content changes behaviour; alloc turn's tag decodes wrong. Fix: alloc before indicator/tag. medium/small
S5 Baby.java:1026-1029, Comms.java:98 full mine list (16) -> unreported() returns the same unlisted mine forever -> SQ_MINE every turn (cats steer to squeaks). Cause includes mirrorMines no half filter (== K5). Fix: skip when full; restore filter. medium
S6 TrapRisk.java:39/51 enemy kings' traps (Range 8 ring) never marked. medium/low-med (arguably design)
S7 RobotPlayer.java:33-53 promotion turn runs baby end-of-turn work (Flow.work, Trail.alloc 2WH, limit 20000). Fix: refresh G.king after turn. high/low
Judged correct: array layout/packing, squeak fields, Sym mirrors & merge, TrapRisk NEAR/clearing, squeak timing, Comms.write, Debug no-throw.

## Verification (adversarial, 2026-11-18), K and S together
K5+S5 CONFIRMED: 6/74 maps >16 mines (cheeseguardians 18, clockwisdom 20, externalsustenance 24, groveisbeingdesecrated 18, superioristhmus 20, TheHeist 18). Safe fix (a): unreported() -1 when full + guard report near loop. (b) half filter = behaviour change on all maps.
K2 CONFIRMED high-when-it-happens: after slot-0 king dies no FORM/END_KING all game. 9/148 xk2 games had a king die and run past r1100. Fix: lowest fresh slot leads.
K6 CONFIRMED (== E1): opening change, needs panel.
K1 CONFIRMED low-med: readKings again before KPath.tick, or skip own turn-start centre in held.
K3=S1 CONFIRMED low: phantom 3 rounds at D+1024; periodic zeroing of stale slots (~150 bc / 16 rounds).
K4 CONFIRMED mismatch only; offset 4 fails clearNow (r2 26>25). Fix comment.
S2 CONFIRMED rare: lastObs.
K7 CONFIRMED low: subtract numKings-1.
S4 CONFIRMED small: Trail.alloc above indicator (== N8 part).
S3 telemetry: re-check after indicator. S7 near-nil: refresh G.king. S6 by design, don't fix.
