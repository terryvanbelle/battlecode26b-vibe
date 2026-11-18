# Audit 2026-11-18: king area (King, Econ, KPath, Comms king side) on bots/ratbot after dead-code removal
K1 KPath.java:137/103-106 held() treats our own turn-start slot position as another king after move(); KPath.tick runs after move, so the target mine within cheb 2 of our old spot is excluded when a search is due that turn. Fix: skip our own slot (k != King.slot). high/medium
K2 King.java:862 only slot 0 runs formation(); the role is never handed on when the slot-0 king dies (END_KING lone-king case never fires; FORM_T stays stale). Fix: lowest live slot runs it. high/medium
K3 Comms.java:91-97 stamps round&1023 never cleared: dead king / ALERT / FORM_T / EKING / CAT entries read fresh again 1024 rounds later (phantom king doubles reserves, babies deliver to dead spot, sites closed). Fix: zero slots older than ~8 rounds. high/low-med
K4 King.java:1047-1049 RING offset 3 makes the second king's body touch the first (comment promises a 1-tile lane); joiners sit on 3-5 of 16 spawn-ring tiles. Fix: offset 4 with clear3, or fix comment. high(mismatch)/low-med
K5 King.java:94-111 mirrorMines adds every twin, no own-half test (doc says "when it lies on our half"); fills 16 MINE_SLOTS with enemy-half mines, own-half mines found later dropped. Fix: homeHalf filter or more slots. medium/low-med
K6 King.java:52-55, Baby.java:2024 newborn never hears its SQ_START (acts in R+1, heardStart keeps getRound()==G.round); may adopt a later newborn's rrSlot; rrNext/roleFor parity mismatch. Fix: accept G.round-1. high/low
K7 King.java:323/895 babies=(cost-10)/10*4 "lower bound of live babies" is inflated by promotions (numBabyRats not decremented on promotion, RULES 6). Fix: subtract promotions. high/low
minor King.java:478 throwDodge move doesn't update G.me / pickUp; effect ~nil.
Judged correct: Comms index map & packing; Econ 2*kings upkeep and FIX pickup accounting; biteCheese/biteFixed; ringArcsFast/DODGE_MASK/catRisk; KPath bounds.

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
