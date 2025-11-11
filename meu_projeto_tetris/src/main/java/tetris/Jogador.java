package tetris;

import java.util.Objects;
import java.util.UUID;

public class Jogador {
    private final UUID id; 
    private String nome; 

    public Jogador(String nome) {
        this(UUID.randomUUID(), nome);
    }
    
    public Jogador(UUID id, String nome) {
        this.id = Objects.requireNonNull(id, "O ID do jogador não pode ser nulo.");
        this.nome = Objects.requireNonNull(nome, "O nome do jogador não pode ser nulo.");
    }
    
    // Construtor auxiliar para a UI/DAO (aceita String ID)
    public Jogador(String idString, String nome) {
        UUID parsedId;
        try {
            parsedId = UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            parsedId = UUID.randomUUID(); 
        }
        
        this.id = Objects.requireNonNull(parsedId, "O ID do jogador não pode ser nulo.");
        this.nome = Objects.requireNonNull(nome, "O nome do jogador não pode ser nulo.");
    }

    public void alterarNome(String novoNome) {
        this.nome = Objects.requireNonNull(novoNome, "O novo nome não pode ser nulo.");
    }
    
    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false; 
        Jogador jogador = (Jogador) o;
        return id.equals(jogador.id); 
    }

    @Override
    public int hashCode() {
        return id.hashCode(); 
    }
    
    @Override
    public String toString() {
        return String.format("Jogador{id=%s, nome='%s'}", id.toString().substring(0, 8), nome);
    }
}