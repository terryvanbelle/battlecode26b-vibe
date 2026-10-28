# LEARNINGS (each lesson with its measurement, added the day it is confirmed)

1. **Kings act before the babies they spawn** (spawn-order exec). A king reading only `readSqueaks(round)` never hears
   its own rats: read round-1 too. (v0b: zero symmetry merges.)
2. **Baby rats act only on their 3 front tiles**: transfer, bite, dig and pickup need the target inside the 90° cone.
   A rat that stands next to the king without turning delivers nothing (v0b EscapeTheNight: 0 cheese delivered, king starved r295).
3. **Several maps box the king in dirt** (EscapeTheNight, closeup): digging is a basic, not an option. The king can
   dig its own spawn ring (range 8).
4. **Cat traps are the cheapest cat damage**: v0 DefaultSmall, 43 of 50 traps fired = 4300 of 6217 cat damage.
5. Engine property is `bc.game.map-path` (not bc.server.*); only the jar's built-in maps load otherwise.
