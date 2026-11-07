# NÓS

Um puzzle de exploração sobre duas presenças e o vínculo entre elas, em libGDX.

## Experiência atual: Sombra e Reflexo na ponte

Você controla somente Sombra. Reflexo tem o mesmo rosto, mas seu próprio ritmo: observa a água, reage e coopera quando você pede ajuda. O recorte continua sendo a ponte, para validar essa relação antes de ampliar o jogo. As versões anteriores com troca de personagem permanecem preservadas nos executáveis antigos.

- WASD/setas: mover Sombra.
- E: pedir ajuda no guincho, chamar Reflexo de volta ou fixar a escora próxima.
- Espaço/Esc: pausa. R: reiniciar.
- H: abrir/fechar ajuda com o jogo pausado. M: ligar/desligar música.
- F6: tamanho do texto. F7 no menu de pausa: configurar teclas. F8 na configuração: restaurar padrão. F11: tela cheia/janela.
- Reflexo não pode ser selecionado por Tab/Q. Não há dano, tinta ou cronômetro neste encontro.

Perto do guincho da primeira margem, peça ajuda e dê espaço. Reflexo caminha até o mecanismo e só então sustenta a ponte. Atravesse com Sombra e fixe a escora na outra margem; ele se solta e vem ao seu encontro pela ponte. A vitória exige ambos no jardim. Perto do guincho, uma nova interação chama Reflexo de volta e pode recolher a ponte enquanto não estiver fixa. Quem ficar sem piso retorna à última margem segura.

A câmera acompanha Sombra, identificada por um marcador por forma e pelo contato dos pés. Há quatro quadros de caminhada, poses próprias de repouso e sustentação, registrados nos pés e em altura comum. Reflexo contorna obstáculos e procura a ponte aberta para reunir-se a você. A tecla de interação atual aparece sobre o mecanismo próximo. Ainda não há animação completa em oito direções.

A ajuda e a configuração são modais: escurecem o cenário, ocultam o HUD e pausam toda a simulação. O objetivo expande quando muda e depois encolhe para um resumo; "Etapa N de 3" indica progresso do encontro. Textos, keycaps e painéis usam uma hierarquia comum. Teclas e tamanho do texto são salvos nas preferências.

A abertura usa três enquadramentos ilustrados e dois temas musicais originais. Terminar ou pular leva ao menu, que espera sem limite de tempo. Somente Começar ou um novo Enter inicia a partida. R no menu revê a abertura; M liga/desliga a música e preserva sua escolha. Volume máximo da música em 20%, conforme pedido. Notas de composição, prompts e limites em `design/OPENING-AND-MUSIC.md`.

## Jogar

Dê dois cliques em `outputs/Jogar-NOS-Ponte-v4.bat`, mantendo `NOS-Ponte-v4.jar` ao lado. O iniciador usa o JDK 17 já instalado neste computador. O executável empacotado não requer Gradle nem downloads. As versões anteriores permanecem preservadas.

## Desenvolvimento

Requer JDK 17 ou superior. Na raiz:

```powershell
$env:JAVA_HOME = "C:\Users\PC Gamer\Documents\Codex\jdks\jdk-17.0.21+9"
.\gradlew.bat lwjgl3:run
.\gradlew.bat core:test
.\gradlew.bat lwjgl3:jar teavm:classes
```

Para usar o cache já existente neste computador:

```powershell
$env:GRADLE_USER_HOME = "C:\Users\PC Gamer\Documents\Codex\2026-11-06\instale-o-jdk-17-por-exemplo\work\gradle-user-home"
```

## Versões preservadas

- `--mirror-room`: sala experimental de movimento espelhado e selos.
- `--legacy`: travessia de tempo, tinta e harmonia.
- A pasta anterior em Documents/Codex não foi alterada. O histórico Git foi importado para esta pasta, incluindo os commits locais.

Exemplo: `java -jar outputs/NOS-Ponte-v4.jar --mirror-room`.

## Organização

- `BridgeEncounter`: estado e regras do encontro, sem OpenGL.
- `BridgeCollisionSystem`: margens, água, obstáculos e ponte.
- `CompanionFollowSystem`: seguimento com busca de desvios caminháveis.
- `CompanionBehaviorSystem`: percepção, reação, pedido de ajuda, sustentação e reencontro de Reflexo.
- `BridgeKeyBindings`: teclas remapeáveis e aliases.
- `BridgeInput`: teclado e botões de toque.
- `BridgeScreen`: orquestração em passos fixos.
- `BridgeRenderer`: apresentação e câmera.
- `BridgeUiState` / `BridgeHudRenderer`: objetivo, avisos, identificação, leitura e menus modais; tempos de apresentação separados da simulação.
- `levels/bridge-encounter.json`: planta do encontro.
- `BridgeTuning`: movimento, distâncias e tempos de resposta.

Os testes JUnit verificam os protótipos preservados e a solução completa da ponte controlando só Sombra, além de reação, observação e navegação de Reflexo, chegada física ao guincho, espaço ocupado, cancelamento, reencontro, pausa, remapeamento, apresentação dos objetivos e início explícito após a cutscene. `--smoke-test CAMINHO_ABSOLUTO` captura entrada, jogo e pausa numa janela invisível e fecha sozinho; os argumentos extras `--help`, `--settings`, `--large`, `--compact` e `--fixed` inspecionam situações específicas. `--intro-smoke CAMINHO_ABSOLUTO natural|reflection|wide|start` inspeciona a montagem e o fluxo. `--audio-check` verifica as duas faixas no dispositivo real com volume zero e janela invisível. Essas verificações não substituem ouvir a música e jogar.

## Arte e direção

Personagens e logo foram aprovados pelo usuário. Cenário, ponte, mecanismos e poses foram criados com geração de imagens e integrados ao jogo; referências originais em `design/`. Tipografia Cormorant Garamond e Atkinson Hyperlegible, com licenças OFL em `assets/ui/`. Notas e prompts em `design/BRIDGE-ARTS.md` e `design/SPRITE-REFINEMENT.md`, também entregues em `outputs/`; gerador dos atlas em `tools/`.

O GDD registra a revisão ativa e preserva as concepções anteriores como histórico. Os 13 níveis e capítulos da concepção original não estão implementados.
