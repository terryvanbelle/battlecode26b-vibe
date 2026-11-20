import json,sys,numpy as np
def load(p,b):
    d={}
    for l in open(p):
        r=json.loads(l); me='A' if r['a']==b else 'B'; op=r['b'] if me=='A' else r['a']
        d[(op.split('/')[-1],r['map'],me)]=r['winner']==me
    return d
c=load('runs/zd730/results.jsonl','xk6')
builds=json.loads(sys.argv[1])   # name -> [results path, replica per100 vs xk6]
arch=sorted({k[0] for k in c})
tab={}
for b,(p,rep) in builds.items():
    x=load(p,b); per={}
    for a in arch+['ALL']:
        ks=[k for k in x if k in c and (a=='ALL' or k[0]==a)]
        per[a]=100*sum(x[k]-c[k] for k in ks)/max(1,len(ks))
    tab[b]=(per,rep)
rep=np.array([tab[b][1] for b in builds])
print('replica per100 vs xk6: '+' '.join(f'{b} {r:+.1f}' for b,r in zip(builds,rep)))
for a in arch+['ALL']:
    v=np.array([tab[b][0][a] for b in builds])
    r=np.corrcoef(v,rep)[0,1] if v.std()>0 else float('nan')
    print(f'{a:7s} '+' '.join(f'{b} {tab[b][0][a]:+5.1f}' for b in builds)+f'   r {r:+.2f}')
