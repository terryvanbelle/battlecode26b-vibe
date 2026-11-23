"""Per-game results of the 21-map study's replica matches (full.json): download each finished match's replay
(contest.py fetch), read it with Bc26Tool replay-info, check that we are team A and that our package is the champion
the match was requested for, and write full_maps.json (one row per game: match, bot, package, map, won, rounds).
  python3 -I research/champ-corr/fetch21.py   (from the repo root; re-run until every match is in)"""
import json, os, subprocess, sys
HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, os.path.join(REPO, 'tools/galaxy26'))
import client as cl

JAVA = os.path.expanduser('~/opt/jdk21/bin/java')
ENGINE = os.path.join(REPO, 'engine/battlecode26-java-1.2.6.jar')
TOOL = os.path.join(REPO, 'build/bc26tool')
REPLAYS = os.path.expanduser('~/projects/vibe/bc26b-galaxy-replays')


def tool():
    if not os.path.exists(os.path.join(TOOL, 'Bc26Tool.class')):
        os.makedirs(TOOL, exist_ok=True)
        subprocess.run([JAVA[:-4] + 'javac', '-cp', ENGINE, '-d', TOOL,
                        os.path.join(REPO, 'tools/galaxy26/java/Bc26Tool.java')], check=True)


def main():
    tool()
    full = json.load(open(os.path.join(HERE, 'full.json')))
    out_path = os.path.join(HERE, 'full_maps.json')
    rows = json.load(open(out_path)) if os.path.exists(out_path) else []
    have = {r['match'] for r in rows}
    c = cl.Client(None); acc = cl.load_accounts(); s = cl.session(c, acc, 'vibe26b')
    pending = 0
    for d in full:
        mid = d['match']
        if mid in have:
            continue
        m = s.api('GET', f'/api/compete/{c.episode}/match/{mid}/')
        if m['status'] != 'OK!':
            pending += 1
            continue
        rp = os.path.join(REPLAYS, str(mid), 'replay.bc26')
        if not os.path.exists(rp):
            subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools/galaxy26/contest.py'), 'fetch', str(mid)],
                           check=True, stdout=subprocess.DEVNULL)
        info = json.loads(subprocess.run([JAVA, '-cp', ENGINE + ':' + TOOL, 'Bc26Tool', 'replay-info', rp], check=True,
                                         capture_output=True, text=True).stdout.strip().splitlines()[-1])
        teams = {t['id']: t for t in info['teams']}
        assert teams[1]['name'] == 'vibe26b', (mid, teams)          # we are team A
        assert teams[1]['package'] == d['bot'], (mid, d['bot'], teams[1]['package'])
        games = info['matches']
        assert sorted(g['map'] for g in games) == sorted(d['maps']), (mid, [g['map'] for g in games], d['maps'])
        for g in games:
            rows.append({'match': mid, 'bot': d['bot'], 'package': teams[1]['package'], 'map': g['map'],
                         'won': g['winner'] == 1, 'rounds': g['total_rounds']})
        print(f"{mid} {d['bot']}: {sum(g['winner'] == 1 for g in games)}-{sum(g['winner'] != 1 for g in games)}")
    json.dump(rows, open(out_path, 'w'), indent=0)
    print(f'{len({r["match"] for r in rows})} of {len(full)} matches in full_maps.json; {pending} not finished')


if __name__ == '__main__':
    main()
