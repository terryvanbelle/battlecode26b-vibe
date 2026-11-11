#!/bin/bash
# x-line line comparison (owner #30/#31, 11:11 11-11): xc3 (x-line tip: xb8 + xb9 WAVE_LATCH/WAVE_MIN + xb7 MINE_ANY/MINE_TIES
# + xc2 reactive rat traps + xc3 BF_GIVEUP 60) on the full sets 3 and 4, all 8 opponents, both seats (32 matches, 96
# cells). Judged against zj3's fresh same-window block #378 (25173-25180, 25289-25312) and xb8's #379.
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set3.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set4.json --queue research/panel/queue.tsv
