package tetris.replay;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;

/**
 * Gerencia a persistência do objeto ReplayData no disco usando Serialização Java.
 */
public class ReplayManager {

    // Nome do arquivo onde a última partida será salva
    private static final String REPLAY_FILE = "last_replay.dat";

    /**
     * Salva o objeto ReplayData no disco.
     * @param data O objeto ReplayData a ser serializado.
     */
    public static void saveReplay(ReplayData data) {
        // Usa try-with-resources para garantir que as streams sejam fechadas
        try (FileOutputStream fileOut = new FileOutputStream(REPLAY_FILE);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            
            objectOut.writeObject(data);
            System.out.println("REPLAY SALVO: Dados de replay salvos em " + REPLAY_FILE);

        } catch (IOException e) {
            System.err.println("ERRO ao salvar o replay: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carrega o último objeto ReplayData salvo do disco.
     * @return O objeto ReplayData carregado, ou null se houver falha.
     */
    public static ReplayData loadReplay() {
        File file = new File(REPLAY_FILE);
        if (!file.exists()) {
            System.out.println("REPLAY INEXISTENTE: Não foi encontrado o arquivo " + REPLAY_FILE);
            return null;
        }

        try (FileInputStream fileIn = new FileInputStream(REPLAY_FILE);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
            
            ReplayData data = (ReplayData) objectIn.readObject();
            System.out.println("REPLAY CARREGADO: Seed inicial: " + data.getInitialSeed());
            return data;

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ERRO ao carregar o replay: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Verifica se existe um replay salvo para habilitar o botão de reprodução.
     */
    public static boolean hasSavedReplay() {
        return new File(REPLAY_FILE).exists();
    }
}