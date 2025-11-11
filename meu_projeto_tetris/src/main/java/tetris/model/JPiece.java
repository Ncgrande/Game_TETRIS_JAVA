package tetris.model;

import tetris.Tetromino;
import tetris.Posicao;
import javafx.scene.paint.Color;

/**
 * Implementação concreta da peça 'J'.
 */
public class JPiece extends Tetromino {
    
    private static final boolean[][][] FORMAS = new boolean[][][] {
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
    };
    
    public JPiece(Posicao posicao) {
        super(posicao);
    }

    @Override
    public boolean[][] getForma() {
        return FORMAS[rotacao]; 
    }

    @Override
    public Color getCor() {
        return Color.BLUE;
    }

    @Override
    public int getTotalRotacoes() {
        return 4;
    }
    @Override
    public Tetromino copiarComNovaPosicao(Posicao novaPosicao) {
        // 1. Cria uma nova instância do TIPO CONCRETO (IPiece).
        JPiece copia = new JPiece(novaPosicao);
        
        // 2. CORREÇÃO CRÍTICA: COPIA O ESTADO DE ROTAÇÃO DA PEÇA ATUAL PARA A CÓPIA.
        copia.rotacao = this.rotacao; 
        
        return copia;
    }
}