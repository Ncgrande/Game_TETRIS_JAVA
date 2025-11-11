package tetris;

//import java.util.UUID;
//import tetris.model.IPiece; 
import java.util.Random; // NOVO: Importar Random

public class Partida {
    private final String id;
    private Jogador jogador;
    private final Tabuleiro tabuleiro;
    private Tetromino tetrominoAtual;
    private Tetromino proximoTetromino;
    private Pontuacao pontuacaoAtual;
    private SistemaPontuacao sistemaPontuacao;
    private int nivel;
    private int totalLinhas;
    private boolean gameOver;
    // sinaliza que um level-up ocorreu na última atualização (consumível pela UI)
    private boolean levelUpFlag = false;

    // --- NOVO PARA REPLAY E DETERMINISMO ---
    private final long initialSeed; 
    private final Random randomGenerator; 
    // ----------------------------------------

    public boolean isLevelUpFlag() {
        return levelUpFlag;
    }

    public void consumeLevelUpFlag() {
        this.levelUpFlag = false;
    }
    
    // CONSTRUTOR MODIFICADO PARA ACEITAR A SEMENTE (Replay/Determinismo)
    public Partida(String id, Jogador jogador, long initialSeed) {
        this.id = id;
        this.jogador = jogador;
        this.tabuleiro = new Tabuleiro();
        this.sistemaPontuacao = new SistemaPontuacao();
        this.pontuacaoAtual = new Pontuacao(0);
        this.nivel = 1;
        this.totalLinhas = 0;
        this.gameOver = false;
        
        // --- INICIALIZAÇÃO DA SEMENTE E DO GERADOR ---
        this.initialSeed = initialSeed;
        // O gerador é inicializado com a semente fornecida
        this.randomGenerator = new Random(initialSeed); 
        // ----------------------------------------------

        // Usa o método estático do Tetromino que aceita o gerador de Random
        this.tetrominoAtual = Tetromino.criarTetrominoAleatorio(randomGenerator);
        this.proximoTetromino = Tetromino.criarTetrominoAleatorio(randomGenerator);
    }
    
    // Construtor auxiliar (Usa a hora atual como semente para jogadas normais)
    public Partida(String id, Jogador jogador) {
        this(id, jogador, System.currentTimeMillis());
    }


    // Permite alterar o jogador associado a esta Partida (por exemplo,
    // quando o usuário escolhe um jogador diferente na UI).
    public void setJogador(Jogador novoJogador) {
        if (novoJogador == null)
            throw new IllegalArgumentException("Jogador não pode ser nulo");
        this.jogador = novoJogador;
    }

    // --- LÓGICA DE JOGO PRINCIPAL (SINCRONIZADA) ---

    public synchronized boolean processarQueda() { // SINCRONIZADO
        if (gameOver)
            return false;
        if (moverTetromino(0, 1)) {
            return true;
        }

        fixarTetromino();

        int linhasEliminadas = tabuleiro.eliminarLinhasCompletas();
        if (linhasEliminadas > 0) {
            totalLinhas += linhasEliminadas;
            int pontosGanhos = sistemaPontuacao.calcularPontos(linhasEliminadas, nivel);
            pontuacaoAtual = pontuacaoAtual.adicionarPontos(pontosGanhos);
            int novoNivel = sistemaPontuacao.calcularNovoNivel(totalLinhas);
            if (novoNivel > nivel) {
                nivel = novoNivel;
                levelUpFlag = true; // marca que houve level-up
            }
        }

        tetrominoAtual = proximoTetromino;
        // NOVO: Usa o gerador de Random da Partida para o determinismo
        proximoTetromino = Tetromino.criarTetrominoAleatorio(randomGenerator); 

        if (!tabuleiro.posicaoValida(tetrominoAtual)) {
            gameOver = true;
        }
        return false;
    }

    public synchronized boolean moverTetromino(int dx, int dy) { // SINCRONIZADO
        if (gameOver)
            return false;

        Posicao novaPosicao = tetrominoAtual.getPosicao().mover(dx, dy);
        Tetromino tetrominoTemporario = criarTetrominoComNovaPosicao(novaPosicao);

        // A lógica de mover/colidir é a principal candidata a Race Condition
        if (tabuleiro.posicaoValida(tetrominoTemporario)) {
            tetrominoAtual.mover(dx, dy);
            return true;
        }
        return false;
    }

    public synchronized boolean rotacionarTetromino() { // SINCRONIZADO
        if (gameOver)
            return false;

        // Cria uma cópia temporária e tenta rotacionar
        Tetromino tetrominoTemporario = criarTetrominoComNovaRotacao();

        if (tabuleiro.posicaoValida(tetrominoTemporario)) {
            tetrominoAtual.rotacionar();
            return true;
        }
        return false;
    }

    // --- MÉTODOS AUXILIARES ---

    private Tetromino criarTetrominoComNovaPosicao(Posicao novaPosicao) {
        return tetrominoAtual.copiarComNovaPosicao(novaPosicao);
    }

    private Tetromino criarTetrominoComNovaRotacao() {
        Tetromino temp = tetrominoAtual.copiarComNovaPosicao(tetrominoAtual.getPosicao());
        temp.rotacionar();
        return temp;
    }

    private void fixarTetromino() {
        tabuleiro.fixarTetromino(tetrominoAtual);
    }

    // --- GETTERS ---
    public Jogador getJogador() {
        return jogador;
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public int getPontuacao() {
        return pontuacaoAtual.getValor();
    }

    public int getNivel() {
        return nivel;
    }

    public boolean isGameOver() {
        return gameOver;
    }
    
    // NOVO GETTER: Essencial para o Replay (dado que será salvo/carregado)
    public long getInitialSeed() {
        return initialSeed;
    }

    public Tetromino getTetrominoAtual() {
        return tetrominoAtual;
    }

    public Tetromino getProximoTetromino() {
        return proximoTetromino;
    }

    public int getTotalLinhas() {
        return totalLinhas;
    }

    /**
     * Retorna true se houve um level-up desde a última vez que este método foi
     * chamado.
     * O método também reseta o flag (consumível).
     */
    public synchronized boolean consumeLevelUp() {
        boolean v = levelUpFlag;
        levelUpFlag = false;
        return v;
    }
}