# Audit 2026-11-18: baby economy (Baby gathering/delivery/seek/form/cash, Econ) on bots/ratbot
E1 Baby.java:2017-2036 heardStart, 294-301 born, King.java:311-315 rrNext: newborn acts R+1 (exec-order snapshot, ObjectInfo.java:100), drops its R SQ_START (takes next newborn's slot or -1); role computed from R+1 cost/parity, so with FIGHT_EVERY=2 king's FIGHT/GATHER counts swap after GATHER_FIRST. == K6 but higher impact. Fix: accept G.round-1; role from spawn round/cost (squeak idx). high/medium
E2 Baby.java:2091-2095 wanderAround: ~36% of wander targets lie under our king's 3x3 (home mine within cheb 1 of king); redraw gated on MINE_KING_R>0 (=0). Gatherers stall ~7 turns on spawn ring. Fix: drop gate. medium/medium
E3 Baby.java:465-471,2289,2377,1036 full mine list -> unreported() never -1: scouts walk home whole window, seekers seekNews 150 rounds, SQ_MINE every turn crowds SQ_CAT. 6/74 maps truncate. == S5/K5. Fix: numMines>=MINE_SLOTS -> nothing unreported. medium/low-med
E4 Baby.java:1992-1996 cashMove steps onto centre without becomeRatKing (unlike FIX_FORM_PROMOTE); site may expire. high/low
E5 Baby.java:1222 held wander chain requires wanderRound==round-1; cooldown-skipped turns break it -> 2-tile flip. Fix: <=2. medium/low
E6 Baby.java:1018 report reads only round-1 squeaks and is skipped on cash/BC_BAIL turns -> gossip lost. medium/low
E7 Baby.java:1186-1190 standing on pile it can't take: Nav.goTo returns before stall++ -> stays forever. Fix: clear cheeseTarget. medium/low
Judged correct: transfer reach; Econ upkeep/pickup; squeak packing; formation checks vs engine; array bounds. FIX_WANDER_BOX dead switch.

## Verification (adversarial, 2026-11-18)
E1 CONFIRMED mech; impact narrower: king belief feeds only rrCount (scout fan). Actual opening: #1,#2 gather+scout, #3-#14 rushers (king thinks #4-#15), FIGHT/GATHER parity swapped after. This IS the tuned behaviour (Baby.java:323 comment). Real fix = opening change (needs panel); safe option = fix comments/docs. Squeak role in SQ_START if wanted.
E3 CONFIRMED on >16-mine maps (6/74): scouts walk home, seekers 150 rounds, SQ_CAT never sent near king. Fix: unreported() -1 when full + guard :1038. Ledger.
E2 PLAUSIBLE med-low: 36% draws under king, >=7 turns lost. Fix: drop MINE_KING_R gate (RNG re-roll -> ledger).
E4 CONFIRMED low: promote after cashMove step.
E6 CONFIRMED low: lastHeard catch-up.
E5 CONFIRMED low: round-wanderRound<=3.
E7 PLAUSIBLE low: attacker stuck on pile when no rush target; clear cheeseTarget.

Owner 11-18: K6/E1 is a FEATURE to consider after debugging, cleanup and the initial panel eval; not part of the bug-fix bundle (comments only now).
