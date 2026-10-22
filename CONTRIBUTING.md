# Como contribuir

Siga a preparação descrita no [README](README.md), usando seu próprio
instalador compatível e JDK 21. Faça mudanças no código em `mod/src/mod/`, nas
regras em `patches/` ou nas ferramentas em `tools/`.

Antes de enviar uma alteração que afete o comportamento do mod, execute:

```sh
python3 tools/build_mod.py
python3 tools/test_mod.py --without-save
```

Quando necessário, complemente com `--save /caminho/para/copia-local.s22` e
validação manual na interface. Descreva o comportamento anterior, o resultado
esperado e os cenários verificados.

Mantenha instaladores, JARs, classes extraídas, saves, preferências e relatórios
locais fora dos commits. Não é necessário enviar uma cópia do jogo para
contribuir com código. Novos materiais de terceiros devem ter sua origem e
condições de uso identificadas separadamente da licença GPL do projeto.

Para reportar um erro, abra uma issue com a distribuição Linux, as versões de
Java e Python, os passos para reproduzir e a mensagem de erro. Em problemas
com competições, inclua o ano da carreira, os clubes e a fase afetada. Remova
dados pessoais de logs e capturas antes de compartilhar.
