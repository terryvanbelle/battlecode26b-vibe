#!/bin/bash
# x-line tip (owner #30/#31, 02:40 11-12): xh6 = xh4 + xh5 (WAR_NO_CAT: at war fighters keep out of cat reach) + xh6
# (WAR_NO_CAT_END 1200: fighters take cats again from r1200, for the cat share); full sets 3 and 4, all 8 opponents,
# both seats (32 matches, 96 cells). Judged on base rates against blocks #388 (xh1) / #389 (xh4) and the zj3/xf3 anchors.
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set3.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set4.json --queue research/panel/queue.tsv
