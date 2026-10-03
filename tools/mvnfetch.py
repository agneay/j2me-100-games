#!/usr/bin/env python3
"""Minimal Maven Central dependency fetcher (stdlib only).

Resolves compile/runtime-scope transitive dependencies of the given
coordinates and downloads the jars into a directory. It is intentionally
simple: no version ranges, no exclusions beyond scope/optional filtering.
It exists so the toolchain can be bootstrapped without Maven or Gradle.

usage: mvnfetch.py <outdir> group:artifact:version [...]
"""
import os
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET

REPO = "https://repo1.maven.org/maven2"
NS = "{http://maven.apache.org/POM/4.0.0}"
_pom_cache = {}


def fetch(url):
    with urllib.request.urlopen(url, timeout=60) as r:
        return r.read()


def pom(g, a, v):
    key = (g, a, v)
    if key not in _pom_cache:
        url = "%s/%s/%s/%s/%s-%s.pom" % (REPO, g.replace(".", "/"), a, v, a, v)
        _pom_cache[key] = ET.fromstring(fetch(url))
    return _pom_cache[key]


def text(el, path):
    x = el.find("/".join(NS + p for p in path.split("/")))
    return x.text.strip() if x is not None and x.text else None


def effective(g, a, v):
    """Return (props, managed, deps) for a POM including its parent chain."""
    root = pom(g, a, v)
    props, managed, deps = {}, {}, []
    parent = root.find(NS + "parent")
    if parent is not None:
        pg, pa, pv = text(parent, "groupId"), text(parent, "artifactId"), text(parent, "version")
        pp, pm, pd = effective(pg, pa, pv)
        props.update(pp)
        managed.update(pm)
        deps.extend(pd)
        props.setdefault("project.parent.version", pv)
    props["project.version"] = v
    props["project.groupId"] = g
    pr = root.find(NS + "properties")
    if pr is not None:
        for p in pr:
            props[p.tag.replace(NS, "")] = (p.text or "").strip()
    dm = root.find(NS + "dependencyManagement/" + NS + "dependencies")
    if dm is not None:
        for d in dm.findall(NS + "dependency"):
            managed[(text(d, "groupId"), text(d, "artifactId"))] = d
    ds = root.find(NS + "dependencies")
    if ds is not None:
        deps.extend(ds.findall(NS + "dependency"))
    return props, managed, deps


def subst(s, props):
    for _ in range(5):
        s2 = re.sub(r"\$\{([^}]+)\}", lambda m: props.get(m.group(1), m.group(0)), s)
        if s2 == s:
            break
        s = s2
    return s


def resolve(coords):
    seen = {}
    queue = list(coords)
    while queue:
        g, a, v = queue.pop(0)
        if (g, a) in seen:
            continue
        seen[(g, a)] = v
        props, managed, deps = effective(g, a, v)
        for d in deps:
            dg = subst(text(d, "groupId"), props)
            da = subst(text(d, "artifactId"), props)
            m = managed.get((text(d, "groupId"), text(d, "artifactId")))
            dv = text(d, "version") or (text(m, "version") if m is not None else None)
            scope = text(d, "scope") or (text(m, "scope") if m is not None else None) or "compile"
            if scope not in ("compile", "runtime") or text(d, "optional") == "true":
                continue
            if (text(d, "type") or "jar") != "jar":
                continue
            if dv is None:
                print("warn: no version for %s:%s" % (dg, da), file=sys.stderr)
                continue
            queue.append((dg, da, subst(dv, props)))
    return seen


def main():
    out = sys.argv[1]
    os.makedirs(out, exist_ok=True)
    coords = [tuple(c.split(":")) for c in sys.argv[2:]]
    for (g, a), v in sorted(resolve(coords).items()):
        name = "%s-%s.jar" % (a, v)
        path = os.path.join(out, name)
        if not os.path.exists(path):
            url = "%s/%s/%s/%s/%s" % (REPO, g.replace(".", "/"), a, v, name)
            data = fetch(url)
            with open(path, "wb") as f:
                f.write(data)
        print(name)


if __name__ == "__main__":
    main()
