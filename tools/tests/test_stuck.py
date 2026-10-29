#!/usr/bin/env python3
"""stuck_report on synthetic tsv dirs: what makes an episode, what ends one, the king distance, untagged teams,
--span/--team/--game/--json, and the total line."""
import contextlib, io, json, os, sys, tempfile
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.join(REPO, 'tools'))
import smuggle as SG, stuck_report as ST  # noqa: E402
fails = checks = 0


def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)


def run(*args):
    out = io.StringIO()
    with contextlib.redirect_stdout(out):
        code = ST.main(list(args)) or 0
    return code, out.getvalue()


def bc(code, rn, king=False):
    return (40 + rn % 23) * 128 + SG.encode(code, False, king)


def game(d, g, team_a='us', team_b='them'):
    """Team 1 tagged: king 1 at (5,5); baby 5 delivers boxed at (7,5)/(7,6) rounds 1-40 then leaves; baby 6 waits
    ('W', not travel) 60 rounds; baby 7 walks to a mine one tile a round; baby 8 chases cheese boxed rounds 1-20
    only, at (2,2) beside baby 6 and ready to move (idle beside an ally 2-20); baby 9 heads to a mine boxed 1-25,
    wanders at 26, then 27-60 again (one episode, 33 rounds), cheese 7, on one tile: ready to move (idle alone) to 40,
    cooling down after. Team 2: a constant count (untagged) and boxed."""
    rows = []
    for rn in range(1, 61):
        rows.append((rn, 1, 1, 'KING', 5, 5, bc('s', rn, True), 0, 'own', 0, 0))
        rows.append((rn, 5, 1, 'RAT', 7 if rn <= 40 else 7 + rn - 40, 5 + rn % 2 if rn <= 40 else 9, bc('d', rn), 10, 'own', 0, 0))
        rows.append((rn, 6, 1, 'RAT', 1, 1, bc('W', rn), 0, 'own', 0, 0))
        rows.append((rn, 7, 1, 'RAT', rn % 60, 20, bc('M', rn), 0, 'own', 0, 0))
        if rn <= 20:
            rows.append((rn, 8, 1, 'RAT', 2, 2, bc('C', rn), 0, 'own', 0, 0))
        rows.append((rn, 9, 1, 'RAT', 20, 2, bc('W' if rn == 26 else 'M', rn), 7 if rn == 30 else 0, 'own', 0, 0 if rn <= 40 else 15))
        rows.append((rn, 9, 1, 'RAT', 20, 2, 5, 0, 'carried', 0, 0))       # a non-own row: ignored
        rows.append((rn, 20, 2, 'RAT', 30, 30, 1912, 0, 'own', 0, 0))
    with open(os.path.join(d, f'g{g}.turns.tsv'), 'w') as f:
        f.write('\t'.join(ST.COLS.split()) + '\n' + ''.join('\t'.join(map(str, r)) + '\n' for r in rows))
    json.dump({'map': f'M{g}', 'teamA': team_a, 'teamB': team_b, 'dumpVersion': 3},
              open(os.path.join(d, f'g{g}.meta.json'), 'w'))


with tempfile.TemporaryDirectory() as t:
    game(t, 0)
    meta, res = ST.analyse(t, 0)
    a, b = res
    eps = {e['id']: e for e in a['episodes']}
    check(a['tagged'] and not b['tagged'] and b['episodes'] == [] and b['turns'] == 0, f'tagged team only {res}')
    check(sorted(eps) == [5, 9], f'episodes: boxed travel >= span only {sorted(eps)}')
    e5, e9 = eps.get(5, {}), eps.get(9, {})
    check(e5.get('start') == 1 and e5.get('end') == 40 and e5.get('rounds') == 39 and e5.get('codes') == {'d': 40}
          and e5.get('cheese') == 10 and e5.get('king') == 2 and (e5.get('x'), e5.get('y')) == (7, 6),
          f'episode 5: leaving the box ends it; king touching {e5}')
    check(e9.get('start') == 27 and e9.get('end') == 60 and e9.get('codes') == {'M': 34} and e9.get('cheese') == 7
          and e9.get('king') == 15, f'episode 9: a non-travel code splits it {e9}')
    check([e['id'] for e in a['episodes']] == [5, 9] and a['lost'] == 39 + 33 and a['byCode'] == {'d': 40, 'M': 34}
          and a['turns'] == 60 * 4 + 20, f'order, lost, byCode, turns {a["lost"]} {a["byCode"]} {a["turns"]}')
    check(a['idle'] == {'ally': 19, 'alone': 38} and b['idle'] == {}, f'idle travel turns {a["idle"]} {b["idle"]}')
    _, res10 = ST.analyse(t, 0, span=10)
    check(sorted(e['id'] for e in res10[0]['episodes']) == [5, 8, 9, 9], f'--span 10 {res10[0]["episodes"]}')
    code, out = run(t)
    lines = out.splitlines()
    check(code == 0 and lines[0].startswith('game 0 (M0): A us vs B them') and '2 episodes, 72 rounds lost (27.7% of 260'
          in lines[1] and 'd 40 M 34' in lines[1] and lines[2].strip() == '#5 r1-40 (7,6) d:40 $10 king 2'
          and 'them: not tagged' in out and 'total' not in out and 'idle in travel 7.3% beside an ally, 14.6% alone'
          in lines[1], f'text {out!r}')
    code, out = run(t, '--top', '1', '--team', 'us')
    check(code == 0 and out.count('#') == 1 and 'them' not in out.split('\n', 1)[1], f'--top, --team {out!r}')
    game(t, 1)
    code, out = run(t)
    check(code == 0 and 'game 1 (M1)' in out and 'total us: 2 games, 4 episodes, 144 rounds lost (27.7% of 520 baby turns)'
          in out and 'idle in travel 7.3% beside an ally, 14.6% alone' in out.splitlines()[-1] and 'total them' not in out,
          f'total line over games, tagged teams only {out!r}')
    code, out = run(t, '--game', '1', '--json')
    j = [json.loads(l) for l in out.splitlines()]
    check(code == 0 and len(j) == 1 and j[0]['game'] == 1 and j[0]['teams'][0]['name'] == 'us'
          and j[0]['teams'][0]['lost'] == 72 and not j[0]['teams'][1]['tagged'], f'--game --json {out[:300]!r}')

print(f'stuck: {checks - fails}/{checks} passed')
sys.exit(1 if fails else 0)
