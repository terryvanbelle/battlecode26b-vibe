#!/usr/bin/env python3
"""Clean-room filter for the battlecode26b project.

Drops every markdown block (section with its subsections, list item with its children, paragraph, table, code fence)
that is tagged with the year 2026 (as a contest year, not a calendar date), names a 2026 team, or uses 2026 game
vocabulary (rats, cats, cheese, squeaks, backstabbing, ...). Conservative: any doubt -> drop.

Adapted from our own 2023-era filter (battlecode-vibe-no2023/filter_year.py)."""
import re, sys

TAG = re.compile(
    r"(?<![\d-])2026(?![-/]\d)"                     # 2026 as a contest year; calendar dates 2026-10-27 are kept
    r"|\bbc-?26|battlecode-?26|\b'26\b|\bBC ?26\b"
    r"|Uneasy Allianc|\brats?\b|rat ?kings?|baby ?rats?|ratnap|kidnap|cheese|squeak|backstab|pounce"
    r"|\bcats?\b(?!\s+[~/$\-\w.]*[/.~$])"           # the cat unit, not `cat ~/file`
    r"|\bfood\b|Stroke'?s|Lorem Ipsum|3Mice|MiceWalk|\bmice\b|TSPAARK|nfgehrs|chirality|\bdirt\b",
    re.I)

def indent(l): return len(l) - len(l.lstrip(' '))
ITEM = re.compile(r"^(\s*)([-*+]|\d+[.)])\s")
HEAD = re.compile(r'^(#+)\s')

def blocks(lines):
    """Yield (kind, level, start, end) blocks over line indices."""
    i, n = 0, len(lines)
    while i < n:
        l = lines[i]
        if not l.strip(): i += 1; continue
        if l.lstrip().startswith('```'):
            j = i + 1
            while j < n and not lines[j].lstrip().startswith('```'): j += 1
            yield ('code', indent(l), i, min(j + 1, n)); i = j + 1; continue
        m = HEAD.match(l)
        if m: yield ('head', len(m.group(1)), i, i + 1); i += 1; continue
        m = ITEM.match(l)
        if m:
            base = indent(l); j = i + 1
            while j < n:
                lj = lines[j]
                if not lj.strip():
                    k = j
                    while k < n and not lines[k].strip(): k += 1
                    if k < n and indent(lines[k]) > base and not HEAD.match(lines[k]): j = k; continue
                    break
                if HEAD.match(lj): break
                if ITEM.match(lj) and indent(lj) <= base: break
                if indent(lj) <= base and lj.lstrip().startswith('```'): break
                j += 1
            yield ('item', base, i, j); i = j; continue
        j = i + 1
        while j < n and lines[j].strip() and not HEAD.match(lines[j]) and not ITEM.match(lines[j]) \
                and not lines[j].lstrip().startswith('```'): j += 1
        yield ('para', indent(l), i, j); i = j

def filt(text):
    lines = text.split('\n'); keep = [True] * len(lines)
    drop_level = None; prev_dropped_colon = False
    for kind, lvl, s, e in blocks(lines):
        body = '\n'.join(lines[s:e]); hit = bool(TAG.search(body))
        if kind == 'head':
            if drop_level is not None and lvl <= drop_level: drop_level = None
            if hit: drop_level = lvl
        dropped = drop_level is not None or hit or (kind == 'code' and prev_dropped_colon)
        if dropped:
            for k in range(s, e): keep[k] = False
        prev_dropped_colon = dropped and body.rstrip().endswith(':')
    out = [l for l, k in zip(lines, keep) if k]
    return re.sub(r'\n{3,}', '\n\n', '\n'.join(out))

if __name__ == '__main__':
    src, dst = sys.argv[1], sys.argv[2]
    t = filt(open(src, errors='replace').read()); open(dst, 'w').write(t)
    print(f"{src}: {len(open(src, errors='replace').read().splitlines())} -> {len(t.splitlines())} lines; "
          f"residual hits: {len(TAG.findall(t))}")
