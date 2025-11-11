package tetris;

import java.util.Objects;

public final class Posicao { 
    private final int x; 
    private final int y; 

    public Posicao(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Posicao mover(int deltaX, int deltaY) {
        return new Posicao(x + deltaX, y + deltaY);
    }

    public Posicao moverParaBaixo() {
        return mover(0, 1);
    }

    public Posicao moverParaEsquerda() {
        return mover(-1, 0);
    }

    public Posicao moverParaDireita() {
        return mover(1, 0);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Posicao posicao = (Posicao) o;
        return x == posicao.x && y == posicao.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }
}