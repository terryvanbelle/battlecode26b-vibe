ids=list(range(26811,26819))
name="xj7a"
R="/home/terryvanbelle/projects/vibe/bc26b-galaxy-replays"
L=["cd /home/terryvanbelle/projects/vibe/2026b","source tools/vm.sh","IP=10.138.0.3; ensure_vm >/dev/null 2>&1"]
for i in ids:
    L.append(f'if test -e {R}/{i}/replay.bc26 || python3 -I tools/galaxy26/contest.py fetch {i} > /dev/null 2>&1; then mkdir -p /dev/shm/rp/{i} && cp {R}/{i}/match.json /dev/shm/rp/{i}/match.json && echo "{i} fetched"; else echo "{i} MISSING"; fi')
files=" ".join(f"{i}/replay.bc26" for i in ids)
L.append(f'(cd {R} && tar czf - {files}) | gssh "test -e ~/diag26b/rp/{name}.tgz || cat > ~/diag26b/rp/{name}.tgz"')
L.append(f'n=$(gssh "test -s ~/diag26b/rp/{name}.tgz && tar tzf ~/diag26b/rp/{name}.tgz | grep -c replay.bc26")')
rms=" && ".join(f"rm {R}/{i}/replay.bc26" for i in ids)
L.append(f'if test "$n" = {len(ids)}; then {rms} && echo "{name}.tgz ok"; else echo "{name}.tgz BAD n=$n"; fi')
open(f"fetch_{name}.sh","w").write("\n".join(L)+"\n")
