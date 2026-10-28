#!/usr/bin/env python3
"""Generate a cells file for arena.py matrix.
cells.py A B [--maps all|LIST|@file] [--seeds 0,1] [--both]   (--both adds the B-vs-A cell for every map/seed)"""
import argparse, os, sys
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ap = argparse.ArgumentParser()
ap.add_argument('a'); ap.add_argument('b')
ap.add_argument('--maps', default='all'); ap.add_argument('--seeds', default='0'); ap.add_argument('--both', action='store_true')
o = ap.parse_args()
if o.maps == 'all':
    maps = sorted(f[:-6] for f in os.listdir(os.path.join(REPO, 'maps')) if f.endswith('.map26'))
elif o.maps.startswith('@'):
    maps = [l.strip() for l in open(o.maps[1:]) if l.strip()]
else:
    maps = o.maps.split(',')
for s in o.seeds.split(','):
    for m in maps:
        print(f'{o.a}\t{o.b}\t{m}\t{s}')
        if o.both:
            print(f'{o.b}\t{o.a}\t{m}\t{s}')
