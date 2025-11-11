package tetris;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Classe que lida com a entrada do teclado, enviando comandos ao GameController.
 * (Controlos do Jogador: Setas direcionais para movimento, Tecla de rotação) [cite: 808]
 */
public class InputHandler implements EventHandler<KeyEvent> {

    private final GameController controller;

    public InputHandler(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void handle(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            KeyCode code = event.getCode();
            
            // Note que o controller precisa de um método para rotação e pausa
            switch (code) {
                case LEFT:
                    controller.moveLeft();
                    break;
                case RIGHT:
                    controller.moveRight();
                    break;
                case DOWN: // Queda suave
                    controller.moveDown();
                    break;
                case UP: // Rotação (Seta para cima) [cite: 809]
                    controller.rotate();
                    break;
                case SPACE: // Queda rápida (Hard Drop) [cite: 809]
                    controller.hardDrop(); // Este método precisa ser adicionado ao Controller
                    break;
                case P:
                    controller.togglePause();
                    break;
                case ESCAPE: // NOVO: Trata a tecla Esc
                    controller.togglePause();
                    break;
                case M: // NOVO: Mute/Unmute BGM
                    controller.toggleBGM();
                    break;
                default:
                    break;
            }
        }
    }
}
