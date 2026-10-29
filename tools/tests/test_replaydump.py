#!/usr/bin/env python3
"""ReplayDump (tools/java/bc26tools/ReplayDump.java): builds it with tools/build-tools.sh, dumps
test/fixtures/throws.bc26 and king.bc26 (a king formation, a trap backstab) and checks invariants of the output. Fails, never skips, if the class does not build.
Usage: test_replaydump.py [OUTDIR...]   with arguments, only checks existing tsv dumps (no build)."""
import collections, csv, json, os, subprocess, sys, tempfile
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
JDK = os.environ.get('BC_JDK', os.path.expanduser('~/opt/jdk21'))
ENGINE = os.path.join(REPO, 'engine', 'battlecode26-java-1.2.6.jar')
CP = os.path.join(REPO, 'build', 'tools') + ':' + ENGINE
FX = os.path.join(REPO, 'test', 'fixtures', 'throws.bc26')
KING = os.path.join(REPO, 'test', 'fixtures', 'king.bc26')   # build/diag/cf6: B forms a king, B's trap backstab
REPLICA = os.path.expanduser('~/projects/vibe/bc26b-galaxy-replays/425/replay.bc26')   # outside the repo: optional
fails = checks = 0

HEAD = {
    'turns': 'round id team type x y dir hp cheese mcd tcd acd bc coop ind seq src dead carrier',
    'actions': 'round id team type action a b c seq src d e f g',
    'rounds': 'round cheeseXferA cheeseXferB catDmgA catDmgB kingsA kingsB babiesA babiesB ratTrapsA ratTrapsB '
              'catTrapsA catTrapsB dirtA dirtB died numKingsA numKingsB bankA bankB engBabiesA engBabiesB '
              'rawCheeseA rawCheeseB mapCheese mined dropped pickedA pickedB',
}


def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)


def rows(path):
    with open(path) as f:
        return list(csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE))


def java(*args):
    return subprocess.run([os.path.join(JDK, 'bin', 'java'), '-cp', CP, 'bc26tools.ReplayDump', *args],
                          capture_output=True, text=True, timeout=300)


def game(d, g, tag):
    """Invariants of one dumped game (OUTDIR d, game g); returns (summary, turns, actions, rounds)."""
    p = os.path.join(d, 'g%d.' % g)
    for k, h in HEAD.items():
        with open(p + k + '.tsv') as f:
            check(f.readline().rstrip('\n').split('\t') == h.split(), f'{tag} {k}.tsv header')
    T, A, R = rows(p + 'turns.tsv'), rows(p + 'actions.tsv'), rows(p + 'rounds.tsv')
    with open(p + 'summary.json') as f:
        s = json.load(f)
    with open(p + 'meta.json') as f:
        meta = json.load(f)
    team = {}
    for b in meta['bodies']:
        team[str(b['id'])] = str(b['team'])
    for a in A:
        if a['action'] == 'spawn':
            team[a['a']] = a['team']
    # records: src values, one own record per robot-round, split/thrown/own triples around every throw
    check({r['src'] for r in T} <= {'own', 'split', 'thrown', 'carried'}, f'{tag} src values')
    own = collections.Counter((r['round'], r['id']) for r in T if r['src'] == 'own')
    check(max(own.values()) == 1, f'{tag} one own record per robot-round {own.most_common(1)}')
    byseq = {(r['round'], int(r['seq'])): r for r in T}
    check(all(r['dead'] == ('1' if int(r['hp']) <= 0 else '0') for r in T), f'{tag} dead flag')
    bad = [r for r in T if r['src'] == 'carried' and (byseq[(r['round'], int(r['seq']) - 1)]['id'] != r['carrier']
                                                      or byseq[(r['round'], int(r['seq']) - 1)]['src'] != 'own')]
    check(not bad, f'{tag} carried record follows its carrier\'s own record {bad[:1]}')
    thr = [a for a in A if a['action'] == 'throw']
    trip = [a for a in thr if a['src'] == 'own' and byseq.get((a['round'], int(a['seq']) - 1), {}).get('src') == 'thrown'
            and byseq[(a['round'], int(a['seq']) - 1)]['id'] == a['a']
            and byseq.get((a['round'], int(a['seq']) - 2), {}).get('src') == 'split'
            and byseq[(a['round'], int(a['seq']) - 2)]['id'] == a['id']]
    check(len(trip) == len(thr) == sum(r['src'] == 'thrown' for r in T) == sum(r['src'] == 'split' for r in T),
          f'{tag} every throw is a split/thrown/own triple {len(trip)}/{len(thr)}')
    # every throw ends in a landing or the rat eaten in flight (from its thrown record on), before the next throw
    key = lambda a: (int(a['round']), int(a['seq']) if a['seq'] != '-1' else 1 << 30)
    end = collections.defaultdict(list)
    for a in A:
        if a['action'] == 'land' or a['action'] == 'die' and a['e'] == 'fed':
            end[a['a']].append(key(a))
    starts = collections.defaultdict(list)
    for a in thr:
        starts[a['a']].append((int(a['round']), int(a['seq']) - 1))
    open_ = []
    for x, st in starts.items():
        for i, k0 in enumerate(st):
            k1 = st[i + 1] if i + 1 < len(st) else (1 << 30, 0)
            if not any(k0 <= k < k1 for k in end[x]):
                open_.append((k0[0], x))
    check(len(open_) <= 1, f'{tag} throws without a landing {open_[:5]}')
    check(all(a['a'] == a['id'] for a in A if a['action'] in ('damage', 'land')), f'{tag} damage/land rows are the thrown rat\'s')
    check(all(a['d'] in ('ground', 'target', 'eaten') for a in A if a['action'] == 'damage'), f'{tag} damage outcome')
    # carry state: grabs are of another robot; a carried own record has a nap/swap by its carrier since its last release
    check(all(a['a'] != a['id'] for a in A if a['action'] in ('nap', 'swap', 'drop')), f'{tag} nap/swap/drop target another robot')
    carrier, bad = {}, []
    for a, r in sorted([((x['round'], int(x['seq'])), ('A', x)) for x in A] + [((x['round'], int(x['seq'])), ('T', x)) for x in T],
                       key=lambda e: (int(e[0][0]), e[0][1] if e[0][1] >= 0 else 1 << 30, e[1][0] == 'T')):
        k, x = r
        if k == 'A':
            if x['action'] in ('nap', 'swap'): carrier[x['a']] = x['id']
            elif x['action'] in ('drop', 'release', 'land', 'die', 'throw'): carrier.pop(x['a'], None)
        elif x['src'] == 'own' and x['carrier'] != carrier.get(x['id'], '0'):
            bad.append((x['round'], x['id'], x['carrier'], carrier.get(x['id'])))
    check(not bad, f'{tag} own records\' carrier = nap/release history {bad[:3]}')
    # deaths: once per robot, nothing written for it afterwards; counters match the rows
    die = [a for a in A if a['action'] == 'die']
    check(len({a['a'] for a in die}) == len(die), f'{tag} one die row per robot')
    when = {a['a']: (int(a['round']), int(a['seq']) if a['seq'] != '-1' else 1 << 30) for a in die}
    late = [r for r in T if r['id'] in when and (int(r['round']), int(r['seq'])) > when[r['id']]]
    check(not late, f'{tag} no record after a robot died {late[:1]}')
    check(all(a['d'] == team.get(a['a'], a['d']) for a in die), f'{tag} die victim team')
    check(all(a['e'] != 'other' for a in die), f'{tag} every death has a cause {[a for a in die if a["e"] == "other"][:1]}')
    for t, k in (('1', 'A'), ('2', 'B')):
        st = s[k]
        rd = collections.Counter(a['e'] for a in die if a['d'] == t and a['c'] == 'RAT')
        kd = collections.Counter(a['e'] for a in die if a['d'] == t and a['c'] == 'KING')
        check(st['deathsRat'] == sum(rd.values()) - rd['merge'] and st['ratsMerged'] == rd['merge']
              and st['deathsKing'] == sum(kd.values()), f'{tag} {k} death counters')
        check(st['ratDeaths'] == {c: n for c, n in rd.items() if c != 'merge'} and st['kingDeaths'] == dict(kd), f'{tag} {k} death causes')
        check(st['terminated'] == rd['terminated'] + kd['terminated'], f'{tag} {k} terminated')
        check(st['spawned'] == sum(1 for a in A if a['action'] == 'spawn' and a['team'] == t and a['b'] == 'RAT'), f'{tag} {k} spawned')
        o = 'B' if k == 'A' else 'A'
        check(st['killsRat'] + st['killsKing'] == sum(1 for a in die if a['g'] == t and a['d'] not in (t, '0')),
              f'{tag} {k} kills = deaths with this killer team')
        check(s[o]['deathsByEnemy'] == sum(1 for a in die if a['g'] == t and a['d'] == ('2' if t == '1' else '1') and a['c'] == 'RAT'),
              f'{tag} {o} deathsByEnemy')
        # bytecode stats from own records only, with the start-of-turn type's limit
        lim = {ty: v['bytecodeLimit'] for ty, v in meta['types'].items()}
        mine = [r for r in T if r['src'] == 'own' and r['team'] == t]
        rat = [int(r['bc']) for r in mine if r['type'] == 'RAT']
        check(st['turnsRat'] == len(rat) and st['maxBcRat'] == max(rat, default=0)
              and st['turnsKing'] == sum(r['type'] == 'KING' for r in mine), f'{tag} {k} turns/maxBc from own records')
        ov = sum(int(r['bc']) >= lim[r['type']] for r in mine if r['type'] in lim)
        n9 = sum(0.9 * lim[r['type']] <= int(r['bc']) < lim[r['type']] for r in mine if r['type'] in lim)
        check(st['overruns'] == ov and st['near90'] == n9, f'{tag} {k} overruns/near90 {st["overruns"]}/{ov} {st["near90"]}/{n9}')
        # grabs, traps, cheese
        naps = [a for a in A if a['action'] == 'nap' and a['team'] == t]
        check(st['naps'] == len(naps) == st['grabsEnemy'] + st['grabsAlly']
              and st['grabsEnemy'] == sum(a['d'] == 'enemy' for a in naps), f'{tag} {k} grabs')
        trg = [a for a in A if a['action'] == 'trigger' and a['d'] == t]
        check(st['trapTriggers'] == len(trg) and st['ratTrapHits'] == sum(a['g'] == 'rat' for a in trg)
              and st['catTrapHits'] == sum(a['g'] == 'cat' for a in trg), f'{tag} {k} trap hits by owner')
        check(st['dmgCat'] == st['catDmgTrap'] + st['catDmgBite'] == s['catDmg' + k], f'{tag} {k} cat damage split')
        check(st['transferAmt'] == sum(int(a['b']) for a in A if a['action'] == 'transfer' and a['team'] == t)
              and st['kingPickupAmt'] == sum(int(a['b']) for a in A if a['action'] == 'kingPickup' and a['team'] == t),
              f'{tag} {k} transfer/kingPickup amounts')
        check(st['pickupAmt'] == sum(int(r['picked' + k]) for r in R), f'{tag} {k} pickupAmt = sum of picked')
    check(all(a['d'] == str(3 - int(a['b'])) for a in A if a['action'] == 'trigger' and a['g'] == 'rat'), f'{tag} rat trap owner')
    check(all(a['b'] == '0' for a in A if a['action'] == 'trigger' and a['g'] == 'cat'), f'{tag} cat traps fire on cats')
    check(s['cheeseModelErr'] == 0, f'{tag} cheese model agrees with kings\' exact pickups and mine rows')
    # mine rows come in pairs (p, sym(p)) in a round's first record; a drop precedes its die row (or its feed row;
    # a terminated robot's has none)
    w, h, sy = meta['width'], meta['height'], meta['symmetry']
    sym = lambda x, y: (x if sy == 1 else w - 1 - x, y if sy == 2 else h - 1 - y)
    xy = lambda c: tuple(map(int, c.split(',')))
    bad, i = [], 0
    while i < len(A):
        a = A[i]
        if a['action'] == 'cheesespawn' and a['d'] == 'mine':
            b = A[i + 1] if i + 1 < len(A) else {}
            if a['seq'] != '0' or a['id'] != '0' or b.get('d') != 'mine' or xy(b['a']) != sym(*xy(a['a'])) or b['seq'] != '0':
                bad.append((a['round'], a['a']))
            i += 2; continue
        i += 1
    check(not bad, f'{tag} mine rows are (p, sym p) pairs in seq 0 {bad[:3]}')
    term = {int(a['round']) for a in die if a['b'] == 'terminated'}
    bad = [(a['round'], a['seq'], a['a']) for i, a in enumerate(A) if a['action'] == 'cheesespawn' and a['d'] == 'drop'
           and not any(i + k < len(A) and A[i + k]['action'] == 'die' and A[i + k]['seq'] == a['seq']
                       and all(x['action'] == 'feed' for x in A[i + 1:i + k]) for k in (1, 2))
           and not {int(a['round']), int(a['round']) - 1} & term]
    check(not bad, f'{tag} drop rows precede a die row or follow a termination {bad[:3]}')
    # a trap that kills a thrown rat after its impact (hitTarget writes the DamageAction last) is the cause
    bad = [(a['round'], a['seq'], a['a']) for i, a in enumerate(A) if a['action'] == 'die' and a['e'] in ('throwLand', 'fed')
           and any(x['action'] == 'trigger' and x['e'] == a['a'] for x in A[max(0, i - 4):i] if (x['round'], x['seq']) == (a['round'], a['seq']))]
    check(not bad, f'{tag} trap kills after an impact are trap deaths {bad[:3]}')
    check(all(int(r['mapCheese']) >= 0 for r in R), f'{tag} tile cheese never negative')
    # babies: rebuilt count = the engine's count minus the kings formed so far (becomeRatKing never decrements it)
    formed = {'1': 0, '2': 0}
    up = collections.Counter((a['round'], a['team']) for a in A if a['action'] == 'upgradeKing')
    bad = []
    for r in R:
        for t, k in (('1', 'A'), ('2', 'B')):
            formed[t] += up[(r['round'], t)]
            if int(r['babies' + k]) != int(r['engBabies' + k]) - formed[t]:
                bad.append((r['round'], k))
            if int(r['numKings' + k]) + 10 * int(r['bank' + k]) != int(r['kings' + k]):
                bad.append((r['round'], k, 'kings'))
    check(not bad, f'{tag} babies = engBabies - kings formed; kings = numKings + 10 bank {bad[:3]}')
    if not any(x['action'] == 'upgradeKing' for x in A):
        for t, k, o in (('1', 'A', 'B'), ('2', 'B', 'A')):
            lost, me, op = hploss(T, A, t), s[k], s[o]
            got = op['dmgRat'] + op['dmgKing'] + me['catDmgTaken'] + me['starveDmg'] + me['friendlyDmg']
            check(lost == got, f'{tag} {k} hp lost {lost} = enemy + cat + starve + friendly damage {got}')
    last = R[-1]
    check(s['rounds'] == int(last['round']) and all(s[k] == int(last[k]) for k in
          ('cheeseXferA', 'cheeseXferB', 'catDmgA', 'catDmgB', 'kingsA', 'kingsB', 'babiesA', 'babiesB', 'bankA', 'numKingsB')),
          f'{tag} summary = last rounds.tsv row')
    return s, T, A, R


def hploss(T, A, team):
    """hp lost by a team's rats and kings (100/600 each; killed = all of it, terminated = up to its last record).
    Robots are seeded from spawn rows: a rat can die before its first record."""
    hp, ty, tm = {}, {}, {}
    for a in A:
        if a['action'] == 'spawn':
            hp[a['a']] = 100 if a['b'] == 'RAT' else 600; ty[a['a']] = a['b']; tm[a['a']] = a['team']
    for r in T:
        hp[r['id']] = int(r['hp']); ty.setdefault(r['id'], r['type']); tm[r['id']] = r['team']
    dead = {a['a'] for a in A if a['action'] == 'die' and a['e'] != 'terminated'}
    return sum((600 if ty[i] == 'KING' else 100) - (0 if i in dead else hp[i]) for i in hp if tm[i] == team)


def fixture(d):
    s, T, A, R = game(d, 0, 'fixture')
    check((s['winner'], s['winType'], s['rounds'], s['backstabRound'], s['backstabBy'], s['backstabCause'])
          == ('B', 'RATKING_DESTROYED', 138, 16, 2, 'bite'), f'fixture match facts {s["winner"]} {s["winType"]} {s["rounds"]}')
    check((s['numKingsA'], s['numKingsB'], s['bankB'], s['maxRounds'], s['dumpVersion']) == (0, 1, 1372, 2000, 2), 'fixture kings/bank/meta')
    a, b = s['A'], s['B']
    check((a['naps'], b['naps'], a['forcedDrops'], b['forcedDrops']) == (11, 35, 3, 3), f'fixture grabs {a["naps"]} {b["naps"]}')
    check((a['ratTrapHits'], b['ratTrapHits'], a['throws'], b['throws'], b['feeds']) == (30, 24, 8, 32, 1), 'fixture traps/throws/feeds')
    check((a['deathsRat'], a['deathsKing'], b['deathsRat'], b['killsKing']) == (33, 1, 18, 1), 'fixture deaths')
    check(b['near90'] == 83 and a['turnsRat'] == 1978, f'fixture bc stats once per robot-round {b["near90"]} {a["turnsRat"]}')
    check(a['kingPickups'] == 0 and b['kingPickups'] == 1 and b['transfers'] == 36, 'fixture king pickups apart from transfers')
    check((a['dmgRat'], a['dmgKing'], b['dmgRat'], b['dmgKing'], a['catDmgTaken'], b['catDmgTaken']) == (1968, 0, 2940, 600, 360, 280),
          'fixture damage by attacker team and target type')
    hit = [x for x in A if x['action'] == 'throwHit']
    check(hit and all(x['f'] != x['e'] and x['e'] != x['a'] for x in hit), 'fixture throwHit rows name victim, thrown rat, thrower')
    check(any(x['action'] == 'drop' and x['c'] == 'killed' for x in A) and any(x['action'] == 'drop' and x['c'] == 'grabbed' for x in A),
          'fixture forced drops classified')
    check(sum(x['action'] == 'catSleep' for x in A) == s['catSleepTurns'] == 2, 'fixture cat sleep turns')
    mines = [x for x in A if x['action'] == 'cheesespawn' and x['d'] == 'mine']
    check(mines and all(x['id'] == '0' and x['f'] == '20' for x in mines), 'fixture mine rows')
    tot = sum(int(r['mined']) + int(r['dropped']) - int(r['pickedA']) - int(r['pickedB']) for r in R)
    check(tot == int(R[-1]['mapCheese']), f'fixture cheese conserved on tiles {tot} {R[-1]["mapCheese"]}')
    with open(os.path.join(d, 'g0.meta.json')) as f:
        meta = json.load(f)
    check(len(meta['catWaypoints']) == 2 and meta['specVersion'] == '1' and meta['packageB'] == 'Version41'
          and {'RAT', 'KING', 'CAT'} <= set(meta['types']) and all('chirality' in b for b in meta['bodies']), 'fixture meta.json')
    with open(os.path.join(d, 'game.json')) as f:
        check(json.load(f)['winner'] == 'B', 'fixture game.json')
    ind = rows(os.path.join(d, 'g0.indicators.tsv'))
    check(len(ind) > 1000 and {r['kind'] for r in ind} <= {'dot', 'line'}, 'fixture indicators.tsv')
    check(all('\t' not in r['ind'] for r in T), 'fixture indicator strings')
    # r32 s31: a thrown rat hits a robot (30), survives at 28 hp and dies on A's trap on the landing tile
    rec = [x for x in A if x['round'] == '32' and x['seq'] == '31']
    check([(x['action'], x['e'], x['f']) for x in rec if x['action'] in ('trigger', 'die')]
          == [('trigger', '11680', '28'), ('die', 'trap', '10973')], f'fixture trap kill after an impact {rec[:2]}')


def king(d):
    s, T, A, R = game(d, 0, 'king')
    a, b = s['A'], s['B']
    check((s['winner'], s['rounds'], s['backstabRound'], s['backstabBy'], s['backstabCause']) == ('B', 349, 15, 2, 'trap'),
          f'king fixture match facts, trap backstab by B {s["backstabBy"]} {s["backstabCause"]}')
    check(b['kingsFormed'] == 1 and b['ratsMerged'] == 6 and 'merge' not in b['ratDeaths'] and b['deathsRat'] == 47,
          f'king fixture: merged rats are not deaths {b["ratsMerged"]} {b["deathsRat"]}')
    check(s['numKingsB'] == 2 and s['engBabiesB'] - s['babiesB'] == 1, 'king fixture: 2 kings, babies drift by 1')
    up = [x for x in A if x['action'] == 'upgradeKing']
    check(len(up) == 1 and up[0]['d'] == '6' and up[0]['e'] == '0'
          and [r['type'] for r in T if r['src'] == 'own' and r['id'] == up[0]['id'] and r['round'] == up[0]['round']] == ['RAT'],
          'king fixture: upgradeKing row; turns.tsv type is the start-of-turn type')


def main():
    if len(sys.argv) > 1:
        for d in sys.argv[1:]:
            g = 0
            while os.path.exists(os.path.join(d, 'g%d.turns.tsv' % g)):
                game(d, g, '%s g%d' % (d, g)); g += 1
    else:
        b = subprocess.run([os.path.join(REPO, 'tools', 'build-tools.sh')], capture_output=True, text=True)
        check(b.returncode == 0 and b.stdout == '' and os.path.exists(os.path.join(REPO, 'build', 'tools', 'bc26tools', 'ReplayDump.class')),
              f'build-tools.sh builds silently: {b.stdout}{b.stderr}')
        b = subprocess.run([os.path.join(REPO, 'tools', 'build-tools.sh')], capture_output=True, text=True)
        check(b.returncode == 0 and b.stdout == b.stderr == '', 'build-tools.sh is idempotent and silent')
        if fails:
            print(f'replaydump: {checks - fails}/{checks} passed'); sys.exit(1)
        with tempfile.TemporaryDirectory() as d:
            r = java('tsv', FX)
            check(r.returncode == 2 and 'usage' in r.stderr, 'tsv without OUTDIR prints usage, exits 2')
            r = java('tsv', FX, d)
            check(r.returncode == 0 and len(r.stdout.splitlines()) == 1, f'tsv dump of the fixture {r.stderr[-300:]}')
            if r.returncode == 0:
                with open(os.path.join(d, 'g0.summary.json')) as f:
                    sj = f.read().strip()
                sm = java('summary', FX).stdout.strip()
                check(sj == r.stdout.strip() == sm, 'summary mode = tsv stdout = g0.summary.json')
                fixture(d)
            e = os.path.join(d, 'king')
            r = java('tsv', KING, e)
            check(r.returncode == 0 and len(r.stdout.splitlines()) == 1, f'king fixture dump {r.stderr[-300:]}')
            if r.returncode == 0:
                king(e)
            if not os.path.exists(REPLICA):
                print('replaydump: SKIP replica 425 checks (no %s)' % REPLICA)
            else:
                e = os.path.join(d, 'replica')
                r = java('tsv', REPLICA, e)
                check(r.returncode == 0 and len(r.stdout.splitlines()) == 3, 'replica 425 dump')
                if r.returncode == 0:
                    s = [game(e, g, 'replica425 g%d' % g)[0] for g in range(3)]
                    check(s[1]['A']['kingsFormed'] == 2 and s[1]['A']['ratsMerged'] > 0 and s[1]['engBabiesA'] - s[1]['babiesA'] == 2,
                          'replica 425 g1: formations, merged rats not deaths, babies drift')
                    check(s[2]['backstabBy'] == 1 and s[2]['backstabCause'] == 'trap', 'replica 425 g2: trap backstab by A')
                    A = rows(os.path.join(e, 'g2.actions.tsv'))
                    check([x['d'] for x in A if x['round'] == '118' and x['seq'] == '0' and x['action'] == 'cheesespawn']
                          == ['mine', 'mine', 'drop'], 'replica 425 g2 r118: a pounced rat\'s 20-cheese drop after the mines is a drop')
    print(f'replaydump: {checks - fails}/{checks} passed')
    sys.exit(1 if fails else 0)


if __name__ == '__main__':
    main()
