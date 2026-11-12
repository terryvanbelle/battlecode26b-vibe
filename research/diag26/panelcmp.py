# usage: panelcmp.py "IDS" -> each cell of IDS (opp, map, our seat): result now vs earlier replica results on that cell
import json, os, sys, collections
ids=[int(x) for x in sys.argv[1].split()]
def games(i):
    p=f'/dev/shm/rp/{i}/match.json'
    if not os.path.exists(p): return []
    m=json.load(open(p)); out=[]
    for g in m['games']:
        us='A' if g['team_a']=='vibe26b' else 'B'
        opp=(g['team_b'] if us=='A' else g['team_a']).split('_',1)[-1]
        out.append(((opp,g['map'],us),'W' if g['winner_team']=='vibe26b' else 'L', g['rounds'], g['reason'][:4]))
    return out
H=collections.defaultdict(str)
for d in sorted((int(x) for x in os.listdir('/dev/shm/rp') if x.isdigit())):
    if d<25900 or d in ids or d>=min(ids): continue
    for c,w,_,_ in games(d): H[c]+=w
W=L=F=B=0
for i in ids:
    for c,w,r,why in games(i):
        h=H.get(c,'')
        last=h[-3:]
        tag=''
        if w=='W' and last and last.count('L')>=2: tag='FLIP+'; F+=1
        if w=='L' and last and last.count('W')>=2: tag='FLIP-'; B+=1
        W+=w=='W'; L+=w=='L'
        print(i, *c, w, r, why, 'hist', h[-8:], tag)
print('W', W, 'L', L, 'flipped to W', F, 'flipped to L', B)
