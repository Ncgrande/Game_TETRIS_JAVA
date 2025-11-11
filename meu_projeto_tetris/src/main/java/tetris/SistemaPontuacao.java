package tetris;

public class SistemaPontuacao {

    // Pontos Base: {0, 100, 300, 500, 800} para 0, 1, 2, 3, 4 linhas eliminadas
    private static final int[] PONTOS_BASE = { 0, 100, 300, 500, 800 };
    // Tornamos a progressão por nível ainda mais rápida: 4 linhas por nível
    private static final int LINHAS_POR_NIVEL = 4;

    public int calcularPontos(int linhasEliminadas, int nivel) {
        if (linhasEliminadas < 0 || linhasEliminadas > 4) {
            return 0;
        }

        int pontosBase = PONTOS_BASE[linhasEliminadas];
        // Aplicamos um multiplicador mais agressivo por nível.
        // Fórmula: pontos = round(pontosBase * (1 + 0.25 * (nivel - 1)))
        double multiplicador = 1.0 + 0.25 * (Math.max(1, nivel) - 1);
        return (int) Math.round(pontosBase * multiplicador);
    }

    public int calcularNovoNivel(int totalLinhas) {
        return 1 + (totalLinhas / LINHAS_POR_NIVEL);
    }
}