# NÓS

*Um puzzle de fluxo sobre tempo, olhar e existência, pintado em aquarela.*

## Mecânica central
Você não controla os heróis: controla o tempo de dois mundos sobrepostos em aquarela. O mundo que você observa com o pincel desacelera; o outro acelera, desbota e esquece o próprio caminho. A graça é dividir o olhar: guiar duas existências ao mesmo tempo, desviar dos perigos e fazê-las chegar juntas, antes que alguma deixe de existir.

## Restrição: Espelho
Cada ação afeta os dois mundos ao mesmo tempo: acelerar um é desacelerar o outro. Nenhuma decisão é local. Salvar um herói sempre cobra algo do outro, e é daí que nascem os três objetivos de cada fase: sobreviver, manter a harmonia e chegar juntos.

## Como jogar
| Ação | Desktop / Web | Android |
|---|---|---|
| Observar (desacelerar um mundo) | Aproximar o cursor do herói | Arrastar o dedo até o herói |
| Pausar | `Esc` | Botão de pausa |

- **Harmonia:** não deixe os mundos se desequilibrarem demais.
- **Pote de tinta:** cada volta ao ponto de controle gasta tinta. Pote vazio é fim de jogo.
- **Encontros de tinta:** fazer os heróis passarem juntos pelos cruzamentos multiplica a pontuação.

## Estrutura
- **3 capítulos** (Água, Fogo, Vento) com 4 fases cada, mais o capítulo final.
- **3 dificuldades:** Brisa, Maré e Tormenta.
- **Telas:** menu inicial, game over e pontuação por fase, com recorde local.

## Executar
Requer JDK 17 ou superior.
```bash
./gradlew lwjgl3:run      # desktop
./gradlew core:test       # testes
```

## Tecnologias
- [libGDX](https://libgdx.com/), com projeto gerado pelo gdx-liftoff.
- Alvos: Desktop (LWJGL3), Android e Web (TeaVM).

---
Design completo em [`GDD.md`](GDD.md).
