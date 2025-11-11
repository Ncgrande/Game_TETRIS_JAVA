package tetris;

public final class Pontuacao {
    private final int valor; 

    public Pontuacao(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Pontuação não pode ser negativa.");
        }
        this.valor = valor;
    }

    public Pontuacao adicionarPontos(int pontos) {
        return new Pontuacao(this.valor + pontos);
    }

    public int getValor() {
        return valor;
    }
    
    @Override
    public String toString() {
        return valor + " pontos";
    }
}