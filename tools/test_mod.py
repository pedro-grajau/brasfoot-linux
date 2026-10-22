#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Run actual game-fixture and Kryo integration tests without opening a GUI."""
from pathlib import Path
import hashlib
import subprocess
import argparse

ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(description=__doc__)
selection=parser.add_mutually_exclusive_group()
selection.add_argument("--save",type=Path,help="Save local opcional para testar elencos reais, sem alterá-lo")
selection.add_argument("--without-save",action="store_true",help="Executar somente a bateria com carreiras controladas")
options=parser.parse_args()
if options.save is not None and not options.save.expanduser().is_file():parser.error("Save não encontrado: "+str(options.save))
build=ROOT/"artifacts/mod-build"
classes=build/"tests"
classes.mkdir(parents=True,exist_ok=True)
cp=":".join(map(str,[ROOT/"artifacts/linux/brasfoot-competitions.jar",*sorted((build/"libs").glob("*.jar"))]))
subprocess.run(["javac","--release","11","-encoding","UTF-8","-cp",cp,"-d",str(classes),
                str(ROOT/"mod/test/mod/CompetitionIntegrationTest.java")],cwd=ROOT,check=True)
command=["java","--add-exports=java.desktop/sun.java2d=ALL-UNNAMED",
         "--add-opens=java.base/java.util=ALL-UNNAMED","--add-opens=java.base/java.lang=ALL-UNNAMED",
         "--add-opens=java.desktop/java.awt=ALL-UNNAMED","-Djava.awt.headless=true",
         "-Djava.util.prefs.userRoot="+str(build/"test-preferences"),
         "-Dmod.test.output="+str(ROOT/"analysis"),
         "-cp",str(classes)+":"+cp,"mod.CompetitionIntegrationTest"]
legacy=ROOT/"artifacts/original/sav/save_100_anos.s22"
if options.save is not None:legacy=options.save.expanduser().resolve()
use_save=not options.without_save and legacy.is_file()
if use_save:command.append(str(legacy))
else:print("Sem save real: testes de elencos reais e resultado/bilheteria ao vivo serão omitidos.",flush=True)
legacy_hash=hashlib.sha256(legacy.read_bytes()).hexdigest() if use_save else None
subprocess.run(command,cwd=ROOT/"runtime/competitions/game",check=True)
if legacy_hash is not None and legacy_hash!=hashlib.sha256(legacy.read_bytes()).hexdigest():
    raise RuntimeError("Save original alterado durante o teste")
