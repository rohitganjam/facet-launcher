#!/usr/bin/env python3
"""Drafts the in-app "What's new" entry for one release: app/src/main/assets/whats_new/<version>.md.

Run by scripts/release.sh right after the version bump and BEFORE the build, so the notes are bundled
into the APK/AAB, and so you can edit them (sign-off) before the build starts. This is separate from
scripts/gen-release-notes.py, which keeps writing the RELEASE_NOTES.md / release_notes/<version>.md
change log after the build; nothing here touches those.

  python3 scripts/gen-whats-new.py --since <commit> --to <commit> --version 0.1.26

Only changes that directly affect the user go in: a "## New" and/or "## Improved" section of "- " bullets
(a fix users could hit is worded as an improvement; invisible fixes and polish are left out). If claude can't be reached, a stub containing TODO is written
instead; scripts/release.sh refuses to build while a TODO is left in the file.

Requires the `claude` CLI, logged in (same requirements as gen-release-notes.py).
"""
from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = ROOT / "app" / "src" / "main" / "assets" / "whats_new"

COMMIT_SEPARATOR = "----COMMIT-END----"

PROMPT_TEMPLATE = """You are writing the "What's new" entry that an Android launcher app shows its \
users inside the app, from the git commit log below (each commit's hash, subject, and body, \
separated by "{sep}").

Write ONLY what directly affects the user: something they can now do, or something they will \
notice working or looking different in their everyday use. Rules:
- Two possible sections, in this order: "## New" (a feature the user can now do or see) and \
"## Improved" (anything existing that now works or reads better in a way the user will notice, \
including fixes of problems users could actually hit). Leave a section out entirely if it would be \
empty — never write "(none)".
- Plain, friendly, concise language a non-technical user understands. One sentence per bullet, \
no class names, file paths, settings keys or internal jargon, no commit hashes. Name features the \
way the app's screens do.
- Leave out anything that doesn't directly affect the user: tests, tooling, docs, refactors, \
dependency bumps, version bumps, generated files, fixes of bugs users couldn't have hit, and \
cosmetic micro-polish nobody would notice (a few pixels of spacing, internal consistency tweaks).
- Merge duplicates and drop things a later commit in the same log undid.
- If nothing at all is user-visible, output a single "## Improved" section with the bullet \
"- Small fixes and polish."
- Output ONLY the Markdown, starting with "# {version}", then the section(s). No preamble, no \
explanation, no code fences.

Commit log:
{log}
"""

STUB = """# {version}

## Improved
- TODO: describe what changed in this release for users (then delete this line)
"""


def run_git(*args: str) -> str:
    return subprocess.run(["git", *args], cwd=ROOT, capture_output=True, text=True, check=True).stdout


def draft_notes(since: str, to: str, version: str) -> str:
    log = run_git(
        "log", "--no-merges", f"{since}..{to}",
        f"--pretty=format:%H%n%s%n%n%b%n{COMMIT_SEPARATOR}%n",
    ).strip()
    if not log:
        raise RuntimeError("no commits in range")

    prompt = PROMPT_TEMPLATE.format(sep=COMMIT_SEPARATOR, version=version, log=log)
    result = subprocess.run(
        ["claude", "-p", prompt, "--restricted", "--permission-prompts", "none"],
        capture_output=True, text=True, timeout=180,
    )
    if result.returncode != 0 or not result.stdout.strip():
        raise RuntimeError(f"claude -p failed (exit {result.returncode}): {result.stderr.strip() or '(no stderr)'}")
    return result.stdout.strip() + "\n"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--since", default="", help="commit the previous release was built from (exclusive)")
    parser.add_argument("--to", default="HEAD", help="commit this release is built from (inclusive, default HEAD)")
    parser.add_argument("--version", required=True)
    args = parser.parse_args()

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    out = OUT_DIR / f"{args.version}.md"

    try:
        if not args.since:
            raise RuntimeError("no previous release commit to start from")
        notes = draft_notes(args.since, args.to, args.version)
        status = 0
    except (subprocess.TimeoutExpired, subprocess.CalledProcessError, RuntimeError, FileNotFoundError) as e:
        print(f"warning: couldn't draft the in-app notes ({e}); wrote a stub with a TODO to fill in.", file=sys.stderr)
        notes = STUB.format(version=args.version)
        status = 0

    out.write_text(notes)
    print(notes)
    sys.exit(status)


if __name__ == "__main__":
    main()
