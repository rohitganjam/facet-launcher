#!/usr/bin/env python3
"""Generates RELEASE_NOTES.md by asking Claude Code to categorize commits since the last
recorded release.

Run automatically by scripts/release.sh after a successful build, using the commit recorded in
scripts/last-release.json as the start point. Can also be run manually for an arbitrary range:
  python3 scripts/gen-release-notes.py --since <commit> --to <commit> --version 0.1.7

Requires the `claude` CLI (Claude Code) to be installed and already logged in (OAuth/keychain —
this deliberately does NOT pass --bare, since that mode only accepts ANTHROPIC_API_KEY auth, not
a normal interactive login). --restricted --permission-prompts none keeps the call tool-free and
non-interactive: it only ever reads the commit log piped to it on stdin and prints Markdown back.

This step is best-effort: if `claude` isn't installed, isn't logged in, or the call otherwise
fails, this exits non-zero with a message on stderr and writes nothing — scripts/release.sh
treats that as "skip release notes this run," it doesn't fail the build over it.

The categorization is still a first draft — skim RELEASE_NOTES.md before sharing it externally
(e.g. a Play Store listing).
"""
from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "RELEASE_NOTES.md"

COMMIT_SEPARATOR = "----COMMIT-END----"

PROMPT_TEMPLATE = """You are writing release notes for an Android launcher app, from the git \
commit log below (each commit's hash, subject, and body, separated by "{sep}").

Categorize every user-meaningful change into exactly these four Markdown sections, in this \
order: "New Features", "Fixes", "Improvements", "Internal". Rules:
- A single commit can contain more than one distinct change (e.g. a fix bundled with an \
improvement, or a user-facing fix alongside the internal rework that enabled it) — split those \
into separate bullets in their own correct sections rather than forcing the whole commit into \
one bucket.
- "New Features" / "Fixes" / "Improvements" are user-facing: rewrite them in plain, concise \
language a non-technical user would understand — no class names, file paths, or internal \
jargon. "Internal" is for the dev team: tests, tooling, docs, refactors, dependency bumps, \
version-bump commits, and anything with no user-visible effect — keep those terse and \
technical, it's fine to stay close to the original commit wording there.
- If a section has nothing, write "- (none)" under it.
- Output ONLY the Markdown, starting with "# Facet Launcher {version}", then the four "## " \
sections. No preamble, no explanation, no code fences.

Commit log:
{log}
"""


def run_git(*args: str) -> str:
    return subprocess.run(
        ["git", *args], cwd=ROOT, capture_output=True, text=True, check=True,
    ).stdout


def build_notes(since: str, to: str, version: str) -> str:
    log = run_git(
        "log", "--no-merges", f"{since}..{to}",
        f"--pretty=format:%H%n%s%n%n%b%n{COMMIT_SEPARATOR}%n",
    ).strip()

    if not log:
        return f"# Facet Launcher {version}\n\n(No commits in range.)\n"

    prompt = PROMPT_TEMPLATE.format(sep=COMMIT_SEPARATOR, version=version, log=log)

    result = subprocess.run(
        ["claude", "-p", prompt, "--restricted", "--permission-prompts", "none"],
        capture_output=True, text=True, timeout=180,
    )
    if result.returncode != 0 or not result.stdout.strip():
        raise RuntimeError(
            f"claude -p failed (exit {result.returncode}): {result.stderr.strip() or '(no stderr)'}",
        )
    return result.stdout.strip() + "\n"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--since", required=True, help="commit the previous release was built from (exclusive)")
    parser.add_argument("--to", default="HEAD", help="commit this release is built from (inclusive, default HEAD)")
    parser.add_argument("--version", required=True)
    args = parser.parse_args()

    try:
        notes = build_notes(args.since, args.to, args.version)
    except (subprocess.TimeoutExpired, RuntimeError, FileNotFoundError) as e:
        print(f"error: couldn't generate release notes via claude -p: {e}", file=sys.stderr)
        sys.exit(1)

    OUT.write_text(notes)
    print(notes)


if __name__ == "__main__":
    main()
