import json, glob, os, sys
want=set(sys.argv[1].split())
for p in sorted(glob.glob("/dev/shm/dcx/*/g*.summary.json")):
    try: s=json.load(open(p))
    except Exception: continue
    rid=os.path.basename(os.path.dirname(s.get("file","")))
    if rid not in want: continue
    us="A" if s["teamA"]=="vibe26b" else "B"; th="B" if us=="A" else "A"
    u=s[us]; t=s[th]
    print(rid, s["map"][:14], us, "W" if s["winner"]==us else "L", s["winType"][:8], s["rounds"], "bs", s["backstabRound"], "kd", u.get("kingDeaths"), "their", t.get("kingDeaths"), "sp", u["spawned"], t["spawned"], "xf", u["transferAmt"], t["transferAmt"], "kf", u["kingsFormed"], t["kingsFormed"], p.split('/')[3]+'/'+os.path.basename(p)[:2])
