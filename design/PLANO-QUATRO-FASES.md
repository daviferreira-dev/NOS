# NÓS — campanha de quatro fases

Decisão do usuário: reduzir o jogo inteiro de treze para quatro fases, aumentar ação e substituir a abertura que alternava duas imagens. Esta decisão prevalece sobre o escopo histórico do GDD.

| Fase | Papel | O que o jogador faz | Diferença principal |
|---|---|---|---|
| 1 — Água / Encontro | Introdução à dupla | Pede ajuda, atravessa a ponte, fixa a escora e resolve o pátio curto | Cooperação sem perigo ambiental |
| 2 — Fogo / Ruptura | Introdução à ação | Transfere a jardineira e escolhe quando atravessar brasas móveis | Antecipação, movimento e janelas de passagem |
| 3 — Vento / Distância | Revezamento | Contorna rajadas, atravessa e sustenta a passagem para Reflexo | O ambiente desloca o jogador; os papéis se invertem |
| 4 — Íris / Convergência | Síntese e encerramento | Combina peso, revezamento, brasas e rajadas até a saída final | Sequência completa com sete objetivos e conclusão própria |

## Redução e reaproveitamento

Não produzir quatro fases de Água, mais quatro de Fogo e quatro de Vento. Os antigos protótipos Água 02/03 viram a base de Fogo e Vento, com perigos, apresentação e papel próprios. A expansão eastRequiresCart proposta anteriormente não integra este recorte. Evitar novas salas que apenas mudem a posição de dois pedais.

Água apresenta o básico; Fogo acrescenta leitura de um perigo; Vento introduz deslocamento e revezamento; Íris usa as regras já aprendidas. Nenhuma quinta fase ou capítulo adicional está planejado. A duração e a diversão serão medidas pelo teste do usuário, sem tratar compilação como aprovação de jogabilidade.

## Ação e recuperação

Shift mantido permite correr quando não se carrega peso. A jardineira conserva velocidade e colisão próprias. Brasas têm antecipação e intervalo de repouso; contato recupera Sombra em piso seguro e preserva mecanismos resolvidos. Rajadas empurram pela colisão do mapa. Se necessário, a peça é solta para não esticar o alcance de quem a segura. Reflexo continua autônomo, sem dano ou teleporte por perigos. Não há cronômetro global, vidas, barras ou combate nesta versão.

## Abertura

Cinco planos de 24 segundos no total: água/reflexo; encontro na margem; travessia do ambiente de Fogo; deslocamento em Vento; aproximação em Íris. Os planos usam cinco cenários distintos e os sprites caminham em tempos diferentes, com pausas e poses de repouso. Essa é animação 2D por camadas; não é a cinematográfica CGI descartada. Sem voz sintética. A marca aparece no menu, separada dos personagens. Pular ou terminar o filme leva apenas ao menu; começar exige comando explícito.

## Entrega e limites

Quatro mapas, progressão e conclusão final; salvamento compatível com as três versões anteriores; desktop e Web/TeaVM compilados; testes de percurso, recuperação e pausa. Navegador e Android precisam de teste próprio. A revisão do usuário ainda deve avaliar ação, repetição, legibilidade e a nova abertura.

Arte nova criada com `image_gen` integrado: `assets/art/fire-bank-v1.png`, `wind-bank-v1.png`, `iris-convergence-v1.png` e `ember-v1.png`. Prompts finais em `fire-v9-prompt.txt`, `wind-v9-prompt.txt`, `iris-v9-prompt.txt` e `ember-v9-prompt.txt`, nesta pasta.
