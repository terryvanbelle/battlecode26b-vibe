import json, glob, os, sys
want=set(sys.argv[1].split())
seen=set()
for p in sorted(glob.glob('/dev/shm/dcx/*/g*.summary.json')):
    try: s=json.load(open(p))
    except Exception: continue
    rid=os.path.basename(os.path.dirname(s.get('file','')))
    if rid not in want: continue
    key=(rid,s.get('game'))
    if key in seen: continue
    seen.add(key)
    us='A' if s['teamA']=='vibe26b' else 'B'
    L=[l.rstrip('\n').split('\t') for l in open(p.replace('.summary.json','.rounds.tsv'))]; h=L[0]; L=L[1:]
    ix={k:i for i,k in enumerate(h)}
    n4=0; first=None
    for row in L:
        r=int(row[0]); b=int(row[ix['babies'+us]]); k=int(row[ix['bank'+us]])
        if r>=60 and b<=4 and k>=700:
            n4+=1
            if first is None: first=r
    res='W' if s['winner']==us else 'L'
    print(f"{rid} {s['map'][:14]:14} {us} {res} r{s['rounds']:4} low-babies-rich-bank rounds {n4:4} first {first}")
