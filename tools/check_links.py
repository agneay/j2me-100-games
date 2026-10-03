#!/usr/bin/env python3
"""Link check for the generated website (and optionally external URLs).

Every href/src in website/_site/**/*.html is resolved. Internal links must
point at an existing file (a directory link needs an index.html); a #fragment
must match an id in the target page. With --external, every distinct
http(s) URL is also requested (HEAD, then GET on 405) and must not answer
4xx/5xx; GitHub release download links are followed through redirects.

usage: python tools/check_links.py [--site website/_site] [--external] [--base-url URL]
--base-url additionally maps absolute links under the published site URL
(e.g. the 404 page's /j2me-100-games/ links) back onto the local folder.
"""
import argparse
import concurrent.futures
import html.parser
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402


class Collector(html.parser.HTMLParser):
    def __init__(self):
        super().__init__()
        self.links, self.ids = [], set()

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if "id" in a:
            self.ids.add(a["id"])
        for k in ("href", "src", "srcset"):
            v = a.get(k)
            if v and not (tag == "link" and a.get("rel") in ("preconnect", "canonical")) and not (
                    tag == "meta"):
                self.links.append((tag, k, v.split()[0] if k == "srcset" else v))


def parse(path):
    c = Collector()
    with open(path, encoding="utf-8") as f:
        c.feed(f.read())
    return c


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--site", default=os.path.join(catalog.ROOT, "website", "_site"))
    ap.add_argument("--external", action="store_true")
    ap.add_argument("--base-url", default=catalog.PAGES_URL)
    a = ap.parse_args()
    site = os.path.abspath(a.site)
    base_path = urllib.parse.urlparse(a.base_url).path.rstrip("/") + "/"
    pages = {}
    for dp, _, fs in os.walk(site):
        for f in fs:
            if f.endswith(".html"):
                pages[os.path.join(dp, f)] = parse(os.path.join(dp, f))
    errors, external = [], {}
    for page, c in pages.items():
        rel_page = os.path.relpath(page, site)
        for tag, attr, url in c.links:
            if url.startswith(("mailto:", "javascript:", "data:")):
                continue
            if url.startswith(("http://", "https://")):
                if url.startswith(a.base_url + "/") or url == a.base_url:
                    url = base_path + url[len(a.base_url):].lstrip("/")
                else:
                    external.setdefault(url, []).append(rel_page)
                    continue
            path, _, frag = url.partition("#")
            path = path.split("?")[0]
            if path.startswith("/"):
                if not path.startswith(base_path):
                    errors.append("%s: absolute link outside the site base: %s" % (rel_page, url))
                    continue
                target = os.path.join(site, path[len(base_path):])
            elif path == "":
                target = page
            else:
                target = os.path.normpath(os.path.join(os.path.dirname(page), urllib.parse.unquote(path)))
            if os.path.isdir(target):
                target = os.path.join(target, "index.html")
            if not os.path.exists(target):
                errors.append("%s: broken %s %s" % (rel_page, attr, url))
                continue
            if frag and target.endswith(".html"):
                tc = pages.get(target) or parse(target)
                if frag not in tc.ids:
                    errors.append("%s: missing anchor #%s in %s" % (rel_page, frag, os.path.relpath(target, site)))
    print("%d pages, %d internal problems, %d distinct external URLs" % (len(pages), len(errors), len(external)))
    if a.external:
        def check(url):
            req = urllib.request.Request(url, method="HEAD", headers={"User-Agent": "j2me-100-games-linkcheck"})
            try:
                with urllib.request.urlopen(req, timeout=30) as r:
                    return url, r.status
            except urllib.error.HTTPError as e:
                if e.code in (403, 405, 429):
                    try:
                        req = urllib.request.Request(url, headers={"User-Agent": "j2me-100-games-linkcheck"})
                        with urllib.request.urlopen(req, timeout=30) as r:
                            return url, r.status
                    except urllib.error.HTTPError as e2:
                        return url, e2.code
                    except Exception as e2:  # noqa: BLE001
                        return url, str(e2)
                return url, e.code
            except Exception as e:  # noqa: BLE001
                return url, str(e)
        with concurrent.futures.ThreadPoolExecutor(8) as ex:
            for url, status in ex.map(check, sorted(external)):
                if not isinstance(status, int) or status >= 400:
                    errors.append("external %s -> %s (used on %s)" % (url, status, external[url][0]))
        print("external URLs checked")
    for e in errors[:200]:
        print("  " + e)
    if len(errors) > 200:
        print("  ... %d more" % (len(errors) - 200))
    sys.exit(1 if errors else 0)


if __name__ == "__main__":
    main()
