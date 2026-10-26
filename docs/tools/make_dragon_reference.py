#!/usr/bin/env python3
"""Turns src/main/resources/data/dragons.csv into a readable page, docs/dragons-reference.md.

Usage (from the project root): python3 docs/tools/make_dragon_reference.py
The game reads dragons.csv, never the generated page, so edit the CSV and re-run this script.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CSV = ROOT / "src/main/resources/data/dragons.csv"
OUT = ROOT / "docs/dragons-reference.md"
CLASS_ORDER = ["Strike", "Stoker", "Boulder", "Tracker", "Sharp", "Tidal", "Mystery"]
TIER_ORDER = {"S": 0, "A": 1, "B": 2, "C": 3}

rows = []
lines = CSV.read_text(encoding="utf-8").splitlines()
header = lines[0].split("|")
for line in lines[1:]:
    if line.strip():
        rows.append(dict(zip(header, line.split("|"))))


def fx(code):
    return "" if code in ("", "NONE") else f" `{code}`"


out = ["# Dragon Reference", "",
       "Generated from `src/main/resources/data/dragons.csv` by `docs/tools/make_dragon_reference.py`. "
       "Edit the CSV, not this page.", "",
       f"{len(rows)} dragons. Tiers: S (rarest), A, B, C (most common). "
       "Shots = how many Signature moves a dragon can fire before it must rest. "
       "Stats are the base values from the CSV (in battle, HP is multiplied by 3 and each dragon also gets random IVs and a Nature). "
       "The code in backticks is the move-effect spec read by the game.", "",
       "## Overview", "",
       "| Dragon | Class | Tier | HP | Atk | Def | Spd | Crit | Shots | Passive |",
       "|---|---|---|--:|--:|--:|--:|--:|--:|---|"]
rows.sort(key=lambda r: (CLASS_ORDER.index(r["class"]), TIER_ORDER[r["tier"]], r["name"]))
for r in rows:
    out.append(f"| {r['name']} | {r['class']} | {r['tier']} | {r['hp']} | {r['atk']} | {r['def']} | "
               f"{r['spd']} | {float(r['crit']):.0%} | {r['shots']} | {r['passive']} |")

for cls in CLASS_ORDER:
    out += ["", f"## {cls}"]
    for r in (x for x in rows if x["class"] == cls):
        elemental = "elemental" if r["ultElemental"] == "1" else "type-less"
        out += ["", f"### {r['name']}  (tier {r['tier']})", "",
                f"- **Stats:** HP {r['hp']} | Atk {r['atk']} | Def {r['def']} | Spd {r['spd']} | "
                f"Crit {float(r['crit']):.0%} | {r['shots']} shots",
                f"- **Basic attack:** {r['typeless']}",
                f"- **Signature:** {r['sig']} (power {r['sigPower']}){fx(r['sigFx'])}  ",
                f"  {r['sigDesc']}",
                f"- **Ultimate:** {r['ult']} (power {r['ultPower']}, {elemental}){fx(r['ultFx'])}  ",
                f"  {r['ultDesc']}",
                f"- **Passive:** {r['passive']}{fx(r['passiveSpec'])}  ",
                f"  {r['passiveDesc']}",
                f"- **Picture file:** `sprites/{r['id']}.png`"]

OUT.write_text("\n".join(out) + "\n", encoding="utf-8")
print("wrote", OUT, len(rows), "dragons")
