package tetris;

import tetris.model.IPiece;
import tetris.model.JPiece;
import tetris.model.LPiece;
import tetris.model.OPiece;
import tetris.model.SPiece;
import tetris.model.TPiece;
import tetris.model.ZPiece;
import javafx.scene.paint.Color;
import java.util.Random;
import java.util.Objects;

/**
 * Classe Base Abstrata para todas as peças do Tetris.
 */
public abstract class Tetromino {
    
    protected Posicao posicao;
    protected int rotacao;

    public Tetromino(Posicao posicao) {
        this.posicao = Objects.requireNonNull(posicao, "Posição inicial não pode ser nula.");
        this.rotacao = 0; 
    }
    
    // --- Métodos Abstratos (Polimorfismo) ---
    public abstract boolean[][] getForma(); 
    public abstract Color getCor();        
    public abstract int getTotalRotacoes(); 
    public abstract Tetromino copiarComNovaPosicao(Posicao novaPosicao); // Para Colisão Temporária
    
    // --- Lógica Comum ---
    
    public void mover(int deltaX, int deltaY) {
        this.posicao = posicao.mover(deltaX, deltaY); 
    }

    public void rotacionar() {
        this.rotacao = (rotacao + 1) % getTotalRotacoes(); 
    }
    
    public String getTipo() {
        return this.getClass().getSimpleName();
    }
    
    // --- Factory Determinística ---
    
    // Método original (mantido, mas não recomendado para Partida)
    public static Tetromino criarTetrominoAleatorio() {
        // Usa uma nova instância de Random sem semente
        return criarTetrominoAleatorio(new Random());
    }
    
    /**
     * NOVO: Factory que aceita uma instância de Random (semeada ou não).
     * Essencial para garantir a reproducibilidade (replay) se for usada uma semente fixa.
     */
    public static Tetromino criarTetrominoAleatorio(Random random) {
        // Usa o objeto Random fornecido
        int tipo = random.nextInt(7);

        Posicao spawnPos = new Posicao(4, 0); 

        return switch (tipo) {
            case 0 -> new IPiece(spawnPos);
            case 1 -> new OPiece(spawnPos);
            case 2 -> new TPiece(spawnPos);
            case 3 -> new SPiece(spawnPos);
            case 4 -> new ZPiece(spawnPos);
            case 5 -> new JPiece(spawnPos);
            case 6 -> new LPiece(spawnPos);
            default -> new IPiece(spawnPos); 
        };
    }
    
    // --- Getters ---
    public Posicao getPosicao() {
        return posicao;
    }

    public int getRotacao() {
        return rotacao;
    }
}