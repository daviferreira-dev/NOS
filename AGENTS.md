# AGENTS.md — Projeto NÓS (libGDX)

Este é **NÓS**, um jogo 2D em libGDX: puzzle de fluxo, poético e filosófico. O jogador controla o tempo de dois mundos sobrepostos em aquarela. O mundo observado desacelera; o outro acelera e desbota.

**Leia `GDD.md` antes de qualquer tarefa.** Ele é a fonte da verdade do design.

## Idioma
- Converse comigo em **português**.
- Código (classes, métodos, variáveis) em **inglês**.
- Comentários e textos do jogo em **português**.
- Textos do jogo (aforismos, versos, UI) ficam em arquivos de dados, **nunca** fixos no código.

## Marco atual
**M1 em validação de jogabilidade; primeira fatia de Água jogável em construção.** Não considerar M1 divertido/aprovado até teste do usuário.

**Escopo ampliado pelo usuário nesta iteração:** percurso contínuo com câmera acompanhando a dupla, ambiente variado, personagens ilustrados, tipografia revisada e abertura curta pulável. Essa autorização antecipa apresentação e um recorte de M2 para testar a experiência completa. Não implica concluir os 13 níveis ou implementar M4/M5 inteiros. Os limites abaixo descrevem o plano original do M1; a ampliação acima prevalece nos itens já autorizados.

**No M1, faça apenas:**
- duas trilhas (`CatmullRomSpline`) e dois heróis andando sozinhos;
- `GazeTimeSystem` (olhar → tempo), `FadeSystem` e `HarmonySystem`;
- formas simples com `ShapeRenderer`;
- tecla de debug para alternar a leitura do olhar entre proximidade e posição horizontal;
- um HUD de debug com velocidades, tinta e harmonia.

**No M1, ainda NÃO faça:** shaders, aquarela, áudio, narrativa, quarta parede, galeria, pincéis ou telas além do mínimo para testar.

## Estrutura de pacotes
Veja a seção 12 do `GDD.md`.
- Pacote base: `com.nos` (ajuste se o gdx-liftoff gerou outro).
- `screens/`, `world/`, `entities/`, `systems/`, `input/`, `render/`, `audio/` e `data/`.

## Regras de arquitetura
- **Uma responsabilidade por classe.** Telas (`Screen`) orquestram; **não** contêm regra de jogo.
- **Sistemas não dependem de renderização.** A lógica em `systems/` e `world/` deve poder ser testada com JUnit, sem contexto OpenGL.
- **Cada `World` tem relógio próprio:** `worldDelta = delta * speed`. Nunca use o `delta` global dentro da lógica de um mundo.
- **A regra do espelho fica centralizada em `MirrorPair`.** A soma das velocidades é constante.
- **Balanceamento em dados:** números ficam em `Tuning` ou em JSON, nada espalhado pelo código.
- **Fases em JSON**, carregadas por `LevelLoader`.
- **Recursos** carregados via `AssetManager` e sempre liberados com `dispose()`.

## Compatibilidade (Desktop, Android, Web/TeaVM)
- Todo o código de jogo fica no módulo `core`.
- Prefira APIs do libGDX: `Gdx.files`, `Preferences`, `Json` e `Gdx.input`.
- Evite `java.io.File`, threads e reflexão.
- Entrada via `InputProcessor` (`BrushInput`), tratando mouse e toque da mesma forma.
- Salvamento **somente** via `Preferences` (`SaveData`).
- Áudio: `Music` não muda o tom. Para faixas que aceleram, use `Sound` com `setPitch` ou crossfade entre versões pré-gravadas (veja o `GDD.md`, seção 10).

## Regras de tom (quando chegar ao M5)
- Nunca explicar a história.
- Nunca ameaçar o jogador.
- Nunca fingir erro técnico (crash falso, save apagado).
- Não ler o nome de usuário do sistema. Use só dados de jogo e a hora do sistema.
- Ruído e estática precisam de opção para reduzir ou desligar, com aviso de fotossensibilidade.

## Fluxo de trabalho
- Antes de mudanças grandes ou de começar um marco, **proponha um plano curto** e espere meu ok.
- Trabalhe em passos pequenos, cada um compilando e rodando.
- Ao concluir uma tarefa, rode `./gradlew core:test` e diga como testar manualmente (comando e o que observar).
- Se uma decisão de design mudar, **atualize o `GDD.md`** e marque o item em "Decisões em aberto".
- Escreva testes JUnit para a lógica de `systems/` (balance, desbotar, harmonia, encontros, pontuação).
- Não adicione dependências novas sem me perguntar.

## Comandos (projeto gerado pelo gdx-liftoff)
Confira os nomes das tarefas no projeto gerado.
- Desktop: `./gradlew lwjgl3:run`
- Testes: `./gradlew core:test`
- Compilar tudo: `./gradlew build`
- Android: `./gradlew android:installDebug`
- Web (TeaVM): veja a tarefa gerada no módulo `teavm`.
