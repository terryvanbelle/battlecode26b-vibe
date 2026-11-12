import json, glob, collections, os
seen=set(); C=collections.Counter(); ex=collections.defaultdict(list); tot=0
for p in glob.glob('/dev/shm/dcx/*/g*.summary.json'):
    try: s=json.load(open(p))
    except Exception: continue
    if 'vibe26b' not in (s.get('teamA'),s.get('teamB')): continue
    us='A' if s['teamA']=='vibe26b' else 'B'; them='B' if us=='A' else 'A'
    key=(s.get('file'),s.get('game'))
    if key in seen: continue
    seen.add(key)
    rid=os.path.basename(os.path.dirname(s.get('file','')))
    if not rid.isdigit() or int(rid)<26000: continue
    tot+=1
    if s['winner']==us: continue
    opp=s['team'+them].split('_',1)[-1]
    kd=s[us].get('kingDeaths',{}) or {}
    if s['winType']=='RATKING_DESTROYED':
        cause='king:'+('starve' if kd.get('starve') else 'bite' if kd.get('bite') else ','.join(kd) or '?')
    else:
        cause='points:'+('coop' if s.get('backstabRound',-1)<0 else 'war')
    C[cause]+=1; ex[cause].append(f"{rid} {s['map']} {us} {opp} r{s['rounds']}")
print('games', tot, 'losses', sum(C.values()))
for k,v in C.most_common():
    print(v, k); print('   ', '; '.join(sorted(ex[k])[-40:]))
