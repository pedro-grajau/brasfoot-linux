#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Build the competition patch reproducibly from the verified original JAR."""
import copy
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import re
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED

ROOT=Path(__file__).resolve().parents[1]
ORIGINAL_SHA="7028a8b5f08c02fb10690fdb8c1cc7e2ec4b14f5b3aafe22db78626110794aeb"


def run(*args):
    subprocess.run(list(map(str,args)),cwd=ROOT,check=True)


def java_string(value):
    return json.dumps(value,ensure_ascii=True)


def main():
    for command in ("java","javac"):
        if not shutil.which(command):raise SystemExit("Instale o JDK 21 completo; falta o comando "+command)
        version=subprocess.run([command,"-version"],text=True,capture_output=True,check=True)
        if not re.search(r'(?:version\s+"|javac\s+)21(?:[.\s"-]|$)',version.stdout+version.stderr):
            raise SystemExit("Use Java e javac do JDK 21. O build depende do ASM interno dessa versão.")
    if not (ROOT/"artifacts/linux/brasfoot.jar").is_file():
        raise SystemExit("Prepare sua cópia do jogo antes: python3 tools/prepare_linux.py --installer /caminho/brasfoot22-23.exe")
    (ROOT/"analysis").mkdir(exist_ok=True)
    run("python3",ROOT/"tools/validate_competitions.py")
    original=ROOT/"artifacts/linux/brasfoot.jar"
    if hashlib.sha256(original.read_bytes()).hexdigest()!=ORIGINAL_SHA:
        raise SystemExit("JAR original diferente do analisado; prepare novamente antes de aplicar o patch.")
    config=json.loads((ROOT/"patches/competitions.json").read_text())
    seed=json.loads((ROOT/"patches/world-cup-2025-seeds.json").read_text())
    trophies=json.loads((ROOT/"patches/trophies.json").read_text())["trophies"]
    rewards=json.loads((ROOT/"patches/rewards.json").read_text())
    build=ROOT/"artifacts/mod-build"
    classes=build/"classes";generated=build/"generated/mod";patched=build/"patched";libs=build/"libs"
    for p in (classes,generated,patched,libs):p.mkdir(parents=True,exist_ok=True)
    with ZipFile(original) as z:
        for name in z.namelist():
            if name.endswith(".jar"):(libs/Path(name).name).write_bytes(z.read(name))
    confeds=["UEFA","CONMEBOL","CAF","AFC","CONCACAF","OFC"]
    world=config["club_world_cup"]
    code='''package mod;
final class Rules {
    static final int WORLD_FIRST_YEAR=%s, WORLD_INTERVAL=%s, INTERCONTINENTAL_FIRST_YEAR=%s;
    static final int HOST_COUNTRY_ID=%s;
    static final String[] CONFEDERATIONS={%s};
    static final int[] SLOTS={%s};
    static final String[] SEED_REFS={%s};
    static final int[] SEED_CONFEDS={%s};
    static String trophyFile(String id){
        switch(id){%s default:throw new IllegalArgumentException(id);}
    }
}
''' % (world["first_year"],world["interval_years"],config["intercontinental_cup"]["first_year"],world["host_selection"]["brasfoot_country_id"],
       ",".join(map(java_string,confeds)),",".join(str(world["slots"][c]) for c in confeds),
       ",".join(java_string(Path(item["team_file"]).stem) for item in seed["seeds"]),
       ",".join(str(confeds.index(item["confederation"])) for item in seed["seeds"]),
       "".join("case %s:return %s;"%(java_string(t["id"]),java_string(t["file"])) for t in trophies))
    reward_code='''
    static int participation(String id){switch(id){%s default:return 0;}}
    static final int[] WORLD_WIN={%s}, INTER_WIN={%s};
    static int placement(String id,int rank,boolean coach){
        int[] values;
        switch(id){%s default:return 0;}
        return rank>0&&rank<=values.length?values[rank-1]:0;
    }
''' % (''.join('case %s:return %s;'%(java_string(k),v) for k,v in rewards['participation'].items()),
        ','.join(map(str,rewards['world_knockout_winner'])),','.join(map(str,rewards['intercontinental_match_winner'])),
        ''.join('case %s:values=coach?new int[]{%s}:new int[]{%s};break;'%(java_string(k),
                ','.join(map(str,rewards['coach_placement_points'][k])),','.join(map(str,v)))
                for k,v in rewards['club_placement_points'].items()))
    code=code.rsplit('}',1)[0]+reward_code+'}\n'
    (generated/"Rules.java").write_text(code)
    runtime_sources=sorted((ROOT/"mod/src").rglob("*.java"))+[generated/"Rules.java"]
    run("javac","--release","11","-encoding","UTF-8","-cp",original,"-d",classes,*runtime_sources)
    exports=["--add-exports=java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED",
             "--add-exports=java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED"]
    tooling=build/"tooling";tooling.mkdir(exist_ok=True)
    run("javac",*exports,"-d",tooling,ROOT/"tools/PatchClasses.java")
    cp=":".join(map(str,[tooling,classes,original,*sorted(libs.glob("*.jar"))]))
    run("java",*exports,"-cp",cp,"PatchClasses",original,patched)
    replacements={str(p.relative_to(classes)):p.read_bytes() for p in classes.rglob("*.class")}
    replacements.update({str(p.relative_to(patched)):p.read_bytes() for p in patched.rglob("*.class")})
    for trophy in trophies:
        asset=ROOT/"mod/assets/trophies"/(trophy["id"]+".png")
        if not asset.is_file():raise RuntimeError("Taça com transparência ausente: "+str(asset))
        replacements["mod/trophies/"+asset.name]=asset.read_bytes()
    for p in (ROOT/"patches").glob("*.json"):replacements["mod/config/"+p.name]=p.read_bytes()
    destination=ROOT/"artifacts/linux/brasfoot-competitions.jar"
    with ZipFile(original) as source,ZipFile(destination.with_suffix(".tmp"),"w") as target:
        for info in source.infolist():
            data=replacements.pop(info.filename,source.read(info.filename))
            target.writestr(copy.copy(info),data)
        for name,data in sorted(replacements.items()):
            info=ZipInfo(name,date_time=(2022,8,25,16,46,48));info.compress_type=ZIP_DEFLATED
            target.writestr(info,data)
    destination.with_suffix(".tmp").replace(destination)
    with ZipFile(destination) as z:
        if z.testzip() is not None:raise RuntimeError("JAR modificado não passou na verificação CRC")
    # Use a separate writable game directory. Never import or overwrite live saves.
    game=ROOT/"runtime/competitions/game"
    game.mkdir(parents=True,exist_ok=True)
    for source in (ROOT/"artifacts/original").iterdir():
        if source.name in ("sav","$PLUGINSDIR","bf22-23.exe","erros.log"):continue
        target=game/source.name
        if target.exists():continue
        if source.is_dir():shutil.copytree(source,target)
        else:shutil.copy2(source,target)
    (game/"sav").mkdir(exist_ok=True)
    # Native history/coach widgets resolve mA() by this existing filesystem API.
    run("javac","--release","11","-cp",destination,"-d",tooling,ROOT/"tools/ExportTrophyIcons.java")
    export_cp=":".join(map(str,[tooling,destination,*sorted(libs.glob("*.jar"))]))
    run("java","-Djava.awt.headless=true","-cp",export_cp,"ExportTrophyIcons",game/"trofeus")
    preferences=ROOT/"runtime/competitions/preferences"
    if not preferences.exists() and (ROOT/"runtime/preferences").exists():
        shutil.copytree(ROOT/"runtime/preferences",preferences)
    report={"original_sha256":ORIGINAL_SHA,"mod_sha256":hashlib.sha256(destination.read_bytes()).hexdigest(),
            "patched_original_classes":["best/f.class","best/a.class","best/at.class","a/al.class","a/de.class","a/di.class","f/G.class","best/I.class","a/hc.class","b/aC.class"],
            "runtime_source":"mod/src/mod/CompetitionMod.java","isolated_game_directory":str(game)}
    (ROOT/"analysis/mod-build.json").write_text(json.dumps(report,indent=2)+"\n")
    print("Patch compilado:",destination)


if __name__=="__main__":main()
