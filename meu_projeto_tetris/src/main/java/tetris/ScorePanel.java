package tetris;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
//import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField; 
import javafx.stage.Stage; 
import javafx.scene.Scene; 
//import java.util.Optional;
import java.sql.SQLException;
import java.util.List;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.InputStream;
import javafx.application.Platform;


/**
 * Painel dedicado à exibição de informações do jogo (placar e próxima peça).
 */
public class ScorePanel {

    private final GameController controller;
    private final VBox root; 
    private final StackPane containerRoot; 
    private final Label scoreLabel;
    private final Label levelLabel;
    private final Label linesLabel;
    private final Label currentPlayerLabel;
    private final VBox rankingBox; 
    private final Label[] rankingLabels; 
    private final RankingDAO rankingDAO;

    private final Canvas nextPieceCanvas;
    private final StackPane nextPieceContainer;
    private final VBox scoreBoxContainer; 
    private final Button novoJogadorButton;
    private final Button pauseButton; 
    private final int tamanhoBloco;
    
    // CORRIGIDO: pixelBold é o atributo final da classe
    private final Font pixelBold; 
    
    // Tamanho do Canvas de pré-visualização (4 blocos)
    private final int CANVAS_REFERENCE_SIZE = 4;
    private final Label levelUpLabel; 

    // --- Cores Neon ---
    private static final String NEON_PINK = "#FF6EC7";
    private static final String NEON_GREEN = "#39FF14";
    private static final String BUTTON_STYLE = 
        "-fx-background-color: %s; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 5 10 5 10;";


    public ScorePanel(GameController controller, int tamanhoBloco) {
        this.controller = controller;
        this.tamanhoBloco = tamanhoBloco;

        // 1. Inicializa Componentes de Texto
        this.scoreLabel = new Label("Pontuação: 0");
        this.levelLabel = new Label("Nível: 1");
        this.linesLabel = new Label("Linhas: 0");

        // Tenta carregar uma fonte pixel a partir de resources/fonts/
        final String TEXT_COLOR_STYLE = "-fx-text-fill: #00BFFF;"; // Azul-claro da borda
        Font infoFont = null;
        Font localPixelBold = null;
        
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/PressStart2P-Regular.ttf");
            if (is != null) {
                infoFont = Font.loadFont(is, 16);
                InputStream is2 = getClass().getResourceAsStream("/fonts/PressStart2P-Regular.ttf");
                if (is2 != null) {
                    localPixelBold = Font.loadFont(is2, 17);
                    is2.close();
                } else {
                    localPixelBold = infoFont;
                }
                is.close();
            }
        } catch (Exception e) {
            System.err.println("Não foi possível carregar fonte customizada: " + e.getMessage());
        }

        if (infoFont == null) {
            infoFont = Font.font("Consolas", 16);
            localPixelBold = Font.font("Consolas", FontWeight.BOLD, 17);
        }
        
        // Atribui o atributo final da classe
        this.pixelBold = localPixelBold;

        // --- CORREÇÃO DO ERRO DE FINAL: familyCss declarado como final e atribuído ---
        final String familyCss;
        
        String tempFamilyCss = "";
        try {
            tempFamilyCss = " -fx-font-family: '" + this.pixelBold.getFamily() + "';";
        } catch (Exception ignored) {
        }
        familyCss = tempFamilyCss; // Atribuição final única
        // -----------------------------------------------------------------


        // Aplica fontes e estilos iniciais
        scoreLabel.setFont(infoFont);
        levelLabel.setFont(infoFont);
        linesLabel.setFont(infoFont);
        scoreLabel.setStyle(TEXT_COLOR_STYLE);
        levelLabel.setStyle(TEXT_COLOR_STYLE);
        linesLabel.setStyle(TEXT_COLOR_STYLE);

        // 2. Inicializa Canvas e Contêiner da PRÓXIMA PEÇA
        int canvasPixelSize = tamanhoBloco * CANVAS_REFERENCE_SIZE;
        this.nextPieceCanvas = new Canvas(canvasPixelSize, canvasPixelSize);

        // StackPane empilha o Canvas e o Label "PRÓXIMA PEÇA"
        this.nextPieceContainer = new StackPane(this.nextPieceCanvas, new Label(""));
        this.nextPieceContainer.setMaxSize(canvasPixelSize, canvasPixelSize);
        this.nextPieceContainer.setPadding(new Insets(3));
        this.nextPieceContainer.setStyle(
                "-fx-border-color: #00BFFF; -fx-border-width: 3; -fx-border-radius: 3; -fx-background-color: black;");

        // 3. Criação e Estilização do Contêiner de PLACAR (SCORE BOX)
        this.scoreBoxContainer = new VBox(5);
        scoreBoxContainer.setPadding(new Insets(10));
        scoreBoxContainer.setAlignment(Pos.CENTER_LEFT);

        // Adiciona as labels de informação ao ScoreBox
        scoreBoxContainer.getChildren().addAll(
                scoreLabel,
                levelLabel,
                linesLabel);
        // Aplica o estilo de borda azul e fundo preto para o bloco de pontuação
        scoreBoxContainer.setStyle(
                "-fx-border-color: #00BFFF; -fx-border-width: 3; -fx-border-radius: 3; -fx-background-color: black;");

        // 4. Inicializa componentes do Ranking
        this.rankingDAO = new RankingDAO();
        this.rankingBox = new VBox(5);
        rankingBox.setAlignment(Pos.CENTER_LEFT);
        rankingBox.setPadding(new Insets(10));
        rankingBox.setStyle(
                "-fx-border-color: #00BFFF; -fx-border-width: 3; -fx-border-radius: 3; -fx-background-color: black;");

        // Inicializa as labels do ranking
        this.rankingLabels = new Label[3];
        Label rankingTitle = new Label("Top 3 Jogadores");
        rankingTitle.setFont(infoFont);
        rankingTitle.setStyle(TEXT_COLOR_STYLE);
        rankingBox.getChildren().add(rankingTitle);

        // Cria as 3 labels para o ranking
        for (int i = 0; i < 3; i++) {
            rankingLabels[i] = new Label("-");
            rankingLabels[i].setFont(infoFont);
            rankingLabels[i].setStyle(TEXT_COLOR_STYLE);
            rankingBox.getChildren().add(rankingLabels[i]);
        }

        // 5. Monta o Layout Raiz (root)
        this.root = new VBox(15);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.TOP_LEFT);
        // Garantir fundo transparente
        root.setStyle("-fx-background-color: transparent;");

        // Label de LEVEL UP (invisível por padrão) sobreposta via containerRoot
        Label localLevelUpLabel = new Label("LEVEL UP!");
        localLevelUpLabel.setFont(Font.font(pixelBold.getFamily(), 22));
        localLevelUpLabel.setStyle(
                "-fx-text-fill: yellow; -fx-font-weight: bold; -fx-effect: dropshadow( gaussian , rgba(255,255,0,0.9), 8, 0.7, 0, 0);");
        localLevelUpLabel.setVisible(false);
        localLevelUpLabel.setOpacity(0.0);

        // containerRoot permite colocar o label de level-up por cima do VBox sem
        // bagunçar o layout
        this.containerRoot = new StackPane(this.root, localLevelUpLabel);
        StackPane.setAlignment(localLevelUpLabel, Pos.TOP_CENTER);
        StackPane.setMargin(localLevelUpLabel, new Insets(6, 0, 0, 0));

        // Botão para cadastrar/definir novo jogador
        this.novoJogadorButton = new Button("Novo Jogador");
        this.novoJogadorButton.setFocusTraversable(false);
        this.novoJogadorButton.setStyle(String.format(BUTTON_STYLE, NEON_GREEN));
        
        // --- Lógica do Novo Jogador (CHAMA O DIÁLOGO CUSTOMIZADO) ---
        // familyCss é passado para o novo método
        novoJogadorButton.setOnAction(evt -> {
            showCustomInputDialog(familyCss); 
        });

        // Label que mostra o jogador atual
        this.currentPlayerLabel = new Label("Jogador: —");
        currentPlayerLabel.setFont(pixelBold);
        currentPlayerLabel.setStyle(TEXT_COLOR_STYLE + " -fx-font-weight: bold;" + familyCss);

        
        this.pauseButton = new Button("Pausar (Esc)");
        this.pauseButton.setFocusTraversable(false);
        this.pauseButton.setStyle(String.format(BUTTON_STYLE, NEON_PINK));

        this.pauseButton.setOnAction(evt -> {
            controller.togglePause(); 
        });

        
        // 1. Cria um espaço flexível (Region)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 2. Cria um HBox para os botões Novo Jogador e Pausar
        HBox topButtonRow = new HBox(10); 
        topButtonRow.setAlignment(Pos.TOP_LEFT);
        topButtonRow.setMaxWidth(Double.MAX_VALUE); 
        
        topButtonRow.getChildren().addAll(
            this.novoJogadorButton, 
            spacer, 
            this.pauseButton 
        );

        // Adiciona a Pré-Visualização e o Bloco de Pontuação
        Label nextPieceTitleLabel = new Label("Próxima Peça"); 
        nextPieceTitleLabel.setFont(pixelBold); 
        nextPieceTitleLabel.setStyle(TEXT_COLOR_STYLE + " -fx-font-weight: bold;" + familyCss); 

        // Ordem final no VBox 'root':
        root.getChildren().addAll(
                topButtonRow, 
                currentPlayerLabel, 
                nextPieceTitleLabel, 
                nextPieceContainer, 
                scoreBoxContainer, 
                rankingBox 
        );

        controller.setScorePanel(this);
        update();

        // guarda label de level-up acessível pelo método showLevelUp
        this.levelUpLabel = localLevelUpLabel;
    }

    // Método que retorna o nó raiz
    public Parent getVisualComponent() {
        return containerRoot;
    }

    /**
     * Mostra uma notificação temporária de Level Up (UI thread)
     */
    public void showLevelUp() {
        Platform.runLater(() -> {
            try {
                levelUpLabel.setVisible(true);
                levelUpLabel.setOpacity(1.0);
                try {
                    controller.playLevelUpTest();
                } catch (Exception ex) {
                    System.err.println("Erro ao disparar SFX de level-up a partir do ScorePanel: " + ex.getMessage());
                }
                PauseTransition pause = new PauseTransition(Duration.millis(1500));
                pause.setOnFinished(ev -> {
                    FadeTransition ft = new FadeTransition(Duration.millis(400), levelUpLabel);
                    ft.setFromValue(1.0);
                    ft.setToValue(0.0);
                    ft.setOnFinished(e -> levelUpLabel.setVisible(false));
                    ft.play();
                });
                pause.play();
            } catch (Exception e) {
                System.err.println("Erro ao mostrar LevelUp: " + e.getMessage());
            }
        });
    }

    /**
     * Método chamado pelo GameController para atualizar os dados do painel.
     */
    public void update() {
        Platform.runLater(() -> {
            Partida partida = controller.getPartida();

            // Atualiza a label do jogador atual
            Jogador jogadorAtual = partida.getJogador();
            if (jogadorAtual != null) {
                currentPlayerLabel.setText("Jogador: " + jogadorAtual.getNome());
            } else {
                currentPlayerLabel.setText("Jogador: —");
            }

            // ATUALIZAÇÃO: Lógica para o botão de Pausa
            boolean isGameOver = partida.isGameOver();
            boolean isPaused = controller.isPaused();
            
            if (isGameOver) {
                pauseButton.setText("FIM DE JOGO");
                pauseButton.setDisable(true);
                pauseButton.setStyle(String.format(BUTTON_STYLE, NEON_PINK));
            } else {
                pauseButton.setText(isPaused ? "Continuar (Esc)" : "Pausar (Esc)");
                pauseButton.setDisable(false);
                pauseButton.setStyle(String.format(BUTTON_STYLE, NEON_PINK));
            }
            
            // Recarrega o estilo do Novo Jogador também no update, por segurança
            novoJogadorButton.setStyle(String.format(BUTTON_STYLE, NEON_GREEN));


            // 1. Atualizar Placar
            scoreLabel.setText("Pontuação: " + partida.getPontuacao());
            levelLabel.setText("Nível: " + partida.getNivel());
            linesLabel.setText("Linhas: " + partida.getTotalLinhas());

            // 2. Desenhar Próxima Peça
            drawNextPiece();

            // 3. Atualizar o ranking
            atualizarRanking();
        });
    }

    /**
     * Atualiza o ranking com os 3 melhores jogadores
     */
    private void atualizarRanking() {
        new Thread(() -> {
            try {
                List<RankingDAO.RegistroRanking> top3 = rankingDAO.obterTopPontuacoes(3);

                Platform.runLater(() -> {
                    for (int i = 0; i < 3; i++) {
                        if (i < top3.size()) {
                            RankingDAO.RegistroRanking registro = top3.get(i);
                            rankingLabels[i].setText(
                                    String.format("%dº %s: %d pts",
                                            registro.posicao,
                                            registro.nomeJogador,
                                            registro.pontuacao));
                        } else {
                            rankingLabels[i].setText("-");
                        }
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro ao carregar ranking");
                    alert.setContentText("Não foi possível carregar o ranking: " + e.getMessage());
                    alert.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Erro inesperado ao carregar ranking: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * MODIFICADO: Agora aceita familyCss para passá-lo ao alerta customizado.
     */
    private void processAndSaveNewPlayer(String nomeTrim, final String familyCss) {
        // Salvar em background para não travar a UI
        
        // familyCss é passado como parâmetro FINAL, resolvendo o erro de compilação.
        
        new Thread(() -> {
            try {
                Jogador j = new Jogador(nomeTrim);
                new JogadorDAO().salvar(j); 
                
                // Após salvar, associa o jogador à Partida atual e atualiza o painel
                Platform.runLater(() -> {
                    try {
                        controller.getPartida().setJogador(j);
                    } catch (Exception e) {
                        System.err.println("Falha ao associar jogador à partida: " + e.getMessage());
                    }
                    
                    String successMessage = "Jogador salvo e selecionado: " + nomeTrim;
                    // CHAMA O ALERTA CUSTOMIZADO
                    showCustomAlert(successMessage, familyCss);
                    
                    update(); // Atualiza o painel para refletir o novo jogador
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    // Mantemos o Alert padrão para ERROS DE BANCO DE DADOS
                    Alert err = new Alert(Alert.AlertType.ERROR, "Falha ao salvar jogador: " + ex.getMessage());
                    err.show();
                });
            }
        }).start();
    }
    
    /**
     * NOVO MÉTODO: Exibe um Stage de alerta customizado com a estética do jogo.
     */
    private void showCustomAlert(String message, String familyCss) {
        Platform.runLater(() -> {
            Stage alertStage = new Stage();
            alertStage.setTitle("Mensagem");
            alertStage.setResizable(false);

            Label messageLabel = new Label(message);
            messageLabel.setFont(pixelBold);
            // Verde Neon para sucesso
            messageLabel.setStyle("-fx-text-fill: " + NEON_GREEN + ";" + familyCss); 

            Button okButton = new Button("OK");
            okButton.setStyle(String.format(BUTTON_STYLE, NEON_PINK)); // Rosa Neon
            okButton.setPrefWidth(80);
            okButton.setFocusTraversable(false);
            
            okButton.setOnAction(e -> {
                alertStage.close();
            });

            VBox alertLayout = new VBox(15);
            alertLayout.setPadding(new Insets(20));
            alertLayout.setAlignment(Pos.CENTER);
            alertLayout.getChildren().addAll(
                messageLabel,
                okButton
            );
            // Aplica o estilo retro/neon ao contêiner da janela
            alertLayout.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #00BFFF; -fx-border-width: 3;");

            Scene alertScene = new Scene(alertLayout, Color.TRANSPARENT);
            alertStage.setScene(alertScene);
            alertStage.centerOnScreen();
            alertStage.showAndWait();
        });
    }


    /**
     * Cria e exibe um diálogo de entrada de texto customizado com o estilo do jogo.
     * Substitui o TextInputDialog padrão.
     */
    private void showCustomInputDialog(final String familyCss) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Novo Jogador");
        dialogStage.setResizable(false);

        // Componentes do Diálogo Customizado
        TextField nameInput = new TextField();
        nameInput.setPromptText("Digite seu nome aqui");
        nameInput.setStyle("-fx-control-inner-background: black; -fx-text-fill: white; -fx-border-color: #00BFFF; -fx-border-width: 1;");
        nameInput.setPrefWidth(250);

        Label titleLabel = new Label("Informe o nome do jogador");
        titleLabel.setFont(pixelBold);
        titleLabel.setStyle("-fx-text-fill: #00BFFF;" + familyCss);
        
        Label promptLabel = new Label("Nome:");
        // Usa uma fonte um pouco menor, mas da mesma família pixel
        promptLabel.setFont(Font.font(pixelBold.getFamily(), 14)); 
        promptLabel.setStyle("-fx-text-fill: white;" + familyCss);

        Button confirmButton = new Button("Salvar e Selecionar");
        confirmButton.setStyle(String.format(BUTTON_STYLE, NEON_GREEN));
        confirmButton.setFocusTraversable(false);
        
        // Configuração do Layout do Diálogo
        VBox dialogLayout = new VBox(10);
        dialogLayout.setPadding(new Insets(20));
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.getChildren().addAll(
            titleLabel,
            new Label(""), // Espaçamento visual
            promptLabel,
            nameInput,
            new Label(""), // Espaçamento visual
            confirmButton
        );
        // Aplica o estilo retro/neon ao contêiner da janela
        dialogLayout.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #00BFFF; -fx-border-width: 3;");

        // Lógica ao confirmar
        confirmButton.setOnAction(e -> {
            String nomeTrim = nameInput.getText().trim();
            if (nomeTrim.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Nome não pode ser vazio.");
                a.show();
                return;
            }

            dialogStage.close();
            // CHAMA A LÓGICA DE SALVAMENTO: Passa o familyCss
            processAndSaveNewPlayer(nomeTrim, familyCss);
        });

        // Configura a cena e mostra a janela
        Scene dialogScene = new Scene(dialogLayout, Color.TRANSPARENT);
        dialogStage.setScene(dialogScene);
        dialogStage.centerOnScreen();
        dialogStage.showAndWait(); // Torna o diálogo modal
        
        // Garante que o foco volte ao jogo após o fechamento do diálogo
        Platform.runLater(() -> {
            if (root.getScene() != null && root.sceneProperty().get() != null) {
                root.getScene().getRoot().requestFocus();
            }
        });
    }

    /**
     * Desenha a peça no Canvas de Pré-visualização com escala e centralização.
     */
    private void drawNextPiece() {
        GraphicsContext gc = nextPieceCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, nextPieceCanvas.getWidth(), nextPieceCanvas.getHeight());

        Tetromino nextTetromino = controller.getPartida().getProximoTetromino();
        if (nextTetromino == null)
            return;

        Color corPeca = nextTetromino.getCor();
        gc.setFill(corPeca.equals(Color.BLACK) ? Color.GRAY : corPeca);

        boolean[][] forma = nextTetromino.getForma();

        int formaLarguraMaxima = forma[0].length;
        int minX = formaLarguraMaxima;
        int minY = forma.length;
        int maxX = 0;
        int maxY = 0;

        for (int i = 0; i < forma.length; i++) {
            for (int j = 0; j < forma[i].length; j++) {
                if (forma[i][j]) {
                    minY = Math.min(minY, i);
                    minX = Math.min(minX, j);
                    maxY = Math.max(maxY, i);
                    maxX = Math.max(maxX, j);
                }
            }
        }

        int larguraUtil = (maxX - minX) + 1;
        int alturaUtil = (maxY - minY) + 1;

        final double FATOR_ESCALA = 0.8;
        final double tamanhoBlocoReduzido = tamanhoBloco * FATOR_ESCALA;

        double larguraTotalPeça = larguraUtil * tamanhoBlocoReduzido;
        double alturaTotalPeça = alturaUtil * tamanhoBlocoReduzido;

        double sobraHorizontal = nextPieceCanvas.getWidth() - larguraTotalPeça;
        double sobraVertical = nextPieceCanvas.getHeight() - alturaTotalPeça;

        double xOffsetRender = sobraHorizontal / 2.0;
        double yOffsetRender = sobraVertical / 2.0;

        // Desenho
        for (int i = minY; i <= maxY; i++) {
            for (int j = minX; j <= maxX; j++) {
                if (forma[i][j]) {
                    double xPos = xOffsetRender + ((j - minX) * tamanhoBlocoReduzido);
                    double yPos = yOffsetRender + ((i - minY) * tamanhoBlocoReduzido);

                    double xFinal = Math.round(xPos);
                    double yFinal = Math.round(yPos);

                    gc.fillRect(xFinal, yFinal, tamanhoBlocoReduzido, tamanhoBlocoReduzido);
                    gc.setStroke(Color.WHITE);
                    gc.strokeRect(xFinal, yFinal, tamanhoBlocoReduzido, tamanhoBlocoReduzido);
                }
            }
        }
    }
}