#!/usr/bin/env python3
"""Pick giveaway winners at random and pair each with a Play promo code.

  scripts/giveaway-pick.py codes.csv --post-url https://www.reddit.com/r/x/comments/abc123/title/ -o winners.md
  scripts/giveaway-pick.py codes.csv --commenters commenters.txt -o winners.md

--post-url fetches every commenter through Reddit's API (app-only OAuth, stdlib only). Create a free "script" app at
https://www.reddit.com/prefs/apps, then export REDDIT_CLIENT_ID and REDDIT_CLIENT_SECRET (the id is under the app name).
--commenters reads a file instead: one username per line, with or without u/.
codes.csv: the Play Console export; the first column of each row is the code (a header row is skipped).
Writes a ready-to-paste DM per winner. Keep the output files out of git: they hold live codes.
"""
import argparse
import base64
import csv
import json
import os
import re
import secrets
import sys
import urllib.error
import urllib.parse
import urllib.request

REDEEM_URL = "https://play.google.com/redeem?code="
USER_AGENT = "script:facet-giveaway-pick:1.0"
DM = """Hi u/{user}, you won a Facet Launcher Pro code. Thanks for entering!

Code: {code}
Redeem: {url}{code}

Open the link on the phone signed in to the Google account you want Pro on, or use Play Store > profile > Payments & subscriptions > Redeem code. Then open the launcher and Pro unlocks. Expires {expiry}.

Any feedback is welcome but never required. Enjoy!"""


def clean(name):
    return name.strip().removeprefix("/").removeprefix("u/").strip()


def read_users(path):
    return [clean(line) for line in open(path, encoding="utf-8") if clean(line)]


def dedupe(names, exclude):
    seen, users = set(), []
    for name in names:
        key = name.lower()
        if name and key not in seen and key not in exclude and key != "[deleted]":
            seen.add(key)
            users.append(name)
    return users


def read_codes(path):
    codes = []
    for row in csv.reader(open(path, encoding="utf-8-sig")):
        cell = row[0].strip() if row else ""
        if cell and "code" not in cell.lower():
            codes.append(cell)
    return codes


def reddit_get(token):
    def get(path, params):
        url = f"https://oauth.reddit.com{path}?{urllib.parse.urlencode({**params, 'raw_json': 1})}"
        req = urllib.request.Request(url, headers={"Authorization": f"bearer {token}", "User-Agent": USER_AGENT})
        with urllib.request.urlopen(req, timeout=30) as r:
            return json.load(r)
    return get


def login():
    cid, secret = os.environ.get("REDDIT_CLIENT_ID"), os.environ.get("REDDIT_CLIENT_SECRET")
    if not cid or not secret:
        sys.exit("set REDDIT_CLIENT_ID and REDDIT_CLIENT_SECRET (see the header of this script)")
    auth = base64.b64encode(f"{cid}:{secret}".encode()).decode()
    req = urllib.request.Request(
        "https://www.reddit.com/api/v1/access_token",
        data=b"grant_type=client_credentials",
        headers={"Authorization": f"Basic {auth}", "User-Agent": USER_AGENT},
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            body = json.load(r)
    except urllib.error.HTTPError as e:
        sys.exit(f"Reddit login failed: HTTP {e.code} (check the client id and secret)")
    if "access_token" not in body:
        sys.exit(f"Reddit login failed: {body}")
    return body["access_token"]


def fetch_authors(post_url, get, top_level_only=False):
    """Every comment author on the post, expanding 'load more' stubs. get(path, params) returns parsed JSON."""
    m = re.search(r"/comments/([a-z0-9]+)", post_url)
    if not m:
        sys.exit("--post-url must be a Reddit post link containing /comments/<id>/")
    post_id = m.group(1)
    authors, queue = [], []

    def walk(things):
        for t in things:
            d = t.get("data", {})
            if t.get("kind") == "t1":
                if not top_level_only or d.get("parent_id", "").startswith("t3_"):
                    authors.append(d.get("author", ""))
                replies = d.get("replies")
                if replies:
                    walk(replies["data"]["children"])
            elif t.get("kind") == "more":
                queue.append(d)

    walk(get(f"/comments/{post_id}", {"limit": 500, "sort": "old"})[1]["data"]["children"])

    seen_ids = set()
    while queue:
        batch, rest = [], []
        for stub in queue:
            if stub.get("children"):
                batch.extend(c for c in stub["children"] if c not in seen_ids)
            elif stub.get("parent_id", "").startswith("t1_"):
                rest.append(stub)  # "continue this thread" stub: no ids, refetch under its parent
        queue.clear()
        for i in range(0, len(batch), 100):
            chunk = batch[i:i + 100]
            seen_ids.update(chunk)
            res = get("/api/morechildren", {"api_type": "json", "link_id": f"t3_{post_id}", "children": ",".join(chunk)})
            walk(res["json"]["data"]["things"])
        for stub in rest:
            sub = get(f"/comments/{post_id}", {"comment": stub["parent_id"][3:], "context": 0, "limit": 500})
            walk(sub[1]["data"]["children"])
    return authors


def main():
    p = argparse.ArgumentParser()
    p.add_argument("codes")
    src = p.add_mutually_exclusive_group(required=True)
    src.add_argument("--post-url", help="fetch commenters from this Reddit post")
    src.add_argument("--commenters", help="file with one username per line")
    p.add_argument("--top-level-only", action="store_true", help="with --post-url, count only replies to the post itself")
    p.add_argument("--save-commenters", metavar="FILE", help="with --post-url, also write the fetched list here for audit")
    p.add_argument("-o", "--out", default="winners.md")
    p.add_argument("--exclude", default="AutoModerator", help="comma-separated usernames to skip (add your own)")
    p.add_argument("--expiry", default="[date]", help="code expiry to print in the DM")
    a = p.parse_args()

    exclude = {clean(n).lower() for n in a.exclude.split(",") if n.strip()}
    if a.post_url:
        names = fetch_authors(a.post_url, reddit_get(login()), a.top_level_only)
    else:
        names = read_users(a.commenters)
    users, codes = dedupe(names, exclude), read_codes(a.codes)
    if a.post_url and a.save_commenters:
        with open(a.save_commenters, "w", encoding="utf-8") as f:
            f.write("\n".join(users) + "\n")
    if len(set(codes)) != len(codes):
        sys.exit("duplicate codes in the CSV")
    n = min(len(users), len(codes))
    if n < len(codes):
        print(f"only {len(users)} eligible entrants for {len(codes)} codes; {len(codes) - n} codes left unused", file=sys.stderr)

    winners = secrets.SystemRandom().sample(users, n)
    with open(a.out, "w", encoding="utf-8") as f:
        f.write(f"# {n} winners of {len(users)} entrants\n\n")
        for user, code in zip(winners, codes):
            f.write(f"## u/{user}\n\n```\n{DM.format(user=user, code=code, url=REDEEM_URL, expiry=a.expiry)}\n```\n\n")
    print(f"{n} winners of {len(users)} entrants -> {a.out}")
    print("Winners:", ", ".join(f"u/{u}" for u in winners))


if __name__ == "__main__":
    main()
