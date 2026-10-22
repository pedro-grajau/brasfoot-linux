#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Validate patch data against local teams/assets, without changing the game."""
from collections import Counter
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONFEDERATIONS = {"UEFA", "CONMEBOL", "AFC", "CAF", "CONCACAF", "OFC"}


def read(name):
    return json.loads((ROOT / "patches" / name).read_text())


def require(condition, message):
    if not condition:
        raise ValueError(message)


def local_path(base, relative):
    path = (base / relative).resolve()
    require(path.is_relative_to(base.resolve()), f"Caminho fora da base: {relative}")
    require(path.is_file(), f"Arquivo ausente: {path}")
    return path


def main():
    config = read("competitions.json")
    world = config["club_world_cup"]
    annual = config["intercontinental_cup"]
    bootstrap = read(world["first_edition_seeds"])
    asset_config = read("trophies.json")
    assets = asset_config["trophies"]
    rewards = read("rewards.json")
    trophy_ids = [item["id"] for item in assets]
    require(len(trophy_ids) == len(set(trophy_ids)), "Taças com IDs repetidos")
    lengths={"club_world_cup":32,"intercontinental_cup":6,"africa_asia_pacific":3,"derby_of_americas":2,"challenger_cup":2}
    require(set(rewards["participation"])=={"club_world_cup","intercontinental_cup"},"Participações devem ser pagas uma vez por torneio principal")
    for name in ("club_placement_points","coach_placement_points"):
        require(set(rewards[name])==set(lengths),"Ranking das cinco taças incompleto")
        for cup,length in lengths.items():
            points=rewards[name][cup]
            require(len(points)==length and all(type(n) is int and n>=0 for n in points),f"Pontos inválidos: {name}/{cup}")
            require(points==sorted(points,reverse=True),f"Pontos não respeitam colocação: {cup}")
    for name,count in (("world_knockout_winner",4),("intercontinental_match_winner",5)):
        require(len(rewards[name])==count and all(type(n) is int and 0<=n<=2147483647 for n in rewards[name]),f"Prêmios inválidos: {name}")
    require(all(type(n) is int and 0<n<=2147483647 for n in rewards["participation"].values()),"Prêmio de participação inválido")
    for trophy in assets:
        p = local_path(ROOT, str(Path(asset_config["asset_base"]) / trophy["file"]))
        require(p.stat().st_size == trophy["bytes"], f"Tamanho alterado: {p.name}")
        require(hashlib.sha256(p.read_bytes()).hexdigest() == trophy["sha256"],
                f"Imagem alterada: {p.name}")
    prepared_base=ROOT/"mod/assets/trophies"
    provenance=json.loads((prepared_base/"provenance.json").read_text())
    require({item["id"] for item in provenance["assets"]}==set(trophy_ids),
            "Conjunto de PNG preparados diferente das cinco taças")
    for item in provenance["assets"]:
        p=local_path(prepared_base,item["file"])
        require(p.read_bytes().startswith(b"\x89PNG\r\n\x1a\n"),f"Taça preparada não é PNG: {p.name}")
        require(hashlib.sha256(p.read_bytes()).hexdigest()==item["sha256"],
                f"PNG preparado alterado: {p.name}")

    slots = world["slots"]
    require(set(slots) == CONFEDERATIONS | {"HOST"}, "Cotas incompletas")
    require(sum(slots.values()) == world["participants"] == 32, "Cotas não somam 32")
    require(world["first_year"] == bootstrap["edition_year"] == 2025,
            "Primeira edição inconsistente")
    require(world["interval_years"] == 4, "Mundial deve ser quadrienal")
    require(not world["repeat_historical_seeds_after_first_edition"],
            "Seeds históricos não podem reaparecer em 2029")
    require(world["trophy"] in trophy_ids, "Taça do Mundial ausente")
    require(bootstrap["qualification_cycle"] == [2021, 2022, 2023, 2024],
            "Ciclo da primeira edição incorreto")
    require(bootstrap["career_result_years"] == [2022, 2023, 2024],
            "Anos de resultados da carreira incorretos")
    require(bootstrap["historical_scores"] is None, "Não inventar placares de 2021")
    seed_files = set()
    counts = Counter()
    for seed in bootstrap["seeds"]:
        local_path(ROOT / "artifacts/original", seed["team_file"])
        require(seed["team_file"] not in seed_files, "Clube pré-classificado repetido")
        seed_files.add(seed["team_file"])
        require(seed["confederation"] in CONFEDERATIONS, "Confederação desconhecida")
        require(seed["title_year"] == bootstrap["historical_year"] == 2021,
                "Seed deve corresponder a 2021")
        require(seed["source_id"] in bootstrap["sources"], "Seed sem fonte")
        counts[seed["confederation"]] += 1
    require(len(seed_files) == 5, "Esperados cinco campeões continentais de 2021")
    require(counts == Counter({c: 1 for c in CONFEDERATIONS - {"OFC"}}),
            "Seeds de 2021 não correspondem às cinco confederações")
    require(bootstrap["ofc_2021"]["seed"] is None,
            "A OFC não teve campeão continental em 2021")
    for confed, count in counts.items():
        require(count <= slots[confed], f"Seeds excedem cota de {confed}")
    group = world["group_stage"]
    knockout = world["knockout_stage"]
    require(group["groups"] == 8 and group["clubs_per_group"] == 4,
            "Esperados oito grupos de quatro")
    require(group["groups"] * group["clubs_per_group"] == world["participants"],
            "Número de grupos incompatível")
    require(group["legs"] == knockout["legs"] == 1, "Jogos devem ser de ida única")
    require(group["rounds"] == 3 and group["qualifiers_per_group"] == 2,
            "Formato de grupos incorreto")
    require(knockout["round_sizes"] == [16, 8, 4, 2], "Mata-mata incompatível")
    group_matches = group["groups"] * group["clubs_per_group"] * (group["clubs_per_group"] - 1) // 2
    knockout_matches = sum(size // 2 for size in knockout["round_sizes"])
    require(group_matches == group["matches"] == 48, "Quantidade de jogos de grupos incorreta")
    require(knockout_matches == knockout["matches"] == 15, "Quantidade de eliminatórias incorreta")
    require(group_matches + knockout_matches == world["total_matches"] == 63,
            "Quantidade total de jogos incorreta")
    require(not knockout["third_place_match"], "Mundial não tem terceiro lugar")

    require(annual["first_year"] == 2024 and annual["interval_years"] == 1,
            "Intercontinental deve ser anual a partir de 2024")
    require(config["legacy_annual_world_cup"]["last_year"] == 2023,
            "Transição do torneio anual incorreta")
    require(annual["participants"] == 6 and not annual["host_slot"],
            "Intercontinental deve ter seis campeões, sem vaga anfitriã")
    require(not annual["third_place_match"] and annual["legs"] == 1,
            "Intercontinental deve ser de ida única sem terceiro lugar")
    rounds = annual["rounds"]
    require(len(rounds) == annual["total_matches"] == 5, "Intercontinental deve ter cinco jogos")
    rotation = annual["afc_caf_rotation"]
    require(rotation["mode"] == "alternate_annually" and rotation["anchor_year"] == 2024
            and rotation["opening_confederation_in_anchor_year"] == "AFC"
            and rotation["other_confederation"] == "CAF", "Alternância AFC/CAF inconsistente")
    awards = []
    for year in (2024, 2025):
        opening = "AFC" if (year - rotation["anchor_year"]) % 2 == 0 else "CAF"
        remaining = "CAF" if opening == "AFC" else "AFC"
        seen = set()
        used_winners = Counter()
        champions = Counter()
        for round_ in rounds:
            rid = round_["id"]
            require(rid not in seen, f"Rodada repetida: {rid}")
            require(len(round_["entrants"]) == 2, f"Rodada sem dois participantes: {rid}")
            for entrant in round_["entrants"]:
                kind, target = entrant.split(":", 1)
                if kind == "winner":
                    require(target in seen, f"Dependência inválida: {entrant}")
                    used_winners[target] += 1
                else:
                    require(kind == "champion", f"Origem inválida: {entrant}")
                    target = {"opening_afc_or_caf": opening,
                              "remaining_afc_or_caf": remaining}.get(target, target)
                    require(target in CONFEDERATIONS, f"Confederação inválida: {target}")
                    champions[target] += 1
            if round_["title"] is not None:
                require(round_["title"] in trophy_ids, f"Taça ausente: {round_['title']}")
                if year == 2024:
                    awards.append(round_["title"])
            seen.add(rid)
        require(champions == Counter({c: 1 for c in CONFEDERATIONS}),
                f"Os seis campeões não entram exatamente uma vez em {year}")
        require(set(used_winners) == seen - {"intercontinental_final"}
                and all(n == 1 for n in used_winners.values()),
                "Chaveamento precisa conduzir cada vencedor à próxima fase uma vez")
    require(len(awards) == len(set(awards)) == 4, "Esperados quatro títulos distintos")
    require("club_world_cup" not in awards, "Título do Mundial não pertence à Intercontinental")
    print("OK: cinco classificados históricos e seus arquivos .ban localizados.")
    print(f"Mundial 2025: {len(seed_files)} vagas iniciais; {32 - len(seed_files)} a preencher na carreira.")
    print("OK: formato de 32 clubes e 63 jogos; Intercontinental com seis clubes e cinco jogos.")
    print("OK: chaveamento validado em 2024/2025; quatro títulos distintos.")
    print("OK: cinco PNGs tratados presentes e íntegros.")
    print("OK: premiações de participação/avanço e ranking das cinco competições.")
    print("Estado: dados validados. Use tools/test_mod.py para verificar a integração ao jogo.")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, KeyError, OSError, json.JSONDecodeError) as error:
        raise SystemExit(f"ERRO: {error}") from error
