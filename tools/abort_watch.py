#!/usr/bin/env python3
"""Stop a local screen once its wins gate can no longer be met (runs on the VM, from the repo root).

  abort_watch.py GATES.tsv [--every 60]

GATES.tsv: one line per run, `RUN CTRL_RUN CTRL_BOT CAND_BOT MIN_WINS` (whitespace separated). The gate counts the
candidate's wins on the cells it shares with the control run (opponent, map, side). While a run is in progress
(results.jsonl present, no DONE), once its losses on those cells exceed shared - MIN_WINS it cannot pass: the arena
process for that run (cmdline starting `python3 -I tools/arena.py matrix runs/RUN/cells.tsv`) and its game children
are killed, and runs/RUN/ABORTED records why. The sequence script then touches DONE and goes on to the next run.
Exits when every gated run has DONE."""
import json, os, signal, subprocess, sys, time


def load(path, me):
    out = {}
    if not os.path.exists(path):
        return out
    for line in open(path):
        try:
            r = json.loads(line)
        except ValueError:
            continue
        side = 'A' if r['a'] == me else 'B'
        opp = r['b'] if side == 'A' else r['a']
        out[(opp, r['map'], side)] = (r['winner'] == 'A') == (side == 'A')
    return out


def cells(path, me):
    keys = set()
    for line in open(path):
        f = line.split()
        if len(f) < 3:
            continue
        a, b = os.path.basename(f[0]), os.path.basename(f[1])
        keys.add((b, f[2], 'A') if a == me else (a, f[2], 'B'))
    return keys


def arena_pids(run):
    want = 'python3 -I tools/arena.py matrix runs/%s/cells.tsv' % run
    pids = []
    for p in os.listdir('/proc'):
        if not p.isdigit() or int(p) == os.getpid():
            continue
        try:
            cmd = open('/proc/%s/cmdline' % p, 'rb').read().replace(b'\0', b' ').decode(errors='replace')
        except OSError:
            continue
        if cmd.startswith(want):
            pids.append(int(p))
    return pids


def children(pid):
    out = subprocess.run(['ps', '-o', 'pid=', '--ppid', str(pid)], capture_output=True, text=True).stdout
    return [int(x) for x in out.split()]


def stop(run, why):
    for pid in arena_pids(run):
        kids = children(pid)
        os.kill(pid, signal.SIGTERM)
        for k in kids:
            for g in [k] + children(k):
                try:
                    os.kill(g, signal.SIGTERM)
                except OSError:
                    pass
    with open('runs/%s/ABORTED' % run, 'w') as f:
        f.write(why + '\n')
    print(time.strftime('%H:%M:%S'), run, why, flush=True)


def main():
    gates = [l.split() for l in open(sys.argv[1]) if l.strip() and not l.startswith('#')]
    every = int(sys.argv[sys.argv.index('--every') + 1]) if '--every' in sys.argv else 60
    while True:
        pending = 0
        for run, crun, cbot, bot, need in gates:
            d = 'runs/%s' % run
            if os.path.exists(d + '/DONE'):
                continue
            pending += 1
            if os.path.exists(d + '/ABORTED') or not os.path.exists(d + '/results.jsonl'):
                continue
            shared = cells(d + '/cells.tsv', bot) & set(load('runs/%s/results.jsonl' % crun, cbot))
            got = load(d + '/results.jsonl', bot)
            lost = sum(1 for k in shared if k in got and not got[k])
            won = sum(1 for k in shared if got.get(k))
            if lost > len(shared) - int(need):
                stop(run, 'gate unreachable: %d lost of %d shared cells after %d games (%d won), needs %s wins'
                     % (lost, len(shared), len(got), won, need))
        if not pending:
            return
        time.sleep(every)


if __name__ == '__main__':
    main()
