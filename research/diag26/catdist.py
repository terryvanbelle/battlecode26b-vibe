import collections, sys
R=collections.defaultdict(lambda: {"cat":[],"A":[],"B":[]})
for l in open(sys.argv[1]):
    f=l.rstrip("\n").split("\t")
    if f[0]=="round": continue
    r=int(f[0]); t=f[2]; ty=f[3]; x,y=int(f[4]),int(f[5])
    if r%50 or f[17]!="0": continue
    if ty=="CAT": R[r]["cat"].append((x,y))
    elif ty!="KING": R[r]["A" if t=="1" else "B"].append((x,y,int(f[8] or 0)))
lo=int(sys.argv[2]) if len(sys.argv)>2 else 0
for r in sorted(R):
    if r<lo: continue
    c=R[r]["cat"]
    def st(L):
        if not c or not L: return "-"
        ds=[min(max(abs(x-a),abs(y-b)) for a,b in c) for x,y,_ in L]
        return "n%d near<=3:%d med%d" % (len(L), sum(d<=3 for d in ds), sorted(ds)[len(ds)//2])
    print(r, "cats", len(c), "| A", st(R[r]["A"]), "| B", st(R[r]["B"]))
