#!/usr/bin/env bash
# Cold start of the Facet Launcher benchmark build: time to the first frame ("Displayed") and until Home has
# faded in ("Fully drawn", from reportFullyDrawn), for each facet count, from ActivityTaskManager's own log.
#
#   scripts/bench-startup.sh <adb-serial> [runs] [facet counts...]
#
# Run `./gradlew :app:assembleBenchmark` first. Emulator numbers: use a real device for anything you act on.
set -euo pipefail

serial=${1:?usage: $0 <adb-serial> [runs] [facet counts...]}
runs=${2:-10}
shift $(( $# > 1 ? 2 : 1 ))
counts=("${@:-1 3 6}")
read -r -a counts <<<"${counts[*]}"
pkg=com.facetlauncher.app
apk=app/build/outputs/apk/benchmark/app-benchmark.apk
adb_=(adb -s "$serial")

# ms from "+1s63ms" or "+414ms"
ms() { perl -ne 'if (/\+(\d+)s(\d+)ms/) { print $1 * 1000 + $2, "\n" } elsif (/\+(\d+)ms/) { print $1, "\n" }'; }
median() { sort -n | awk '{ v[NR]=$1 } END { if (NR) print (NR%2 ? v[(NR+1)/2] : (v[NR/2]+v[NR/2+1])/2) }'; }

for n in "${counts[@]}"; do
  "${adb_[@]}" uninstall $pkg >/dev/null 2>&1 || true
  "${adb_[@]}" install -r "$apk" >/dev/null
  "${adb_[@]}" shell am broadcast -n "$pkg/$pkg.benchmark.BenchmarkSeedReceiver" --ei facets "$n" >/dev/null
  sleep 3
  displayed=(); drawn=()
  for ((i = 0; i < runs + 1; i++)); do
    "${adb_[@]}" shell am force-stop $pkg
    sleep 1
    "${adb_[@]}" logcat -c
    "${adb_[@]}" shell am start -W -n "$pkg/.LauncherActivity" >/dev/null
    sleep 4
    log=$("${adb_[@]}" logcat -d -s ActivityTaskManager:I | tr -d '\r')
    d=$(grep "Displayed $pkg" <<<"$log" | tail -1 | ms)
    f=$(grep "Fully drawn $pkg" <<<"$log" | tail -1 | ms)
    (( i == 0 )) && continue   # the first start after install runs on uncompiled code
    displayed+=("$d"); drawn+=("$f")
  done
  printf '%2s facets  first frame median %4s ms (%s)   fully drawn median %4s ms (%s)\n' "$n" \
    "$(printf '%s\n' "${displayed[@]}" | median)" "$(printf '%s ' "${displayed[@]}")" \
    "$(printf '%s\n' "${drawn[@]}" | median)" "$(printf '%s ' "${drawn[@]}")"
done
