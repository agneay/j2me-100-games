#!/bin/sh
# Build JAR + JAD for every game.
# usage: tools/build-all.sh [--jobs N]     (parallel, via tools/build_all.py)
#        tools/build-all.sh --ant          (sequential, plain Ant: ant dist)
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
PY="$(command -v python3 || command -v python)"
if [ "$1" = "--ant" ]; then
    exec "$HERE/ant.sh" -f "$HERE/../build.xml" dist
fi
"$PY" "$HERE/bootstrap.py" --no-teavm
"$HERE/ant.sh" -q -f "$HERE/../build.xml" tools
exec "$PY" "$HERE/build_all.py" --target dist "$@"
