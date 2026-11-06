# NÓS — Documento de Design (GDD)

> Jogo 2D em libGDX. Puzzle de fluxo, poético e filosófico, com inspiração temática em *Serial Experiments Lain*: identidade incerta, existir apenas enquanto alguém percebe, realidade que vaza.
> Não é cyberpunk: a estética é aquarela, com o ruído da "realidade" vazando por baixo.

---

## 1. Mecânica central (versão do README)

> Você não controla os heróis: controla o tempo de dois mundos sobrepostos em aquarela. O mundo que você observa com o pincel desacelera; o outro acelera, desbota e esquece o próprio caminho. A graça é dividir o olhar: guiar duas existências ao mesmo tempo, desviar dos perigos e fazê-las chegar juntas, antes que alguma deixe de existir.

**Pilares**
1. **Fluxo hipnótico.** O jogador entra em transe ao conduzir o tempo com um gesto contínuo.
2. **Inversão de papéis.** Você não é o herói. Você é o olhar que mantém o mundo existindo.
3. **Tudo tem duas faces.** Cada decisão ajuda um mundo e custa ao outro.
4. **Nada é explicado.** O sentido aparece aos poucos e só se completa no final.

---

## 2. Restrição: Espelho

**Regra:** cada ação do jogador afeta os dois mundos ao mesmo tempo. Acelerar um mundo desacelera o outro.

**Efeito na jogabilidade**
- Não existe solução local. Salvar um herói sempre cobra algo do outro.
- A restrição gera os três objetivos de cada fase: sobreviver, manter a harmonia e chegar juntos.
- É também a "mentira" do jogo. A reviravolta final revela que **nunca foram dois mundos**.

---

## 3. Perspectiva, controles e plataformas

- **Vista de cima.** Os dois mundos ocupam **o mesmo espaço**, sobrepostos em duas camadas de tinta translúcidas.
- **Os heróis andam sozinhos**, cada um por uma **trilha sinuosa** pintada. As trilhas se cruzam.
- **Controle:** o jogador arrasta o pincel (mouse no desktop e na web, dedo no Android). Um único gesto contínuo.
- **Plataformas:** Desktop (LWJGL3), Android e Web (TeaVM).

---

## 4. Sistemas

### 4.1 O tempo é o olhar
- O mundo **observado** desacelera. O outro **acelera** ("o tempo corre quando ninguém olha").
- **Leitura recomendada:** proximidade do pincel a cada herói.
  - `balance = (dB - dA) / (dA + dB)`, em [-1, 1], onde `dA` e `dB` são as distâncias do cursor a cada herói.
  - `speedA = 1 - k·balance` e `speedB = 1 + k·balance`. A soma é constante, o que implementa a regra do espelho.
- **Alternativa a testar:** posição horizontal do cursor na tela.
- ⚠️ **Decisão em aberto:** escolher entre as duas leituras no protótipo, com tecla de debug para alternar.
- Cada `World` tem **relógio próprio**: `deltaMundo = delta × speed`.

### 4.2 Desbotar
- Cada mundo tem um nível de tinta entre 0 e 1.
- A tinta diminui enquanto o mundo está acelerado (não observado) e se recupera quando ele é observado.
- **Tinta em 0:** a trilha daquele mundo se apaga e o herói **para, perdido**, até ser observado de novo.
- Visual: saturação e opacidade da camada caem junto com a tinta.

### 4.3 Harmonia (a vida)
- É uma barra de desequilíbrio entre os mundos.
- **Sobe com:** golpes de obstáculos, e `|balance|` acima de um limiar por tempo prolongado.
- **Desce** lentamente quando o equilíbrio volta.
- **Se estourar:** o jogador volta ao último **ponto de controle** da fase e gasta tinta do pote.

### 4.4 Pote de tinta (tentativas)
- Estoque **único por capítulo**.
- Cada volta ao ponto de controle **gasta** tinta.
- **Encontros de tinta** recarregam um pouco o pote.
- **Pote vazio = GAME OVER.**
- O tamanho do pote depende da dificuldade.

### 4.5 Encontros de tinta
- Os pontos onde as trilhas se cruzam são **pré-calculados** a partir das splines.
- Se os dois heróis passam pelo mesmo cruzamento com diferença menor que uma janela de tempo Δt, as tintas se misturam:
  - o **multiplicador** de pontos sobe;
  - o pote recebe uma pequena recarga.

### 4.6 Chegar juntos
- Cada trilha termina num ponto de chegada.
- O herói que chega primeiro espera ali.
- A **sincronia** é medida pela diferença entre as duas chegadas e entra na pontuação base.

### 4.7 Os heróis
- São a **sombra e o reflexo de um corpo ausente**.
- A forma deles é **moldada pelo rastro do pincel** do jogador: amostras recentes do movimento deformam a silhueta.
- No final, o corpo que nunca apareceu é **quem segura o pincel**.

---

## 5. Dificuldade

| Estágio | Pote de tinta | Velocidade de desbotar | Tolerância da harmonia |
|---|---|---|---|
| **Brisa** | grande | lenta | alta |
| **Maré** | médio | normal | média |
| **Tormenta** | pequeno | rápida | baixa |

Os valores ficam em dados (JSON ou constantes de `Tuning`), nunca espalhados pelo código.

---

## 6. Estrutura de níveis

São **13 fases**: 3 capítulos com 4 fases cada, mais a fase final.
- Cada fase tem um **aforismo como nome**. Exemplo: *"O rio não sabe de qual margem é"*.
- Entre as fases aparece **um verso pintado** na tela.

| Capítulo | Obstáculo novo | Camada que entra |
|---|---|---|
| **1. Água** | Correntes que arrastam destroços até a trilha | Os versos variam conforme a hora real |
| **2. Fogo** | Chamas que crescem mais rápido no mundo acelerado | Se o pincel fica parado, os heróis param e **olham para fora da tela** |
| **3. Vento** | Rajadas que deslocam a tinta e deformam as trilhas | Surge na galeria um quadro feito das **suas falhas**; quadros antigos mudam sozinhos |
| **ÍRIS** (final) | Os três elementos juntos | A revelação |

### 6.1 ÍRIS: o final em três atos
1. **Soltar o pincel.** Os mundos começam a se fundir sozinhos. Controlar o tempo os separa de novo. Vencer é **parar de controlar**.
2. **Um só.** A sombra e o reflexo se sobrepõem e viram uma única figura.
3. **A galeria se revela.** Todos os quadros das fases anteriores se sobrepõem e formam **uma única imagem**, que sempre esteve lá.

**Depois do final**
- Créditos com **"pintado por ___"** em branco.
- Ao reabrir o jogo, o menu pergunta: *"primeira vez aqui?"*, mesmo com a galeria cheia.

> 💡 **Desenhar de trás para frente:** definir primeiro a imagem final da galeria e então distribuir os pedaços entre as 12 fases.

---

## 7. Narrativa e tom (pegada Lain)

**Nada é explicado.** Palavras são mínimas: aforismos e versos.

### 7.1 Quarta parede em escalada
| Momento | O que acontece |
|---|---|
| Água | Versos mudam conforme a hora real (de madrugada, falam de insônia) |
| Fogo | O pincel fica parado → a tinta desbota e os heróis olham para o jogador. Verso: *"você também parou?"* |
| Vento | Quadro na galeria feito dos rastros das tentativas falhas, com legenda tirada de dados reais (tentativas, horas jogadas) |
| ÍRIS | O herói único olha para o cursor; a última pincelada é do jogador |
| Pós-jogo | O menu "não reconhece" o jogador |

**Regras de ouro**
- Nunca explicar.
- Nunca ameaçar.
- Nunca fingir erro técnico (nada de crash falso ou save apagado).
- Usar só dados de jogo e a hora do sistema. **Não** ler o nome de usuário do sistema operacional.

### 7.2 Outras camadas
- **Existir é ser observado:** o próprio sistema de desbotar (4.2).
- **Memória não confiável:** versos que contradizem os anteriores e quadros da galeria que mudam.
- **Ruído na aquarela:** chiados, estática e linhas de tela antiga esporádicos, como se a realidade vazasse.

---

## 8. Pontuação

```
pontuação_fase = base × multiplicador
base           = sincronia_na_chegada + harmonia_média + golpes_evitados
multiplicador  = cresce com os encontros de tinta
```
- Cada fase recebe uma nota e tem **recorde local** salvo em `Preferences`.
- A pontuação aparece na tela de resultado de cada fase.

---

## 9. Rejogabilidade

- **Versos ocultos:** um por fase, escondido num ponto que só aparece se for observado. Encontrá-lo **desbloqueia pincéis**.
- **Pincéis modificadores** (exemplos):
  - *Seco:* olhar mais preciso, multiplicador menor.
  - *Esquecimento:* desbota mais rápido, multiplicador maior.
- **Galeria:** o rastro de cada fase vira um quadro salvo.

---

## 10. Visual e som

- **Aquarela:**
  - framebuffers (FBO) com difusão de tinta e textura de papel;
  - bordas escurecidas;
  - a sobreposição das duas camadas forma uma terceira cor.
- **Ruído:** shader de estática e glitch, com **opção de reduzir ou desligar**.
- **Paleta:** duas cores distinguíveis por pessoas com daltonismo, porque o jogo depende de separar as camadas.
- **Som:** cada mundo tem **sua própria faixa**, que acelera ou desacelera junto com o tempo daquele mundo.
  - ⚠️ A classe `Music` do libGDX não muda o tom.
  - Opções: `Sound` com `setPitch` em loops curtos, ou crossfade entre versões pré-gravadas em várias velocidades (mais seguro na web).

---

## 11. Telas

1. **MenuScreen:** jogar, capítulos, galeria, pincéis, opções e sair. É também o menu que "esquece" o jogador no pós-jogo.
2. **DifficultyScreen:** Brisa, Maré ou Tormenta.
3. **ChapterSelectScreen**
4. **VerseScreen:** transição entre fases, com o aforismo e o verso.
5. **GameScreen**, com **PauseOverlay**.
6. **ResultScreen:** pontuação da fase.
7. **GameOverScreen:** a tinta escorre pela tela.
8. **GalleryScreen**
9. **BrushScreen**
10. **EndingScreen:** final e créditos.

---

## 12. Organização de classes

```
nos/
├─ NosGame (extends Game)       → AssetManager, SaveData, troca de telas
├─ screens/
│   BaseScreen (abstrata), MenuScreen, DifficultyScreen, ChapterSelectScreen,
│   VerseScreen, GameScreen, PauseOverlay, ResultScreen, GameOverScreen,
│   GalleryScreen, BrushScreen, EndingScreen
├─ world/
│   World (relógio próprio, tinta, obstáculos, herói)
│   MirrorPair (liga os dois mundos e aplica a regra do espelho)
│   Trail (caminho via CatmullRomSpline), Checkpoint
├─ entities/
│   Hero (sombra/reflexo, forma vinda do rastro)
│   Obstacle (abstrata) → Current, Debris, Flame, Gust
│   HiddenVerse
├─ systems/
│   GazeTimeSystem, FadeSystem, HarmonySystem, InkPot,
│   InkEncounterSystem, ScoreSystem
│   FourthWallDirector (hora real, pausas, tentativas)
│   MemoryDirector (versos contraditórios, galeria que muda)
├─ input/    BrushInput (InputProcessor: mouse e toque)
├─ render/   WatercolorRenderer, GlitchEffect, PaintingRecorder
├─ audio/    DualTrackAudio
└─ data/     LevelData, LevelLoader (fases em JSON), Difficulty (enum),
             Brush, Verses, SaveData (Preferences), Tuning
```

---

## 13. Marcos

| # | Marco | Pronto quando |
|---|---|---|
| M1 | **Protótipo em cinza** | Duas trilhas, dois heróis, olhar → tempo, desbotar e harmonia funcionando com formas simples, e **é divertido** |
| M2 | **Água jogável** | 4 fases, menu, dificuldade, pote, game over e pontuação. **Cumpre o mínimo exigido** |
| M3 | **Fogo e Vento** | 8 fases a mais, com obstáculos novos e pontos de controle |
| M4 | **Aquarela e som** | Shaders de tinta, ruído, áudio duplo e galeria |
| M5 | **Narrativa** | Aforismos, versos, versos ocultos, pincéis, quarta parede e memória não confiável |
| M6 | **ÍRIS e polimento** | Final em três atos, pós-jogo, acessibilidade e testes nas três plataformas |

**Ferramenta interna:** um **editor de trilhas em modo debug**. O desenvolvedor desenha a trilha com o mouse e o jogo exporta o JSON da fase.

---

## 14. Riscos

- **Legibilidade dos mundos sobrepostos.** Mitigar com contraste, contornos nítidos nos obstáculos e testes cedo.
- **Áudio com variação de tom em todas as plataformas.** Fazer um protótipo de áudio isolado já no M1 ou M2.
- **Desempenho dos shaders no celular e na web.** Medir no M4 e ter um modo "aquarela leve".
- **Quarta parede soar como truque.** Testar com jogadores e dosar.
- **Volume de texto** (aforismos, versos e variações). Escrever em paralelo ao código.

## 15. Decisões em aberto

- [ ] Leitura do olhar: proximidade × posição horizontal (M1)
- [ ] Números de balanceamento (k, limiares, Δt, tamanhos do pote)
- [ ] Paleta final das duas tintas
- [ ] Imagem final da galeria em ÍRIS
- [ ] Textos: 13 aforismos, 13 versos e 13 versos ocultos
- [ ] Lista final de pincéis
