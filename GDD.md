# NÓS — Documento de Design (GDD)

## Revisão ativa — Uma presença controlável, duas pessoas (07/11/2026)

O usuário aprovou testar uma relação em que se controla apenas uma presença e se percebe a outra como pessoa distinta, apesar do mesmo rosto. O recorte continua sendo a ponte; não se antecipa a história completa nem se explica o mistério. Esta revisão substitui a troca por Tab da versão anterior.

- O jogador move somente Sombra (WASD/setas por padrão). Reflexo não é selecionável. Câmera, marcador por forma e sombra de contato identificam Sombra sem depender apenas da cor.
- Reflexo tem um pequeno tempo de reação e distâncias diferentes para partir e parar. Ao se aproximarem do rio, observa a água uma vez e pergunta discretamente "Você também viu?". O pedido de ajuda interrompe essa observação; ele retoma o acompanhamento se o jogador se afasta. Não usar a autonomia para bloquear a sessão ou resolver o desafio sozinho.
- No guincho, a ação contextual pede ajuda. Reflexo caminha até o mecanismo respeitando pedras, margens e água; se Sombra ocupa o ponto, espera espaço. A ponte só começa a estender quando Reflexo chega e assume a sustentação. Sombra continua sob controle durante todo o processo.
- Sombra atravessa e fixa a escora com a ação contextual. Reflexo não fixa essa escora sozinho. A ponte fica segura; ele solta o guincho, faz uma pequena pausa e se reúne a Sombra pela ponte. Ambos precisam chegar ao jardim para concluir.
- Perto do guincho, pedir novamente cancela a aproximação ou chama Reflexo de volta. Não há comando remoto para soltar a sustentação do meio da ponte. Sem cronômetro, dano ou tinta; o retorno à margem segura continua como recuperação de perda inesperada de piso.
- Abertura ilustrada, menu com início explícito, sprites e trilhas originais são aproveitados. Volume máximo da música reduzido a 20% por pedido do usuário. A cutscene sempre termina no menu; somente Começar ou um novo Enter inicia a partida.
- Interface: ajuda/pausa modal interrompe toda a simulação, escurece a cena e substitui o HUD. Comandos em teclas visuais e grupos. Objetivo mostra "Etapa N de 3", expande ao mudar e depois encolhe; os atalhos ficam discretos com o tempo. Painéis compartilham papel creme, títulos serifados e texto de apoio legível. Textos e teclas podem ser ajustados pelo menu; preferências são salvas. Sem suporte de gamepad nesta implementação.
- Apresentação ambiental: contato dos pés mais legível, remoção de riscos soltos sobre a água, pedra sem tintura verde chapada, destino com indicação legível sem cruzar a coluna, laterais em cor da cena e opção de tela cheia. Nenhuma alteração na planta de colisão.
- Arquitetura: `BridgeEncounter`, `CompanionBehaviorSystem`, `CompanionFollowSystem`, `BridgeCollisionSystem`, `BridgeKeyBindings`, `BridgeInput`, `BridgeScreen`, `BridgeUiState`, `BridgeHudRenderer` e `BridgeRenderer`. Ajustes em `BridgeTuning`; planta no JSON existente.

**Decisões atuais**
- [x] Testar Sombra como única presença controlável e Reflexo como pessoa autônoma.
- [x] Manter o pedido de ajuda contextual e a solução final da escora nas mãos do jogador.
- [x] Pausar a simulação durante ajuda e configuração, com comandos visuais e hierarquia de leitura.
- [ ] Validar se Reflexo parece uma pessoa, e não um corpo que simplesmente copia o jogador.
- [ ] Validar ritmo da cooperação, clareza do guincho e interesse da ponte.
- [ ] Validar leitura, tamanho de texto, remapeamento e nova apresentação do cenário.
- [ ] Aprofundar as diferenças de percepção e memória somente depois desse teste.

---

## Revisão anterior — Encontro da ponte com troca de controle

Depois de considerar a aparência e a jogabilidade dos testes anteriores insatisfatórias, o usuário pediu referências de jogos publicados e aprovou seguir com um encontro ambiental: **um sustenta, o outro atravessa e fixa a ponte**. Esta revisão é o teste atual e prevalece sobre as revisões anteriores preservadas abaixo.

- Controle experimental mais simples: WASD/setas movem a presença ativa; Tab troca; a outra acompanha enquanto estiver solta. E é a ação contextual. O espelhamento simultâneo foi mantido na sala anterior para comparação, mas não é obrigatório na exploração atual.
- Na margem inicial, E ata a presença ativa ao mecanismo e troca o controle para a outra. O vínculo sustenta a ponte, que se estende de forma visível. Uma presença atada fica parada.
- A outra atravessa e usa E na escora da margem oposta. A ponte fica permanentemente segura, o nó se solta e as duas presenças podem seguir ao jardim. A saída exige ambas.
- Sem tempo limite, dano, tinta ou pontuação. Soltar cedo permite que a ponte recue; quem ficar sem piso retorna à última margem segura, sem morte e sem bloquear a sessão. Espaço/Esc pausa; R recomeça.
- Planta em `levels/bridge-encounter.json` e `BridgeTuning`. Margens, água, faixa da ponte e obstáculos têm geometria explícita. Sistemas de colisão e estado podem ser testados sem OpenGL.
- Apresentação: cenário próprio com duas margens e vão central, ponte pintada, ciclo inicial de quatro quadros por personagem, câmera acompanhando o corpo ativo. Não é uma animação final em oito direções.
- Abertura revisada a pedido do usuário: montagem ilustrada de 23 segundos com planos da margem, do reflexo e retorno ao conjunto, movimentos lentos de câmera, transições e duas composições instrumentais originais. Terminar ou pular leva a um menu que espera indefinidamente. A logo fica separada dos personagens; somente Começar ou um novo Enter no menu inicia a travessia. Rever abertura e ligar/desligar música estão disponíveis no menu. M alterna a música durante o jogo; Preferences preserva a escolha. A música pausa ao perder foco. Isso amplia a autorização de apresentação e áudio sem antecipar a narrativa completa do M5.
- Refinamento: repouso e sustentação têm poses próprias; os quadros compartilham altura visual e origem nos pés. A caminhada avança pela distância realmente percorrida e para diante de colisões. Mecanismos usam sprites em aquarela com estados de repouso/acionamento, e a ação contextual aparece junto ao objeto próximo. O rodapé informa quando o parceiro está sustentando a ponte.
- Seguimento: o parceiro procura um desvio caminhável quando a linha direta encontra pedra ou água; cruza somente pela faixa da ponte aberta. A busca não teletransporta o corpo e respeita as mesmas colisões do jogador. O retorno à margem segura continua reservado à perda de piso quando a ponte recolhe.
- Interface revisada após a abertura: o objetivo atual fica num cartão compacto com verbo de ação e etapa 1/3, 2/3 ou 3/3; não expira junto dos avisos. Mudanças importantes aparecem em avisos temporários de maior contraste. A identificação da presença ativa e os atalhos são secundários, em painéis menores no rodapé. H mostra a ajuda completa. No desktop, controles de toque não são exibidos nem respondem ao mouse como direcional invisível; em dispositivos de toque, direcional e ações permanecem disponíveis. A entrada do jardim recebe uma indicação discreta depois de fixar a ponte. Não houve mudança nas regras do encontro.
- Referências de princípios, não de reprodução de assets: [Unravel Two](https://www.ea.com/games/unravel/unravel-two) (relação entre dois corpos e vínculo), [COCOON](https://annapurnainteractive.com/games/cocoon) (regras combinadas em desafios ambientais), [GRIS](https://store.steampowered.com/app/683320/GRIS/) (coerência visual, movimento e apresentação).

**Decisões atuais**
- [x] Prototipar uma ponte que muda fisicamente por ação das duas presenças.
- [x] Preservar os testes anteriores e a pasta antiga; trabalho ativo na nova pasta da OneDrive.
- [x] Separar o fim da cutscene do início da partida e exigir uma ação explícita no menu.
- [ ] Validar a nova montagem e os temas musicais com o usuário.
- [ ] Validar a leitura dos objetivos e dos avisos durante a travessia.
- [ ] Validar com o usuário o controle de uma presença ativa e o seguimento do parceiro.
- [ ] Validar animação, clareza do mecanismo e interesse do encontro antes de produzir mais mapas.

---

## Revisão anterior — Sala de exploração, espelho e vínculo



Após jogar os protótipos de tempo, o usuário aprovou substituir o núcleo por movimento direto e puzzles espaciais. **Esta revisão prevalece sobre as seções históricas abaixo.** O código e o executável anteriores ficam preservados para comparação.

- WASD/setas movem Sombra; Reflexo responde invertendo esquerda/direita, mantendo cima/baixo. Não há movimento automático.
- Cada camada tem colisões próprias. Pedras índigo bloqueiam Sombra; pedras claras bloqueiam Reflexo. As bordas e a ponte fechada bloqueiam os dois. Um corpo bloqueado não impede o outro de responder.
- `E` ata/solta Reflexo; `Q` ata/solta Sombra. Apenas uma presença pode estar atada por vez. O vínculo dura até ser solto ou trocado, sem contagem regressiva. Isto é uma escolha provisória para experimentar sem pressão.
- Primeira sala: colocar cada presença no selo de sua cor abre a ponte permanentemente. Depois é preciso conduzir ambas às flores além da passagem. Não há tinta, harmonia, dano, pontuação ou cronômetro nessa sala.
- A posição assimétrica dos selos exige reposicionar os corpos. O jogador pode usar o vínculo e as colisões independentes para isso. `R` reinicia sem penalidade; Espaço/Esc pausa.
- Abertura, personagens, logo, aquarela e tipografia são mantidos. O objetivo atual é validar uma única sala, antes de ampliar mapas e produção artística.
- Arquitetura ativa: `PuzzleHero`, `MirrorMovementSystem`, `AnchorSystem`, `RoomCollisionSystem`, `MirrorPuzzle`, `PuzzleInput`, `PuzzleScreen` e `PuzzleRenderer`. Geometria e posições em `levels/knot-room.json`; ajustes em `PuzzleTuning`.
- Entrada nesta implementação: teclado e direcional/botões de toque. Gamepad e animações de caminhada por direção permanecem pendentes.

**Decisões desta revisão**
- [x] Substituir controle de tempo por movimento direto e espelhado, aprovado pelo usuário.
- [x] Testar a habilidade de atar uma presença para reposicionar a outra.
- [ ] Validar sensação do movimento, clareza das colisões e interesse do primeiro puzzle com o usuário.
- [ ] Decidir se futuras salas usam vínculo livre ou pontos específicos de amarração, após teste da sala atual.
- [ ] Replanejar os capítulos e a progressão somente depois de validar o novo núcleo.

---

## Histórico — Concepção de tempo e trilhas

As seções abaixo documentam a concepção anterior e não autorizam reintroduzir seus sistemas na sala ativa sem uma nova decisão de design.

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
- **Travessia contínua (revisão após teste):** a câmera acompanha os heróis por uma paisagem de 3.200 unidades, passando de margens com juncos a madeira abandonada e ruínas de jardim. As trilhas seguem adiante, sem dar voltas na mesma arena. A câmera enquadra ambos quando se afastam.
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
- **Controles do protótipo:** mouse ou arraste do primeiro toque controla o olhar; `Tab` alterna proximidade/horizontal; `R` reinicia as trilhas. Na leitura horizontal, esquerda observa A e direita observa B. Sem entrada ativa ou nas barras fora da área da fase, o equilíbrio volta a zero. Ao soltar o toque, o olhar fica neutro; no desktop, mover o mouse ativa novamente a observação.
- Na fatia jogável, a leitura **horizontal é o padrão provisório**, para evitar perseguir dois alvos móveis. `Tab` permite comparar com proximidade. O caminho de Reflexo é mais longo, então deixar a simulação neutra não sincroniza as chegadas.
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
- A primeira fatia usa três medidas no pote e um ponto de controle após ambos passarem de 48% da trilha. Tinta e posição são recuperadas ao retornar; perder a última medida encerra a sessão.

### 4.3.1 Correntes da primeira fatia
- Os antigos ícones de X e círculo foram substituídos por troncos que se deslocam transversalmente à trilha. O espaço livre é a indicação visual da passagem; aproximar-se do tronco custa tinta, aumenta o desequilíbrio e empurra o herói.
- **Decisão experimental:** a maré é um relógio ambiental compartilhado da sessão (pausa junto com ela), independente dos relógios locais dos heróis. Isso permite esperar uma abertura alterando o tempo de um mundo; se herói e abertura usassem exatamente o mesmo relógio e progresso, desacelerar não mudaria o resultado.

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

### Direção visual aprovada (conceitos V1)

- Dois heróis são manifestações da mesma pessoa de aparência andrógina: Sombra em índigo e Reflexo em marfim. Cabelo curto assimétrico, presilhas em lados opostos, suéter amplo, calça curta e tênis simples; expressão contida e vista elevada. A influência de *Serial Experiments Lain* está no clima e no cotidiano dos anos 1990, com personagens próprios.
- Marca: duas cordas índigo/marfim unidas num nó compacto, sem alças de laço de presente. A logo V1 coloca o nó acima do nome `NÓS`, com letras serifadas e textura de tinta.
- Na roupa, o símbolo é um bordado pequeno e discreto, rente ao tecido e adaptado à perspectiva; não um fecho volumoso. Referências em `design/`.
- Os conceitos aprovados são referências para sprites e marca. Legibilidade em tamanho de jogo e acessibilidade da paleta ainda precisam de validação.
- Na fatia jogável, as ilustrações aprovadas já são usadas como sprites estáticos com deslocamento e oscilação suave. Não são ainda animações de caminhada por direção. O ambiente é uma aquarela panorâmica original gerada por IA, sem repetição de tiles. Os arquivos de referência originais continuam em `design/`.
- Tipografia: Cormorant Garamond para títulos e Atkinson Hyperlegible para leitura, rasterizadas em alta resolução; licenças OFL acompanham os atlas em `assets/ui/`.
- Abertura: sequência de aproximadamente dez segundos em camadas animadas, pulável por clique ou tecla, antes da tela de início. Água, duas presenças, nó e logo; texto mínimo. Não é um vídeo renderizado nem contém áudio nesta versão.

### Tratamento previsto

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
- [x] Percurso contínuo com câmera acompanhando os personagens, escolhido pelo usuário após testar a arena fixa.
- [ ] Validar a nova leitura horizontal, a sinalização dos troncos e o ritmo da travessia com o usuário.
- [ ] Validar maré ambiental compartilhada e balanceamento do primeiro recorte de Água.
- [ ] Números de balanceamento (k, limiares, Δt, tamanhos do pote)
- [ ] Paleta final das duas tintas
- [x] Direção inicial dos personagens, nó de duas cores e logo V1 (conceitos em `design/`; paleta final e legibilidade ainda em teste)
- [ ] Imagem final da galeria em ÍRIS
- [ ] Textos: 13 aforismos, 13 versos e 13 versos ocultos
- [ ] Lista final de pincéis
