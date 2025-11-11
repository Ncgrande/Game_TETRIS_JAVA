package tetris;

import java.util.Objects;
import javafx.scene.paint.Color; 

/**
 * Enum que define as formas, rotações e cores de cada Tetromino.
 * Todas as formas são padronizadas em matrizes para facilitar a colisão.
 */
public enum TipoTetromino {

    I(Color.CYAN, new boolean[][][]{
    // Rotação 0: 1x4
    {{true, true, true, true}}, 
    // Rotação 1: 4x1
    {{true}, {true}, {true}, {true}} 
    }),

    O(Color.YELLOW, new boolean[][][]{
        // Rotação 0
        {
            {false, true,  true,  false},
            {false, true,  true,  false},
            {false, false, false, false},
            {false, false, false, false}
        }
    }),

    T(Color.PURPLE, new boolean[][][]{
        // Rotação 0 (Topo para cima)
        {
            {false, true,  false, false},
            {true,  true,  true,  false},
            {false, false, false, false},
            {false, false, false, false}
        }, 
        // Rotação 1 (Direita para cima)
        {
            {false, true,  false, false},
            {false, true,  true,  false},
            {false, true,  false, false},
            {false, false, false, false}
        },
        // Rotação 2 (Base para cima)
        {
            {false, false, false, false},
            {true,  true,  true,  false},
            {false, true,  false, false},
            {false, false, false, false}
        }, 
        // Rotação 3 (Esquerda para cima)
        {
            {false, true,  false, false},
            {true,  true,  false, false},
            {false, true,  false, false},
            {false, false, false, false}
        }
    }),

    S(Color.GREEN, new boolean[][][]{
        // Rotação 0
        {
            {false, true,  true,  false},
            {true,  true,  false, false},
            {false, false, false, false},
            {false, false, false, false}
        },
        // Rotação 1
        {
            {true,  false, false, false},
            {true,  true,  false, false},
            {false, true,  false, false},
            {false, false, false, false}
        }
    }),

    Z(Color.RED, new boolean[][][]{
        // Rotação 0
        {
            {true,  true,  false, false},
            {false, true,  true,  false},
            {false, false, false, false},
            {false, false, false, false}
        },
        // Rotação 1
        {
            {false, true,  false, false},
            {true,  true,  false, false},
            {true,  false, false, false},
            {false, false, false, false}
        }
    }),

    J(Color.BLUE, new boolean[][][]{
        // Rotação 0
        {
            {true,  false, false, false},
            {true,  true,  true,  false},
            {false, false, false, false},
            {false, false, false, false}
        },
        // Rotação 1
        {
            {false, true,  true,  false},
            {false, true,  false, false},
            {false, true,  false, false},
            {false, false, false, false}
        },
        // Rotação 2
        {
            {false, false, false, false},
            {true,  true,  true,  false},
            {false, false, true,  false},
            {false, false, false, false}
        },
        // Rotação 3
        {
            {false, true,  false, false},
            {false, true,  false, false},
            {true,  true,  false, false},
            {false, false, false, false}
        }
    }),

    L(Color.ORANGE, new boolean[][][]{
        // Rotação 0
        {
            {false, false, true},
            {true,  true,  true},
            {false, false, false}
            
        },
        // Rotação 1
        {
            {false, true,  false},
            {false, true,  false},
            {false, true,  true}
            
        },
        // Rotação 2
        {
            {true,  true,  true},
            {true,  false, false},
            {false, false, false}
        },
        // Rotação 3
        {
            {true,  true,  false},
            {false, true,  false},
            {false, true,  false}
        
        }
    });

    private final boolean[][][] formas;
    private final Color cor;

    TipoTetromino(Color cor, boolean[][][] formas) {
        this.cor = cor;
        this.formas = Objects.requireNonNull(formas, "Formas do Tetromino não pode ser nulo.");
    }

    public boolean[][] getForma(int rotacao) {
        return formas[rotacao % formas.length];
    }

    public int getTotalRotacoes() {
        return formas.length;
    }

    public Color getCor() {
        return cor;
    }
}