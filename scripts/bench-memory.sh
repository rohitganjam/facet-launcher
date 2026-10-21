#!/usr/bin/env bash
# Memory of the Facet Launcher benchmark build in each state, for 1, 3 and 6 facets (or the counts given).
#
#   scripts/bench-memory.sh <adb-serial> [facet counts...]
#
# Builds nothing: run `./gradlew :app:assembleBenchmark` first. For each count it reinstalls the app, seeds that
# many facets (see BenchmarkSeedReceiver), and prints PSS in MB from `dumpsys meminfo` after Home settles and
# with each panel open. The stock launcher's total is printed once for scale. Emulator numbers: use a real device
# for anything you act on.
set -euo pipefail

serial=${1:?usage: $0 <adb-serial> [facet counts...]}
shift
counts=("${@:-1 3 6}")
read -r -a counts <<<"${counts[*]}"
pkg=com.facetlauncher.app
apk=app/build/outputs/apk/benchmark/app-benchmark.apk
adb_=(adb -s "$serial")

size=$("${adb_[@]}" shell wm size | tail -1 | sed 's/.*: //' | tr -d '\r')
w=${size%x*}
h=${size#*x}

swipe() { "${adb_[@]}" shell input swipe "$(awk "BEGIN{print int($w*$1)}")" "$(awk "BEGIN{print int($h*$2)}")" "$(awk "BEGIN{print int($w*$3)}")" "$(awk "BEGIN{print int($h*$4)}")" 300; }

# "Java Heap / Native Heap / Code / Graphics / Private Other / System / TOTAL PSS" in MB, from the App Summary.
summary() {
  "${adb_[@]}" shell dumpsys meminfo "$1" | tr -d '\r' | awk '
    /App Summary/ { on = 1 }
    on && /Java Heap:/    { j = $3 }
    on && /Native Heap:/  { n = $3 }
    on && /Code:/         { c = $2 }
    on && /Graphics:/     { g = $2 }
    on && /Private Other:/{ p = $3 }
    on && /System:/       { s = $2 }
    on && /TOTAL PSS:/    { t = $3 }
    END { printf "java %5.1f  native %5.1f  code %5.1f  graphics %5.1f  other %5.1f  system %5.1f  | TOTAL PSS %6.1f MB\n", j/1024, n/1024, c/1024, g/1024, p/1024, s/1024, t/1024 }'
}

sample() { # state label
  "${adb_[@]}" shell dumpsys meminfo $pkg >/dev/null
  sleep 1
  printf '  %-22s %s\n' "$1" "$(summary $pkg)"
}

echo "== stock launcher, for scale"
printf '  %-22s %s\n' "nexuslauncher" "$(summary com.google.android.apps.nexuslauncher)"

for n in "${counts[@]}"; do
  echo "== $n facets"
  "${adb_[@]}" uninstall $pkg >/dev/null 2>&1 || true
  "${adb_[@]}" install -r "$apk" >/dev/null
  "${adb_[@]}" shell am broadcast -n "$pkg/$pkg.benchmark.BenchmarkSeedReceiver" --ei facets "$n" >/dev/null
  sleep 3
  "${adb_[@]}" shell am force-stop $pkg
  "${adb_[@]}" shell am start -W -n "$pkg/.LauncherActivity" | grep -E 'TotalTime|WaitTime' | tr '\n' ' '
  echo
  sleep 6
  sample "home, settled"
  swipe 0.8 0.45 0.2 0.45; sleep 2.5
  sample "carousel open"
  "${adb_[@]}" shell input keyevent KEYCODE_BACK; sleep 2
  sample "carousel closed"
  swipe 0.5 0.8 0.5 0.2; sleep 2
  sample "drawer open"
  "${adb_[@]}" shell input keyevent KEYCODE_BACK; sleep 2
  swipe 0.2 0.45 0.8 0.45; sleep 2
  sample "hub open"
  "${adb_[@]}" shell input keyevent KEYCODE_BACK; sleep 2
  sample "home again"
done
