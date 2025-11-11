package tetris.model;

import tetris.Tetromino;
import tetris.Posicao;
import javafx.scene.paint.Color;

/**
 * Implementação concreta da peça 'T'.
 */
public class TPiece extends Tetromino {
    
    // Forma precisa baseada na correção (usando a matriz exata do TipoTetromino)
    private static final boolean[][][] FORMAS = new boolean[][][] {
        // Rotação 0
        {
            {false, true, false, false},
            {true,  true, true,  false},
            {false, false, false, false},
            {false, false, false, false}
        }, 
        // Rotação 1
        {
            {false, true, false, false},
            {false, true, true, false},
            {false, true, false, false},
            {false, false, false, false}
        },
        // Rotação 2
        {
            {false, false, false, false},
            {true,  true,  true,  false},
            {false, true,  false, false},
            {false, false, false, false}
        }, 
        // Rotação 3
        {
            {false, true, false, false},
            {true,  true, false, false},
            {false, true, false, false},
            {false, false, false, false}
        }
    };
    
    public TPiece(Posicao posicao) {
        super(posicao);
    }

    @Override
    public boolean[][] getForma() {
        return FORMAS[rotacao]; 
    }

    @Override
    public Color getCor() {
        return Color.PURPLE;
    }

    @Override
    public int getTotalRotacoes() {
        return FORMAS.length;
    }
    @Override
    public Tetromino copiarComNovaPosicao(Posicao novaPosicao) {
        // 1. Cria uma nova instância do TIPO CONCRETO (IPiece).
    TPiece copia = new TPiece(novaPosicao);
        
        // 2. CORREÇÃO CRÍTICA: COPIA O ESTADO DE ROTAÇÃO DA PEÇA ATUAL PARA A CÓPIA.
        copia.rotacao = this.rotacao; 
        
        return copia;
    }
}
