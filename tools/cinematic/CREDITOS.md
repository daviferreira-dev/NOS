# Créditos e procedência — NÓS CGI v1

## Arte e som próprios

Direção, cena, iluminação, materiais procedurais, rig de mão, animação, montagem, texto em português, composição e programação instrumental preparados para o NÓS. A marca aprovada do jogo é reutilizada no encerramento. Não foram usados vídeo, trilha, diálogo, modelo de personagem ou voz de Dark Souls ou Serial Experiments Lain.

## Geometria da mão

Base humana do MakeHuman Community, somente a região da mão e do punho, extraída e adaptada com rig próprio. O arquivo `base.obj` identifica expressamente a licença CC0. O MakeHuman distingue seus assets CC0 do código de ferramentas sob outras licenças.

- [Arquivo de origem](https://github.com/makehumancommunity/makehuman/blob/master/makehuman/data/3dobjs/base.obj)
- [Licenciamento oficial](https://www.makehumancommunity.org/about/license.html)
- SHA256 do OBJ utilizado: `8e761e6624b8f54536409135d1636da63b32486a90d4897f84e121d144f6fb4c`.

## Piano e ambiente

Piano do banco **GeneralUser GS 2.0.3**, de S. Christian Collins, executado pelo TinySoundFont. A licença do banco permite sua utilização gratuita em composições e gravações, inclusive comerciais. A partitura e a programação de tempo/dinâmica foram preparadas para este plano, sem reutilizar uma música existente. A água é um ambiente discreto criado por processamento de sinal, não uma gravação de campo.

- [GeneralUser GS e documentação oficial](https://github.com/mrbumpy409/GeneralUser-GS)
- [Licença do banco](https://github.com/mrbumpy409/GeneralUser-GS/blob/main/documentation/LICENSE.txt)
- [TinySoundFont Python, licença MIT](https://github.com/amberwhitehead/tinysoundfont-pybind)

O usuário rejeitou a voz sintética do teste e escolheu adiar a narração. A prévia entregue não contém fala, coro ou samples vocais. Os experimentos de voz anteriores não são incorporados à entrega nem ao jogo.

## Ferramentas e tipografia

- [Blender 4.5 LTS](https://www.blender.org/download/lts/4-5/), aplicação portátil, licença GPL. Checksum oficial do ZIP conferido antes da execução: `52dd02fd74877fa08e74b67d7854cf32468a232c02aaa19b6844c91285574f8d`.
- FFmpeg já existente no computador, usado apenas para montagem e codificação; nenhum binário é incorporado ao jogo.
- Atkinson Hyperlegible para a legenda, com licença OFL já usada no projeto. A marca ilustrada mantém sua composição original.

As cópias das licenças e manifestos de origem estão na pasta `licenses/`. Os WAV/MP3/MP4 são mídia pré-renderizada: o jogo não precisa de Blender, Python ou TinySoundFont para reproduzir esses arquivos futuramente.
