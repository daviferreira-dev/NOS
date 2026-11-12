# NÓS

Jogo 2D em libGDX (Game Jam, 2026).

**Mecânica central:** você controla a Sombra e o Reflexo age sozinho, então cada passo seu muda o caminho dele.
A graça é cooperar sem comandá-lo: preparar o terreno, observar por onde ele vai e esperar o reencontro.
Quatro fases (Água, Fogo, Vento, Íris), cada uma com um jeito diferente de atravessar.

**Restrição sorteada: "Espelho".** Cada ação afeta os dois personagens ao mesmo tempo; não existe solução local, o que ajuda um custa ao outro.

**Requisitos da atividade:** menu inicial (com abertura animada), 4 fases com dificuldade crescente, tela de **Fim de jogo** ao terminar a Íris, **3 vidas por fase** (cada contato com brasa tira uma; zerar leva ao Fim de jogo de derrota) e **pontuação** = 5000 − 4 pontos por segundo jogado − 150 por recomeço. Código separado em `screens`, `systems`, `world`, `entities`, `render`, `data`, `input` e `audio`.

**Como jogar rápido:** baixe `NOS.jar` (Releases) e rode `java -jar NOS.jar` (Java 17+), ou abra `Jogar NÓS.bat`.

## Campanha

1. **Água.** Peça ajuda no guincho, atravesse a ponte e fixe a escora para que Reflexo possa reencontrar você.
2. **Fogo.** Escolha o corredor das brasas e abra a porta para Reflexo, ou explorem juntos o desvio entre as máquinas.
3. **Vento.** Suba a encosta pelas curvas da trilha. Na abertura, pegue a rajada lateral e chame Reflexo do abrigo.
4. **Íris.** Contorne o observatório prestando atenção à galeria interior. A passagem indicada por Reflexo leva ao reencontro final.

O [GDD](GDD.md) descreve a campanha atual e registra as experiências anteriores.

## Jogar

Com progresso salvo, o jogo abre no menu. A partir de Fogo, pressione F ou clique em **Escolher fase** para revisitar uma fase desbloqueada. R no menu revê a abertura.

Abra **Jogar NÓS.bat** na raiz do projeto. As atualizações usam sempre o mesmo arquivo `outputs/NOS.jar`. Em uma cópia recém-clonada, execute `.\gradlew.bat lwjgl3:jar` e copie o JAR da aplicação de `lwjgl3/build/libs` para `outputs/NOS.jar`.

- WASD/setas movem Sombra. E executa a ação contextual.
- Segure Shift para correr.
- H ou Esc abre ajuda e pausa tudo. R reinicia o trecho/fase atual.
- M liga/desliga música; F11 alterna tela cheia; F6 ajusta texto.
- F7 na pausa configura teclas; F8 nessa configuração restaura o padrão.

Reflexo não é selecionado por Tab/Q. Ele chega fisicamente aos mecanismos e acompanha pelo piso real. Sem cronômetro global, dano, vidas ou barras. Brasas recuperam Sombra em piso seguro, preservando mecanismos resolvidos. Em Vento, a corrente lateral leva Sombra até um patamar protegido.

A abertura tem cinco planos diferentes, caminhada dos sprites, pausas, câmera e transições. É animação 2D por camadas, sem CGI rejeitado ou voz sintética. Terminar/pular leva ao menu; começar requer outro comando. R no menu revê o filme. Três trilhas instrumentais, volume máximo de 20%, mute persistente e pausa ao perder foco.

Cada resultado espera Enter/clique para seguir; Íris volta ao menu. Preferences guarda checkpoint e conclusões monotônicas. Continuar escolhe o trecho mais avançado disponível; N começa outra sessão sem apagar progresso. Chaves antigas water01/02/03 preservadas; quarta em water04.journey. Inspeções usam memória e não alteram o save real.

## Desenvolvimento

Requer JDK 17 ou superior. Na raiz:

```powershell
.\gradlew.bat lwjgl3:run
.\gradlew.bat core:test
.\gradlew.bat lwjgl3:jar teavm:gdx_teavm_web_js_release_build
```

Neste computador: JDK em `C:\Users\PC Gamer\Documents\Codex\jdks\jdk-17.0.21+9` e cache Gradle em `C:\Users\PC Gamer\Documents\Codex\2026-11-06\instale-o-jdk-17-por-exemplo\work\gradle-user-home`.

Lógica em BridgeEncounter, FurnaceSystem, RidgeSystem, ObservatorySystem, EnvironmentalActionSystem, CompanionBehaviorSystem e CompanionFollowSystem. Geometria em JSON/LevelLoader e ajustes em dados/Tuning. Regras separadas da renderização. Entrada por BridgeInput/BridgeKeyBindings; recursos via AssetManager/dispose; persistência por SaveData/Preferences e roteamento por JourneyProgress.

Testes percorrem os quatro mapas sem OpenGL: cooperação, colisões, pausa, corrida, brasas, rajadas e recuperação. Desktop e Web/TeaVM compilados; navegador e Android precisam de teste próprio.

## Inspeção e arte

`--smoke-test PREFIXO --garden-menu|--water-two-menu|--water-three-menu|--iris-won|--iris-menu|--iris-large` inspeciona sem janela visível. `--intro-smoke PREFIXO shot-0` até `shot-4` verifica os planos; `--resume-iris` verifica continuação. `--audio-check` verifica áudio sem som. Isso não substitui jogar e ouvir.

Personagens e logo aprovados, fontes e aquarela preservados. Novas aquarelas em assets/art/furnace-interior.png, ridge-gorge.png e observatory-interior.png. As colisões usam a geometria dos mapas. Cormorant Garamond e Atkinson Hyperlegible com licenças OFL em assets/ui. Licença GeneralUser junto do áudio.

`--mirror-room` e `--legacy` preservam os experimentos. O GDD conserva planos antigos como histórico; a revisão do início do documento prevalece. Commits e pushes após etapas validadas na branch codex/encontro-da-ponte.
