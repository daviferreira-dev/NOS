# NÓS

Um puzzle de exploração sobre duas presenças e o vínculo entre elas, em libGDX.

## Experiência atual: o encontro da ponte

Esta é a nova experiência curta, criada após estudar princípios de Unravel Two, COCOON e GRIS. Um sustenta a passagem, o outro atravessa e fixa a escora. Depois os dois podem seguir ao jardim. O objetivo é validar controle, animação e interação ambiental, antes de ampliar o jogo.

- WASD/setas: mover a presença ativa.
- Tab: trocar Sombra/Reflexo.
- E: usar o mecanismo próximo.
- Espaço/Esc: pausa. R: reiniciar.
- O parceiro acompanha quando está solto. Não há movimento automático por trilhas, dano, tinta ou cronômetro neste encontro.

Perto do mecanismo da primeira margem, E ata a presença e passa o controle à outra. A ponte se estende. Na margem oposta, E fixa a escora e solta o vínculo. A vitória exige as duas presenças no jardim adiante. Soltar cedo pode recolher a ponte; quem estiver sem piso retorna à última margem segura, sem morte.

A câmera acompanha quem você controla. Há um ciclo inicial de quatro quadros por personagem, ainda não uma animação completa em oito direções. Abertura pulável preservada; áudio não implementado.

## Jogar

Dê dois cliques em `outputs/Jogar-NOS-Ponte.bat`, mantendo `NOS-Ponte.jar` ao lado. O iniciador usa o JDK 17 já instalado neste computador. O executável empacotado não requer Gradle nem downloads.

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

Exemplo: `java -jar outputs/NOS-Ponte.jar --mirror-room`.

## Organização

- `BridgeEncounter`: estado e regras do encontro, sem OpenGL.
- `BridgeCollisionSystem`: margens, água, obstáculos e ponte.
- `BridgeInput`: teclado e botões de toque.
- `BridgeScreen`: orquestração em passos fixos.
- `BridgeRenderer`: apresentação e câmera.
- `levels/bridge-encounter.json`: planta do encontro.
- `BridgeTuning`: movimento, distâncias e tempos de resposta.

Os testes JUnit verificam os protótipos preservados e a solução completa da ponte por movimento e ações contextuais, além de colisões, sustentação, retorno seguro, pausa e liberação de teclas. O modo `--smoke-test CAMINHO_ABSOLUTO` captura menu, jogo e pausa numa janela invisível e fecha sozinho; `--intro-smoke` inspeciona a abertura.

## Arte e direção

Personagens e logo foram aprovados pelo usuário. Cenário, ponte e ciclo inicial de caminhada foram criados com geração de imagens e integrados ao jogo; referências originais em `design/`. Tipografia Cormorant Garamond e Atkinson Hyperlegible, com licenças OFL em `assets/ui/`. Notas e prompts em `design/BRIDGE-ARTS.md` (também entregues em `outputs/ARTE-E-REFERENCIAS.md`); gerador dos atlas em `tools/`.

O GDD registra a revisão ativa e preserva as concepções anteriores como histórico. Os 13 níveis e capítulos da concepção original não estão implementados.
