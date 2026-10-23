#!/usr/bin/env python3
"""Generates the UML class diagrams of the project from the compiled classes.

Usage: python3 docs/uml/make_uml.py <classes-dir> <out-dir>
Needs `javap` (JDK) and Graphviz `dot`.  The diagrams are drawn from the real bytecode, so they
always match the code.
"""
import html
import re
import subprocess
import sys
from pathlib import Path

CLASSES = Path(sys.argv[1])
OUT = Path(sys.argv[2])
OUT.mkdir(parents=True, exist_ok=True)

DIAGRAMS = {
    "01-dragons": ("Dragons: species, owned dragons, moves", [
        "model.dragon.DragonClass", "model.dragon.Stat", "model.dragon.Tier", "model.dragon.Grade",
        "model.dragon.Nature", "model.dragon.DragonSpecies", "model.dragon.OwnedDragon",
        "model.skill.Move", "model.skill.MoveKind", "model.skill.MoveEffect",
        "model.skill.PassiveSkill", "model.GameRules"]),
    "02-effects-passives": ("Polymorphism 1: move effects and passive skills", [
        "model.skill.MoveEffect", "model.skill.PierceEffect", "model.skill.CritEffect",
        "model.skill.MultiHitEffect", "model.skill.SureHitEffect", "model.skill.DotEffect",
        "model.skill.StatDebuffEffect", "model.skill.SelfBuffEffect", "model.skill.HealSelfEffect",
        "model.skill.DodgeBuffEffect", "model.skill.EffectFactory",
        "model.skill.PassiveSkill", "model.skill.AbstractPassive", "model.skill.Passives",
        "model.skill.PassiveFactory"]),
    "03-items-players-bots": ("Inheritance and polymorphism 2: items, players and bots", [
        "model.item.Item", "model.item.HealItem", "model.item.ReloadItem", "model.item.BuffItem",
        "model.item.CleanseItem", "model.item.ItemType",
        "model.player.Player", "model.player.BotPlayer", "model.player.Difficulty",
        "bot.BotStrategy", "bot.RandomBotStrategy", "bot.GreedyBotStrategy", "bot.SmartBotStrategy",
        "bot.BotFactory", "bot.BotSupport"]),
    "04-battle-engine": ("Battle engine (no GUI code)", [
        "battle.BattleSession", "battle.BattleEngine", "battle.Combatant", "battle.BattleAction",
        "battle.BattleEvent", "battle.EventType", "battle.BattleLog", "battle.BattleClock",
        "battle.TimeSource", "battle.DamageCalculator", "battle.TypeChart", "battle.AttackContext",
        "battle.StatModifier", "battle.Side", "model.field.Battlefield", "model.field.BattlefieldType"]),
    "05-data": ("Data, gacha and save", [
        "data.DragonRepository", "data.GachaService", "data.Profile", "data.SaveManager", "data.CoinPack",
        "model.dragon.OwnedDragon", "model.dragon.DragonSpecies"]),
    "06-gui": ("GUI (Swing)", [
        "app.Main", "gui.GameWindow", "gui.GameContext", "gui.ScreenPanel", "gui.MenuPanel",
        "gui.EggPanel", "gui.TopUpPanel", "gui.CollectionPanel", "gui.TeamSelectPanel", "gui.FieldSelectPanel",
        "gui.BattlePanel", "gui.HowToPanel", "gui.Arena", "gui.MessageBox", "gui.DragonTile",
        "gui.DragonDetailPanel", "gui.RetroButton", "gui.RetroPanel", "gui.MatchSetup",
        "gui.SpriteFactory", "gui.BackgroundFactory", "gui.SoundPlayer", "gui.Theme"]),
}

PKG = re.compile(r"\b(?:[a-z_][a-z0-9_]*\.)+(?=[A-Z])")


def short(text):
    return PKG.sub("", text).replace("$", ".")


def javap(name):
    result = subprocess.run(["javap", "-p", "-cp", str(CLASSES), "httyd." + name],
                            capture_output=True, text=True)
    return result.stdout.splitlines()


def parse(name):
    lines = javap(name)
    header = next(l for l in lines if "class " in l or "interface " in l or "enum " in l)
    simple = name.split(".")[-1]
    kind = "class"
    stereotype = ""
    if "interface " in header:
        kind = "interface"
        stereotype = "«interface»"
    elif re.search(r"\benum\b", header) or "extends java.lang.Enum" in header:
        kind = "enum"
        stereotype = "«enumeration»"
    elif "extends java.lang.Record" in header:
        kind = "record"
        stereotype = "«record»"
    elif "abstract class" in header:
        stereotype = "«abstract»"
    extends = re.search(r"extends ([\w.$<>, ?]+?)(?: implements|\s*\{|$)", header)
    parents = []
    if extends and kind not in ("enum", "record", "interface"):
        base = short(extends.group(1)).split("<")[0].strip()
        if base not in ("Object", "Record", "Enum"):
            parents.append(("extends", base))
    if kind == "interface" and extends:
        for p in short(extends.group(1)).split(","):
            parents.append(("implements", p.split("<")[0].strip()))
    implements = re.search(r"implements ([\w.$<>, ?]+?)\s*\{", header)
    if implements:
        for p in re.split(r",\s*(?![^<]*>)", implements.group(1)):
            parents.append(("implements", short(p).split("<")[0].strip()))
    sealed = "sealed" in header
    fields, methods = [], []
    for l in lines[1:]:
        l = l.strip()
        if not l.endswith(";") or "(" in l and l.startswith("static {}"):
            continue
        if "lambda$" in l or "$values" in l or "access$" in l or l.startswith("static {}"):
            continue
        vis = "+" if l.startswith("public") else "#" if l.startswith("protected") else "-" if l.startswith("private") else "~"
        static = " static " in " " + l
        if "(" in l:
            mm = re.search(r"([\w$<>\[\], ?.]+?)\s+([\w$]+)\((.*?)\)(?: throws .*)?;$", l)
            if mm:
                ret, mname, params = mm.groups()
                ret = ret.split()[-1] if ret.split() else ret
                if kind == "enum" and mname in ("values", "valueOf"):
                    continue
                if kind == "record" and mname in ("equals", "hashCode", "toString"):
                    continue
                if mname == simple.split(".")[-1] or ret == "":
                    continue
                if vis == "-" or vis == "~":
                    continue
                text = f"{vis} {mname}({short(params)}): {short(ret)}"
                methods.append(("<U>" + text + "</U>") if static else text)
            else:
                # constructor
                mc = re.search(r"([\w$.]+)\((.*?)\)(?: throws .*)?;$", l)
                if mc and vis in "+#":
                    methods.insert(0, f"{vis} {short(mc.group(1)).split('.')[-1]}({short(mc.group(2))})")
        else:
            mf = re.search(r"([\w$<>\[\], ?.]+?)\s+([\w$]+);$", l)
            if mf:
                typ, fname = mf.groups()
                typ = typ.split()[-1]
                if fname in ("$VALUES",) or (kind == "enum" and typ == simple and "static" in l and "final" in l and vis == "+"):
                    continue
                if kind == "enum" and static:
                    continue
                if vis == "~":
                    continue
                text = f"{vis} {fname}: {short(typ)}"
                fields.append(("<U>" + text + "</U>") if static else text)
    return dict(name=simple, kind=kind, stereo=stereo(stereotype, sealed), parents=parents,
                fields=fields, methods=methods[:14],
                more=max(0, len(methods) - 14), abstract="abstract class" in header)


def stereo(s, sealed):
    return (s + (" sealed" if sealed else "")).strip()


def esc(text):
    # keep our <U> markers, escape the rest
    parts = re.split(r"(</?U>)", text)
    return "".join(p if p in ("<U>", "</U>") else html.escape(p) for p in parts)


def node(c):
    rows = []
    head = c["name"]
    if c["abstract"] or c["kind"] == "interface":
        head = f"<I>{html.escape(head)}</I>"
    else:
        head = html.escape(head)
    top = f'<TR><TD BGCOLOR="#fff2b3"><B>{head}</B>'
    if c["stereo"]:
        top = f'<TR><TD BGCOLOR="#fff2b3"><FONT POINT-SIZE="9">{html.escape(c["stereo"])}</FONT><BR/><B>{head}</B>'
    rows.append(top + "</TD></TR>")
    fields = "<BR ALIGN=\"LEFT\"/>".join(esc(f) for f in c["fields"]) or " "
    rows.append(f'<TR><TD ALIGN="LEFT" BALIGN="LEFT">{fields}<BR ALIGN="LEFT"/></TD></TR>')
    methods = [esc(m) for m in c["methods"]]
    if c["more"]:
        methods.append(f"… +{c['more']} more")
    body = "<BR ALIGN=\"LEFT\"/>".join(methods) or " "
    rows.append(f'<TR><TD ALIGN="LEFT" BALIGN="LEFT">{body}<BR ALIGN="LEFT"/></TD></TR>')
    return (f'  {c["name"]} [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="4">'
            + "".join(rows) + "</TABLE>>];\n")


for key, (title, names) in DIAGRAMS.items():
    classes = [parse(n) for n in names]
    known = {c["name"] for c in classes}
    dot = ['digraph G {\n  rankdir=BT; nodesep=0.35; ranksep=0.6; labelloc=t; fontsize=20;\n',
           f'  label="{title}";\n',
           '  node [shape=plain, fontname="DejaVu Sans", fontsize=10];\n  edge [fontname="DejaVu Sans", fontsize=9];\n']
    for c in classes:
        dot.append(node(c))
    drawn = set()
    for c in classes:
        for rel, parent in c["parents"]:
            if parent in known:
                style = 'arrowhead=empty' + (', style=dashed' if rel == "implements" else '')
                dot.append(f'  {c["name"]} -> {parent} [{style}];\n')
                drawn.add((c["name"], parent))
        # associations from field types
        for f in c["fields"]:
            ftype = re.sub(r"</?U>", "", f).split(": ", 1)[-1]
            for other in known:
                if other != c["name"] and re.search(rf"\b{other}\b", ftype) and (c["name"], other) not in drawn:
                    dot.append(f'  {c["name"]} -> {other} [arrowhead=vee, arrowtail=diamond, dir=both, color="#555555"];\n')
                    drawn.add((c["name"], other))
    dot.append("}\n")
    dot_path = OUT / f"{key}.dot"
    dot_path.write_text("".join(dot), encoding="utf-8")
    subprocess.run(["dot", "-Tpng", "-Gdpi=110", str(dot_path), "-o", str(OUT / f"{key}.png")], check=True)
    print("wrote", key)
