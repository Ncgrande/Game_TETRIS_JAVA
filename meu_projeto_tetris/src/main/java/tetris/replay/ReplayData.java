package tetris.replay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReplayData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long initialSeed;
    private final List<ReplayEvent> events;

    public ReplayData(long initialSeed) {
        this.initialSeed = initialSeed;
        this.events = new ArrayList<>();
    }

    public long getInitialSeed() {
        return initialSeed;
    }

    public void addEvent(ReplayEvent event) {
        this.events.add(event);
    }

    // Retorna uma cópia imutável da lista de eventos
    public List<ReplayEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }
}