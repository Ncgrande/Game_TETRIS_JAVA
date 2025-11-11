package tetris;

import java.util.concurrent.TimeUnit;

public class TetrisDDD {
    
    public static void main(String[] args) {
        
        // --- 1. CONFIGURAÇÃO (Camada de Aplicação) ---
        Jogador jogador = new Jogador("Juca");
        Partida partida = new Partida("SIM-001", jogador);
        
        // --- 2. CRIAÇÃO E INÍCIO DO GAME LOOP EM UMA NOVA THREAD ---
        
        // 2a. Cria o Controller (Thread)
        GameController controller = new GameController(partida);
        Thread gameThread = new Thread(controller);
        
        // 2b. Inicia a Thread (o Game Loop começa a rodar)
        gameThread.start();
        
        // 2c. Demonstração de comandos (simulando input do usuário)
        try {
            System.out.println("Iniciando Game Loop. Peca inicial em: " + partida.getTetrominoAtual().getPosicao());
            
            // Simula o usuário pressionando a seta direita
            TimeUnit.MILLISECONDS.sleep(500);
            controller.moveLeft();
            System.out.println("Moveu Esquerda. Nova Posicao: " + partida.getTetrominoAtual().getPosicao());
            
            // Espera o Game Loop correr
            TimeUnit.SECONDS.sleep(5); 
            
            // Para a thread para terminar a simulação
            controller.stop();
            gameThread.join(); // Espera a thread morrer
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n-------------------------------------");
        System.out.println("SIMULAÇÃO ENCERRADA.");
        System.out.println("Pontuação: " + partida.getPontuacao());
        System.out.println("Nível: " + partida.getNivel());
        System.out.println("Game Over: " + partida.isGameOver());
    }
}