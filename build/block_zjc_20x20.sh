#!/bin/bash
# zjc's registered replica block (02:17 11-11): every panel v4 match with a 20x20 map, both seats (22 matches)
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-" --panel research/panel/v4-set1.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,HugoIngelsson_Xzibit@+,HugoIngelsson_Xzibit@-" --panel research/panel/v4-set2.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-" --panel research/panel/v4-set3.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-,AdamTan12_Finals@+,AdamTan12_Finals@-,thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-" --panel research/panel/v4-set4.json --queue research/panel/queue.tsv
