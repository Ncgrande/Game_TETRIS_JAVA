package tetris.model;

import tetris.Tetromino;
import tetris.Posicao;
import javafx.scene.paint.Color;

/**
 * Implementação concreta da peça 'I'.
 */
public class IPiece extends Tetromino {
    
    // Definição da matriz de formas para a Peça I
    private static final boolean[][][] FORMAS = new boolean[][][] {
        // Rotação 0 (Horizontal: 1 linha, 4 colunas)
        {{true, true, true, true}}, 
        // Rotação 1 (Vertical: 4 linhas, 1 coluna) - CORREÇÃO DE LARGURA
        {{true}, {true}, {true}, {true}}
    };
    
    public IPiece(Posicao posicao) {
        super(posicao);
    }

    @Override
    public boolean[][] getForma() {
        // Retorna a forma para a rotação atual, garantindo que o Tetromino.java use a dimensão correta.
        return FORMAS[rotacao]; 
    }

    @Override
    public Color getCor() {
        return Color.CYAN;
    }

    @Override
    public int getTotalRotacoes() {
        return FORMAS.length;
    }
    @Override
    public Tetromino copiarComNovaPosicao(Posicao novaPosicao) {
        // 1. Cria uma nova instância do TIPO CONCRETO (IPiece).
        IPiece copia = new IPiece(novaPosicao);
        
        // 2. CORREÇÃO CRÍTICA: COPIA O ESTADO DE ROTAÇÃO DA PEÇA ATUAL PARA A CÓPIA.
        copia.rotacao = this.rotacao; 
        
        return copia;
    }

}
