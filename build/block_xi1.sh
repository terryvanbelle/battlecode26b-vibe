#!/bin/bash
# x-line tip (owner #30/#31, 05:10 11-12): xi1 = xh6 + END_CAT_COOP_FREE 1750 (a lone king's babies take cats in
# cooperation until r1750) + END_CAT_CENTRE (with no cat known they walk to the map centre); full sets 3 and 4, all 8
# opponents, both seats (32 matches, 96 cells). Judged on base rates against blocks #390 (xh6) / #389 (xh4).
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set3.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set4.json --queue research/panel/queue.tsv
