package tetris;

import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import java.sql.SQLException; 
import java.time.Instant; 
import java.time.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioClip;
import tetris.replay.ReplayData; // IMPORT NECESSÁRIO
import tetris.replay.ReplayEvent; // IMPORT NECESSÁRIO
import tetris.replay.ReplayEventType; // IMPORT NECESSÁRIO

/**
 * Game Controller (GameEngine): Orquestra a execução da Partida em tempo real.
 * Roda em uma Thread separada para gerenciar a queda automática (Game Loop).
 */
public class GameController implements Runnable {

    private final Partida partida;
    private final PartidaDAO partidaDAO;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private long velocidadeQuedaMs = 1000;
    
    private boolean isBgmPlaying = true; 

    // Audio
    private MediaPlayer bgmPlayer = null;
    private AudioClip lineClearClip = null;
    private AudioClip levelUpClip = null;
    
    private static final long BASE_DROP_MS = 800L;
    private static final double DROP_FACTOR = 0.75; 
    private static final long MIN_DROP_MS = 30L; 
    
    private ScorePanel scorePanel;
    private GamePanel gamePanel;
    private TetrisApp app; 

    private int animationTicks = 0;
    private final int MAX_ANIMATION_TICKS = 3; 

    private final Instant inicioPartida;
    
    // --- NOVO PARA REPLAY ---
    private ReplayData replayData;
    private boolean recording = true; // Se está gravando a partida atual
    
    private boolean isReplaying = false; // NOVO: Se está reproduzindo um replay
    private int currentEventIndex = 0; // NOVO: Índice do próximo evento a ser executado
    private long replayStartTime = 0; // NOVO: Tempo de início da reprodução para sincronização
    // --- FIM REPLAY ---

    public GameController(Partida partida) {
        this.partida = partida;
        this.partidaDAO = new PartidaDAO(); 
        this.inicioPartida = Instant.now();
        
        // --- INICIALIZAÇÃO REPLAY ---
        this.replayData = new ReplayData(partida.getInitialSeed());
        // ---------------------------

        // Carregar recursos de áudio no JavaFX Application Thread (Mantido)
        Platform.runLater(() -> {
            try {
                var bgmUrl = getClass().getResource("/sounds/bgm.mp3");
                if (bgmUrl != null) {
                    Media media = new Media(bgmUrl.toExternalForm());
                    bgmPlayer = new MediaPlayer(media);
                    bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    bgmPlayer.setVolume(0.35);
                    bgmPlayer.play();
                    isBgmPlaying = true; 
                }

                var lineUrl = getClass().getResource("/sounds/line.wav");
                if (lineUrl != null) {
                    lineClearClip = new AudioClip(lineUrl.toExternalForm());
                }

                var lvlUrl = getClass().getResource("/sounds/levelup.wav");
                if (lvlUrl != null) {
                    levelUpClip = new AudioClip(lvlUrl.toExternalForm());
                    levelUpClip.setVolume(0.9);
                    System.out.println("Level-up SFX carregado: " + lvlUrl.toExternalForm());
                }
            } catch (Exception e) {
                System.err.println("Erro ao inicializar áudio: " + e.getMessage());
            }
        });
    }
    
    // --- NOVO MÉTODO: Configura o controlador para o modo Replay ---
    public void startReplay(ReplayData data) {
        this.replayData = data;
        this.isReplaying = true;
        this.recording = false; 
        this.currentEventIndex = 0;
        this.replayStartTime = System.currentTimeMillis(); // Marca o início real do playback
        // Força a UI a atualizar para o estado de replay
        notifyObservers(); 
        System.out.println("INICIANDO REPLAY da semente: " + data.getInitialSeed());
    }
    // --------------------------------------------------------------
    
    public void setApp(TetrisApp app) {
        this.app = app;
    }

    public void toggleBGM() {
        Platform.runLater(() -> {
            if (bgmPlayer == null) {
                System.err.println("BGM não carregada.");
                return;
            }

            if (isBgmPlaying) {
                bgmPlayer.pause();
                isBgmPlaying = false;
                System.out.println("Música de fundo DESLIGADA.");
            } else {
                bgmPlayer.play();
                isBgmPlaying = true;
                System.out.println("Música de fundo LIGADA.");
            }
        });
    }

    public void playLevelUpTest() {
        if (levelUpClip != null) {
            Platform.runLater(() -> {
                try {
                    System.out.println("Teste: tocando level-up SFX (manual)");
                    levelUpClip.play();
                } catch (Exception e) {
                    System.err.println("Erro ao tocar level-up SFX no teste: " + e.getMessage());
                }
            });
        } else {
            System.err.println("Teste: levelUpClip não carregado");
        }
    }

    public void playLineClearTest() {
        if (lineClearClip != null) {
            Platform.runLater(() -> {
                try {
                    System.out.println("Teste: tocando line-clear SFX (manual)");
                    lineClearClip.play();
                } catch (Exception e) {
                    System.err.println("Erro ao tocar line-clear SFX no teste: " + e.getMessage());
                }
            });
        } else {
            System.err.println("Teste: lineClearClip não carregado");
        }
    }

    public void setScorePanel(ScorePanel scorePanel) {
        this.scorePanel = scorePanel;
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public Partida getPartida() {
        return partida;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getAnimationTicks() {
        return animationTicks;
    }
    
    // Método auxiliar para executar um evento de replay
    private void executeReplayEvent(ReplayEventType type) {
        // Simula o input do jogador
        switch (type) {
            case MOVE_LEFT:
                partida.moverTetromino(-1, 0);
                break;
            case MOVE_RIGHT:
                partida.moverTetromino(1, 0);
                break;
            case MOVE_DOWN:
                partida.moverTetromino(0, 1);
                break;
            case ROTATE:
                partida.rotacionarTetromino();
                break;
            case HARD_DROP:
                // Hard drop no replay apenas move a peça e a Game Loop fará o processarQueda
                while (partida.moverTetromino(0, 1));
                partida.processarQueda();
                break;
        }
    }

    // NOVO: A Lógica de Replay será inserida aqui no próximo passo
    @Override
    public void run() {

        while (running && !partida.isGameOver()) {

            if (paused) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue; 
            }
            
            // --- LÓGICA DE REPLAY (Próxima etapa de implementação) ---
            if (isReplaying) {
                long currentTime = System.currentTimeMillis();
                long elapsedReplayTime = currentTime - replayStartTime;
                
                // Processa todos os eventos que deveriam ter ocorrido até o momento atual
                while (currentEventIndex < replayData.getEvents().size()) {
                    ReplayEvent nextEvent = replayData.getEvents().get(currentEventIndex);
                    
                    if (nextEvent.getTimeMs() <= elapsedReplayTime) {
                        executeReplayEvent(nextEvent.getType());
                        currentEventIndex++;
                    } else {
                        break; // Ainda não é hora deste evento
                    }
                }
                
                // Se todos os eventos foram executados, o replay está no fim
                if (currentEventIndex >= replayData.getEvents().size()) {
                    System.out.println("REPLAY CONCLUÍDO!");
                    isReplaying = false; // Sai do modo replay
                    // Pausa a execução para o jogador ver o Game Over
                    if (!partida.isGameOver()) {
                         this.paused = true; 
                    }
                }
            }
            // --- FIM DA LÓGICA DE REPLAY ---

            // Restante da lógica do Game Loop (Mantido e unificado)

            if (animationTicks > 0) {
                animationTicks--;
                notifyObservers(); 

                if (animationTicks == 0) {
                    partida.getTabuleiro().executarRemocaoReal();
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue; 
            }

            // 1. Processamento da Lógica (Queda Automática)
            partida.processarQueda();

            // 2. Inicia a animação se linhas foram detectadas
            boolean hasLinesToRemove = false;
            for (boolean b : partida.getTabuleiro().getLinhasParaRemover()) {
                if (b) { hasLinesToRemove = true; break; }
            }
            if (hasLinesToRemove) {
                animationTicks = MAX_ANIMATION_TICKS;
                if (lineClearClip != null) {
                    Platform.runLater(() -> lineClearClip.play());
                }
            }

            // 3. Notificação da UI
            notifyObservers(); 

            // 4. Controle de Velocidade/Tempo
            try {
                TimeUnit.MILLISECONDS.sleep(velocidadeQuedaMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 5. Atualiza velocidade e nível
            this.velocidadeQuedaMs = calcularVelocidadeBaseadaNoNivel(partida.getNivel());
            if (partida.consumeLevelUp()) {
                if (scorePanel != null) {
                    Platform.runLater(() -> scorePanel.showLevelUp());
                }
                if (levelUpClip != null) {
                    Platform.runLater(() -> {
                        try {
                            System.out.println("Tocando level-up SFX...");
                            levelUpClip.play();
                        } catch (Exception e) {
                            System.err.println("Erro ao tocar level-up SFX: " + e.getMessage());
                        }
                    });
                } else {
                    System.err.println("levelUpClip está nulo ao tentar tocar SFX de level-up");
                }

                int nivelAtual = partida.getNivel();
                if (nivelAtual >= 5 && ((nivelAtual - 5) % 3 == 0)) {
                    partida.getTabuleiro().addGarbageLines(1);
                    notifyObservers();
                }
            }
        }

        // --- LÓGICA DE GAME OVER E PERSISTÊNCIA ---
        if (partida.isGameOver()) {
            System.out.println("GAME OVER! Pontuação Final: " + partida.getPontuacao());
            
            // FINALIZAÇÃO DO REPLAY: Salva os dados para um arquivo
            if (recording) {
                tetris.replay.ReplayManager.saveReplay(replayData); 
            }

            long duracaoSegundos = Duration.between(inicioPartida, Instant.now()).getSeconds();

            try {
                partidaDAO.salvarPartidaCompleta(partida, duracaoSegundos);
                System.out.println("Partida salva com sucesso no banco de dados.");
            } catch (SQLException e) {
                System.err.println("ERRO FATAL DE PERSISTÊNCIA: Falha ao salvar a partida. " + e.getMessage());
            }

            notifyObservers();
            
            if (app != null) {
                app.showGameOverMenu(); 
            }
        }
    }

    private void notifyObservers() {
        if (gamePanel != null) {
            gamePanel.draw(); 
            if (partida.isLevelUpFlag()) {
                gamePanel.showLevelUpMessage();
                partida.consumeLevelUpFlag(); 
            }
        }
        if (scorePanel != null) {
            scorePanel.update(); 
        }
    }

    // --- Comandos do Usuário (Chamados pelo InputHandler) ---

    public void moveLeft() {
        // Ignora input do usuário se estiver em modo replay
        if (isReplaying) return;
        
        if (!paused && partida.moverTetromino(-1, 0)) {
            recordEvent(ReplayEventType.MOVE_LEFT); 
            notifyObservers();
        }
    }
    
    // ... (Os métodos moveRight, moveDown, rotate, e hardDrop também devem 
    // ignorar o input se isReplaying for true.
    // Lembre-se de adicionar 'if (isReplaying) return;' no topo de cada um.)
    // Apenas vou adicionar os demais métodos para completar o código, mas 
    // com a verificação de isReplaying.

    public void moveRight() {
        if (isReplaying) return;
        
        if (!paused && partida.moverTetromino(1, 0)) {
            recordEvent(ReplayEventType.MOVE_RIGHT); 
            notifyObservers();
        }
    }

    public void moveDown() {
        if (isReplaying) return;
        
        if (!paused && partida.moverTetromino(0, 1)) {
            recordEvent(ReplayEventType.MOVE_DOWN); 
            notifyObservers();
        }
    }

    public void rotate() {
        if (isReplaying) return;
        
        if (!paused && partida.rotacionarTetromino()) {
            recordEvent(ReplayEventType.ROTATE); 
            notifyObservers();
        }
    }

    public void hardDrop() {
        if (isReplaying) return;
        
        if (paused)
            return;

        while (partida.moverTetromino(0, 1));
        
        recordEvent(ReplayEventType.HARD_DROP); 

        partida.processarQueda();
        
        notifyObservers(); 
    }
    
    public void togglePause() {
        this.paused = !this.paused;
        System.out.println(this.paused ? "JOGO PAUSADO" : "JOGO RETOMADO");
        notifyObservers();
    }

    private void recordEvent(ReplayEventType type) {
        if (recording && !paused) {
            long timeElapsedMs = Duration.between(inicioPartida, Instant.now()).toMillis();
            replayData.addEvent(new ReplayEvent(timeElapsedMs, type));
        }
    }
    
    public void stop() {
        this.running = false;
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }
    }

    private long calcularVelocidadeBaseadaNoNivel(int nivel) {
        if (nivel <= 1)
            return BASE_DROP_MS;
        double factor = Math.pow(DROP_FACTOR, Math.max(0, nivel - 1));
        long computed = (long) Math.round(BASE_DROP_MS * factor);
        return Math.max(MIN_DROP_MS, computed);
    }
}