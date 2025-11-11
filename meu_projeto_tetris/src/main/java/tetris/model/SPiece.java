package tetris.model;

import tetris.Tetromino;
import tetris.Posicao;
import javafx.scene.paint.Color;

/**
 * Implementação concreta da peça 'S'.
 */
public class SPiece extends Tetromino {
    
    private static final boolean[][][] FORMAS = new boolean[][][] {
        // Rotação 0
        {
            {false, true,  true,  false},
            {true,  true,  false, false},
            {false, false, false, false},
            {false, false, false, false}
        },
        // Rotação 1 (90 graus)
        {
            {true,  false, false, false},
            {true,  true,  false, false},
            {false, true,  false, false},
            {false, false, false, false}
        }
    };
    
    public SPiece(Posicao posicao) {
        super(posicao);
    }

    @Override
    public boolean[][] getForma() {
        return FORMAS[rotacao]; 
    }

    @Override
    public Color getCor() {
        return Color.GREEN;
    }

    @Override
    public int getTotalRotacoes() {
        return 2; // Peça S só tem duas rotações únicas
    }
    @Override
    public Tetromino copiarComNovaPosicao(Posicao novaPosicao) {
        // 1. Cria uma nova instância do TIPO CONCRETO (IPiece).
        SPiece copia = new SPiece(novaPosicao);
        
        // 2. CORREÇÃO CRÍTICA: COPIA O ESTADO DE ROTAÇÃO DA PEÇA ATUAL PARA A CÓPIA.
        copia.rotacao = this.rotacao; 
        
        return copia;
    }
}