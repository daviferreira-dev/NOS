# NÓS — Direção da cinematográfica CGI, v1

Status: piloto 3D curto concluído com ferramentas gratuitas locais. O jogo continua com a abertura ilustrada atual. O quadro visual deste documento é uma referência estática de aparência CGI; o vídeo piloto é uma cena 3D distinta, com mão, tecido e água animados. A cinematográfica completa com os dois personagens ainda não foi produzida.

## Direção aprovada pelo usuário

Cinematográfica em CGI com narração falada, buscando o melhor acabamento viável. Preservar os rostos, cabelo, grampos, roupa índigo/marfim e o pequeno nó dos personagens aprovados. Materiais detalhados, luz cinematográfica e atuação sutil. Manter a paleta do NÓS e o mistério; não explicar a história nem copiar texto, personagens ou voz da referência de Dark Souls.

## Proposta de duração e planos

| Tempo aproximado | Imagem e movimento | Voz / som |
| --- | --- | --- |
| 0–7 s | Câmera rente à água; correnteza, névoa e tecido em movimento. Revelar a margem aos poucos. | Água e uma nota sustentada. “Algumas lembranças chegam antes de nós.” |
| 7–15 s | Sombra se ajoelha e estende a mão; cotovelo, dedos, cabelo e roupa respondem ao gesto. | “Outras… usam o nosso rosto.” Pausa. |
| 15–23 s | Close da mão e do reflexo: o movimento refletido se antecipa discretamente; Sombra hesita. | A música recua no toque. “E quando a água se aquieta…” |
| 23–31 s | Reflexo vira o olhar para Sombra; rostos iguais, expressões distintas. A corda entre as mãos ganha tensão. | “Nem sempre é você quem olha de volta.” |
| 31–36 s | Plano abre, transição para escuro e título em área livre. | Última nota e reverberação. Menu espera Começar. |

A duração final depende da interpretação da voz e da montagem. Priorizar movimento corporal convincente e continuidade dos rostos em poucos planos antes de alongar a cena.

## Texto original da narração

Algumas lembranças chegam antes de nós.

Outras… usam o nosso rosto.

E quando a água se aquieta…

nem sempre é você quem olha de volta.

## Direção de voz e música

**Decisão atual:** a voz sintética foi rejeitada pelo usuário. Concluir o piloto com música, ambiente e legenda; narração fica para depois.

Proposta inicial: voz feminina adulta, baixa, íntima e pausada, em português brasileiro; interpretação contida, sem imitar nenhuma atriz. Legendas separadas do vídeo para leitura e acessibilidade. Água, respiração e tecido deixam espaço para a voz. Composição específica para a cena, com desenvolvimento e encerramento; evitar o loop curto da abertura anterior. Preservar o volume baixo da música e evitar salto de intensidade ao entrar a fala.

## Produção e validação

1. Validar o quadro de aparência CGI com os personagens existentes.
2. Gerar um plano de 6–8 segundos com ação corporal para avaliar identidade, anatomia e movimento.
3. Produzir narração e ajustar o ritmo antes de gerar todos os planos.
4. Revisar mãos, rostos, reflexo, continuidade de roupa, corda, contato com o chão e áudio.
5. Montar, legendar e só então integrar ao jogo. A cutscene continua pulável e termina no menu, sem começar a partida automaticamente.

Produção gratuita aprovada e preparada: Blender 4.5.15 LTS portátil, ambiente Python isolado, piano amostrado gratuito GeneralUser GS/TinySoundFont e FFmpeg existente. A voz sintética foi rejeitada pelo usuário; a narração fica adiada e a prévia usa música, ambiente e legenda. Nenhuma compra, assinatura ou API paga. Render em segundo plano, CPU com duas threads e prioridade baixa. `outputs/CGI-v1/` contém o piloto de oito segundos, a trilha instrumental, a cena editável e o relatório de validação. `tools/cinematic/` guarda as fontes e licenças. A água do .blend está gravada em shape keys, sem exigir execução automática de scripts.

Este plano testa gesto, água, câmera, música e legenda. O rosto, cabelo, roupa e atuação dos dois personagens completos ainda precisam de produção e continuidade. O acabamento atual não equivale à imagem de referência nem a uma cinematográfica de grande estúdio.

Não adicionar uma dependência de reprodução de vídeo ao jogo antes de ter mídia revisável e cumprir a regra do AGENTS.md sobre novas dependências.

## Direção visual do quadro de referência

CGI de aparência realista estilizada: Sombra de suéter índigo ajoelhada junto ao rio, mão próxima da superfície; Reflexo de marfim atrás, mesmo rosto e expressão própria. Fio discreto entre os pulsos, pedra antiga, glicínias, névoa e luz suave da manhã. Materiais detalhados e composição com espaço para os personagens, sem logo sobre os corpos. A imagem é referência de direção, não prova de modelagem ou animação concluída.
