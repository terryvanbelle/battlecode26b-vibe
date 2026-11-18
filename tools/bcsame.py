#!/usr/bin/env python3
"""Behaviour-exact check of a refactor: every method of NEW must compile to the same bytecode as the same method of
OLD (javap -c -p, package names and constant-pool indices normalised).

  bcsame.py OLD NEW          OLD, NEW: bot source dirs (bots/NAME) or compiled class dirs; exit 0 when exact

Methods, fields and classes only OLD has may be gone (dead code removed: javac would refuse NEW if a remaining method
still called one). A method only NEW has, or any method whose code differs, fails the check. Field constant values
are not compared (constants are inlined into the code, which is compared).
"""
import os
import re
import subprocess
import sys

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVAP = os.path.join(os.environ.get('BC_JDK', os.path.expanduser('~/opt/jdk21')), 'bin', 'javap')


def classdir(spec):
    if spec.startswith('bots/') or os.path.isfile(os.path.join(spec, 'RobotPlayer.java')):
        out = subprocess.run([sys.executable, '-I', os.path.join(REPO, 'tools', 'arena.py'), 'compile', spec],
                             cwd=REPO, capture_output=True, text=True, check=True).stdout.strip().splitlines()[-1]
        pkg = os.path.basename(os.path.normpath(spec))
        return os.path.join(out, pkg), pkg
    return spec, os.path.basename(os.path.normpath(spec))


def methods(cdir, pkg):
    """{(class, signature): normalised code} for every method and static initializer."""
    out = {}
    for f in sorted(os.listdir(cdir)):
        if not f.endswith('.class'):
            continue
        cls = f[:-6]
        text = subprocess.run([JAVAP, '-c', '-p', os.path.join(cdir, f)], capture_output=True, text=True,
                              check=True).stdout
        text = re.sub(r'\b%s\b' % re.escape(pkg), 'PKG', text)
        text = re.sub(r'#\d+', '#', text)
        text = re.sub(r'(?<=\S)[ \t]+', ' ', text)   # javap pads comments to the index width
        sig, body = None, []
        for line in text.splitlines():
            if line.startswith('  ') and not line.startswith('    ') and line.rstrip().endswith(';'):
                if sig is not None:
                    out[(cls, sig)] = '\n'.join(body)
                sig, body = line.strip(), []
            elif sig is not None:
                body.append(line)
        if sig is not None:
            out[(cls, sig)] = '\n'.join(body)
    # fields have no Code: keep methods only (a signature with parentheses) and the static initializer
    return {k: v for k, v in out.items() if '(' in k[1] or k[1].startswith('static {}')}


def main():
    old, new = sys.argv[1], sys.argv[2]
    a, b = methods(*classdir(old)), methods(*classdir(new))
    bad = 0
    for k in sorted(b):
        if k not in a:
            print('NEW ONLY', *k); bad += 1
        elif a[k] != b[k]:
            print('DIFFERS ', *k); bad += 1
    gone = sorted(set(a) - set(b))
    print(f'{len(b)} methods checked, {len(gone)} removed, {bad} not exact')
    sys.exit(1 if bad else 0)


if __name__ == '__main__':
    main()
