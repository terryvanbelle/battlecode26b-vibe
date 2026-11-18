# Audit 2026-11-18: baby combat (Baby fight/carry/throw/cats, GrabMicro, CatLane) on bots/ratbot
C1 Baby.java:1113-1126 (+:753) cat flee runs before carry-to-cat branch: GATHER/early-FIGHT carriers oscillate at dist2~20 instead of feeding; tryThrow holds throws <150 up to 8 rounds. Fix: skip flee while carrying enemy and carryCat!=null, or reorder. high(order)/medium
C2 TrapRisk.java:22-30, Baby.java:252-258,:772 heardE/nHeard never written: carryThreat & guardTurn ignore heard enemies (no squeak type exists). high dead / medium intent
C3 Baby.java:1116 Nav.flee(cat.location) flees the 2x2 low corner, not the centre (comment says centre; flee2 unused). Up to 45 deg off; breaks mirror symmetry. Fix: flee2(2x+1,2y+1). high/low-med
C4 Baby.java:1016,1028,1045-1047 report() squeaks (SQ_CAT, SQ_MINE) without catInEarshot guard; ATTACK cats steer to oldest squeak (InternalRobot 1309-1316) -> beside our king. medium
C5 Baby.java:552,:725-751 FLIGHT_DMG assumes thrown rat acts before thrower; younger rat loses a tick: hits 24 less at 3-6, never reaches 7-8. Fix: ID-based table. medium/low
C6 Baby.java:629 aimCarry ignores Nav.avoid (gate RISK_FRESH=0): hold-aim step can enter cat lane -> crush. medium/low-med
C7 Baby.java:155-158, GrabMicro.java:351-366 pre-move actions ignore saving; failed grab loses bite fallback. medium/low
notes: EKING slot/SQ_EKING never sent or read; tryBiteCat/war turn without updating G.facing.
Judged correct: FLIGHT_DMG older case; cat geometry; GrabMicro cones/threat; FIX_SWAP_HOLD >11; finishCheese/kingTile.

## Verification (adversarial, 2026-11-18)
C1 CONFIRMED, impact medium-low (feeds still happen point-blank or diagonal; loss = certain feed -> 10-46 throw + ~8 carrier rounds). Fix at :1112 skip flee when carrying enemy and carryCat!=null; ship with C6.
C4 CONFIRMED mech (cat earshot dist2 16, GameWorld:1081; ATTACK cat turns to oldest squeak). Fix: report() returns if catInEarshot. Frequency unmeasured.
C6 CONFIRMED low-med. Fix: lane mask test in aimCarry (not full avoid).
C7 (a) CONFIRMED rare: guard :156-158 with !saving. (b) PLAUSIBLE: bite fallback after failed grab.
C5 CONFIRMED but corrected: younger thrown rat hits 8 less at tiles 3-6, lands 10 after 6; IDs shuffled so no ID table. Fix: conservative/average table.
C3 CONFIRMED low: flee2(2x+1,2y+1).
C2 CONFIRMED dead code, zero behaviour: delete (owner #36).
