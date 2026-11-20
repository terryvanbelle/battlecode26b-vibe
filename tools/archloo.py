import json,numpy as np,itertools
def load(p,b):
    d={}
    for l in open(p):
        r=json.loads(l); me='A' if r['a']==b else 'B'; op=r['b'] if me=='A' else r['a']
        d[(op.split('/')[-1],r['map'],me)]=r['winner']==me
    return d
c=load('runs/zd730/results.jsonl','xk6')
B={"xi9":("zd736",-6.0,84),"xr8":("zd739",-6.8,132),"xr13":("zd744",-1.5,132),"xr16":("zd747",-0.8,132),"xk2":("zd752",2.8,36),"zjc":("zd753",0.0,36),"zj3":("zd754",-4.2,96),"zdb":("zd755",-6.2,96),"xf3":("zd756",-7.3,96),"xh1":("zd757",-9.4,96),"xc6":("zd758",-10.4,96),"xi1":("zd759",-12.5,96),"xg2":("zd761",-16.7,96),"xh4":("zd762",-20.8,96),"xa5":("zd760",-11.5,96)}
arch=sorted({k[0] for k in c})
X={};y=[];names=list(B)
maps=set(json.loads(l)['map'] for l in open('runs/zd752/results.jsonl'))  # the 25 reduced maps: compare all builds on the same cells
for b in names:
    x=load(f'runs/{B[b][0]}/results.jsonl',b)
    row={}
    for a in arch+['ALL']:
        ks=[k for k in x if k in c and k[1] in maps and (a=='ALL' or k[0]==a)]
        row[a]=100*sum(x[k]-c[k] for k in ks)/len(ks)
    X[b]=row; y.append(B[b][1])
y=np.array(y); w=np.array([B[b][2] for b in names])
print('replica SE per100 for a 50% game ~', ' '.join(f'{b}:{100*np.sqrt(2*0.25/B[b][2]):.0f}' for b in names[:1]), '(36 games: %.0f, 132: %.0f)'%(100*np.sqrt(.5/36),100*np.sqrt(.5/132)))
def loo(feats):
    errs=[]
    for i in range(len(names)):
        tr=[j for j in range(len(names)) if j!=i]
        A=np.array([[1]+[X[names[j]][f] for f in feats] for j in tr]); 
        coef,*_=np.linalg.lstsq(A,y[tr],rcond=None)
        pred=coef@np.array([1]+[X[names[i]][f] for f in feats]); errs.append(pred-y[i])
    return np.sqrt(np.mean(np.square(errs)))
print('baseline (predict mean) LOO rmse %.2f'%loo([]))
res=[]
for k in (1,2):
    for fs in itertools.combinations(arch+['ALL'],k): res.append((loo(list(fs)),fs))
for r,fs in sorted(res)[:10]: print('%.2f'%r, '+'.join(fs))
print('ALL alone %.2f'%loo(['ALL']))
