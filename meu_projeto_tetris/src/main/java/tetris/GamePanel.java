package tetris;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox; 
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;


public class GamePanel {

    private final GameController controller;
    private final Canvas canvas;
    private final VBox container; // NOVO: Contêiner para a borda
    private final int tamanhoBloco;
    private Image backgroundImage;
    private boolean showingLevelUp = false;
    private int levelUpTicks = 0;
    private static final int LEVEL_UP_DURATION = 24; // Reduzido para 24 frames (~0.4 segundos)
    private static final int BLINK_RATE = 4; // Pisca a cada 4 frames
    private javafx.scene.text.Font pixelFont;

    public GamePanel(GameController controller, int tamanhoBloco) {
            // Tenta carregar a fonte pixel usando InputStream
            try {
                var fontStream = getClass().getResourceAsStream("/fonts/PressStart2P-Regular.ttf");
                if (fontStream != null) {
                    pixelFont = javafx.scene.text.Font.loadFont(fontStream, 24);
                    fontStream.close();
                    if (pixelFont == null) {
                        System.err.println("Falha ao carregar a fonte (loadFont retornou null)");
                    } else {
                        System.out.println("Fonte carregada com sucesso: " + pixelFont.getName());
                    }
                } else {
                    System.err.println("Arquivo de fonte não encontrado");
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar fonte: " + e.getMessage());
                e.printStackTrace();
            }

        this.controller = controller;
        this.tamanhoBloco = tamanhoBloco;

        int larguraCanvas = Tabuleiro.LARGURA * tamanhoBloco;
        int alturaCanvas = Tabuleiro.ALTURA * tamanhoBloco;
        this.canvas = new Canvas(larguraCanvas, alturaCanvas);

        // O VBox será o contêiner visível
        this.container = new VBox(this.canvas);

        // Fixar o tamanho do contêiner ao tamanho do canvas para evitar que o
        // layout expanda/encolha o canvas quando a janela for maximizada.
        this.container.setMinSize(larguraCanvas, alturaCanvas);
        this.container.setPrefSize(larguraCanvas, alturaCanvas);
        this.container.setMaxSize(larguraCanvas, alturaCanvas);

        // Mantemos o contêiner transparente e sem padding; a borda será
        // desenhada diretamente no Canvas para garantir alinhamento perfeito
        // entre borda e área de jogo.
        this.container.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Tenta carregar a imagem de fundo
        try {
            // Primeiro tenta do classpath
            var url = getClass().getResource("/fundo.jpg");
            if (url != null) {
                backgroundImage = new Image(url.toExternalForm());
            } else {
                // Tenta carregar do arquivo durante desenvolvimento
                String devPath = "src/main/resources/fundo.jpg";
                if (new java.io.File(devPath).exists()) {
                    backgroundImage = new Image(new java.io.File(devPath).toURI().toString());
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao carregar fundo.jpg: " + ex.getMessage());
        }
        // --------------------------------------------------------------------

        controller.setGamePanel(this);
        draw();
    }

    // Agora retornamos o contêiner VBox (que é o que tem a borda)
    public Node getVisualComponent() {
        return container;
    }

    /**
     * Desenha o estado atual do jogo. Deve ser chamado pela thread do Controller.
     */
    public void draw() {
        // Garantir que a atualização da UI ocorra na thread correta (JavaFX Application
        // Thread)
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // 1. Desenhar fundo do tabuleiro
            if (backgroundImage != null) {
                // Desenhar a imagem de fundo ajustada ao tamanho do canvas
                gc.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
                // Aplicar uma camada semi-transparente preta para dar contraste às peças
                gc.setFill(Color.BLACK.deriveColor(0, 0, 0, 0.7));
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            } else {
                // Fallback: fundo preto se não houver imagem
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }

            // Desenhar borda externa diretamente no canvas para alinhamento
            gc.setStroke(Color.web("#00BFFF"));
            gc.setLineWidth(3);
            double arc = 8.0;
            // small inset (0.5) to make stroke fully visible inside pixel grid
            gc.strokeRoundRect(0.5, 0.5, canvas.getWidth() - 1, canvas.getHeight() - 1, arc, arc);

            // 2. Desenhar blocos fixos (Tabuleiro)
            drawFixedBlocks(gc);

            // 3. Desenhar o Tetromino atual
            drawActiveTetromino(gc);

            // 4. Desenhar tela de pausa (se estiver pausado)
            if (controller.isPaused()) {
                drawPauseScreen(gc);
            }
            
            // 5. NOVO: Desenhar tela de Game Over (se o jogo acabou)
            if (controller.getPartida().isGameOver()) {
                drawGameOverScreen(gc);
            }

            // Desenhar mensagem de LEVEL UP se estiver ativa
            if (showingLevelUp && levelUpTicks > 0) {
                    // Só mostra a mensagem quando levelUpTicks % BLINK_RATE == 0 (efeito de piscar)
                    if (levelUpTicks % BLINK_RATE == 0) {
                        // Alterna entre duas cores neon (rosa e azul) por fase de piscar
                        int phase = (levelUpTicks / BLINK_RATE) % 2; // 0 ou 1
                        javafx.scene.paint.Color neonPink = javafx.scene.paint.Color.web("#FF6EC7");
                        javafx.scene.paint.Color neonBlue = javafx.scene.paint.Color.web("#00E5FF");
                        javafx.scene.paint.Color fillColor = (phase == 0) ? neonPink : neonBlue;
                        // stroke para contraste (leve escuro)
                        javafx.scene.paint.Color strokeColor = javafx.scene.paint.Color.web("#0A0A0A", 0.9);

                        gc.setFill(fillColor);
                        gc.setStroke(strokeColor);
                        gc.setLineWidth(2);

                        // Usa a fonte pixel se disponível, senão usa Arial como fallback
                        if (pixelFont != null) {
                            gc.setFont(pixelFont);
                        } else {
                            gc.setFont(javafx.scene.text.Font.font("Arial", 28));
                        }

                        String msg = "LEVEL UP!";
                        // Centraliza horizontalmente e posiciona ligeiramente acima do centro
                        var oldAlign = gc.getTextAlign();
                        var oldBaseline = gc.getTextBaseline();
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.setTextBaseline(VPos.CENTER);
                        double cx = canvas.getWidth() / 2.0;
                        // colocar um pouco acima do centro (42% da altura)
                        double cy = canvas.getHeight() * 0.42;
                        gc.strokeText(msg, cx, cy);
                        gc.fillText(msg, cx, cy);
                        // Restaura valores anteriores
                        gc.setTextAlign(oldAlign);
                        gc.setTextBaseline(oldBaseline);
                    }
                    levelUpTicks--;
                    if (levelUpTicks <= 0) {
                        showingLevelUp = false;
                    }
            }
        });
    }

    public void showLevelUpMessage() {
        showingLevelUp = true;
        levelUpTicks = LEVEL_UP_DURATION;
    }

    /**
     * CORREÇÃO APLICADA: Desenha os blocos fixos utilizando o Color[][] do
     * Tabuleiro.
     */
    private void drawFixedBlocks(GraphicsContext gc) {
        Tabuleiro tabuleiro = controller.getPartida().getTabuleiro();
        boolean[] linhasParaRemover = tabuleiro.getLinhasParaRemover();

        // Lógica de piscar (alterna a cor a cada tick de animação)
        boolean flashOn = (controller.getAnimationTicks() % 2 == 1);

        for (int y = 0; y < Tabuleiro.ALTURA; y++) {
            boolean isAnimating = linhasParaRemover[y];

            for (int x = 0; x < Tabuleiro.LARGURA; x++) {

                // Usa o novo método do Tabuleiro para obter a cor
                Color blocoCor = tabuleiro.getBlocoCor(x, y);

                if (blocoCor != null) { // Se houver um bloco fixo (uma cor está presente)

                    if (isAnimating) {
                        // Pisca entre branco/preto durante a animação
                        gc.setFill(flashOn ? Color.RED : Color.GRAY);
                    } else {
                        gc.setFill(blocoCor); // Usa a cor original da peça fixada
                    }

                    gc.fillRect(x * tamanhoBloco, y * tamanhoBloco, tamanhoBloco, tamanhoBloco);

                    // Desenhar borda
                    gc.setStroke(Color.GRAY.darker());
                    gc.strokeRect(x * tamanhoBloco, y * tamanhoBloco, tamanhoBloco, tamanhoBloco);
                }
            }
        }
    }

    /**
     * Desenha a peça ativa (Tetromino em queda).
     */
    private void drawActiveTetromino(GraphicsContext gc) {
        Tetromino tetromino = controller.getPartida().getTetrominoAtual();
        if (tetromino == null)
            return;

        boolean[][] forma = tetromino.getForma();
        Posicao pos = tetromino.getPosicao();

        // CORREÇÃO: Usa a cor REAL da Peça (definida na subclasse)
        gc.setFill(tetromino.getCor());

        for (int i = 0; i < forma.length; i++) {
            for (int j = 0; j < forma[i].length; j++) {
                if (forma[i][j]) {
                    int xAbs = (pos.getX() + j);
                    int yAbs = (pos.getY() + i);

                    if (yAbs >= 0) {
                        gc.fillRect(xAbs * tamanhoBloco, yAbs * tamanhoBloco, tamanhoBloco, tamanhoBloco);

                        // Desenhar borda
                        gc.setStroke(Color.WHITE);
                        gc.strokeRect(xAbs * tamanhoBloco, yAbs * tamanhoBloco, tamanhoBloco, tamanhoBloco);
                    }
                }
            }
        }
    }

    /**
     * Desenha uma tela de pausa semi-transparente.
     */
    private void drawPauseScreen(GraphicsContext gc) {
        // Semi-transparência
        gc.setFill(Color.BLACK.deriveColor(0, 0, 0, 0.5));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Texto de Pausa
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 40));
        gc.fillText("PAUSADO", (canvas.getWidth() / 2) - 90, canvas.getHeight() / 2);
    }
    
    /**
     * NOVO: Desenha a tela de Game Over e a pontuação final.
     */
    private void drawGameOverScreen(GraphicsContext gc) {
        // 1. Fundo do Game Over (Vermelho escuro semi-transparente)
        gc.setFill(Color.web("#8B0000").deriveColor(0, 1.0, 1.0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Configuração de alinhamento para centralizar o texto
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        double cx = canvas.getWidth() / 2.0;
        double cy = canvas.getHeight() * 0.40;

        // 2. Texto de Game Over
        gc.setFill(Color.WHITE);
        
        if (pixelFont != null) {
            gc.setFont(pixelFont.font(40));
        } else {
            gc.setFont(new javafx.scene.text.Font("Arial", 40));
        }

        gc.fillText("GAME OVER", cx, cy);

        // 3. Pontuação Final
        gc.setFill(Color.web("#00BFFF")); // Azul Neon
        
        if (pixelFont != null) {
            gc.setFont(pixelFont.font(20));
        } else {
            gc.setFont(new javafx.scene.text.Font("Arial", 20));
        }
        
        String scoreMsg = "SCORE: " + controller.getPartida().getPontuacao();
        gc.fillText(scoreMsg, cx, cy + 50); // Abaixo do Game Over
        
        // As instruções/botões de jogar novamente serão adicionadas na UI (TetrisApp)
    }
}