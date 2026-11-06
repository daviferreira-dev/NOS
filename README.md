# NÓS

*Um puzzle de fluxo sobre tempo, olhar e existência, pintado em aquarela.*

## Mecânica central
Você não controla os heróis: controla o tempo de dois mundos sobrepostos em aquarela. O mundo que você observa com o pincel desacelera; o outro acelera, desbota e esquece o próprio caminho. A graça é dividir o olhar: guiar duas existências ao mesmo tempo, desviar dos perigos e fazê-las chegar juntas, antes que alguma deixe de existir.

## Restrição: Espelho
Cada ação afeta os dois mundos ao mesmo tempo: acelerar um é desacelerar o outro. Nenhuma decisão é local. Salvar um herói sempre cobra algo do outro, e é daí que nascem os três objetivos de cada fase: sobreviver, manter a harmonia e chegar juntos.

## Como jogar
| Ação | Desktop / Web | Android |
|---|---|---|
| Observar (modo horizontal padrão) | Cursor à esquerda: Sombra; à direita: Reflexo | Arrastar para a esquerda/direita |
| Comparar com proximidade | `Tab` | — |
| Pausar | `Espaço` / `Esc` | — |
| Recomeçar | `R` | Toque após o resultado |

- **Harmonia:** não deixe os mundos se desequilibrarem demais.
- **Pote de tinta:** cada volta ao ponto de controle gasta tinta. Pote vazio é fim de jogo.
- **Encontros de tinta:** fazer os heróis passarem juntos pelos cruzamentos multiplica a pontuação.

## Estrutura planejada
- **3 capítulos** (Água, Fogo, Vento) com 4 fases cada, mais o capítulo final.
- **3 dificuldades:** Brisa, Maré e Tormenta.
- **Telas:** menu inicial, game over e pontuação por fase, com recorde local.

## Executar
Requer JDK 17 ou superior.
```bash
./gradlew lwjgl3:run      # desktop
./gradlew core:test       # testes
```

### Primeira travessia jogável

- Abertura de dez segundos, pulável por clique/tecla, seguida de início com três dificuldades. Nesta versão não há áudio.
- Uma fase contínua de aproximadamente um minuto, com câmera acompanhando os personagens por margens, madeira e um jardim abandonado. A implementação completa dos capítulos ainda não existe.
- Mova o cursor para a esquerda para desacelerar Sombra e acelerar Reflexo; para a direita, o inverso. Reflexo tem uma trilha mais longa. O centro mantém velocidades iguais, mas não resolve automaticamente a fase.
- Os troncos atravessam as trilhas conforme a maré: desacelere o herói para esperar o espaço abrir. Impactos empurram e gastam tinta. O desequilíbrio prolongado custa uma medida do pote e retorna ao ponto de controle; a última medida perdida encerra a sessão.
- Olhar recupera a tinta do mundo observado. Tinta zerada faz o herói parar até receber atenção. Encontros nas flores recarregam o pote e aumentam os pontos, uma vez por cruzamento.
- Chegue com os dois ao final dentro de quatro segundos de diferença. Há resultado e recorde local, salvo somente via `Preferences`. `R` volta ao início; `Espaço` pausa.
- Os personagens já usam as ilustrações aprovadas, com deslocamento e oscilação, mas ainda não têm animação de caminhada por direção. Os originais estão em `design/`.
- `core:test` executa JUnit para olhar, entrada, câmera, tinta, harmonia, pausa, tentativas e travessia, além das verificações de movimento. Uma simulação confirma vitória com atenção alternada e derrota ao deixar a fase sem controle.
- A verificação gráfica `java -jar lwjgl3/build/libs/NOS-1.14.2.3.jar --smoke-test CAMINHO_ABSOLUTO` captura menu, jogo e pausa com janela invisível e fecha sozinha. `--intro-smoke` verifica a abertura. Não é necessário executá-la para jogar.

### Arte e tipografia

Conceitos e aquarelas criados com geração de imagens a partir da direção aprovada pelo usuário. Cormorant Garamond (títulos) e Atkinson Hyperlegible (leitura) usam atlas de alta resolução, com licenças OFL incluídas em `assets/ui/`. Gerador e notas em `tools/`.

## Tecnologias
- [libGDX](https://libgdx.com/), com projeto gerado pelo gdx-liftoff.
- Alvos: Desktop (LWJGL3), Android e Web (TeaVM).

---
Design completo em [`GDD.md`](GDD.md).
