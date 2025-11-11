package tetris;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.HBox; 
import javafx.scene.layout.VBox; 
import javafx.scene.control.Button; 
//import javafx.scene.control.Label; 
import javafx.scene.control.Alert; 
//import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
//import javafx.scene.layout.Region;
//import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.File;
import tetris.replay.ReplayManager; // Importar o Manager
import tetris.replay.ReplayData; // Importar o ReplayData


public class TetrisApp extends Application {

    private static final int LARGURA_BLOCO = 30;
    private static final int LARGURA_TABULEIRO = Tabuleiro.LARGURA * LARGURA_BLOCO;
    private static final int ALTURA_TABULEIRO = Tabuleiro.ALTURA * LARGURA_BLOCO;

    private Stage primaryStage;
    private StackPane root;
    private GameController controller;
    private Thread gameThread;
    
    private HBox mainHBox; // Referência ao HBox principal para fácil remoção
    private ImageView bgView; // Referência da imagem de fundo (carregada apenas uma vez)


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new StackPane();
        
        // 1. Carregar Background e Adicionar ao Root (Apenas uma vez)
        this.bgView = createBackgroundView(root);
        if (bgView != null) {
             root.getChildren().add(bgView);
        }
        
        // 2. Inicializa o Jogo (método auxiliar)
        initializeGame();
        
        // 3. Configurar Cena e Palco (mantido)
        double sceneWidth = LARGURA_TABULEIRO + 500;
        double sceneHeight = ALTURA_TABULEIRO + 200;
        
        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        scene.setFill(Color.LIGHTGRAY);

        // 4. Adicionar Controles do Jogador (Teclas)
        scene.setOnKeyPressed(new InputHandler(controller)); 

        // 5. Configurar e Mostrar Janela
        primaryStage.setTitle("Tetris DDD - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 6. Tratamento de Fechamento: Garante que a thread do jogo pare.
        primaryStage.setOnCloseRequest(e -> {
            if (controller != null) controller.stop();
            if (gameThread != null && gameThread.isAlive()) {
                gameThread.interrupt();
            }
            Platform.exit();
        });
    }

    /**
     * Configura e inicia um novo jogo do zero (Modo Normal).
     */
    private void initializeGame() {
        // --- LIMPEZA CRÍTICA ---
        root.getChildren().remove(mainHBox); 
        root.getChildren().removeIf(node -> node instanceof StackPane && node != bgView.getParent()); 
        
        // 1. Configurar Domínio e Controller
        if (controller != null) {
            controller.stop(); // Para a thread antiga
        }
        // Partida normal: usa construtor sem semente (que gera uma nova semente baseada no tempo)
        Jogador jogador = new Jogador("GAME-ID", "Player JavaFX");
        Partida partida = new Partida("PARTIDA-FX", jogador); 
        
        controller = new GameController(partida);
        controller.setApp(this); 

        // 2. Montar a nova UI de Jogo e Placar
        setupGameUI();
        
        // 3. Adicionar o HBox ao root (StackPane)
        root.getChildren().add(mainHBox);

        // 4. Iniciar o Game Loop em uma nova thread
        gameThread = new Thread(controller);
        gameThread.start();
        
        // 5. Reatribuir InputHandler ao novo controller
        if (primaryStage.getScene() != null) {
             primaryStage.getScene().setOnKeyPressed(new InputHandler(controller));
        }
    }
    
    /**
     * NOVO MÉTODO: Carrega a partida salva e inicia o GameController no modo Replay.
     */
    private void initializeReplayMode() {
        // 1. Carregar os dados de replay do disco
        ReplayData replayData = ReplayManager.loadReplay();

        if (replayData == null) {
            System.err.println("Falha ao carregar dados de replay. Cancelando reprodução.");
            // Volta ao menu normal
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Não foi possível carregar o arquivo de replay.");
                errorAlert.showAndWait();
                initializeGame();
            });
            return;
        }

        // 2. Limpeza Crítica 
        root.getChildren().remove(mainHBox);
        
        // 3. Configurar Domínio para Replay
        // Usa a SEMENTE SALVA para garantir o determinismo na sequência de peças
        Jogador jogador = new Jogador("REPLAY-ID", "Replay"); 
        Partida partida = new Partida("REPLAY-FX", jogador, replayData.getInitialSeed());
        
        if (controller != null) {
            controller.stop(); 
        }
        
        // NOVO: GameController precisa ser inicializado antes de iniciar o replay
        controller = new GameController(partida);
        controller.setApp(this);
        controller.startReplay(replayData); // Inicia o Controller no MODO REPLAY

        // 4. Montar a nova UI de Jogo e Placar (reutiliza a estrutura)
        setupGameUI();
        
        // 5. Adicionar o HBox ao root (StackPane)
        root.getChildren().add(mainHBox);

        // 6. Iniciar o Game Loop em uma nova thread
        gameThread = new Thread(controller);
        gameThread.start();
        
        // 7. Reatribuir InputHandler (o InputHandler vai ignorar inputs pois o controller está em Replay)
        if (primaryStage.getScene() != null) {
             primaryStage.getScene().setOnKeyPressed(new InputHandler(controller));
        }
    }


    /**
     * Monta o HBox principal com GamePanel e ScorePanel.
     */
    private void setupGameUI() {
        // 1. Componentes da UI (Novas Instâncias)
        GamePanel gamePanel = new GamePanel(controller, LARGURA_BLOCO);
        ScorePanel scorePanel = new ScorePanel(controller, LARGURA_BLOCO);
        controller.setScorePanel(scorePanel);

        // 2. Layout Principal (HBox)
        mainHBox = new HBox(10); // Inicializa o atributo da classe
        mainHBox.setAlignment(Pos.CENTER);
        mainHBox.setPadding(new Insets(6));

        ((VBox) gamePanel.getVisualComponent()).setMinHeight((double) ALTURA_TABULEIRO);
        mainHBox.setFillHeight(false);

        VBox rightColumn = new VBox(15);
        rightColumn.setPadding(new Insets(10));
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setStyle("-fx-background-color: transparent;");

        rightColumn.getChildren().add(scorePanel.getVisualComponent());
        rightColumn.setPrefHeight((double) ALTURA_TABULEIRO);

        mainHBox.getChildren().addAll(
                gamePanel.getVisualComponent(),
                rightColumn);
    }


    /**
     * Cria e exibe a tela de Game Over e os botões de controle.
     */
    public void showGameOverMenu() {
        // Garante que a UI seja atualizada na thread correta
        Platform.runLater(() -> {
            
            // 1. Criar botões existentes
            Button restartButton = new Button("Jogar Novamente");
            restartButton.setStyle("-fx-font-size: 18px; -fx-background-color: #39FF14; -fx-text-fill: black; -fx-font-weight: bold;");
            restartButton.setPrefWidth(200);
            restartButton.setOnAction(e -> {
                // Limpa o menu overlay
                root.getChildren().removeIf(node -> node instanceof StackPane && node.getStyle().contains("rgba(0, 0, 0, 0.7)")); 
                initializeGame(); // Inicia um novo jogo normal
            });
            
            Button closeButton = new Button("Fechar Jogo");
            closeButton.setStyle("-fx-font-size: 18px; -fx-background-color: #FF6EC7; -fx-text-fill: black; -fx-font-weight: bold;");
            closeButton.setPrefWidth(200);
            closeButton.setOnAction(e -> primaryStage.close());

            // 2. Criar painel do menu (VBox)
            VBox menu = new VBox(20); 
            menu.getChildren().addAll(restartButton); // Adiciona Jogar Novamente

            // 3. NOVO: Adiciona o botão de Replay se houver um arquivo salvo
            if (ReplayManager.hasSavedReplay()) {
                 Button replayButton = new Button("Replay Partida");
                 replayButton.setStyle("-fx-font-size: 18px; -fx-background-color: #00BFFF; -fx-text-fill: black; -fx-font-weight: bold;"); // Azul Neon
                 replayButton.setPrefWidth(200);
                 
                 replayButton.setOnAction(e -> {
                     // Limpa o menu overlay
                     root.getChildren().removeIf(node -> node instanceof StackPane && node.getStyle().contains("rgba(0, 0, 0, 0.7)")); 
                     // Inicia o jogo no MODO REPLAY
                     initializeReplayMode(); 
                 });
                 menu.getChildren().add(replayButton);
            }
            
            menu.getChildren().add(closeButton); // Adiciona Fechar Jogo por último
            menu.setAlignment(Pos.CENTER);
            menu.setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);

            // 4. Criar a tela de overlay semi-transparente (StackPane)
            StackPane overlay = new StackPane(menu);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); 

            // 5. Adicionar o overlay no StackPane principal (root)
            root.getChildren().add(overlay);
        });
    }

    // Método auxiliar para criar o ImageView do background (Use sua lógica original completa)
    private ImageView createBackgroundView(StackPane container) {
        final ImageView[] bgViewRef = new ImageView[1];
        try {
            java.net.URL res = TetrisApp.class.getResource("/background.webp");
            System.out.println("Resource URL for background: " + res);
            if (res != null) {
                Image bg = new Image(res.toExternalForm());
                // Verifica se a imagem WebP carregou corretamente
                if (bg.getWidth() <= 0 || bg.getHeight() <= 0) { 
                    System.err.println(
                            "Loaded image has 0 size; WebP may not be supported by JavaFX. Trying PNG fallback...");
                    java.net.URL pngRes = TetrisApp.class.getResource("/background.png");
                    if (pngRes != null) {
                        bg = new Image(pngRes.toExternalForm());
                        System.out.println("Background PNG loaded from classpath: /background.png (w=" + bg.getWidth()
                                + ", h=" + bg.getHeight() + ")");
                    } else {
                        System.err.println("No /background.png on classpath. Will attempt file fallback later.");
                    }
                } else {
                    System.out.println("Background image loaded from classpath: /background.webp (w=" + bg.getWidth()
                            + ", h=" + bg.getHeight() + ")");
                }

                bgViewRef[0] = new ImageView(bg);
                bgViewRef[0].setPreserveRatio(false);
                bgViewRef[0].setOpacity(0.9);
                bgViewRef[0].fitWidthProperty().bind(container.widthProperty());
                bgViewRef[0].fitHeightProperty().bind(container.heightProperty());
                bgViewRef[0].setMouseTransparent(true);
            } else {
                System.err.println(
                        "Background resource '/background.webp' not found on classpath — trying development file path...");
                // Fallback para ambiente de desenvolvimento: tenta carregar do disco (src/main/resources)
                File devFile = new File("src/main/resources/background.webp");
                if (devFile.exists()) {
                    Image bg = new Image(devFile.toURI().toString());
                    if (bg.getWidth() <= 0 || bg.getHeight() <= 0) {
                        // Tenta fallback PNG no disco
                        File pngDev = new File("src/main/resources/background.png");
                        if (pngDev.exists()) {
                            bg = new Image(pngDev.toURI().toString());
                            System.out.println("Background PNG loaded from file: " + pngDev.getAbsolutePath());
                        } else {
                            System.err.println("Dev WebP exists but could not be decoded and no PNG fallback found: "
                                    + devFile.getAbsolutePath());
                        }
                    } else {
                        System.out.println("Background image loaded from file: " + devFile.getAbsolutePath());
                    }
                    bgViewRef[0] = new ImageView(bg);
                    bgViewRef[0].setPreserveRatio(false);
                    bgViewRef[0].setOpacity(0.9);
                    bgViewRef[0].fitWidthProperty().bind(container.widthProperty());
                    bgViewRef[0].fitHeightProperty().bind(container.heightProperty());
                    bgViewRef[0].setMouseTransparent(true);
                } else {
                    System.err.println("Fallback file not found: " + devFile.getAbsolutePath());
                    // Tenta PNG diretamente no disco
                    File pngDev = new File("src/main/resources/background.png");
                    if (pngDev.exists()) {
                        Image bg = new Image(pngDev.toURI().toString());
                        bgViewRef[0] = new ImageView(bg);
                        bgViewRef[0].setPreserveRatio(false);
                        bgViewRef[0].fitWidthProperty().bind(container.widthProperty());
                        bgViewRef[0].fitHeightProperty().bind(container.heightProperty());
                        bgViewRef[0].setMouseTransparent(true);
                        bgViewRef[0].setOpacity(0.9);
                        System.out.println("Background PNG loaded from file: " + pngDev.getAbsolutePath());
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Background image not found or failed to load: " + ex.getMessage());
        }
        
        // Retorna o ImageView (se foi criado)
        return (bgViewRef[0] != null) ? bgViewRef[0] : null;
    }

    // Método main para iniciar o JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}