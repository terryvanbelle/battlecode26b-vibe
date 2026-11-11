#!/bin/bash
# zjf's registered replica block (05:09 11-11): set-3/4 matches with a sub-1600-tile map holding a home mine under
# front 20 (5t4rv4t10n_1337, externalsustenance, mincemeat, closeup, arrows, evileye), both seats (12 matches)
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "HugoIngelsson_YokoOno@+,HugoIngelsson_YokoOno@-,AdamTan12_Icarus@+,AdamTan12_Icarus@-" --panel research/panel/v4-set3.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "AdamTan12_Icarus@+,AdamTan12_Icarus@-,AdamTan12_Finals@+,AdamTan12_Finals@-,VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-" --panel research/panel/v4-set4.json --queue research/panel/queue.tsv
