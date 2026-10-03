#!/bin/sh
# Run the pinned Apache Ant from tools/lib (bootstraps the toolchain on first use).
# usage: tools/ant.sh [-f build.xml] [target...]
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
if [ ! -f "$HERE/lib/ant-1.10.15.jar" ] || [ ! -f "$HERE/lib/ecj-3.26.0.jar" ]; then
    python3 "$HERE/bootstrap.py" || python "$HERE/bootstrap.py"
fi
SEP=":"
LIBDIR="$HERE/lib"
case "$(uname -s 2>/dev/null)" in
    MINGW*|MSYS*|CYGWIN*) SEP=";"; LIBDIR="$(cygpath -w "$HERE/lib")" ;;
esac
exec java -cp "$LIBDIR/ant-1.10.15.jar${SEP}$LIBDIR/ant-launcher-1.10.15.jar" org.apache.tools.ant.Main "$@"
