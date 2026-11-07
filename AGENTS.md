# AGENTS.md — Projeto NÓS (libGDX)

Este é **NÓS**, um jogo 2D em libGDX, poético e filosófico. O jogador controla somente Sombra; Reflexo é uma pessoa autônoma com o mesmo rosto, seu próprio ritmo e uma reação à água. Na ponte, Sombra pede ajuda no guincho, Reflexo caminha até ele e sustenta a passagem; Sombra atravessa e fixa a escora. Depois Reflexo se reúne a Sombra pelo caminho real. Não há troca de controle por Tab/Q. A arte em aquarela e a identidade da dupla permanecem. As experiências espelhada e de controle do tempo ficam preservadas.

**Leia `GDD.md` antes de qualquer tarefa.** Ele é a fonte da verdade do design.

## Idioma
- Converse comigo em **português**.
- Código (classes, métodos, variáveis) em **inglês**.
- Comentários e textos do jogo em **português**.
- Textos do jogo (aforismos, versos, UI) ficam em arquivos de dados, **nunca** fixos no código.

## Marco atual

**Refinamento v6 autorizado na continuação:** priorizar a demo curta da primeira fase. `SaveData` guarda pátio alcançado/conclusão via `Preferences`, sem regredir checkpoint em replay; menu oferece continuar/revisitar ou jogar do começo, sempre com novo comando explícito após o filme. Testes/smokes usam memória. `CompanionClearanceSystem` faz Reflexo livre ceder espaço à jardineira somente por rota física segura; nunca abandonar o pedal para isso. Avisos de bloqueio têm intervalo e a pausa congela todo o movimento. Cabos dos pedais ficam no JSON e a apresentação em `GardenMechanismRenderer`. Ainda não considerar a primeira fase divertida/aprovada nem as treze fases concluídas.

**Ampliação autorizada em 07/11/2026:** depois de rejeitar o piloto CGI, o usuário pediu continuar o jogo com decisões de implementação automáticas e trabalho paralelo para acelerar. O primeiro trecho agora inclui os contrapesos no pátio após a ponte: Sombra move uma jardineira com a ação contextual e WASD; Reflexo pode ser chamado ao pedal menor e chega caminhando. O pedal largo exige a jardineira. Os dois pesos abrem uma grade com trava permanente; depois Reflexo acompanha pelo piso real. Sem controle de Reflexo, Tab/Q, dano ou pressão de tempo. `GardenGateSystem` contém as regras; `GardenData` opcional no JSON e `GardenTuning` contêm planta e ajustes. Sem esses dados, a ponte original continua com três etapas. `R` reinicia o pátio quando ele já foi alcançado na sessão. A diversão continua em validação, não considerar M1/M2 concluídos. O piloto CGI foi rejeitado, arquivado fora do projeto e não deve ser integrado nem retomado. Narração adiada.
**M1 em validação: Sombra controlável e Reflexo autônomo no encontro da ponte.** O usuário aprovou testar duas pessoas parecidas, com comportamentos diferentes. Não considerar divertido/aprovado até teste do usuário. Sem pressão de tempo, dano ou gestão de barras.

**Plano atual, que prevalece sobre os planos antigos abaixo:** `BridgeEncounter` mantém Sombra no índice 0 e Reflexo no índice 1; somente Sombra recebe movimento. `CompanionBehaviorSystem` decide observar, acompanhar, ir ao guincho, sustentar e reunir-se; `CompanionFollowSystem` faz a navegação pela mesma colisão do jogador. Pedir ajuda não teletransporta nem muda o controle. Reflexo espera espaço se o jogador ocupa o guincho; somente a chegada física começa a abrir a ponte. O jogador fixa a escora. `BridgeKeyBindings` traduz teclas remapeáveis; `BridgeInput` usa movimento e uma ação contextual. `BridgeScreen` orquestra e pausa a simulação durante ajuda/configuração; `BridgeRenderer`/`BridgeHudRenderer` apresentam. Geometria em `levels/bridge-encounter.json`, ajustes em `BridgeTuning`. Câmera acompanha Sombra. Não retomar troca por Tab, selos coloridos ou movimento automático de Sombra sem nova decisão. `--mirror-room` e `--legacy` preservam as experiências antigas.

**Plano anterior preservado na sala espelhada:** movimento WASD/setas com espelho, Q/E para atar/soltar, dois selos e flores. Regra em `MirrorMovementSystem`, vínculo em `AnchorSystem`, estado em `MirrorPuzzle`, geometria em `levels/knot-room.json`. Isso não rege a ponte atual. Controle por gamepad ainda não implementado; não adicionar dependências sem perguntar.

**Escopo ampliado pelo usuário nesta iteração:** percurso contínuo com câmera acompanhando a dupla, ambiente variado, personagens ilustrados, tipografia revisada e abertura curta pulável. Essa autorização antecipa apresentação e um recorte de M2 para testar a experiência completa. Não implica concluir os 13 níveis ou implementar M4/M5 inteiros. Os limites abaixo descrevem o plano original do M1; a ampliação acima prevalece nos itens já autorizados.

**Revisão de apresentação pedida em 07/11/2026:** cutscene ilustrada mais elaborada, música original e menu que espera um comando explícito para começar. Fim ou pulo do filme nunca inicia a partida. Depois disso, refinar hierarquia das mensagens e uso do espaço da interface do encontro. Áudio de abertura/travessia e esses ajustes de apresentação estão autorizados e prevalecem sobre a restrição histórica de áudio do M1 abaixo.

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
