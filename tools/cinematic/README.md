> Experimento rejeitado pelo usuário. Estes scripts são arquivo de produção, não uma abertura aprovada. Os vídeos foram retirados da pasta ativa; não integrar nem retomar esta tentativa.

# Produção local do piloto CGI

Ferramentas de autoria externas ao libGDX; não adicionam dependências ao Gradle. A instalação e o uso gratuito dessas ferramentas foram aprovados pelo usuário. Não há chamadas a APIs pagas.

Arquivos principais:

- `setup_free_tools.py`: Blender 4.5.15 portátil, verificação SHA256, ambiente de áudio e base CC0 da mão.
- `setup_sound.py`: piano GeneralUser GS e licenças oficiais.
- `cinematic_pilot.py`: constrói, anima e renderiza a cena.
- `render_background.py`: executa o render sem janela, duas threads CPU e prioridade baixa.
- `prepare_editable.py`: salva a água em shape keys e gera o Blender editável.
- `inspect_scene.py`: verifica movimento e ausência de texturas externas em uma cena reaberta.
- `narration.json`: textos propostos, usados como legendas e separados do código. A narração foi adiada por decisão do usuário.
- `score.json`: notas, duração e dinâmica da composição.
- `compose_pilot_score.py`: piano amostrado, ambiente discreto, mixagem e legenda, sem voz.
- `assemble_pilot.py`: sequência de PNG, marca, legenda, mixagem e MP4.

## Ambiente usado

Windows x64, Python 3.12. Blender 4.5.15 LTS, TinySoundFont 0.3.7, SoundFile 0.14.0 e NumPy 2.5.4. FFmpeg disponível no PATH.

As ferramentas locais ficam em `C:/Users/PC Gamer/Documents/Codex/tools/nos-cinema`. Os scripts aceitam `NOS_CINEMA_TOOLS` para escolher outra pasta. A instalação não modifica PATH, registro ou pacotes Python globais. O Blender verifica o checksum antes de extrair; o ambiente de áudio tem seu próprio Python.

Para reproduzir o piloto, copie esta pasta para uma pasta de trabalho fora do código do jogo. `render_background.py` e os scripts de áudio gravam ali seus arquivos. `assemble_pilot.py` usa o caminho do projeto e o arquivo TTF indicados no início do script; ajuste esses dois caminhos ao mudar de computador. O render direto também aceita `NOS_CINEMA_WORK` para definir sua saída.

```powershell
python setup_free_tools.py
# voice-env é o nome histórico do ambiente isolado; a prévia atual é instrumental.
& "$env:NOS_CINEMA_TOOLS/voice-env/Scripts/python.exe" setup_sound.py
& "$env:NOS_CINEMA_TOOLS/voice-env/Scripts/python.exe" compose_pilot_score.py
python render_background.py
python prepare_editable.py
python assemble_pilot.py
```

Defina `NOS_CINEMA_TOOLS` com a pasta escolhida antes desses comandos. Para um quadro de inspeção, execute o Blender em background com `--python cinematic_pilot.py -- --still --width 768 --samples 12`. Para salvar a cena com água animada independente do script, use `--save-only --bake-water`.

O arquivo editável final não exige habilitar scripts automáticos. A câmera anima lentamente e a mão possui animação de corpo e articulações; a água é deformada em geometria. Isso difere da abertura ilustrada atualmente distribuída no jogo, baseada em planos de imagens.

## Estado de integração

O piloto de oito segundos é entregue para avaliação de técnica e direção, com música, ambiente e legenda. O usuário rejeitou a voz sintética e escolheu adiar a narração. Ainda não existe a cinematográfica completa com os dois rostos e corpos. Não foi adicionada uma dependência de vídeo a desktop, Android ou TeaVM. A integração futura precisa preservar pular → menu → início explícito e uma alternativa de apresentação onde não houver decodificação de vídeo.
