# Atlas tipográficos

`FontAtlas.java` gera os arquivos BMFont de `assets/ui/` com Java 17/AWT, em modo headless. Não é uma dependência do jogo. As licenças OFL estão junto dos atlas.

Fontes originais: Cormorant Garamond e Atkinson Hyperlegible, obtidas de `google/fonts` (diretórios `ofl/cormorantgaramond` e `ofl/atkinsonhyperlegible`).

Após baixar os TTF, na raiz do projeto:

```powershell
java -Djava.awt.headless=true tools/FontAtlas.java caminho/CormorantGaramond.ttf assets/ui/title 64
java -Djava.awt.headless=true tools/FontAtlas.java caminho/AtkinsonHyperlegible-Regular.ttf assets/ui/body 40
```

Artes em `assets/art/`: conceitos de personagens e marca aprovados pelo usuário; panorama de aquarela e tronco criados com geração de imagens. O renderer usa recortes dos personagens, sem modificar os originais em `design/`. Os sprites ainda não têm quadros de caminhada por direção.
