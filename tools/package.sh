#!/bin/sh
# Build every game and collect release files into release/:
#   release/games/*.jar|*.jad and release/j2me-100-games-v<version>.zip
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
"$HERE/build-all.sh"
exec "$HERE/ant.sh" -f "$HERE/../build.xml" -Dskip.dist=true package
