#!/bin/bash
# x-line tip (owner #30/#31, 06:45 11-12): xi9 = xi1 + xh9 (RICH_LATE) + xi2 (FORM_MAX 3, FLOOR_PER_KING, FORM3_BANK, FORM_CLOSE)
# + xi4 (CHEESE_LOS) + xi5 (WAR_NO_CAT_FIGHT) + xi8 (DELIVER_REACH over-wall delivery, SWAP_LOADED 10); full sets 3 and 4,
# all 8 opponents, both seats (32 matches, 96 cells). Judged on base rates against blocks #391 (xi1) / #390 (xh6).
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set3.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set4.json --queue research/panel/queue.tsv
