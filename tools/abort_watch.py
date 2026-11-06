#!/usr/bin/env python3
"""Stop a local screen once its wins gate can no longer be met (runs on the VM, from the repo root).

  abort_watch.py GATES.tsv [--every 60] [--follow]

GATES.tsv: one line per run, `RUN CTRL_RUN CTRL_BOT CAND_BOT GATE` (whitespace separated), on the cells the run
shares with the control run (opponent, map, side). GATE is MIN_WINS (an integer: the candidate needs that many wins)
or t:T (TRAINING_ALGORITHM s6: reject at paired t <= T on per-cell win/loss, d = cand - ctrl in {-1, 0, +1}). While a
run is in progress (results.jsonl present, no DONE), once the gate cannot be met even if every remaining shared cell
goes its way (a win, so +1 where the control lost, 0 where it won), the arena process for that run (cmdline starting
`python3 -I tools/arena.py matrix runs/RUN/cells.tsv`) and its game children are killed, and runs/RUN/ABORTED
records why. The sequence script then touches DONE and goes on to the next run.
The gates file is re-read every pass, so a gate can be appended while it runs. Exits when every gated run has DONE,
unless --follow (then it keeps watching for new lines)."""
import json, math, os, signal, subprocess, sys, time


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


def paired_t(x):
    n = len(x)
    D = sum(1 for v in x if v)
    if n < 2 or D == 0:
        return 0.0
    m = sum(x) / n
    var = (D - n * m * m) / (n - 1)
    return m / math.sqrt(var / n) if var > 0 else (math.inf if m > 0 else -math.inf)


def best_t(shared, ctrl, got):
    """The paired t over every shared cell when each cell not yet played comes out a win."""
    return paired_t([(int(got[k]) if k in got else 1) - int(ctrl[k]) for k in shared])


def main():
    every = int(sys.argv[sys.argv.index('--every') + 1]) if '--every' in sys.argv else 60
    while True:
        gates = [l.split() for l in open(sys.argv[1]) if l.strip() and not l.startswith('#')]
        pending = 0
        for run, crun, cbot, bot, need in gates:
            d = 'runs/%s' % run
            if os.path.exists(d + '/DONE'):
                continue
            pending += 1
            if os.path.exists(d + '/ABORTED') or not os.path.exists(d + '/results.jsonl'):
                continue
            ctrl = load('runs/%s/results.jsonl' % crun, cbot)
            shared = cells(d + '/cells.tsv', bot) & set(ctrl)
            got = load(d + '/results.jsonl', bot)
            lost = sum(1 for k in shared if k in got and not got[k])
            won = sum(1 for k in shared if got.get(k))
            if need.startswith('t:'):
                bt = best_t(shared, ctrl, got)
                if bt <= float(need[2:]):
                    stop(run, 'gate unreachable: best final paired t %+.2f <= %s after %d of %d shared cells (%d won)'
                         % (bt, need[2:], sum(1 for k in shared if k in got), len(shared), won))
            elif lost > len(shared) - int(need):
                stop(run, 'gate unreachable: %d lost of %d shared cells after %d games (%d won), needs %s wins'
                     % (lost, len(shared), len(got), won, need))
        if not pending and '--follow' not in sys.argv:
            return
        time.sleep(every)


if __name__ == '__main__':
    main()
