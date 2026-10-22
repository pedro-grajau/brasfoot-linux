#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
set -euo pipefail
project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
if ! command -v java >/dev/null 2>&1; then
    echo "É necessário instalar um Java com suporte a interface gráfica." >&2
    exit 1
fi
if [[ ! -f "$project_dir/artifacts/linux/brasfoot.jar" ]]; then
    echo "Prepare os arquivos com: python3 tools/prepare_linux.py" >&2
    exit 1
fi
# The original game loads external teams, sounds, configuration and saves here.
cd -- "$project_dir/artifacts/original"
mkdir -p -- "$project_dir/runtime/preferences" "$project_dir/runtime/tmp"
exec java \
    --add-exports=java.desktop/sun.java2d=ALL-UNNAMED \
    --add-opens=java.base/java.util=ALL-UNNAMED \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.desktop/java.awt=ALL-UNNAMED \
    "-Djava.util.prefs.userRoot=$project_dir/runtime/preferences" \
    "-Djava.io.tmpdir=$project_dir/runtime/tmp" \
    -jar "$project_dir/artifacts/linux/brasfoot.jar" "$@"
