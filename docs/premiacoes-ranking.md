# Premiações e ranking das novas competições

Os valores são da economia do Brasfoot. Não reproduzem os valores financeiros
reais da FIFA. A configuração fica em `patches/rewards.json`; o menu da
competição tem uma aba **Premiações e ranking** com os mesmos valores.

## Dinheiro

### Renda de estádio

Os jogos das cinco taças recebem o estádio do mandante. O próprio motor calcula
o público por setor e os preços dos ingressos, usando a capacidade do estádio,
a reputação dos clubes e os demais fatores normais do Brasfoot. A tela original
de resultados mostra o nome do estádio, o público e a renda, em vez de “n/i”.

O pós-jogo credita a bilheteria no saldo do mandante controlado pelo jogador,
como nas competições nativas. Usa a categoria financeira **renda**, separada
de **premiações**. O visitante não recebe a renda do mandante. Clubes da CPU
têm público e renda calculados/exibidos, preservando a política financeira
original do jogo para seus saldos.

O construtor e a simulação recuperam o estádio em partidas pendentes de saves
do beta. Jogos antigos já disputados sem público registrado não recebem
valores inventados de bilheteria. O mando é uma adaptação ao Brasfoot: não
reproduz os estádios neutros da sede real da FIFA.

### Participação e avanço

Todos os participantes, incluindo clubes controlados pelo computador, recebem:

| Evento | Crédito |
|---|---:|
| Participação no Mundial | 2 milhões |
| Vitória nas oitavas do Mundial | 2 milhões |
| Vitória nas quartas do Mundial | 2 milhões |
| Vitória na semifinal do Mundial | 5 milhões |
| Título do Mundial | 7 milhões |
| Participação na Intercontinental | 500 mil |
| Vitória no play-off África–Ásia–Pacífico | 500 mil |
| Título África–Ásia–Pacífico | 1 milhão |
| Título Derby das Américas | 1 milhão |
| Título Challenger Cup | 2 milhões |
| Título Intercontinental | 7 milhões |

Os prêmios se acumulam: o campeão do Mundial recebe 18 milhões no total.
A entrada na Intercontinental é paga uma vez para cada um dos seis clubes;
as taças intermediárias não geram outras taxas de participação.
Não existe bônus financeiro por vitória ou empate nos grupos.

O crédito usa `best.ah.v(valor, 3)`, que alimenta o saldo e a categoria
**premiações** do balanço nativo. O prêmio automático de `f.G` é desativado
somente para fases do patch, pois o novo caminho paga e registra esse efeito
uma única vez. Todas as outras competições mantêm o comportamento original.

## Pontuação

As partidas continuam no ranking corrido original: 3 pontos por vitória e
1 por empate para os clubes. O ranking dos técnicos conserva os efeitos
originais das partidas. A colocação final acrescenta:

| Competição / colocação | Clube | Técnico |
|---|---:|---:|
| Mundial: campeão / vice | 25 / 15 | 40 / 20 |
| Mundial: 3º–4º / 5º–8º | 8 / 4 | 10 / 5 |
| Mundial: 9º–16º / 17º–32º | 2 / 1 | 3 / 1 |
| Intercontinental: campeão / vice | 10 / 5 | 25 / 5 |
| Intercontinental: 3º–4º / 5º–6º | 3 / 1 | 3 / 1 |
| África–Ásia–Pacífico: campeão / vice | 3 / 1 | 10 / 3 |
| Derby das Américas: campeão / vice | 3 / 1 | 10 / 3 |
| Challenger Cup: campeão / vice | 3 / 1 | 10 / 3 |

O terceiro participante da África–Ásia–Pacífico não ganha pontos por colocação
nessa taça; continua com os pontos das partidas e da Intercontinental.
Ganhar duas taças no mesmo ano soma suas pontuações. As colocações são
registradas quando cada competição termina. Os pontos aparecem no ranking
original do jogo. Na tabela à direita, cada taça aparece pelo próprio nome,
com seus pontos por colocação ou pontos corridos e gols. O Mundial anterior
a 2024 fica separado. A soma das linhas mantém o total original do clube;
a mudança de apresentação não acrescenta pontos novamente.

O bônus genérico que o Brasfoot aplicava a qualquer taça de tipo 5 é descontado
antes de aplicar o peso próprio da competição: isso evita somar o bônus antigo
e o novo. Troféus, estatísticas e efeitos nativos de reputação continuam pelo
caminho original. O coeficiente continental que seleciona os classificados do
Mundial permanece separado: estas taças não geram vagas ou pontos continentais.

## Carreiras já iniciadas

Salve e reabra a carreira pelo lançador do patch. Para edições ainda em
andamento, o carregamento recupera a participação não paga. Etapas encerradas
não recebem dinheiro retroativamente: versões anteriores já podiam pagar um
bônus nativo de avanço, sem guardar recibo individual dessa transferência.

O ranking das edições concluídas é ajustado a partir do histórico de campeões
e das colocações salvas, descontando os pontos nativos identificáveis. Para
técnicos de temporadas anteriores, só há recuperação quando o histórico
identifica o técnico que ganhou a colocação; não são atribuídos pontos a um
substituto contratado posteriormente.

Cada crédito e ajuste tem recibo persistido na estrutura já existente do save
(`Intercontinental.history`, registros `SeasonScore` com confederação −1 e
origem `reward:v1:`). Não foram adicionados campos às classes distribuídas nas
versões anteriores do beta. Esses registros não entram no coeficiente
continental. Reabrir a carreira, abrir a galeria ou repetir uma chamada de
finalização não repete dinheiro, pontos ou troféus.
