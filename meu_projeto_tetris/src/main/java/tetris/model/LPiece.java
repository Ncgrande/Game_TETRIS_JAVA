package tetris.model;

import tetris.Tetromino;
import tetris.Posicao;
import javafx.scene.paint.Color;

/**
 * Implementação concreta da peça 'L'.
 */
public class LPiece extends Tetromino {
    
    private static final boolean[][][] FORMAS = new boolean[][][] {
        // Rotação 0
        {
            {false, false, true,  false},
            {true,  true,  true,  false},
            {false, false, false, false},
            {false, false, false, false}
        },
        // Rotação 1
        {
            {false, true,  false, false},
            {false, true,  false, false},
            {false, true,  true,  false},
            {false, false, false, false}
        },
        // Rotação 2
        {
            {false, false, false, false},
            {true,  true,  true,  false},
            {true,  false, false, false},
            {false, false, false, false}
        },
        // Rotação 3
        {
            {true,  true,  false, false},
            {false, true,  false, false},
            {false, true,  false, false},
            {false, false, false, false}
        }
    };
    
    public LPiece(Posicao posicao) {
        super(posicao);
    }

    @Override
    public boolean[][] getForma() {
        return FORMAS[rotacao]; 
    }

    @Override
    public Color getCor() {
        return Color.ORANGE;
    }

    @Override
    public int getTotalRotacoes() {
        return 4;
    }
    @Override
    public Tetromino copiarComNovaPosicao(Posicao novaPosicao) {
        // 1. Cria uma nova instância do TIPO CONCRETO (IPiece).
        LPiece copia = new LPiece(novaPosicao);
        
        // 2. CORREÇÃO CRÍTICA: COPIA O ESTADO DE ROTAÇÃO DA PEÇA ATUAL PARA A CÓPIA.
        copia.rotacao = this.rotacao; 
        
        return copia;
    }
}