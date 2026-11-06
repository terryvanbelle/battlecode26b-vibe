import csv, ast, statistics as st, sys
r = list({(a['match'], a['game']): a for a in csv.DictReader(open(sys.argv[1]))}.values())
def g(*spans): return [a for a in r if any(lo <= int(a['match']) <= hi for lo, hi in spans)]
sets = {'twin set1': g((12771, 12813)), 'u9p set1': g((16486, 16537)), 'u9q set1': g((16957, 16964), (17011, 17018)),
        'w0c set1': g((17145, 17176)), 'twin set3': g((13248, 13291)), 'u9p set3': g((16422, 16473)),
        'u9q set3': g((16881, 16942)), 'z0a set3': g((17040, 17082)), 'w0c set3': g((17090, 17110))}
for k, v in sets.items():
    if not v: continue
    f = lambda c: st.mean(float(a[c] or 0) for a in v)
    print('%-10s games %2d won %2d xUs %5.0f dEnemy %5.1f dCat %5.1f rounds %4.0f catUs %5.0f' % (
        k, len(v), sum(a['won'] == 'True' for a in v), f('xUs'), f('deathsEnemyUs'), f('deathsCatUs'), f('rounds'), f('catUs')))
