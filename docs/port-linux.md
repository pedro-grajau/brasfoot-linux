# Port para Linux

## Aplicativo recuperado

O instalador NSIS do Brasfoot 2022–2023 contém `bf22-23.exe`, um lançador
Launch4j com um arquivo Java incorporado. A interface usa Java/Swing e pode
rodar diretamente em uma sessão gráfica Linux.

`tools/prepare_linux.py` usa 7-Zip para extrair o instalador e normaliza o ZIP
incorporado em um JAR. A preparação verifica nomes, CRCs e conteúdo das
entradas: a normalização do contêiner não altera os bytes das classes originais.
O JAR contém 2.193 entradas, das quais 1.038 são classes, com bytecode Java 7.
A entrada é `org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader`.

Referências de integridade da edição utilizada:

| Artefato | SHA-256 |
| --- | --- |
| Instalador `brasfoot22-23.exe` | `8e3c7a77768d551a1f3c722ef2447c3e451af830613e8a65de33f35bc0e2b33d` |
| Lançador `bf22-23.exe` | `2fa65476a92611419ce28c0cfd158a0c7f741af235aa988b0bed00c44443bb1c` |
| JAR normalizado | `7028a8b5f08c02fb10690fdb8c1cc7e2ec4b14f5b3aafe22db78626110794aeb` |

Os relatórios de integridade e inventário são gerados em `analysis/` após a
preparação. Esses arquivos ficam locais.

## Compilação e execução

Use o JDK 21. O código de execução do mod é compilado com `--release 11`, mas
a ferramenta que aplica os pontos de integração utiliza a implementação ASM
interna do JDK 21. Uma versão diferente não faz parte do ambiente validado.

O build verifica o hash do JAR original antes de modificar classes. Depois,
compila as regras JSON, adiciona as classes do mod e gera outro JAR, mantendo
o original. Bibliotecas como Kryo, MinLog, Objenesis e ReflectASM são extraídas
do próprio jogo; o build não baixa dependências externas.

Os lançadores configuram os acessos a módulos necessários ao aplicativo e à
serialização Kryo no Java 21, incluindo `sun.java2d`, `java.util`, `java.lang`
e `java.awt`. O lançador do mod também isola as preferências e o diretório de
trabalho. Use os scripts em vez de iniciar o JAR manualmente sem esses argumentos.

Não é necessário compilar uma reconstrução de fontes decompiladas. O motor
original continua sendo executado; o repositório contém apenas os acréscimos
e ferramentas do projeto.

## Solução de problemas

| Sintoma | Verificação |
| --- | --- |
| `7z`/`7zz` não encontrado | Instale o pacote 7-Zip da sua distribuição; no Mint 22, `p7zip-full`. |
| Instalador não encontrado | Use `--installer /caminho/brasfoot22-23.exe`. |
| `javac` ausente | Instale o JDK completo, não somente o JRE. |
| Versão Java incompatível | Confira `java -version` e `javac -version`; ambos devem usar 21. |
| JAR de entrada incompatível | Confira a edição e o hash acima. O patch não é genérico para outras versões. |
| Erro de reflexão/acesso a módulos | Inicie com `./run-brasfoot-mod.sh` ou `./run-brasfoot.sh`. |
| Erro `HeadlessException` ou ausência de display | Inicie o jogo dentro de uma sessão gráfica Linux. Os testes, por sua vez, podem rodar sem interface. |
| Novas competições ausentes | Confira o lançador usado e se a carreira possui competições internacionais; acompanhe o ano de estreia de cada torneio. |
| Save não aparece | Confira a pasta escolhida nas opções. A instalação do mod possui pasta de trabalho e preferências próprias. |

Ao reportar um problema, informe distribuição, versões de Java/Python, comando
executado, mensagem de erro e, para problemas de carreira, ano e fase do torneio.
