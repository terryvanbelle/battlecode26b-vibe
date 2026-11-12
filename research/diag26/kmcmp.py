import json,glob,collections,sys
C=collections.defaultdict(dict); bots=[]
for d in sys.argv[1:]:
    for f in sorted(glob.glob(d+"/*.log")):
        L=[l for l in open(f) if l.startswith("{")]
        if not L: continue
        j=json.loads(L[-1]); n=f.split("/")[-1][:-4]; p=n.split("_")
        opp,bot,seat=p[0],p[1],p[-1]; m="_".join(p[2:-1])
        if bot not in bots: bots.append(bot)
        s="A" if j["a"]==bot else "B"; o="B" if s=="A" else "A"
        C[(opp,m,seat)][bot]="%s r%4d fd %2d/%d trap %3d/%3d hit %3d/%3d kill %3d die %3d xf %5d ov %d" % ("W" if j["winner"]==s else "L", j["rounds"], j[s]["feeds"], j[o]["feeds"], j[s]["ratTraps"], j[o]["ratTraps"], j[s]["ratTrapHits"], j[o]["ratTrapHits"], j[s]["killsRat"], j[s]["deathsRat"], j["cheeseXfer"+s], j[s]["overruns"])
W=collections.Counter()
for k in sorted(C):
    print(k)
    for b in bots:
        v=C[k].get(b); print("   ",b,v)
        if v and v[0]=="W": W[b]+=1
print("wins",dict(W))
