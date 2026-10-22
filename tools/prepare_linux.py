#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Extract the supplied NSIS installer and preserve its Java payload unchanged."""
import copy
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import struct
import subprocess
import tempfile
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--installer",type=Path,default=ROOT/"brasfoot22-23.exe",
                        help="Caminho para sua cópia do instalador Brasfoot 2022–2023")
    installer=parser.parse_args().installer.expanduser().resolve()
    if not installer.is_file():
        raise SystemExit("Instalador não encontrado. Use: python3 tools/prepare_linux.py --installer /caminho/brasfoot22-23.exe")
    seven_zip = shutil.which("7z") or shutil.which("7zz")
    if not seven_zip:
        raise SystemExit("É necessário ter o comando 7z ou 7zz instalado.")
    original = ROOT / "artifacts/original"
    original.mkdir(parents=True, exist_ok=True)
    # Skip existing files so preparing again preserves saves and local settings.
    subprocess.run([seven_zip, "x", "-aos", "-bso0", "-bsp0",
                    f"-o{original}", str(installer)], check=True)
    executable = original / "bf22-23.exe"
    output = ROOT / "artifacts/linux/brasfoot.jar"
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=output.parent) as staging:
        temporary = Path(staging) / "brasfoot.jar"
        with ZipFile(executable) as source, ZipFile(temporary, "w") as target:
            classes = []
            for info in source.infolist():
                data = source.read(info.filename)
                # ZipFile.writestr updates header_offset; never reuse source metadata.
                target.writestr(copy.copy(info), data)
                if info.filename.endswith(".class"):
                    classes.append({"name": info.filename,
                                    "major": struct.unpack_from(">H", data, 6)[0]})
        with ZipFile(executable) as source, ZipFile(temporary) as target:
            if target.testzip() is not None:
                raise RuntimeError("Falha de integridade no JAR gerado")
            assert source.namelist() == target.namelist()
            for name in source.namelist():
                if source.read(name) != target.read(name):
                    raise RuntimeError(f"Conteúdo alterado: {name}")
        temporary.replace(output)
    analysis = ROOT / "analysis"
    analysis.mkdir(exist_ok=True)
    (analysis / "class-inventory.json").write_text(
        json.dumps(classes, indent=2) + "\n")
    (analysis / "payload-integrity.json").write_text(json.dumps({
        "installer_sha256": digest(installer),
        "windows_launcher_sha256": digest(executable),
        "linux_jar_sha256": digest(output),
        "class_count": len(classes),
        "class_versions": sorted({item["major"] for item in classes}),
        "all_zip_entry_contents_identical": True,
        "executed_installer": False,
    }, indent=2) + "\n")
    print(f"Pronto: {output}\nExecute ./run-brasfoot.sh para iniciar.")


if __name__ == "__main__":
    main()
