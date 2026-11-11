#!/bin/bash
# zj3 anchors on sets 1/2 (11-11): zjc's set-1/2 20x20 matches plus zjd's set-2 collegejunior and TheComplexMerlin
# (EscapeTheNight, fullofsecrets), both seats (14 matches). zjd's set-1 matches (final4, TSPAARK) are among zjc's.
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-" --panel research/panel/v4-set1.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set2.json --queue research/panel/queue.tsv
