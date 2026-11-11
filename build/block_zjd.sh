#!/bin/bash
# zjd's registered replica block (02:56 11-11): every panel v4 match with DefaultMedium, EscapeTheNight, fullofsecrets
# or DefaultLarge, both seats (12 matches)
cd ~/projects/vibe/2026b
C=tools/galaxy26/contest.py
python3 -I $C scrim --vs "VarunVejalla_final4_run_away10@+,VarunVejalla_final4_run_away10@-,spsquared_TSPAARK_QUALS@+,spsquared_TSPAARK_QUALS@-" --panel research/panel/v4-set1.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "thewaxmango_collegejunior@+,thewaxmango_collegejunior@-,nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set2.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "thewaxmango_collegejunior@+,thewaxmango_collegejunior@-" --panel research/panel/v4-set3.json --queue research/panel/queue.tsv
python3 -I $C scrim --vs "nzjt_TheComplexMerlin@+,nzjt_TheComplexMerlin@-" --panel research/panel/v4-set4.json --queue research/panel/queue.tsv
