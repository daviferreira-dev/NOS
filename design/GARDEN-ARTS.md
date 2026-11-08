# Contrapesos do pátio — arte e áudio

Quatro sprites gerados com a ferramenta integrada **image_gen**, sem CLI/API externa paga. Imagens novas, transparência RGBA original preservada. Recortes em `TextureRegion`, sem remover fundo ou pintar por Python. Os heróis e o cenário aprovado foram preservados.

Arquivos: `assets/art/garden-planter-v1.png`, `garden-pedal-v1.png`, `garden-gate-v1.png` e `garden-flowerbed-v1.png`. A grade contém moldura e painel em células separadas; o painel é recortado ao subir atrás da moldura. Canteiros e objetos têm a mesma planta consultada pela colisão. A jardineira entra na ordenação por profundidade com os personagens.

## Prompt comum, usado em cada chamada

Use case: stylized-concept. Production asset for the 2D game NÓS. Original refined hand-painted watercolor and fine graphite edges, warm parchment stone, muted olive foliage, dusty lavender flowers and weathered taupe-brown wood. Match an elevated orthographic game view from about 35 degrees above, gentle light from upper left. Clear readable silhouette when reduced to about 60-100 pixels wide, carefully constructed anatomy of objects and plausible supports. Delicate watercolor texture and soft contact shade attached directly to base. Actual transparent RGBA background, no floor island, no white matte, no text, symbols, labels, UI, people, neon, flat vector blocks or photorealism. Center isolated object with clean breathing room.

## Sujeitos acrescentados ao prompt comum

**Jardineira:** ONE heavy low rectangular weathered wooden garden planter on four small iron-rimmed wheels, two near wheels clearly visible and far wheels mostly hidden by the box in perspective. Modest cluster of tiny ivory and lavender flowers and muted green leaves growing from soil. A low simple horizontal wooden pushing rail on the near side, plausible hardware. Squat portable counterweight, about 1.5 times wider than tall, foot contact clear. It should look like a specific aged object belonging in a wisteria courtyard, not a generic crate. Output only this planter.

**Pedal:** ONE low pressure treadle: a wide wooden plank panel mounted in a shallow warm limestone rim, four aged wood slats, a small visible dark iron hinge along its far edge. Top face clearly visible, a slight raised near lip shows this can depress under weight. No inscription or glow. Very low profile, approximately 1.65 times wider than tall including the view of its top. Output only this floor mechanism.

**Grade:** A single coherent gate asset sheet, two equal cells arranged horizontally with a large transparent gutter. LEFT CELL: an old warm limestone rectangular doorway frame with two slim weathered posts and a lintel, a few restrained wisteria tendrils on the top corners, completely transparent empty opening. RIGHT CELL: only the wooden sliding lattice panel that fits that opening, without posts, vines, handles or background; a sensible criss-cross trellis reinforced with two crossbars. Frame and panel viewed at the exact same slightly elevated orthographic front angle, no rotation between cells, all edges uncut. The panel is intended to slide upward behind the lintel in the game. This is one mechanical prop split into its two compositing layers, no duplicated doors.

**Canteiro:** ONE long low warm gray limestone flowerbed edge, a few naturally irregular rounded blocks enclosing muted olive leaves and small dusty lavender and ivory flowers. About 3 times wider than tall. Dense low planting with a clear solid stone boundary and attached base shade. It is a small courtyard barrier seen from slightly above, not a tall wall and not a floating green blob. No extra scattered standalone objects. Output only this flowerbed.

## Música

`assets/audio/o-jardim-escuta.ogg`: 112 segundos, estéreo 32 kHz. Composição e programação próprias, piano e intervenções espaçadas de harpa do banco gratuito GeneralUser GS, de S. Christian Collins. Fonte: `tools/compose_garden_score.py`; usa TinySoundFont no ambiente de autoria, sem biblioteca nova no jogo. Não é uma gravação de pianista. Licença do banco em `assets/audio/GeneralUser-LICENSE.txt` e fonte oficial em https://github.com/mrbumpy409/GeneralUser-GS . A voz sintética rejeitada não faz parte do jogo ou deste tema.

O tema entra por crossfade ao chegar ao pátio, mantendo o limite de volume, mute salvo e pausa ao perder foco. A integração foi exercitada com volume zero para não interferir nos outros trabalhos do usuário. Avaliação musical permanece com o usuário.
