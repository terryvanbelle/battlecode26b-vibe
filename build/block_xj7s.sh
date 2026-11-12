#!/bin/bash
# starvation panel (owner #32), round 3: xj7 = xj6 + AUSTERE_CORE 99 (no austerity core cut). xj6 held 4 babies on
# a 1300 bank on Stash B vs YokoOno (26804) and starved; research/panel/starve1.json: the 18 map/opponent pairs behind the 32 starved-king
# losses among 85 lost dumped replica games, 6 opponents, both seats (12 matches, 36 cells). Same seed per map as before,
# so each cell compares directly with its earlier results.
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "AdamTan12_Icarus@+,AdamTan12_Icarus@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,AdamTan12_Finals@+,AdamTan12_Finals@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-,spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-" --panel research/panel/starve1.json --queue research/panel/queue.tsv
