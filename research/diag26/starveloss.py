# starveloss.py DUMPCACHE PKG: our starveDmg in z3x replica games, by result; lists the losses with starveDmg >= 200
import sys, os, json
root, pkg = sys.argv[1], sys.argv[2]; seen = set(); out = []; tot = {True: [0, 0], False: [0, 0]}
for d in sorted(os.listdir(root)):
    gj = os.path.join(root, d, 'game.json')
    if not os.path.exists(gj): continue
    g = json.load(open(gj))
    for k in range(g.get('games', 3)):
        mp, sp = os.path.join(root, d, f'g{k}.meta.json'), os.path.join(root, d, f'g{k}.summary.json')
        if not (os.path.exists(mp) and os.path.exists(sp)): continue
        m = json.load(open(mp))
        us = 'A' if m.get('packageA') == pkg and g.get('teamA') == 'vibe26b' else 'B' if m.get('packageB') == pkg and g.get('teamB') == 'vibe26b' else None
        if not us: continue
        s = json.load(open(sp)); key = (m['map'], us, s['rounds'], s['winner'])
        if key in seen: continue
        seen.add(key); th = 'B' if us == 'A' else 'A'; won = s['winner'] == us
        sd = s[us].get('starveDmg', 0); tot[won][0] += 1; tot[won][1] += sd > 0
        if sd >= 200 or (not won and sd > 0): out.append((won, m['map'], us, (g['teamB'] if us == 'A' else g['teamA']).split('_')[-1][:10], s['rounds'], s['winType'][:4], 'starve', sd, 'them', s[th].get('starveDmg', 0), 'spawned', s[us]['spawned'], s[th]['spawned'], 'xfer', s[us]['transferAmt'], s[th]['transferAmt']))
for o in sorted(out): print(o)
print('games, with any starveDmg: lost', tot[False], 'won', tot[True])
