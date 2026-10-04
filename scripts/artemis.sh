#!/bin/sh
set -eu
PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
ARTEMIS_ROOT=${TB1_ARTEMIS_ROOT:-"$PROJECT_ROOT/../../../.tools/artemis"}
TOOLS_ROOT=$(dirname "$ARTEMIS_ROOT")
SDK_ROOT=${ANDROID_HOME:-"$HOME/Library/Android/sdk"}
export PATH="$TOOLS_ROOT/bin:$SDK_ROOT/platform-tools:$PATH"
export ARTEMIS_DESKTOP_NOTIFY=false
cd "$ARTEMIS_ROOT"
exec "$ARTEMIS_ROOT/.venv/bin/artemis" "$@"
