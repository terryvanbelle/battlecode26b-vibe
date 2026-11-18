# Suspected issues noticed during the 2026-11-18 refactor (xk6); not yet verified; after the panel run
R1 King.pickHomeMine `held` compares kings' centres to G.me, which may have moved this turn (K1's pattern; KPath.held uses King.turnC).
R2 Baby.tryBiteCat (~899): rc.canTurn() then rc.turn(dir) without updating G.facing (check engine whether turn(dir) can throw).
R3 Nav.swapPast: SWAP_HOLD path requires SWAP_LOADED > 0; inert while SWAP_HOLD = 0. Nav pairBlocked always false (SWAP_PAIR = 0): dead.
R4 King.spawn aims at enemies[0] (sense order), not the nearest enemy.
R5 King.traps never incremented: indicator tr= always 0.
R6 Comms.stamp(): round%1024==0 -> 1023, so fresh() sees it 1 round old; harmless at current maxAges.
R7 GrabMicro.java:110: on the bytecode bail, the best move may come from a partial scan and execute() acts on it.
R8 Cleanups: G.fan/fan5 unused `from` param; merge Flow/BitFlow/KPath left() into G.left().
