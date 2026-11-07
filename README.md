# NÓS

Um puzzle de exploração sobre duas presenças e o vínculo entre elas, em libGDX.

## Experiência atual: ponte e contrapesos no jardim

Você controla somente Sombra. Reflexo tem o mesmo rosto, mas seu próprio ritmo: observa a água, reage e coopera quando você pede ajuda. A primeira fase inclui a ponte e um desafio de posicionamento no pátio seguinte. A relação e a diversão continuam em validação. As versões anteriores permanecem nos executáveis antigos.

- WASD/setas: mover Sombra.
- E: interagir com o objeto próximo; pedir ajuda, fixar a escora ou segurar/soltar a jardineira.
- Espaço/Esc: pausa. R: reiniciar o trecho; no pátio, a ponte concluída continua fixa.
- H: abrir/fechar ajuda com o jogo pausado. M: ligar/desligar música.
- F6: tamanho do texto. F7 no menu de pausa: configurar teclas. F8 na configuração: restaurar padrão. F11: tela cheia/janela.
- Reflexo não pode ser selecionado por Tab/Q. Não há dano, tinta ou cronômetro neste encontro.

Perto do guincho da primeira margem, peça ajuda e dê espaço. Reflexo caminha até o mecanismo e só então sustenta a ponte. Atravesse com Sombra e fixe a escora na outra margem; ele se solta e vem ao seu encontro pela ponte. Perto do guincho, uma nova interação chama Reflexo de volta e pode recolher a ponte enquanto não estiver fixa. Quem ficar sem piso retorna à última margem segura.

No pátio, os dois pedais liberam a grade. Peça ajuda no menor e posicione a jardineira sobre o mais largo. E segura/solta a peça; WASD permite empurrar e puxar. A grade fica travada aberta e Reflexo se reúne pelo caminho real. Ambos precisam atravessá-la para concluir. Não há prazo nem punição por reposicionar o peso. Os cabos mostram a ligação com a grade; se Reflexo livre bloqueia a peça, ele pode sair da frente por uma rota segura. Um bloqueio persistente dá uma dica curta para reposicionar.

O jogo guarda o checkpoint ao fixar a ponte e a conclusão do trecho. Ao reabrir, o menu permite continuar ou revisitar o pátio. N / Jogar do começo reinicia a sessão sem apagar o avanço mais distante. A abertura nunca inicia a partida sozinha. Testes e inspeções usam progresso em memória para preservar o save real.
Depois de concluir, Enter/clique volta ao menu e R repete o pátio. Retomar já enquadra o trecho correto, sem atravessar a cena com a câmera.

A câmera acompanha Sombra, identificada por um marcador por forma e pelo contato dos pés. Há quatro quadros de caminhada, poses próprias de repouso e sustentação, registrados nos pés e em altura comum. Reflexo contorna obstáculos e procura a ponte aberta para reunir-se a você. A tecla de interação atual aparece sobre o mecanismo próximo. Ainda não há animação completa em oito direções.

A ajuda e a configuração são modais: escurecem o cenário, ocultam o HUD e pausam toda a simulação. O objetivo expande quando muda e depois encolhe para um resumo; "Etapa N de 5" indica progresso do trecho atual. Teclas e tamanho do texto são salvos nas preferências.

A abertura usa enquadramentos ilustrados. Terminar ou pular leva ao menu; somente Começar ou um novo Enter inicia a partida. R no menu revê a abertura; M liga/desliga a música e preserva sua escolha. O pátio recebe um terceiro tema, com 112 segundos de piano/harpa amostrados, crossfade e volume máximo de 20%. Não há narração sintética. Notas, prompts e licenças em `design/OPENING-AND-MUSIC.md` e `design/GARDEN-ARTS.md`.

## Jogar

Dê dois cliques em `outputs/Jogar-NOS-Demo-v6.bat`, mantendo `NOS-Demo-v6.jar` ao lado. O iniciador usa o JDK 17 já instalado neste computador. O executável empacotado não requer Gradle nem downloads. A v5 continua preservada.

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

Exemplo: `java -jar outputs/NOS-Demo-v6.jar --mirror-room`.

## Organização

- `BridgeEncounter`: estado e regras do encontro, sem OpenGL.
- `BridgeCollisionSystem`: margens, água, obstáculos e ponte.
- `CompanionFollowSystem`: seguimento com busca de desvios caminháveis.
- `CompanionBehaviorSystem`: percepção, reação, pedido de ajuda, sustentação e reencontro de Reflexo.
- `GardenGateSystem`: jardineira, pedais, ajuda física e trava da grade.
- `CompanionClearanceSystem`: desvio físico de Reflexo livre para ceder passagem à peça.
- `SaveData`: recorde legado e checkpoints monotônicos via Preferences.
- `GardenData` / `GardenTuning`: planta opcional e ajustes dos contrapesos.
- `BridgeKeyBindings`: teclas remapeáveis e aliases.
- `BridgeInput`: teclado e botões de toque.
- `BridgeScreen`: orquestração em passos fixos.
- `BridgeRenderer`: apresentação e câmera.
- `BridgeUiState` / `BridgeHudRenderer`: objetivo, avisos, identificação, leitura e menus modais; tempos de apresentação separados da simulação.
- `levels/bridge-encounter.json`: planta do encontro.
- `BridgeTuning`: movimento, distâncias e tempos de resposta.

Os testes JUnit verificam os protótipos preservados e o percurso completo da ponte ao jardim, controlando só Sombra. Incluem chegada física, espera por espaço, arrastar/puxar, peso insuficiente de pessoas, bloqueio da caixa por Reflexo, pausa, independência de frames e abertura permanente. `--smoke-test CAMINHO_ABSOLUTO --garden|--garden-grab|--garden-open|--garden-large` inspeciona situações do pátio sem janela visível. Os cenários anteriores continuam disponíveis. `--audio-check` verifica as três faixas com volume zero. Essas verificações não substituem jogar e ouvir a música.

## Arte e direção

Personagens e logo foram aprovados pelo usuário. Cenário, ponte, mecanismos e poses foram criados com geração de imagens e integrados ao jogo; referências originais em `design/`. Tipografia Cormorant Garamond e Atkinson Hyperlegible, com licenças OFL em `assets/ui/`. Notas e prompts em `design/BRIDGE-ARTS.md` e `design/SPRITE-REFINEMENT.md`, também entregues em `outputs/`; gerador dos atlas em `tools/`.

O GDD registra a revisão ativa e preserva as concepções anteriores como histórico. Os 13 níveis e capítulos da concepção original não estão implementados.
