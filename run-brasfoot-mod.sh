#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
set -euo pipefail
project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
if ! command -v java >/dev/null 2>&1; then
    echo "É necessário instalar o JDK 21 com suporte a interface gráfica." >&2
    exit 1
fi
if [[ ! -f "$project_dir/artifacts/linux/brasfoot-competitions.jar" ]]; then
    echo "Compile o patch com: python3 tools/build_mod.py" >&2
    exit 1
fi
cd -- "$project_dir/runtime/competitions/game"
mkdir -p -- "$project_dir/runtime/competitions/preferences" "$project_dir/runtime/competitions/tmp"
exec java \
    --add-exports=java.desktop/sun.java2d=ALL-UNNAMED \
    --add-opens=java.base/java.util=ALL-UNNAMED \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.desktop/java.awt=ALL-UNNAMED \
    "-Djava.util.prefs.userRoot=$project_dir/runtime/competitions/preferences" \
    "-Djava.io.tmpdir=$project_dir/runtime/competitions/tmp" \
    -jar "$project_dir/artifacts/linux/brasfoot-competitions.jar" "$@"
