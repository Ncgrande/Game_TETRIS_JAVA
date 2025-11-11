package tetris.model;

import tetris.Tetromino;
import tetris.Posicao;
import javafx.scene.paint.Color;

/**
 * Implementação concreta da peça 'Z'.
 */
public class ZPiece extends Tetromino {
    
    private static final boolean[][][] FORMAS = new boolean[][][] {
        // Rotação 0
        {
            {true,  true,  false, false},
            {false, true,  true,  false},
            {false, false, false, false},
            {false, false, false, false}
        },
        // Rotação 1 (90 graus)
        {
            {false, true,  false, false},
            {true,  true,  false, false},
            {true,  false, false, false},
            {false, false, false, false}
        }
    };
    
    public ZPiece(Posicao posicao) {
        super(posicao);
    }

    @Override
    public boolean[][] getForma() {
        return FORMAS[rotacao]; 
    }

    @Override
    public Color getCor() {
        return Color.RED;
    }

    @Override
    public int getTotalRotacoes() {
        return 2; // Peça Z só tem duas rotações únicas
    }
    @Override
    public Tetromino copiarComNovaPosicao(Posicao novaPosicao) {
        // 1. Cria uma nova instância do TIPO CONCRETO (IPiece).
        ZPiece copia = new ZPiece(novaPosicao);
        
        // 2. CORREÇÃO CRÍTICA: COPIA O ESTADO DE ROTAÇÃO DA PEÇA ATUAL PARA A CÓPIA.
        copia.rotacao = this.rotacao; 
        
        return copia;
    }
}