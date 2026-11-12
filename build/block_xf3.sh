#!/bin/bash
# x-line tip candidate (owner #30/#31, 18:25 11-11): xf3 = xe6 + xf1 (heardStart from nearest same-round START squeak,
# scout slots) + xf2 (GM_DELIVER deliver guard in GrabMicro) + xf3 (THREAT_THROW, GM_BAIL); full sets 3 and 4, all 8
# opponents, both seats (32 matches, 96 cells). Judged on base rates (anyrate/cellrate); replica games are
# deterministic per build, so zj3's single block #378 is a winner's-curse draw, not a fresh comparison.
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set3.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set4.json --queue research/panel/queue.tsv
