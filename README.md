# NÓS

Um puzzle de exploração sobre duas presenças e o vínculo entre elas, em libGDX.

## Experiência atual: o encontro da ponte

Esta é a nova experiência curta, criada após estudar princípios de Unravel Two, COCOON e GRIS. Um sustenta a passagem, o outro atravessa e fixa a escora. Depois os dois podem seguir ao jardim. O objetivo é validar controle, animação e interação ambiental, antes de ampliar o jogo.

- WASD/setas: mover a presença ativa.
- Tab: trocar Sombra/Reflexo.
- E: usar o mecanismo próximo.
- Espaço/Esc: pausa. R: reiniciar.
- H: mostrar/esconder ajuda. M: ligar/desligar música.
- O parceiro acompanha quando está solto. Não há movimento automático por trilhas, dano, tinta ou cronômetro neste encontro.

Perto do mecanismo da primeira margem, E ata a presença e passa o controle à outra. A ponte se estende. Na margem oposta, E fixa a escora e solta o vínculo. A vitória exige as duas presenças no jardim adiante. Soltar cedo pode recolher a ponte; quem estiver sem piso retorna à última margem segura, sem morte.

A câmera acompanha quem você controla. Há quatro quadros de caminhada, poses próprias de repouso e sustentação, registrados nos pés e em altura comum. O parceiro contorna obstáculos e procura a ponte aberta para reunir-se a você. A ação E aparece sobre o mecanismo próximo. Ainda não há animação completa em oito direções.

A abertura usa três enquadramentos ilustrados e dois temas musicais originais. Terminar ou pular leva ao menu, que espera sem limite de tempo. Somente Começar ou um novo Enter inicia a partida. R no menu revê a abertura; M liga/desliga a música e preserva sua escolha. Notas de composição, prompts e limites em `design/OPENING-AND-MUSIC.md`.

## Jogar

Dê dois cliques em `outputs/Jogar-NOS-Ponte-v3.bat`, mantendo `NOS-Ponte-v3.jar` ao lado. O iniciador usa o JDK 17 já instalado neste computador. O executável empacotado não requer Gradle nem downloads. As versões anteriores permanecem preservadas.

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

Exemplo: `java -jar outputs/NOS-Ponte-v3.jar --mirror-room`.

## Organização

- `BridgeEncounter`: estado e regras do encontro, sem OpenGL.
- `BridgeCollisionSystem`: margens, água, obstáculos e ponte.
- `CompanionFollowSystem`: seguimento com busca de desvios caminháveis.
- `BridgeInput`: teclado e botões de toque.
- `BridgeScreen`: orquestração em passos fixos.
- `BridgeRenderer`: apresentação e câmera.
- `BridgeHudRenderer`: objetivo, avisos, identificação e ajuda; objetivo persistente separado dos avisos temporários.
- `levels/bridge-encounter.json`: planta do encontro.
- `BridgeTuning`: movimento, distâncias e tempos de resposta.

Os testes JUnit verificam os protótipos preservados e a solução completa da ponte por movimento e ações contextuais, além de colisões, sustentação, retorno seguro, pausa, liberação de teclas, estados de objetivo e o início explícito após a cutscene. O modo `--smoke-test CAMINHO_ABSOLUTO` captura entrada, jogo e pausa numa janela invisível e fecha sozinho; `--intro-smoke CAMINHO_ABSOLUTO natural|reflection|wide|start` inspeciona a montagem e o fluxo. `--audio-check` verifica as duas faixas no dispositivo real com volume zero e janela invisível. Essas verificações não substituem ouvir a música e jogar.

## Arte e direção

Personagens e logo foram aprovados pelo usuário. Cenário, ponte, mecanismos e poses foram criados com geração de imagens e integrados ao jogo; referências originais em `design/`. Tipografia Cormorant Garamond e Atkinson Hyperlegible, com licenças OFL em `assets/ui/`. Notas e prompts em `design/BRIDGE-ARTS.md` e `design/SPRITE-REFINEMENT.md`, também entregues em `outputs/`; gerador dos atlas em `tools/`.

O GDD registra a revisão ativa e preserva as concepções anteriores como histórico. Os 13 níveis e capítulos da concepção original não estão implementados.
