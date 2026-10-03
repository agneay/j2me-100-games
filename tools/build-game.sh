#!/bin/sh
# Build one game's JAR + JAD.  usage: tools/build-game.sh 001 [target]
# target defaults to dist; others: test, media, web, clean
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
if [ -z "$1" ]; then echo "usage: $0 <game id, e.g. 001> [target]" >&2; exit 2; fi
DIR="$(ls -d "$HERE/../games/$1"-* 2>/dev/null | head -n 1)"
if [ -z "$DIR" ]; then echo "no game with id $1" >&2; exit 1; fi
exec "$HERE/ant.sh" -f "$DIR/build.xml" "${2:-dist}"
