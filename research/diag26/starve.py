import json, glob, os
seen=set(); rows=[]
for p in glob.glob('/dev/shm/dcx/*/g*.summary.json'):
    try: s=json.load(open(p))
    except Exception: continue
    if 'vibe26b' not in (s.get('teamA'),s.get('teamB')): continue
    key=(s.get('file'),s.get('game'))
    if key in seen: continue
    seen.add(key)
    rid=os.path.basename(os.path.dirname(s.get('file','')))
    if not rid.isdigit() or int(rid)<26000: continue
    us='A' if s['teamA']=='vibe26b' else 'B'; th='B' if us=='A' else 'A'
    if s['winner']==us or s['winType']!='RATKING_DESTROYED' or not (s[us].get('kingDeaths') or {}).get('starve'): continue
    rp=p.replace('.summary.json','.rounds.tsv')
    L=[l.rstrip('\n').split('\t') for l in open(rp)]; h=L[0]; L=L[1:]
    ix={k:i for i,k in enumerate(h)}
    def at(r,col):
        r=min(r,len(L)); return L[r-1][ix[col]]
    u=s[us]; t=s[th]
    def f(d): return "sp%3d rt%3d ct%2d dig%3d kf%d xf%5d" % (d['spawned'],d['ratTraps'],d['catTraps'],d['dirtDug'],d['kingsFormed'],d['transferAmt'])
    bb=lambda r: f"{at(r,'bank'+us)}/{at(r,'bank'+th)} b{at(r,'babies'+us)}/{at(r,'babies'+th)}"
    print(f"{rid} {s['map'][:12]:12} {us} {s['team'+th].split('_',1)[-1][:10]:10} r{s['rounds']:4} bs{s['backstabRound']:4} | us {f(u)} | them {f(t)} | r50 {bb(50)} r150 {bb(150)} r300 {bb(300)}")
