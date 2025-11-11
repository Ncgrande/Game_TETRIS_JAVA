Instruções para adicionar uma fonte pixel ao projeto

1) Baixe uma fonte pixel livre, por exemplo "Press Start 2P" (SIL Open Font License) disponível em:
   https://fonts.google.com/specimen/Press+Start+2P

2) Coloque o arquivo TTF dentro desta pasta:
   src/main/resources/fonts/PressStart2P-Regular.ttf

3) A classe `ScorePanel` tentará carregar automaticamente esse arquivo. Se não encontrar, ela usará uma fonte monospace de fallback (Consolas).

4) Para mudar a fonte, substitua o arquivo TTF pelo nome e variante desejada e atualize o caminho no código se necessário.

Observação: Não é recomendado commitar fontes que não possuem permissão adequada. Use fontes com licença permissiva (ex: OFL) ou que você possua permissão para distribuir.