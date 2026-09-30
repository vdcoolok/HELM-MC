#!/bin/sh
set -e

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$SCRIPT_DIR"

./gradlew helmBuild --console=plain --warning-mode all "$@"

JAR=$(ls -1 build/libs/HELM*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR" ]; then
    echo "Build produced no jar in build/libs." >&2
    exit 1
fi

cp -f "$JAR" "$SCRIPT_DIR/$(basename "$JAR")"
echo "Jar written to $SCRIPT_DIR/$(basename "$JAR")"
