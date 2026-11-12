#!/bin/bash
# starvation panel (owner #32), round 4: xj8 = xj7 + LOW_UNDER_RESERVE (under 8 babies the spawn floor is LOW_FLOOR,
# not KING_RESERVE; mincemeat A vs Icarus 26799 held 4 babies r400-1300 and starved); research/panel/starve1.json: the 18 map/opponent pairs behind the 32 starved-king
# losses among 85 lost dumped replica games, 6 opponents, both seats (12 matches, 36 cells). Same seed per map as before,
# so each cell compares directly with its earlier results.
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "AdamTan12_Icarus@+,AdamTan12_Icarus@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,AdamTan12_Finals@+,AdamTan12_Finals@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-,spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-" --panel research/panel/starve1.json --queue research/panel/queue.tsv
