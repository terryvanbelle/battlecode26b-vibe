#!/usr/bin/env python3
"""replica_gcp.spool Queue.bump (spool_bump.py): picked ready messages move ahead of the rest in their own order, a
second bump with them already in front moves nothing, and a leased message is never touched."""
import os, sys, tempfile
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.join(REPO, 'tools', 'galaxy26'))
from replica_gcp import spool  # noqa: E402
fails = checks = 0


def check(ok, what):
    global fails, checks
    checks += 1
    if not ok:
        fails += 1; print('FAIL', what)


def order(q):
    out = []
    while True:
        item = q.lease()
        if item is None:
            return out
        out.append(item[1]['team'])


with tempfile.TemporaryDirectory() as t:
    q = spool.Queue(t).ensure()
    for i, team in enumerate(['f1', 'us', 'f2', 'f3', 'us2', 'f4']):
        q.put({'team': team, 'n': i}, msg_id=f'{1000 + i:020d}-1-{i}')
    mine = lambda o: o['team'].startswith('us')
    moved = q.bump(mine)
    check(len(moved) == 2 and [m[0][-1] for m in moved] == ['1', '4'], f'two moved, in order {moved}')
    check(q.bump(mine) == [], 'already in front: nothing moves')
    held = q.lease()
    check(held and held[1]['team'] == 'us', f'oldest ready is ours {held}')
    check(q.bump(mine) == [], 'the other one still leads: nothing moves')
    check(order(q) == ['us2', 'f1', 'f2', 'f3', 'f4'], 'remaining order')
    check(q.bump(mine) == [] and q.bump(lambda o: True) == [], 'empty queue')
    q.put({'team': 'f5'}, msg_id=f'{2000:020d}-1-9')
    q.put({'team': 'us3'}, msg_id=f'{2001:020d}-1-9')
    check(len(q.bump(mine)) == 1 and order(q) == ['us3', 'f5'], 'a late one of ours jumps the field')

print(f'spool: {checks - fails}/{checks} passed')
sys.exit(1 if fails else 0)
