# NÓS

Jogo 2D em libGDX, com Sombra controlável e Reflexo autônomo. A campanha atual tem **quatro fases no total**, substituindo o plano histórico de treze. Diversão e acabamento ainda dependem do teste do usuário.

## Campanha

1. **Água — Encontro:** ponte, pedido de ajuda, escora e pátio.
2. **Fogo — Ruptura:** transferência da jardineira entre mecanismos, com brasas móveis e intervalos de passagem.
3. **Vento — Distância:** rajadas e revezamento da sustentação para Reflexo atravessar.
4. **Íris — Convergência:** combinação de peso, revezamento e perigos até a conclusão final.

Plano, cortes e limites: [design/PLANO-QUATRO-FASES.md](design/PLANO-QUATRO-FASES.md). A quarta fase encerra a campanha; não há quinto mapa planejado.

## Jogar

Com progresso salvo, o jogo abre no menu. A partir de Fogo, pressione F ou clique em **Escolher fase** para revisitar uma fase desbloqueada. R no menu revê a abertura.

Abra **Jogar NÓS.bat** na raiz do projeto. As atualizações usam sempre o mesmo arquivo `outputs/NOS.jar`. Em uma cópia recém-clonada, execute `.\gradlew.bat lwjgl3:jar` e copie o JAR da aplicação de `lwjgl3/build/libs` para `outputs/NOS.jar`.

- WASD/setas movem Sombra. E executa a ação contextual.
- Shift mantido corre sem peso. E segura/solta a jardineira; o movimento empurra ou puxa.
- H ou Esc abre ajuda e pausa tudo. R reinicia o trecho/fase atual.
- M liga/desliga música; F11 alterna tela cheia; F6 ajusta texto.
- F7 na pausa configura teclas; F8 nessa configuração restaura o padrão.

Reflexo não é selecionado por Tab/Q. Ele chega fisicamente aos mecanismos e acompanha pelo piso real. Sem cronômetro global, dano, vidas ou barras. Brasas recuperam Sombra em piso seguro, preservando mecanismos resolvidos. Rajadas empurram pela colisão. A peça é solta se um deslocamento ambiental exigir isso.

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

Lógica em BridgeEncounter, GardenGateSystem, RelayGateSystem, EnvironmentalActionSystem, CompanionBehaviorSystem e CompanionFollowSystem. Geometria em JSON/LevelLoader e ajustes em dados/Tuning. Regras separadas da renderização. Entrada por BridgeInput/BridgeKeyBindings; recursos via AssetManager/dispose; persistência por SaveData/Preferences e roteamento por JourneyProgress.

Testes percorrem os quatro mapas sem OpenGL: cooperação, colisões, pausa, corrida, brasas, rajadas e recuperação. Desktop e Web/TeaVM compilados; navegador e Android precisam de teste próprio.

## Inspeção e arte

`--smoke-test PREFIXO --garden-menu|--water-two-menu|--water-three-menu|--iris-won|--iris-menu|--iris-large` inspeciona sem janela visível. `--intro-smoke PREFIXO shot-0` até `shot-4` verifica os planos; `--resume-iris` verifica continuação. `--audio-check` verifica áudio sem som. Isso não substitui jogar e ouvir.

Personagens e logo aprovados, fontes e aquarela preservados. Arte nova por image_gen integrado; caminhos e prompts finais no plano de quatro fases em design. Cormorant Garamond e Atkinson Hyperlegible com licenças OFL em assets/ui. Licença GeneralUser junto do áudio.

`--mirror-room` e `--legacy` preservam os experimentos. O GDD conserva planos antigos como histórico; v9 prevalece. Commits e pushes após etapas validadas na branch codex/encontro-da-ponte.
