package tetris.model;

import tetris.Tetromino;
import tetris.Posicao;
import javafx.scene.paint.Color;

/**
 * Implementação concreta da peça 'O' (Quadrado).
 * Não possui rotações funcionais, totalRotacoes = 1.
 */
public class OPiece extends Tetromino {
    
    private static final boolean[][][] FORMAS = new boolean[][][] {
        // Rotação 0 (Única forma)
        {
            {false, true,  true,  false},
            {false, true,  true,  false},
            
        }
    };
    
    public OPiece(Posicao posicao) {
        super(posicao);
    }

    @Override
    public boolean[][] getForma() {
        // Rotacao % 1 sempre será 0
        return FORMAS[rotacao]; 
    }

    @Override
    public Color getCor() {
        return Color.YELLOW;
    }

    @Override
    public int getTotalRotacoes() {
        return 1; // Peça O só tem uma rotação
    }
    @Override
    public Tetromino copiarComNovaPosicao(Posicao novaPosicao) {
        // 1. Cria uma nova instância do TIPO CONCRETO (IPiece).
        OPiece copia = new OPiece(novaPosicao);
        
        // 2. CORREÇÃO CRÍTICA: COPIA O ESTADO DE ROTAÇÃO DA PEÇA ATUAL PARA A CÓPIA.
        copia.rotacao = this.rotacao; 
        
        return copia;
    }
}