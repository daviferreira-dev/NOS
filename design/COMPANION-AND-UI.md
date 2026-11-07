# Sombra, Reflexo e revisão de interface — 07/11/2026

O usuário aprovou testar uma presença controlável e outra pessoa autônoma com o mesmo rosto; também pediu trabalho em paralelo na interface após compartilhar uma análise de HUD.

## Recorte jogável

Sombra recebe movimento; Reflexo tem reação, histerese de acompanhamento, uma observação curta da água e cooperação no guincho. A observação ocorre uma vez; pedidos de ajuda têm prioridade e o afastamento do jogador interrompe a pausa. Não é uma IA narrativa generativa: são comportamentos determinísticos para testar a relação.

Pedir ajuda não muda o controle e não teletransporta Reflexo. Ele percorre a margem até o mecanismo; espera se Sombra ocupa o ponto, sustenta a ponte ao chegar e continua até ser chamado de volta ou até Sombra fixar a escora. Depois, se reúne pelo piso real. A escora depende da interação do jogador; Reflexo não conclui a sala sozinho.

## Interface

Ajuda/pausa modal com simulação parada e fundo escurecido. Comandos agrupados, teclas visuais, títulos serifados e painéis em papel creme. Objetivo expande após mudar, depois se recolhe para resumo com "Etapa N de 3". Atalhos reduzem com o tempo. Teclas de jogo remapeáveis e tamanho de texto 100/115/130%, salvos por Preferences; setas e Escape mantêm aliases. M controla música; F6 texto; F7 configuração dentro da pausa; F8 restaura teclas; F11 alterna tela cheia/janela. Não há gamepad.

Sombra recebe marcador em forma e contato mais forte. O rótulo do jardim tem fundo legível e fica fora da coluna. Riscos soltos sobre a água foram removidos; laterais usam cor da paisagem. A planta e as colisões não foram alteradas.

## Arte da pedra

Nova imagem gerada pela ferramenta integrada image_gen, com `assets/art/bridge-bank-v1.png` como referência de estilo/perspectiva. Arquivo final no projeto: `assets/art/bridge-stone-v2.png`. Fundo transparente original preservado; recorte só em TextureRegion durante a apresentação. O sprite antigo permanece para o legado.

### Prompt final

Use case: stylized-concept. Asset: a single transparent environment sprite for the game NÓS. The supplied image is the STYLE and PERSPECTIVE reference only. Paint ONE compact low rounded BOULDER of warm gray weathered stone, clearly a solid three-dimensional object. Match the reference's muted watercolor washes and fine pencil edges, the same slightly overhead orthographic game camera and soft light from upper-left. A squat irregular rounded silhouette wider than tall (about 1.45:1), with a large pale taupe-gray upper face, two shaded lavender-gray side planes, a few broad stone cracks, tiny moss ONLY in one or two crevices at the base. Stone should dominate, not green moss. Clear readable top-versus-side shading when reduced to 65x45 pixels. A very thin attached contact-occlusion shade at the base, no detached shadow. Center the whole boulder with about 8% transparent padding. No floor, grass patch, dirt island, extra pebbles, foliage, lettering, glow or graphic outline. No photorealism or low-poly angular rock. Actual transparent background, retain delicate painted edge. Do not repaint the whole scene; output only this isolated game prop.

## Limites

É o mesmo encontro curto da ponte, com a relação e a leitura revistas. A diferenciação de memórias e escolhas ao longo de capítulos, animações completas em oito direções e gamepad continuam fora deste recorte. A qualidade da relação e a diversão ainda precisam do teste do usuário.
