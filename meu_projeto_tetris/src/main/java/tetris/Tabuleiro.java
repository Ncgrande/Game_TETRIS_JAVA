package tetris;

import java.util.UUID;
import java.util.Arrays;
import javafx.scene.paint.Color; // Importe esta classe


public class Tabuleiro {
    // Usaremos valores fixos conforme especificado no projeto
    public static final int LARGURA = 10;
    public static final int ALTURA = 20;

    private final UUID id;
    // CORREÇÃO ESTRUTURAL: A grade agora armazena a cor (Color) em vez de boolean
    private final Color[][] grid; 
    
    // Array para estado de animação (usado no GameController)
    private final boolean[] linhasParaRemover; 

    public Tabuleiro() {
        this(LARGURA, ALTURA);
    }
    
    // Construtor que permite dimensões personalizadas para testes
    public Tabuleiro(int largura, int altura) {
        this.id = UUID.randomUUID();
        // A grade é ALTURA x LARGURA e inicializa com nulls
        this.grid = new Color[altura][largura]; 
        this.linhasParaRemover = new boolean[altura];
    }
    
    // --- MÉTODOS AUXILIARES ---
    
    /**
     * NOVO: Retorna a cor de um bloco fixo (para renderização).
     */
    public synchronized Color getBlocoCor(int x, int y) {
        if (x >= 0 && x < LARGURA && y >= 0 && y < ALTURA) {
            return grid[y][x];
        }
        return null;
    }
    
    // NOVO: Verifica se há um bloco fixo (diferente de null)
    public boolean temBloco(int x, int y) {
        // Verifica limites e retorna true se houver uma cor (não null)
        return x >= 0 && x < LARGURA && y >= 0 && y < ALTURA && grid[y][x] != null;
    }
    
    // Método temporário para testes
    public boolean temBlocosFixos() {
        for (int y = 0; y < ALTURA; y++) {
            for (int x = 0; x < LARGURA; x++) {
                if (grid[y][x] != null) return true; // <-- Verifica se há uma cor salva
            }
        }
        return false;
    }
    
    // Getter para a Partida (mantém a compatibilidade com a antiga matriz boolean)
    // Nota: Retornar a matriz de Color é a opção mais limpa.
    public Color[][] getGrid() {
        // Retorna uma cópia para proteger o estado interno
        return Arrays.copyOf(grid, grid.length);
    }
    
    // Getter para a animação
    public boolean[] getLinhasParaRemover() {
        return linhasParaRemover;
    }
    
    // --- LÓGICA DE COLISÃO E FIXAÇÃO ---

    public boolean posicaoValida(Tetromino tetromino) {
        boolean[][] forma = tetromino.getForma();
        Posicao pos = tetromino.getPosicao();

        for (int i = 0; i < forma.length; i++) {
            for (int j = 0; j < forma[i].length; j++) { 
                if (forma[i][j]) { 
                    int xAbs = pos.getX() + j;
                    int yAbs = pos.getY() + i;

                    // 1. Colisão com Bordas
                    if (xAbs < 0 || xAbs >= LARGURA || yAbs >= ALTURA) {
                        return false;
                    }
                    
                    // 2. Colisão com Bloco Fixo (yAbs >= 0 verifica se o bloco é diferente de null)
                    if (yAbs >= 0 && grid[yAbs][xAbs] != null) { // <-- COLISÃO CONTRA COR (não boolean)
                        return false; 
                    }
                }
            }
        }
        return true;
    }

    public synchronized void fixarTetromino(Tetromino tetromino) {
        boolean[][] forma = tetromino.getForma();
        Posicao pos = tetromino.getPosicao();
        Color cor = tetromino.getCor(); // <-- Obtém a cor da peça!

        for (int i = 0; i < forma.length; i++) {
            for (int j = 0; j < forma[i].length; j++) {
                if (forma[i][j]) {
                    int xAbs = pos.getX() + j;
                    int yAbs = pos.getY() + i;
                    
                    if (yAbs >= 0 && yAbs < ALTURA && xAbs >= 0 && xAbs < LARGURA) {
                        grid[yAbs][xAbs] = cor; // <-- SALVA A COR NA GRADE
                    }
                } 
            }
        }
    } 

    // --- LÓGICA DE LINHA ---

    public synchronized int eliminarLinhasCompletas() {
        int linhasEliminadas = 0;
        
        for (int y = ALTURA - 1; y >= 0; y--) {
            if (isLineComplete(y)) {
                // A lógica de animação requer que esta linha seja alterada para marcar o array linhasParaRemover
                linhasParaRemover[y] = true; 
                linhasEliminadas++;
            }
        }
        return linhasEliminadas;
    }

    /**
     * Verifica se uma linha específica (y) está totalmente preenchida.
     */
    private boolean isLineComplete(int y) {
        for (int x = 0; x < LARGURA; x++) {
            if (grid[y][x] == null) { // <-- Checa se a cor é null (vazio)
                return false; 
            }
        }
        return true; 
    }

    /**
     * Remove a linha especificada (y) e move todas as linhas acima para baixo.
     * Implementa a lógica da remoção REAL (chamada após a animação).
     */
    public void executarRemocaoReal() {
        int linhasMovidas = 0;
        
        for (int y = ALTURA - 1; y >= 0; y--) {
            if (linhasParaRemover[y]) {
                linhasMovidas++;
                linhasParaRemover[y] = false; 
            } else if (linhasMovidas > 0) {
                // Copia a cor da linha y para a nova posição (y + linhasMovidas)
                System.arraycopy(grid[y], 0, grid[y + linhasMovidas], 0, LARGURA);
                // Limpa a linha original (seta para null)
                Arrays.fill(grid[y], null);
            }
        }
        // Limpa as linhas que ficaram no topo
        for (int y = 0; y < linhasMovidas; y++) {
            Arrays.fill(grid[y], null);
        }
    }

    /**
     * Adiciona "garbage lines" ao fundo do tabuleiro.
     * Cada linha de lixo empurra a grade para cima; a nova linha inferior
     * é preenchida com blocos (cor cinza) deixando exatamente um buraco aleatório.
     *
     * @param count número de linhas de lixo a adicionar
     */
    public void addGarbageLines(int count) {
        if (count <= 0) return;
        for (int c = 0; c < count; c++) {
            // Move todas as linhas para cima (linha y+1 -> y)
            for (int y = 0; y < ALTURA - 1; y++) {
                System.arraycopy(grid[y + 1], 0, grid[y], 0, LARGURA);
                linhasParaRemover[y] = linhasParaRemover[y + 1];
            }

            // Gera a nova linha inferior com um buraco em posição aleatória
            int hole = (int) (Math.random() * LARGURA);
            for (int x = 0; x < LARGURA; x++) {
                grid[ALTURA - 1][x] = (x == hole) ? null : Color.GRAY;
            }
            // limpa o flag de animação para a última linha
            linhasParaRemover[ALTURA - 1] = false;
        }
    }
}