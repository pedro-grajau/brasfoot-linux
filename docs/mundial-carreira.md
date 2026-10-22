# Mundial e Intercontinental na carreira

## Usar o patch

O patch está integrado como versão beta em `artifacts/linux/brasfoot-competitions.jar`.
Execute `./run-brasfoot-mod.sh`. A versão modificada usa o diretório de jogo
`runtime/competitions/game`, com saves próprios em sua subpasta `sav`.
O JAR original e seus saves são preservados.

Inicie uma carreira em 2022, com competições internacionais habilitadas, para
acompanhar a transição de formatos em 2024 e disputar o primeiro Mundial em 2025.
As regras e imagens estão configuradas em `patches/`. Para compilar e testar:

```sh
python3 tools/validate_competitions.py
python3 tools/build_mod.py
python3 tools/test_mod.py
```

O build usa o JDK 21 deste computador e não exige novo download de dependências.
A versão original permanece disponível por `./run-brasfoot.sh`.

## Mundial de 32 clubes

- Primeira edição em 2025, seguida de 2029, 2033 etc.
- Oito grupos de quatro, turno único, dois classificados por grupo.
- Oitavas, quartas, semifinal e final, em jogo único, sem terceiro lugar.
- 48 partidas de grupos e 15 eliminatórias: 63 partidas, sete rodadas por finalista.
- Desempate por confronto direto, reaplicado aos clubes que permanecem
  empatados; depois saldo/gols gerais, fair play dos cartões de jogadores
  registrados pelo motor e sorteio reproduzível, persistido no save.

| Confederação | Vagas |
|---|---:|
| UEFA | 12 |
| CONMEBOL | 6 |
| AFC | 4 |
| CAF | 4 |
| Concacaf | 4 |
| OFC | 1 |
| Anfitrião | 1 |
| Total | 32 |

Os campeões continentais do ciclo têm prioridade. As vagas restantes seguem o
ranking da carreira, com limite de dois clubes por país, exceto os classificados
como campeões continentais. A OFC seleciona o melhor ranqueado entre seus campeões.
Cada clube ocupa uma única vaga, inclusive quando tem mais de um título no ciclo.

O sorteio separa clubes do mesmo país e permite no máximo um representante de
cada confederação por grupo, exceto a UEFA, com um ou dois. A distribuição é
gerada para a carreira; os potes e participantes reais de 2025 não ficam fixos.

### Classificados iniciais de 2025

Por decisão do usuário, os campeões continentais reais de 2021 entram como
seeds da primeira edição. As outras 27 vagas são preenchidas com os resultados
da carreira em 2022–2024:

| Confederação | Clube | Arquivo localizado |
|---|---|---|
| UEFA | Chelsea | `teams/chelsea.ban` |
| CONMEBOL | Palmeiras | `teams/palmeiras.ban` |
| AFC | Al Hilal | `teams/al_hilal.ban` |
| CAF | Al Ahly, do Egito | `teams/alahly_egi.ban` |
| Concacaf | Monterrey | `teams/monterrey_mex.ban` |

Os cinco campeões constam do [relatório oficial da FIFA sobre o Mundial de
2021](https://publications.fifa.com/en/annual-report-2022/2022-at-a-glance/fifa-club-world-cup-2021/).
A competição da OFC de 2021 foi cancelada, conforme o [comunicado da FIFA sobre
o sorteio](https://inside.fifa.com/tournaments/mens/clubworldcup/uae2021/media-releases/fifa-club-world-cup-uae-2021-draw-to-take-place-29-november).
Não há seed da OFC nem campeão inventado para 2021.

Esses seeds só valem para 2025. Em 2029, o ciclo é 2025–2028, inteiramente
gerado pela carreira. Resultados do ano da edição não entram em sua classificação.
O título histórico de um seed não é registrado como conquista do treinador
da carreira, e não são inventados placares ou pontos de ranking de 2021.

### Ranking e sede: adaptações explícitas

O patch guarda resultados continentais antes da limpeza do calendário. Para
temporadas jogadas com ele, aplica pesos de vitória/empate e progressão de fase
aos formatos continentais simulados pelo Brasfoot: UEFA, 2/1 pontos, 4 pela fase
de grupos, 5 pela primeira eliminatória e 1 pelas seguintes; outras confederações,
3/1 pontos e 3 por fase alcançada. O coeficiente da carreira não reproduz os
pontos históricos reais publicados pela FIFA.

Registros agregados de saves anteriores permitem recuperar vitórias e empates,
mas não todos os detalhes das fases. Para eles, contam apenas os pontos dos
resultados disponíveis, sem bônus reconstruídos por suposição. Reputação e força
de elenco não completam vagas. Empates absolutos no ranking usam a recência do
título conhecido e depois o identificador estável do clube.

Quando não há histórico suficiente, o patch informa a confederação e a cota
incompleta. Não reduz o torneio nem usa clubes repetidos. O histórico do save
existente foi suficiente para selecionar 32 participantes em um teste em memória.

A sede padrão é EUA, inclusive nas edições seguintes. Sua vaga vai ao campeão
nacional anterior, se essa liga foi simulada e o clube ainda não se classificou;
caso contrário, ao melhor clube estadunidense ainda disponível no ranking
continental do ciclo. Essa adaptação está em `patches/competitions.json`; não
replica a decisão excepcional sobre a Supporters' Shield da edição real de 2025.

Fontes de referência:

- [FIFA — formato, periodicidade e metodologia de ranking](https://www.fifa.com/en/tournaments/mens/fifa-club-world-cup/articles/fifa-council-confirms-key-details-club-world-cup-2025).
- [FIFA — classificação para 2025](https://www.fifa.com/pt/tournaments/mens/club-world-cup/usa-2025/articles/classificacao-mundial-2025).

## Intercontinental anual a partir de 2024

O Mundial anual original permanece em 2022–2023. Em 2024, dá lugar à Copa
Intercontinental de Clubes. A partir de 2025, a Intercontinental anual e o
Mundial quadrienal coexistem, com históricos e taças distintos.

A Intercontinental recebe os seis campeões continentais da temporada da
carreira, sem vaga anfitriã e sem terceiro lugar. Os seeds históricos do Mundial
não classificam clubes para ela.

| Jogo | Participantes | Título |
|---|---|---|
| 1 | Campeão AFC ou CAF × campeão OFC | Classificação ao jogo 2 |
| 2 | Vencedor do jogo 1 × o outro campeão AFC/CAF | Copa África–Ásia–Pacífico |
| 3 | Campeão CONMEBOL × campeão Concacaf | Derby das Américas |
| 4 | Vencedores dos jogos 2 e 3 | Challenger Cup |
| 5 | Campeão UEFA × vencedor do jogo 4 | Copa Intercontinental |

AFC abre o caminho contra a OFC em 2024; CAF o abre em 2025, com alternância
anual. As quatro taças têm registros próprios e um clube pode conquistar mais
de uma na mesma edição. Ganhar a Challenger Cup ainda não torna o clube campeão
intercontinental: falta a final contra o campeão europeu.

Referências: [FIFA — edição de 2024](https://www.fifa.com/en/tournaments/mens/intercontinentalcup/2024),
[FIFA — sequência da edição de 2025](https://vod.fifa.com/organisation/news/intercontinental-cup-2025-dates-venues-first-match-ups-confirmed).

## Calendário e regras do motor

O jogo trabalha com uma categoria principal de partidas por data. O patch
reserva sete datas livres entre 14 de junho e 31 de julho, com pelo menos três
dias entre suas rodadas, para o Mundial. A Intercontinental recebe cinco datas
depois da última data continental do calendário original.

Somente as três antigas datas do Mundial anual são removidas a partir de 2024.
Partidas nacionais/continentais e eventos de salários não são apagados.
Isso evita jogos de duas competições na mesma data, mas não impõe uma pausa
global nas ligas nem garante 72 horas entre um jogo do Mundial e outra competição.
O calendário não reproduz exatamente as datas reais da FIFA.

Substituições, suspensões, lesões e elencos seguem o motor do Brasfoot. As
premiações usam a escala financeira do jogo, com entrada e avanço pagos no
saldo/balanço nativos. As colocações pontuam nos rankings de clubes e técnicos;
os valores ficam na aba “Premiações e ranking” e em
[premiacoes-ranking.md](premiacoes-ranking.md). Não foram implantadas as janelas
especiais de transferência/inscrição nem os valores reais da FIFA. O fair play usa os cartões dos
jogadores registrados pelo jogo; cartões de comissão técnica não são modelados.

## Taças e telas

| Título | PNG em `mod/assets/trophies/` |
|---|---|
| Mundial de Clubes | `club_world_cup.png` |
| Intercontinental | `intercontinental_cup.png` |
| África–Ásia–Pacífico | `africa_asia_pacific.png` |
| Derby das Américas | `derby_of_americas.png` |
| Challenger Cup | `challenger_cup.png` |

As fotografias JPEG usadas como referência foram removidas da raiz.
A validação e o build usam somente as versões PNG de
`mod/assets/trophies/`: o Mundial já tinha transparência; as outras quatro taças
receberam recortes com fundo transparente pela ferramenta integrada de imagens.
Os prompts e hashes estão em `mod/assets/trophies/provenance.json`.
A exibição mantém a proporção, remove margens transparentes e centraliza as
taças em quadros de 20 × 20 pixels nos seletores e 84 × 124 pixels nos cartões
nativos da galeria. O build exporta PNGs nesse último tamanho para
`runtime/competitions/game/trofeus/`, pois o renderizador HTML original não
redimensiona arquivos grandes. Esses PNGs também servem para
os componentes originais que procuram arquivos pelo nome retornado por `mA()`.

As duas telas de troféus dos clubes (`a.de` e `a.di`, esta última aberta por
`best.S.v`) agora distinguem as competições, suas quantidades e os anos das
conquistas. A tela original `a.di` agrupava todos os torneios de tipo 5 e
ignorava conquistas adicionais na mesma temporada. O agrupamento usa o índice
da competição além do tipo. A galeria `a.di` preserva seu layout, cabeçalho,
botão de fechar, cores e componentes `components.k`: taça à esquerda, anos à
direita e contador no canto inferior. A altura continua sendo escolhida pela
janela original a partir do número de cartões. Acima de 16 categorias, os
mesmos cartões ficam em uma área de rolagem. `a.de` mantém sua tabela e seu
renderizador originais, com as categorias distintas no modelo nativo.

As telas de competição têm abas de grupos, partidas, classificação ao Mundial
e títulos. A navegação e o botão de fechar originais permanecem disponíveis.
Os títulos do Mundial anual anterior ao patch aparecem como
“Mundial de Clubes (até 2023)”, com a imagem original,
sem serem renomeados retroativamente como Intercontinental.

## Implementação

O código está em `mod/src/mod/CompetitionMod.java`. `tools/PatchClasses.java`
acrescenta dezoito chamadas em dez classes originais, sem recompilar a saída do
descompilador: `best.f`, `best.a`, `best.at`, `a.al`, `a.de`, `a.di`, `f.G`,
`best.I`, `a.hc` e `b.aC`.
A simulação de partidas não foi substituída.

A classe `Intercontinental` estende o Mundial original e é gravada no mesmo
campo já existente da carreira. Ela contém o Mundial quadrienal, as taças
intermediárias, o histórico de classificação e as fases em andamento. Os campos
das outras classes do save permanecem com a estrutura original.

Não foram adicionados campos persistidos às classes do beta nesta atualização.
Ao carregar, `best.f.G()` reconcilia o histórico de campeões com os registros de
conquistas dos clubes e técnicos: repõe apenas o que falta, preserva temporada
e clube e não repete alterações de dinheiro ou reputação. A operação é
idempotente. Para usar em uma carreira do beta, salve, feche e abra novamente
pelo lançador do patch; não é necessário começar outra carreira.

`mod/src/mod/Rewards.java` integra os efeitos financeiros e os pesos próprios
de ranking, com recibos em registros isolados da confederação continental
(confederação −1). O hook de `f.G` evita o pagamento de dois prêmios pelo mesmo
avanço; fases originais fora do patch mantêm seus valores.

`NativeUi.java` discrimina as competições no modelo `b.I` e no renderizador
`b.aC` originais do ranking. O total da tabela à direita coincide com o total
do clube: a categoria agregada do Mundial é repartida pelas taças, mantendo
o histórico anterior a 2024 em sua própria linha. Os jogos usam o estádio do
mandante e o cálculo original de público/preços de `best.v.b(I)`. A renda de
bilheteria é creditada pelo pós-jogo na categoria 5, distinta da premiação
(categoria 3), com recibo persistido para impedir duplicações.

As competições usam índices de histórico distintos: Mundial 70, Intercontinental
71, África–Ásia–Pacífico 72, Derby 73 e Challenger 74. Seus jogos usam a categoria
de partidas internacionais de clubes já existente no motor.

O ano civil da temporada é `best.f.op() + best.f.H() - 1`; `H()` é o número
da temporada da carreira, não o ano civil. O histórico dos campeões continentais
vem de `best.at.cv(int)`, com partidas em `f.s`/`f.z` e estatísticas em `best.aj`.

## Validação e limites deste beta

`mod/test/mod/CompetitionIntegrationTest.java` verifica seleção, cotas, exceção
de múltiplos campeões por país, vaga anfitriã sem duplicação, sorteio, criação das
63 partidas, avanço das fases e campeão no histórico nativo. Também cobre:

- Intercontinental em 2024/2025, alternância AFC/CAF e quatro taças em cinco jogos.
- Saves durante grupos, mata-mata e Intercontinental, com referência aos clubes
  preservada e títulos restaurados.
- Arquivamento dos títulos antigos e virada nativa de 2024 para 2025.
- Classificação em 2029 sem seeds de 2021 nem resultados do ano da edição.
- Confronto direto reaplicado, fair play, pesos de ranking e vencedor por pênaltis.
- Dispatcher das telas originais e duas taças distintas, com os respectivos
  anos, nas duas galerias do clube; recuperação idempotente de conquista ausente.
- Transparência, proporção e tamanho dos cinco ícones; acesso das telas
  originais aos PNG; nome e imagem do Mundial anterior em saves do beta.
- Entrada de todos os participantes, prêmios de avanço no saldo e no balanço,
  rankings nativos de clubes/técnicos e recuperação de participação em andamento.
- Recibos preservados no save sem pagamento ou pontuação em duplicidade;
  bônus nativo antigo descontado ao migrar os pesos de colocação.
- Público e renda nas cinco taças, crédito no saldo e no balanço de renda,
  recibo após salvar/carregar e público/renda na tela nativa de resultados.
- Ranking nativo com cada taça nomeada, pontos corridos e por colocação
  discriminados, mantendo a soma do total original.
- Galeria com layout/cartões nativos, contadores e dimensão original da janela.
- Leitura do save original com 668 clubes somente em memória, recuperação de
  seu histórico e simulação de uma partida com elencos reais pelo motor original.
  O hash do save original é conferido depois do teste.

Os testes de avanço usam placares controlados; o teste com elencos reais usa
o simulador original. Eles não equivalem a ter jogado manualmente uma carreira
completa até 2029. O áudio não foi validado.

O resultado está em `analysis/mod-tests.log`. As telas renderizadas para
inspeção estão em `analysis/mod-world-panel.png` e `analysis/mod-trophies-panel.png`.
O segundo exemplo usa clubes sintéticos para testar duas taças na mesma edição.

Não há importação automática de carreiras originais nem conversão retroativa
de calendários já em andamento. A leitura de um save antigo foi validada em
memória; para este beta, comece uma nova carreira em 2022.
Saves do patch contêm novas classes e devem ser abertos com o JAR modificado.

O JAR original mantém SHA-256
`7028a8b5f08c02fb10690fdb8c1cc7e2ec4b14f5b3aafe22db78626110794aeb`.
O hash do JAR modificado e as classes alteradas estão em `analysis/mod-build.json`.

## Arquivos de configuração e análise

- `patches/competitions.json`: transição, periodicidade, cotas, sede e chaveamento.
- `patches/world-cup-2025-seeds.json`: cinco classificados históricos e fontes.
- `patches/trophies.json`: associação das imagens, dimensões e hashes.
- `analysis/decompiled/`: reconstruções de classes para investigação; não são o
  código-fonte original e não foram recompiladas para aplicar o patch.
- `analysis/competition-strings.json`, `analysis/competition-signatures.txt` e
  `analysis/world-cup-bytecode.txt`: evidências estáticas do mapeamento.
- CFR 0.152 foi usado para análise, com SHA-256
  `f686e8f3ded377d7bc87d216a90e9e9512df4156e75b06c655a16648ae8765b2`.
  [Documentação do autor](https://www.benf.org/other/cfr/).
